package com.fetchrewards.exercise.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fetchrewards.exercise.entities.Transaction;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, UUID>{

}
