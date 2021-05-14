package com.fetchrewards.exercise.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fetchrewards.exercise.dtos.SpendPointRequest;
import com.fetchrewards.exercise.dtos.SpendPointsDTO;
import com.fetchrewards.exercise.dtos.SpendPointsResponse;
import com.fetchrewards.exercise.dtos.TransactionRequest;
import com.fetchrewards.exercise.entities.Balance;
import com.fetchrewards.exercise.services.Services;

@RestController
public class Controller {
	@Autowired
	Services services;
	
	private static final String BAD_REQUEST = "You cannot spend more points than you have.";
	private static final String SUCCESS = "Transaction has been successfully processed";

	/**
	 * Method that gets all payer point balances for the specific user
	 * @param user
	 * @return map that contains payer point balances
	 */
	@GetMapping("/points/{user}")
	public Map<String, Integer> getPointBalance(@PathVariable("user") String user) {
		List<Balance> userBalance = services.getPointBalance(user);
		Map<String, Integer> response = new HashMap<>();
		userBalance.forEach(balance -> {
			String payer = balance.getId().getPayer();
			int points = balance.getPoints();
			response.put(payer, points);
		});
		return response;
	}
	
	/**
	 * Method that processes adding transaction request
	 * @param request
	 * @return response that tells whether the request has been successfully processed
	 */
	@PostMapping("/transaction")
	public ResponseEntity<String> addTransaction(@RequestBody TransactionRequest request) {
		boolean success = services.addTransaction(request);
		HttpStatus status = success ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		String message = success ? SUCCESS : BAD_REQUEST;
		ResponseEntity<String> response = new ResponseEntity<>(message, status);
		return response;
	}
	
	/**
	 * Method that processes spending points request
	 * @param request
	 * @return response that tells whether the request has been successfully processed
	 */
	@PostMapping("/spend_points")
	public ResponseEntity<SpendPointsResponse> spendPoints(@RequestBody SpendPointRequest request) {
		List<SpendPointsDTO> responseList = services.spendPoints(request);
		SpendPointsResponse response = new SpendPointsResponse();
		String message = SUCCESS;
		HttpStatus status = HttpStatus.OK;
		if(responseList == null) {
			message = BAD_REQUEST;
			status = HttpStatus.BAD_REQUEST;
		}
		response.setMessage(message);
		response.setPointSpentList(responseList);
		ResponseEntity<SpendPointsResponse> responseEntity = new ResponseEntity<>(response, status);
		
		return responseEntity;
	}
}
