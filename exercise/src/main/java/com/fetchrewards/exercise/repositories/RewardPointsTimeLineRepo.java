package com.fetchrewards.exercise.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fetchrewards.exercise.entities.RewardPointsTimeLine;

@Repository
public interface RewardPointsTimeLineRepo extends JpaRepository<RewardPointsTimeLine, String>{
	List<RewardPointsTimeLine> findByUserName(String userName);
	
	List<RewardPointsTimeLine> findByUserNameAndPayer(String userName, String payer);
}
