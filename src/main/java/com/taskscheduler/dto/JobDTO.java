package com.taskscheduler.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.taskscheduler.util.Constants.TriggerState;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class JobDTO {

	private String jobName;
	private String groupName;
	private TriggerState jobStatus;
	private List<TriggerDTO> triggers;

	public JobDTO(String jobName, String groupName) {
		super();
		this.jobName = jobName;
		this.groupName = groupName;
	}

	public JobDTO(String jobName, String groupName, TriggerState jobStatus) {
		super();
		this.jobName = jobName;
		this.groupName = groupName;
		this.jobStatus = jobStatus;
	}

	public JobDTO(String jobName, String groupName, TriggerState jobStatus, List<TriggerDTO> triggers) {
		super();
		this.jobName = jobName;
		this.groupName = groupName;
		this.jobStatus = jobStatus;
		this.triggers = triggers;
	}

}
