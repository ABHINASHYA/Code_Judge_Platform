package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.model.Submission;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class SubmissionQueue {

	private final BlockingQueue<Submission> queue = new LinkedBlockingQueue<>();

	public void addSubmission(Submission submission) {
		queue.offer(submission);
	}

	public Submission takeSubmission() throws InterruptedException {
		return queue.take();
	}
}
