package com.taskscheduler.contoller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.taskscheduler.dto.JobDTO;
import com.taskscheduler.service.SchedulerService;
import com.taskscheduler.util.Constants;
import com.taskscheduler.wrappers.RequestWrapper;
import com.taskscheduler.wrappers.ResponseWrapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedulers")
@RequiredArgsConstructor
public class SchedulerController {
	private static Logger logger = LogManager.getLogger(SchedulerController.class);

	private final SchedulerService schedulerService;
	
	@GetMapping
	public ResponseEntity<ResponseWrapper> getAll() {
		try {
			List<JobDTO> jobs = schedulerService.getAllJobs();
			Map<String, List<JobDTO>> jobGroups = jobs.stream().collect(Collectors.groupingBy(JobDTO::getGroupName));
			return ResponseEntity.ok(ResponseWrapper.builder().status(Constants.Status.SUCCESS).jobs(jobs).jobGroups(jobGroups).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.getAll(): {}", e.getClass().getName(), e);
			return ResponseEntity.internalServerError().body(ResponseWrapper.builder().errorMessage(e.getMessage()).build());
		}
	}
	
	@GetMapping("/{groupKey}")
	public ResponseEntity<ResponseWrapper> getAllJobsByGroup(@PathVariable String groupKey) {
		try {
			Map<String, List<JobDTO>> jobGroups = schedulerService.getAllJobsByGroups();
			return ResponseEntity.ok(ResponseWrapper.builder()
											.status(Constants.Status.SUCCESS).jobs(jobGroups.getOrDefault(groupKey, Collections.emptyList())).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.getAll(): {}", e.getClass().getName(), e);
			return ResponseEntity.internalServerError().body(ResponseWrapper.builder().errorMessage(e.getMessage()).build());
		}
	}
	
	@GetMapping("/{groupKey}/{jobKey}")
	public ResponseEntity<ResponseWrapper> getJob(@PathVariable String groupKey, @PathVariable String jobKey) {
		try {
			if (!schedulerService.isJobWithNamePresent(jobKey, groupKey))
				return ResponseEntity.notFound().build();
			JobDTO jobDTO = schedulerService.getJob(jobKey, groupKey);
			return ResponseEntity.ok().body(ResponseWrapper.builder().status(Constants.Status.SUCCESS).job(jobDTO).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.getJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), jobKey, groupKey, e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(jobKey, groupKey)).errorMessage(e.getMessage()).build());
		}
	}
	
	@PostMapping
	public ResponseEntity<ResponseWrapper> createJob(@RequestBody @Valid RequestWrapper requestWrapper) {
		try {
			if (schedulerService.isJobWithNamePresent(requestWrapper.getJobKey(), requestWrapper.getGroupKey()))
				return ResponseEntity.badRequest()
						.body(ResponseWrapper.builder()
								.status(Constants.Status.INVALID).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey()))
									.errorMessage(Constants.JobState.DUPLICATE.toString()).build());

