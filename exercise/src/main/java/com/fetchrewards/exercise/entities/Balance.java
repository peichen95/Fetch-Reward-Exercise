package com.fetchrewards.exercise.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class Balance {
	@EmbeddedId
	private BalanceId id;
	@Column
	private int points;

	public BalanceId getId() {
		return id;
	}

	public void setId(BalanceId id) {
		this.id = id;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

}
