package com.fetchrewards.exercise.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fetchrewards.exercise.dtos.SpendPointRequest;
import com.fetchrewards.exercise.dtos.SpendPointsDTO;
import com.fetchrewards.exercise.dtos.TransactionRequest;
import com.fetchrewards.exercise.entities.Balance;
import com.fetchrewards.exercise.entities.BalanceId;
import com.fetchrewards.exercise.entities.RewardPointsTimeLine;
import com.fetchrewards.exercise.entities.Transaction;
import com.fetchrewards.exercise.repositories.BalanceRepo;
import com.fetchrewards.exercise.repositories.RewardPointsTimeLineRepo;
import com.fetchrewards.exercise.repositories.TransactionRepo;

@Service
public class Services {
	@Autowired
	RewardPointsTimeLineRepo rewardPointsTimeLineRepo;

	@Autowired
	TransactionRepo transactionRepo;

	@Autowired
	BalanceRepo balanceRepo;

	private static final String USER_NAME = "user";

	/**
	 * Method that gets payer point balance for a specific user
	 * @param userName
	 * @return List<Balance>
	 */
	public List<Balance> getPointBalance(String userName) {
		return balanceRepo.findByIdUserName(userName);
	}

	/**
	 * Method that checks if the points of transaction is negative first
	 * If it's negative, spend the points of the same payer(for oldest to latest)
	 * Otherwise add it to the balance or create a new payer balance
	 * Then save the transaction
	 * @param request
	 * @return
	 */
	public boolean addTransaction(TransactionRequest request) {
		BalanceId id = new BalanceId();
		id.setPayer(request.getPayer());
		id.setUserName(USER_NAME);
		Optional<Balance> balance = balanceRepo.findById(id);

		if (request.getPoints() < 0) {
			if (!balance.isPresent() || balance.get().getPoints() + request.getPoints() < 0)
				return false;
			else
				spendSamePayerPoints(request, balance.get());
		}

		saveTransaction(request);
		if(balance.isPresent())
			addToBalance(request.getPoints(), balance.get());
		else 
			saveToBalance(id, request.getPoints());
		return true;
	}

	/**
	 * When the points of transaction is negative, use the point balance from the same payer to spend points
	 * in the order of oldest to latest
	 * @param request
	 * @param balance
	 */
	private void spendSamePayerPoints(TransactionRequest request, Balance balance) {
		List<RewardPointsTimeLine> samePayerPoints = rewardPointsTimeLineRepo.findByUserNameAndPayer(USER_NAME,
				request.getPayer());
		Collections.sort(samePayerPoints, (point1, point2) -> {
			return point1.getCreatedTime().compareTo(point2.getCreatedTime());
		});

		int spentPoints = -request.getPoints();
		for (int i = 0; i < samePayerPoints.size(); i++) {
			RewardPointsTimeLine reward = samePayerPoints.get(i);
			if (spentPoints >= reward.getPoints()) {
				spentPoints -= reward.getPoints();
				rewardPointsTimeLineRepo.delete(reward);
			} else {
				reward.setPoints(reward.getPoints() - spentPoints);
				rewardPointsTimeLineRepo.save(reward);
				break;
			}
		}
	}

	/**
	 * Save the transaction
	 * if the points of transaction is positive, save it to RewardPointsTimeLine 
	 * @param request
	 */
	private void saveTransaction(TransactionRequest request) {
		Transaction transaction = new Transaction();
		transaction.setPayer(request.getPayer());
		transaction.setPoints(request.getPoints());
		transaction.setUserName(USER_NAME);
		transaction.setTimestamp(new Timestamp(System.currentTimeMillis()));
		transactionRepo.save(transaction);
		
		if(request.getPoints() > 0) {
			RewardPointsTimeLine timeLine = new RewardPointsTimeLine();
			timeLine.setPayer(request.getPayer());
			timeLine.setPoints(request.getPoints());
			timeLine.setUserName(USER_NAME);
			timeLine.setCreatedTime(new Timestamp(System.currentTimeMillis()));
			rewardPointsTimeLineRepo.save(timeLine);
		}
	}

	/**
	 * Add the points to balance
	 * @param points
	 * @param balance
	 */
	private void addToBalance(int points, Balance balance) {
		balance.setPoints(points + balance.getPoints());
		balanceRepo.save(balance);
	}
	
	/**
	 * Create new payer balance
	 * @param id
	 * @param points
	 */
	private void saveToBalance(BalanceId id, int points) {
		Balance balance = new Balance();
		balance.setId(id);
		balance.setPoints(points);
		balanceRepo.save(balance);
	}

	/**
	 * Method that first checks if the spent points is more than total balance
	 * Then based on the time line of transactions, spend the points and update balance
	 * @param request
	 * @return List<SpendPointsDTO>
	 */
	public List<SpendPointsDTO> spendPoints(SpendPointRequest request) {
		int spentPoints = request.getPoints();
		List<Balance> balanceList = balanceRepo.findByIdUserName(USER_NAME);
		int totalPoints = 0;

		for (Balance balance : balanceList)
			totalPoints += balance.getPoints();
		if (totalPoints < spentPoints) 
			return null;

		List<RewardPointsTimeLine> timeLine = rewardPointsTimeLineRepo.findByUserName(USER_NAME);
		Collections.sort(timeLine, (point1, point2) -> {
			return point1.getCreatedTime().compareTo(point2.getCreatedTime());
		});

		Map<String, Integer> cost = new HashMap<>();
		calculateCost(cost, timeLine, spentPoints);
		updateBalance(cost, balanceList);
		
		List<SpendPointsDTO> responseList = new ArrayList<>();
		createSpendPointsResponse(cost, responseList);
		
		return responseList;
	}

	/**
	 * Calculate the cost of points spent of corresponding payer
	 * @param cost
	 * @param timeLine
	 * @param spentPoints
	 */
	private void calculateCost(Map<String, Integer> cost, List<RewardPointsTimeLine> timeLine, int spentPoints) {
		int i = 0;
		while (spentPoints > 0) {
			RewardPointsTimeLine reward = timeLine.get(i);
			String payer = reward.getPayer();
			cost.putIfAbsent(payer, 0);

			if (spentPoints >= reward.getPoints()) {
				cost.put(payer, cost.get(payer) + reward.getPoints());
				rewardPointsTimeLineRepo.delete(reward);
				spentPoints -= reward.getPoints();
			} else {
				cost.put(payer, cost.get(payer) + spentPoints);
				reward.setPoints(reward.getPoints() - spentPoints);
				rewardPointsTimeLineRepo.save(reward);
				spentPoints = 0;
			}
			i++;
		}
	}

	/**
	 * Update payer balances according to points spent
	 * @param cost
	 * @param balanceList
	 */
	private void updateBalance(Map<String, Integer> cost, List<Balance> balanceList) {
		balanceList.forEach(balance -> {
			String payer = balance.getId().getPayer();
			int original = balance.getPoints();
			if (cost.containsKey(payer)) {
				balance.setPoints(original - cost.get(payer));
				balanceRepo.save(balance);
			}
		});
	}

	/**
	 * Create response according to points spent
	 * @param cost
	 * @param responseList
	 */
	private void createSpendPointsResponse(Map<String, Integer> cost, List<SpendPointsDTO> responseList) {
		cost.forEach((payer, points) -> {
			SpendPointsDTO spendPointsDTO = new SpendPointsDTO();
			spendPointsDTO.setPayer(payer);
			spendPointsDTO.setPoints(-points);
			responseList.add(spendPointsDTO);
		});
	}
}
