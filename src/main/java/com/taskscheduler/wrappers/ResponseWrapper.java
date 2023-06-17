package com.taskscheduler.wrappers;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.taskscheduler.dto.JobDTO;
import com.taskscheduler.util.Constants.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(value = Include.NON_NULL)
public class ResponseWrapper {

	private Status status;
	private String errorMessage;
	private JobDTO job;
	private List<JobDTO> jobs;
	private Map<String, List<JobDTO>> jobGroups;

	public ResponseWrapper(Status status, JobDTO job) {
		super();
		this.status = status;
		this.job = job;
	}

	public ResponseWrapper(Status status, JobDTO job, String errorMessage) {
		super();
		this.status = status;
		this.job = job;
		this.errorMessage = errorMessage;
	}

	public ResponseWrapper(Status status, List<JobDTO> jobs, Map<String, List<JobDTO>> jobGroups) {
		super();
		this.status = status;
		this.jobs = jobs;
		this.jobGroups = jobGroups;
	}

}
