package com.taskscheduler.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.taskscheduler.model.PostModel;
import com.taskscheduler.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;

	public List<PostModel> getAllPost() {
		return postRepository.findAll();
	}

	public PostModel getById(String id) {
		return postRepository.getReferenceById(Long.parseLong(id));
	}

}
