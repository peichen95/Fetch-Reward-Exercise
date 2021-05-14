package com.fetchrewards.exercise.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class BalanceId implements Serializable {
	private static final long serialVersionUID = 2013174149462984495L;
	private String userName;
	private String payer;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPayer() {
		return payer;
	}

	public void setPayer(String payer) {
		this.payer = payer;
	}

}
