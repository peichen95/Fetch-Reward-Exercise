package com.fetchrewards.exercise.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fetchrewards.exercise.dtos.SpendPointRequest;
import com.fetchrewards.exercise.dtos.SpendPointsDTO;
import com.fetchrewards.exercise.dtos.TransactionRequest;
import com.fetchrewards.exercise.entities.Balance;
import com.fetchrewards.exercise.entities.BalanceId;
import com.fetchrewards.exercise.entities.RewardPointsTimeLine;
import com.fetchrewards.exercise.repositories.BalanceRepo;
import com.fetchrewards.exercise.repositories.RewardPointsTimeLineRepo;
import com.fetchrewards.exercise.repositories.TransactionRepo;

@RunWith(SpringRunner.class)
public class ServicesTest {
	@TestConfiguration
	static class ServicesTestContextConfiguration {

		@Bean
		public Services services() {
			return new Services();
		}
	}

	@Autowired
	Services services;

	@MockBean
	RewardPointsTimeLineRepo rewardPointsTimeLineRepo;
	@MockBean
	TransactionRepo transactionRepo;
	@MockBean
	BalanceRepo balanceRepo;

	private BalanceId balanceId;
	private List<Balance> balanceList;
	// private Optional<Balance> optional;
	private List<RewardPointsTimeLine> timeLine;
	private TransactionRequest transactionRequest;
	private SpendPointRequest spendPointRequest;

	@Before
	public void setup() {
		balanceId = new BalanceId();
		balanceId.setPayer("test_payer");
		balanceId.setUserName("test_user");
		Balance balance = new Balance();
		balance.setId(balanceId);
		balance.setPoints(100);

		RewardPointsTimeLine reward = new RewardPointsTimeLine();
		reward.setPayer("test_payer");
		reward.setPoints(100);
		reward.setUserName("test_user");
		reward.setCreatedTime(new Timestamp(System.currentTimeMillis()));

		transactionRequest = new TransactionRequest();
		transactionRequest.setPayer("test_payer");
		transactionRequest.setPoints(100);
		transactionRequest.setTimestamp(new Timestamp(System.currentTimeMillis()));

		spendPointRequest = new SpendPointRequest();
		spendPointRequest.setPoints(10);

		balanceList = new ArrayList<>();
		balanceList.add(balance);
		// optional = Optional.of(balance);

		timeLine = new ArrayList<>();
		timeLine.add(reward);
	}

	@Test
	public void getPointBalanceTest() {
		Mockito.when(balanceRepo.findByIdUserName("test_user")).thenReturn(balanceList);
		List<Balance> list = services.getPointBalance("test_user");
		assertEquals(list.size(), 1);
	}

	@Test
	public void addTransactionSuccessTest() {
		Mockito.when(balanceRepo.findById(balanceId)).thenReturn(Optional.empty());
		Mockito.when(balanceRepo.save(Mockito.mock(Balance.class))).thenReturn(Mockito.mock(Balance.class));
		boolean success = services.addTransaction(transactionRequest);
		assertTrue(success);
	}

	@Test
	public void addTransactionFailTest() {
		transactionRequest.setPoints(-100);
		Mockito.when(balanceRepo.findById(balanceId)).thenReturn(Optional.empty());
		Mockito.when(balanceRepo.save(Mockito.mock(Balance.class))).thenReturn(Mockito.mock(Balance.class));
		boolean success = services.addTransaction(transactionRequest);
		assertFalse(success);
	}

	@Test
	public void spendPointsSuccessTest() {
		Mockito.when(balanceRepo.findByIdUserName("user")).thenReturn(balanceList);
		Mockito.when(rewardPointsTimeLineRepo.findByUserName("user")).thenReturn(timeLine);
		Mockito.doNothing().when(rewardPointsTimeLineRepo).delete(Mockito.mock(RewardPointsTimeLine.class));
		Mockito.when(rewardPointsTimeLineRepo.save(Mockito.mock(RewardPointsTimeLine.class)))
				.thenReturn(Mockito.mock(RewardPointsTimeLine.class));
		Mockito.when(balanceRepo.save(Mockito.mock(Balance.class))).thenReturn(Mockito.mock(Balance.class));
		List<SpendPointsDTO> result = services.spendPoints(spendPointRequest);
		assertEquals(result.size(), 1);
	}
	
	@Test
	public void spendPointsFaliTest() {
		spendPointRequest.setPoints(1000);
		Mockito.when(balanceRepo.findByIdUserName("user")).thenReturn(balanceList);
		List<SpendPointsDTO> result = services.spendPoints(spendPointRequest);
		assertNull(result);
	}
}
