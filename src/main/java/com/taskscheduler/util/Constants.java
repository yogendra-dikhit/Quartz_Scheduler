package com.taskscheduler.util;

import org.quartz.Job;

import com.taskscheduler.schedulers.CitiScheduler;
import com.taskscheduler.schedulers.HDFCScheduler;

public class Constants {

	public enum Status {
		SUCCESS("SUCCESS"), FAILED("FAILED"), INVALID("INVALID"), ERROR("ERROR"), DUPLICATE_JOB("DUPLICATE_JOB");

		private String value;

		private Status(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}

	public enum JobState {
		DUPLICATE("Duplicate Job Name / Group Name"),
		NOT_FOUND("Job with Job Name: '%s' and Group Name: '%s' Not Found"), ALREADY_RUNNING("Job Already Running");

		private String status;

		private JobState(String status) {
			this.status = status;
		}

		@Override
		public String toString() {
			return this.status;
		}
	}

	public enum TriggerState {
		NONE("NONE"), SCHEDULED("SCHEDULED"), PAUSED("PAUSED"), COMPLETE("COMPLETE"), ERROR("ERROR"),
		BLOCKED("BLOCKED"), RUNNING("RUNNING");

		private String state;

		private TriggerState(String state) {
			this.state = state;
		}

		@Override
		public String toString() {
			return this.state;
		}
	}

	public enum Generic {
		VALIDATION_FAILED("Validation Failed!"), DUPLICATE_USERNAME("Duplicate UserName!");

		private String value;

		private Generic(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}

	private Constants() {
	}

	public static Class<? extends Job> getSchedulerClassName(String groupName) {
		if (groupName.equalsIgnoreCase("HDFC"))
			return HDFCScheduler.class;
		else if (groupName.equalsIgnoreCase("Citi"))
			return CitiScheduler.class;
		else
			throw new IllegalArgumentException(
					String.format("Invalid Group Name- No scheduler found for group: %s", groupName));
	}

}
