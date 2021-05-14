package com.fetchrewards.exercise.dtos;

import java.util.List;

public class SpendPointsResponse {
	private String message;
	private List<SpendPointsDTO> pointSpentList;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<SpendPointsDTO> getPointSpentList() {
		return pointSpentList;
	}

	public void setPointSpentList(List<SpendPointsDTO> pointSpentList) {
		this.pointSpentList = pointSpentList;
	}

}