			schedulerService.scheduleCronJob(Constants.getSchedulerClassName(requestWrapper.getGroupKey()), requestWrapper);
			return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{groupKey}/{jobKey}").buildAndExpand(requestWrapper.getGroupKey(), requestWrapper.getJobKey()).toUri())
					.body(ResponseWrapper.builder()
							.status(Constants.Status.SUCCESS).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey())).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.createJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), requestWrapper.getJobKey(), requestWrapper.getGroupKey(), e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey())).errorMessage(e.getMessage()).build());		
		}
	}
	
	@PutMapping
	public ResponseEntity<ResponseWrapper> updateJob(@RequestBody @Valid RequestWrapper requestWrapper) {
		try {
			if (!schedulerService.isJobWithNamePresent(requestWrapper.getJobKey(), requestWrapper.getGroupKey()))
				return ResponseEntity.badRequest()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.INVALID).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey()))
								.errorMessage(String.format(Constants.JobState.NOT_FOUND.toString(), requestWrapper.getJobKey() ,requestWrapper.getGroupKey())).build());
			if (schedulerService.isJobRunning(requestWrapper.getJobKey(), requestWrapper.getGroupKey()))
				return ResponseEntity.badRequest()
						.body(ResponseWrapper.builder()
								.status(Constants.Status.INVALID).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey())).errorMessage(Constants.JobState.ALREADY_RUNNING.toString()).build());


			schedulerService.updateCronJob(requestWrapper);
			return ResponseEntity.ok().body(ResponseWrapper.builder()
					.status(Constants.Status.SUCCESS).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey())).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.updateJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), requestWrapper.getJobKey(), requestWrapper.getGroupKey(), e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(requestWrapper.getJobKey(), requestWrapper.getGroupKey())).errorMessage(e.getMessage()).build());
		}
	}
	
	@PostMapping("/{groupKey}/{jobKey}/update-trigger")
	public ResponseEntity<ResponseWrapper> updateJobTrigger(@PathVariable String groupKey, @PathVariable String jobKey, @RequestBody @Valid RequestWrapper requestWrapper) {
		try {
			if (!schedulerService.isJobWithNamePresent(jobKey, groupKey))
				return ResponseEntity.notFound().build();
			if (schedulerService.isJobRunning(jobKey, groupKey))
				return ResponseEntity.badRequest()
						.body(ResponseWrapper.builder()
								.status(Constants.Status.INVALID).job(new JobDTO(jobKey, groupKey)).errorMessage(Constants.JobState.ALREADY_RUNNING.toString()).build());
			JobDTO jobDTO = schedulerService.updateJobTrigger(requestWrapper);
			return ResponseEntity.ok().body(ResponseWrapper.builder().status(Constants.Status.SUCCESS).job(jobDTO).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.getJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), jobKey, groupKey, e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(jobKey, groupKey)).errorMessage(e.getMessage()).build());
		}
	}
	
	@PostMapping("/{groupKey}/{jobKey}/unschedule-trigger")
	public ResponseEntity<ResponseWrapper> unschedulerJobTrigger(@PathVariable String groupKey, @PathVariable String jobKey) {
		try {
			if (!schedulerService.isTriggerWithNamePresent(jobKey, groupKey))
				return ResponseEntity.notFound().build();
			if (schedulerService.isJobRunning(jobKey, groupKey))
				return ResponseEntity.badRequest()
						.body(ResponseWrapper.builder()
								.status(Constants.Status.INVALID).job(new JobDTO(jobKey, groupKey)).errorMessage(Constants.JobState.ALREADY_RUNNING.toString()).build());
			schedulerService.unscheduleJob(jobKey, groupKey);
			return ResponseEntity.ok().body(ResponseWrapper.builder().status(Constants.Status.SUCCESS).job(new JobDTO(jobKey, groupKey)).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.getJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), jobKey, groupKey, e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(jobKey, groupKey)).errorMessage(e.getMessage()).build());
		}
	}
	
	@PostMapping("/{groupKey}/{jobKey}/fire-now")
	public ResponseEntity<ResponseWrapper> fireNow(@PathVariable String groupKey, @PathVariable String jobKey) {
		try {
			if (!schedulerService.isJobWithNamePresent(jobKey, groupKey))
				return ResponseEntity.notFound().build();
			if (schedulerService.isJobRunning(jobKey, groupKey))
				return ResponseEntity.badRequest()
						.body(ResponseWrapper.builder()
								.status(Constants.Status.INVALID).job(new JobDTO(jobKey, groupKey)).errorMessage(Constants.JobState.ALREADY_RUNNING.toString()).build());
			schedulerService.fireNow(jobKey, groupKey);
			return ResponseEntity.ok().body(ResponseWrapper.builder().status(Constants.Status.SUCCESS).job(new JobDTO(jobKey, groupKey)).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.getJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), jobKey, groupKey, e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(jobKey, groupKey)).errorMessage(e.getMessage()).build());
		}
	}
	
	@DeleteMapping("/{groupKey}/{jobKey}")
	public ResponseEntity<ResponseWrapper> deleteJob(@PathVariable String groupKey, @PathVariable String jobKey) {
		try {
			if (!schedulerService.isJobWithNamePresent(jobKey, groupKey))
				return ResponseEntity.notFound().build();
			if (schedulerService.isJobRunning(jobKey, groupKey))
				return ResponseEntity.badRequest()
						.body(ResponseWrapper.builder()
								.status(Constants.Status.INVALID).job(new JobDTO(jobKey, groupKey)).errorMessage(Constants.JobState.ALREADY_RUNNING.toString()).build());
			
			schedulerService.deleteJob(jobKey, groupKey);
			return ResponseEntity.ok()
					.body(ResponseWrapper.builder().status(Constants.Status.SUCCESS).job(new JobDTO(jobKey, groupKey)).build());
		} catch (Exception e) {
			logger.error("{} in SchedulerController.deleteJob(): jobKey: {} groupKey: {} {}", e.getClass().getName(), jobKey, groupKey, e);
			return ResponseEntity.internalServerError()
					.body(ResponseWrapper.builder()
							.status(Constants.Status.ERROR).job(new JobDTO(jobKey, groupKey)).errorMessage(e.getMessage()).build());
		}
	}
}
