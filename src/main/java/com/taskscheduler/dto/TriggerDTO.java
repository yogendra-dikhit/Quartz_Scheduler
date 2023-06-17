package com.taskscheduler.dto;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TriggerDTO {

	private String triggerName;
	private String groupName;
	private Date scheduleTime;
	private Date lastFiredTime;
	private Date nextFireTime;

	public TriggerDTO(String triggerName, String groupName, Date scheduleTime, Date lastFiredTime, Date nextFireTime) {
		super();
		this.triggerName = triggerName;
		this.groupName = groupName;
		this.scheduleTime = scheduleTime;
		this.lastFiredTime = lastFiredTime;
		this.nextFireTime = nextFireTime;
	}

}
