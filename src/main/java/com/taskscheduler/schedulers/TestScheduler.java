package com.taskscheduler.schedulers;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.client.RestTemplate;

import com.taskscheduler.model.PostModel;
import com.taskscheduler.service.PostService;

public class TestScheduler extends QuartzJobBean {

	private String URL = "https://jsonplaceholder.typicode.com/posts";

	@Autowired
	private PostService postService;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {
			System.out.println("Scheduler running: " + context.getJobDetail().getKey());
			String postId = (String) context.getMergedJobDataMap().get("postId");
			System.out.println("postId: " + postId);
			PostModel post = postService.getById(postId);
			ResponseEntity<String> response = restTemplate.postForEntity(URL, post, String.class);
			System.out.println("API Response: " + response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
