package com.taskscheduler.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;

import com.taskscheduler.dto.TriggerDTO;
import com.taskscheduler.wrappers.RequestWrapper;

public class JobUtil {
	private static Logger logger = LogManager.getLogger(JobUtil.class.getName());

	private JobUtil() {}
	
	public static JobDetail createJob(Class<? extends Job> jobClass, RequestWrapper requestWrapper) {
		return JobBuilder.newJob(jobClass)
				.withIdentity(requestWrapper.getJobKey(), requestWrapper.getGroupKey())
				.withDescription(requestWrapper.getJobDescription())
				.usingJobData(new JobDataMap(requestWrapper.getDataMap()))
				.storeDurably()
				.requestRecovery()
				.build();
	}

	public static Trigger createCronTrigger(JobKey jobKey, RequestWrapper requestWrapper) {
		TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().forJob(jobKey)
				.withIdentity(jobKey.getName(), jobKey.getGroup())
				.withDescription(requestWrapper.getTriggerDescription());
		
		if (requestWrapper.getStartAt() == null || requestWrapper.getStartAt().toString().isEmpty())
			builder.startNow();
		else
			builder.startAt(requestWrapper.getStartAt());
		if (requestWrapper.getCronExpression().isEmpty())
			builder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow());
		else
			builder.withSchedule(CronScheduleBuilder.cronSchedule(requestWrapper.getCronExpression()).withMisfireHandlingInstructionFireAndProceed());
		return builder.build();
	}
	
	public static Constants.TriggerState getJobState(JobKey jobKey, Scheduler scheduler) {
		logger.info("SchedulerServiceImpl.getJobState()");
		try {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);

			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
			if(triggers != null && !triggers.isEmpty()){
				for (Trigger trigger : triggers) {
					TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

					if (TriggerState.PAUSED.equals(triggerState)) {
						return Constants.TriggerState.PAUSED;
					}else if (TriggerState.BLOCKED.equals(triggerState)) {
						return Constants.TriggerState.BLOCKED;
					}else if (TriggerState.COMPLETE.equals(triggerState)) {
						return Constants.TriggerState.COMPLETE;
					}else if (TriggerState.ERROR.equals(triggerState)) {
						return Constants.TriggerState.ERROR;
					}else if (TriggerState.NONE.equals(triggerState)) {
						return Constants.TriggerState.NONE;
					}else if (TriggerState.NORMAL.equals(triggerState)) {
						return Constants.TriggerState.SCHEDULED;
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("SchedulerException while checking state for job with name and group:", e);
		}
		return Constants.TriggerState.NONE;
	}
	
	public static List<TriggerDTO> getAllTriggers(JobKey jobKey, Scheduler scheduler) {
		try {
			return scheduler.getTriggersOfJob(jobKey).stream()
			.map(trigger -> new TriggerDTO(trigger.getKey().getName(), trigger.getKey().getGroup(), 
					trigger.getStartTime(), trigger.getPreviousFireTime(), trigger.getNextFireTime())).collect(Collectors.toList());
		} catch (SchedulerException e) {
			logger.error("SchedulerException while fetching triggers for job with name and group:", e);
		}
		return Collections.emptyList();
	}

}
