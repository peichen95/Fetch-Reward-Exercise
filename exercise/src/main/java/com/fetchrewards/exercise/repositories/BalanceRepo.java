package com.fetchrewards.exercise.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fetchrewards.exercise.entities.Balance;
import com.fetchrewards.exercise.entities.BalanceId;

@Repository
public interface BalanceRepo extends JpaRepository<Balance, BalanceId> {

	List<Balance> findByIdUserName(String userName);
}
