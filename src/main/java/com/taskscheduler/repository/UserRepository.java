package com.taskscheduler.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.taskscheduler.model.UserModel;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {
	
	UserModel findByUserName(String userName);

}
