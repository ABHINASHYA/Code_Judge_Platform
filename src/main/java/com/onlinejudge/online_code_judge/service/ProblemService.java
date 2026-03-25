package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.model.Problem;
import com.onlinejudge.online_code_judge.repository.ProblemRepository;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.TestCaseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {

	private final ProblemRepository problemRepository;
	private final TestCaseRepository testCaseRepository;
	private final SubmissionRepository submissionRepository;

	public ProblemService(
			ProblemRepository problemRepository,
			TestCaseRepository testCaseRepository,
			SubmissionRepository submissionRepository) {
		this.problemRepository = problemRepository;
		this.testCaseRepository = testCaseRepository;
		this.submissionRepository = submissionRepository;
	}

	public Problem createProblem(Problem problem) {
		validateProblemPayload(problem);
		String title = sanitizeTitle(problem.getTitle());
		if (problemRepository.existsByTitleIgnoreCase(title)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Problem title already exists");
		}

		problem.setTitle(title);
		problem.setDescription(sanitizeDescription(problem.getDescription()));
		problem.setDifficulty(normalizeDifficulty(problem.getDifficulty()));
		return problemRepository.save(problem);
	}

	public Page<Problem> getProblems(int page, int size) {
		return problemRepository.findAll(PageRequest.of(page, size));
	}

	public Problem getProblemById(Long id) {
		return problemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
	}

	public Problem updateProblem(Long id, Problem updatedProblem) {
		validateProblemPayload(updatedProblem);
		String title = sanitizeTitle(updatedProblem.getTitle());

		Problem existing = getProblemById(id);

		if (problemRepository.existsByTitleIgnoreCaseAndIdNot(title, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Problem title already exists");
		}

		existing.setTitle(title);
		existing.setDescription(sanitizeDescription(updatedProblem.getDescription()));
		existing.setDifficulty(normalizeDifficulty(updatedProblem.getDifficulty()));
		return problemRepository.save(existing);
	}

	@Transactional
	public void deleteProblem(Long id) {
		if (!problemRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found");
		}

		submissionRepository.deleteByProblemId(id);
		testCaseRepository.deleteByProblemId(id);
		problemRepository.deleteById(id);
	}

	private void validateProblemPayload(Problem problem) {
		if (problem == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem payload is required");
		}
		if (sanitizeTitle(problem.getTitle()).isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem title is required");
		}
		if (sanitizeDescription(problem.getDescription()).isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem description is required");
		}
	}

	private String sanitizeTitle(String title) {
		return title == null ? "" : title.trim();
	}

	private String sanitizeDescription(String description) {
		return description == null ? "" : description.trim();
	}

	private String normalizeDifficulty(String difficulty) {
		String normalized = difficulty == null ? "" : difficulty.trim().toUpperCase();
		if (normalized.isBlank()) {
			return "MEDIUM";
		}
		if ("EASY".equals(normalized) || "MEDIUM".equals(normalized) || "HARD".equals(normalized)) {
			return normalized;
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Difficulty must be EASY, MEDIUM, or HARD");
	}
}
