package com.taskscheduler.service;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.SchedulerException;

import com.taskscheduler.dto.JobDTO;
import com.taskscheduler.wrappers.RequestWrapper;

public interface SchedulerService {

	List<JobDTO> getAllJobs() throws SchedulerException;

	Map<String, List<JobDTO>> getAllJobsByGroups() throws SchedulerException;

	JobDTO getJob(String jobName, String groupName) throws SchedulerException;

	JobDTO updateJobTrigger(RequestWrapper requestWrapper) throws SchedulerException;

	void scheduleCronJob(Class<? extends Job> jobClass, RequestWrapper requestWrapper) throws SchedulerException;

	void updateCronJob(RequestWrapper requestWrapper) throws SchedulerException;

	void deleteJob(String jobName, String groupName) throws SchedulerException;

	boolean isJobRunning(String jobKey, String groupKey) throws SchedulerException;

	boolean isJobWithNamePresent(String jobKey, String groupKey) throws SchedulerException;

	boolean isTriggerWithNamePresent(String jobKey, String groupKey) throws SchedulerException;

	void fireNow(String jobKey, String groupKey) throws SchedulerException;

	void unscheduleJob(String jobKey, String groupKey) throws SchedulerException;

}
