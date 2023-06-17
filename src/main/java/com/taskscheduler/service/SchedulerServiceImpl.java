package com.taskscheduler.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import com.taskscheduler.dto.JobDTO;
import com.taskscheduler.dto.TriggerDTO;
import com.taskscheduler.util.Constants;
import com.taskscheduler.util.JobUtil;
import com.taskscheduler.wrappers.RequestWrapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {
	private Logger logger = LogManager.getLogger(getClass().getName());

	private final Scheduler scheduler;

	@Override
	public List<JobDTO> getAllJobs() throws SchedulerException {
		List<JobDTO> list = new ArrayList<>();
		for (String groupName : scheduler.getJobGroupNames()) {
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
				list.add(extractJobObject(jobKey));
			}
		}
		return list;
	}
	
	public Map<String, List<JobDTO>> getAllJobsByGroups() throws SchedulerException {
		return getAllJobs().stream().collect(Collectors.groupingBy(JobDTO::getGroupName));
	}
	
	@Override
	public JobDTO getJob(String jobName, String groupName) throws SchedulerException {
		JobKey jobKey = new JobKey(jobName, groupName);
		return extractJobObject(jobKey);
	}
	
	@Override
	public JobDTO updateJobTrigger(RequestWrapper requestWrapper) throws SchedulerException {
		logger.info("Request received for updating cron job : jobKey : {} , date: {}", requestWrapper.getJobKey(), requestWrapper.getStartAt());
		JobKey jobKey = new JobKey(requestWrapper.getJobKey(), requestWrapper.getGroupKey());
		Trigger newTrigger = JobUtil.createCronTrigger(jobKey, requestWrapper);
		if (Boolean.TRUE.equals(scheduler.checkExists(newTrigger.getKey()))) {
			Date dt = scheduler.rescheduleJob(TriggerKey.triggerKey(requestWrapper.getJobKey(), requestWrapper.getGroupKey()), newTrigger);
			logger.info("Trigger associated with jobKey :{} groupKey: {} rescheduled successfully for: {}", requestWrapper.getJobKey(), requestWrapper.getGroupKey(), dt);			
		} else {
			Date dt = scheduler.scheduleJob(newTrigger);
			logger.info("Job: jobKey: {} groupKey: {} scheduledAt: {}", requestWrapper.getJobKey(), requestWrapper.getGroupKey(), dt);
		}
		return extractJobObject(jobKey);
	}
	
	@Override
	public void unscheduleJob(String jobKey, String groupKey) throws SchedulerException {
		TriggerKey tkey = new TriggerKey(jobKey, groupKey);
		scheduler.unscheduleJob(tkey);
		logger.info("Job Name: {} , Group Name: {} , unscheduled successfully.", jobKey, groupKey);
	}
	
	@Override
	public void scheduleCronJob(Class<? extends Job> jobClass, RequestWrapper requestWrapper) throws SchedulerException {
		JobDetail jobDetail = JobUtil.createJob(jobClass, requestWrapper);
		logger.info("Creating trigger for job: jobKey: {} groupKey: {} startAt: {}", requestWrapper.getJobKey(), requestWrapper.getGroupKey(), requestWrapper.getStartAt());
		if (Boolean.TRUE.equals(requestWrapper.getIsStandAloneJob())) {
			scheduler.addJob(jobDetail, false);
			logger.info("Job: jobKey: {} groupKey: {} StandAloneJob Added to the scheduler.", requestWrapper.getJobKey(), requestWrapper.getGroupKey());
		} else {
			Trigger cronTrigger = JobUtil.createCronTrigger(jobDetail.getKey(), requestWrapper);
			Date dt = scheduler.scheduleJob(jobDetail, cronTrigger);
			logger.info("Job: jobKey: {} groupKey: {} scheduledAt: {}", requestWrapper.getJobKey(), requestWrapper.getGroupKey(), dt);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void updateCronJob(RequestWrapper requestWrapper) throws SchedulerException {
		logger.info("Request received for updating cron job : jobKey : {} , date: {}", requestWrapper.getJobKey(), requestWrapper.getStartAt());
		JobKey jobKey = new JobKey(requestWrapper.getJobKey(), requestWrapper.getGroupKey());
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		List triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
		scheduler.addJob(jobDetail, true);
		boolean status = scheduler.getTriggersOfJob(jobDetail.getKey()).addAll(triggers);
		logger.info("Job associated with jobKey :{} groupKey: {} updated status: {}", requestWrapper.getJobKey(), requestWrapper.getGroupKey(), status);
	}
	
	@Override
	public void deleteJob(String jobName, String groupName) throws SchedulerException {
		logger.info("Request received for deleting job: jobKey: {} groupKey: {}", jobName, groupName);
		JobKey jkey = new JobKey(jobName, groupName); 
		boolean status = scheduler.deleteJob(jkey);
		logger.info("Deleted job: jobKey: {} groupKey: {} status: {}", jobName, groupName, status);
	}
	
	@Override
	public boolean isJobRunning(String jobKey, String groupKey) throws SchedulerException {
		logger.info("Request received for checking job is running now: jobKey: {} groupKey: {}", jobKey, groupKey);
		List<JobExecutionContext> currentJobs = scheduler.getCurrentlyExecutingJobs();
		return currentJobs.stream()
					.anyMatch(jobCtx -> jobKey.equalsIgnoreCase(jobCtx.getJobDetail().getKey().getName()) 
						&& groupKey.equalsIgnoreCase(jobCtx.getJobDetail().getKey().getGroup()));
	}

	@Override
	public boolean isJobWithNamePresent(String jobName, String groupName) throws SchedulerException {
		JobKey jobKey = new JobKey(jobName, groupName);
		return scheduler.checkExists(jobKey);
	}
	
	@Override
	public boolean isTriggerWithNamePresent(String jobKey, String groupKey) throws SchedulerException {
		TriggerKey tkey = new TriggerKey(jobKey, groupKey);
		return scheduler.checkExists(tkey);
	}
	
	@Override
	public void fireNow(String jobKey, String groupKey) throws SchedulerException {
		JobKey jKey = new JobKey(jobKey, groupKey);
		scheduler.triggerJob(jKey);
		logger.info("Job Name: {} , Group Name: {} , triggered successfully.", jobKey, groupKey);
	}

	public JobDTO extractJobObject(JobKey jobKey) throws SchedulerException {
		String jobName = jobKey.getName();
		String jobGroup = jobKey.getGroup();

		List<TriggerDTO> triggers = JobUtil.getAllTriggers(jobKey, scheduler);
		
		Constants.TriggerState jobState = isJobRunning(jobName, jobGroup) ? Constants.TriggerState.RUNNING : JobUtil.getJobState(jobKey, scheduler);
		logger.info("Job Name: {} , Group Name: {} , Triggers: {}", jobName, jobGroup, triggers.size());
		return new JobDTO(jobName, jobGroup, jobState, triggers);
	}

}
