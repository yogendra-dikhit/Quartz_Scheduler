package com.taskscheduler.schedulers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class CitiScheduler extends QuartzJobBean {
	
	private Logger logger = LogManager.getLogger(getClass());

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.info("Citi Scheduler Running!!! :: DataMap: {}", context.getMergedJobDataMap().getWrappedMap());
		logger.info("Scheduler Name: {}", context.getJobDetail().getKey().getName());
	}
}
