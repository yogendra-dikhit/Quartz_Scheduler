package com.taskscheduler.wrappers;

import java.util.Date;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestWrapper {

	@NotEmpty
	private String jobKey;
	@NotEmpty
	private String groupKey;
	@NotNull
	private String jobDescription;
	@NotNull
	private String triggerDescription;
	@NotNull
	private Boolean isStandAloneJob;

	private Date startAt;
	@NotNull
	private String cronExpression;
	@NotNull
	private Map<String, String> dataMap;

}
