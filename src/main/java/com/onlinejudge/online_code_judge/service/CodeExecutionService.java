package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.model.Language;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

	private static final String CODE_DIR = "C:/code-execution";
	private static final int TIME_LIMIT_SECONDS = 5;

	public String executeCode(String code, String input, Language language) throws Exception {

		Path dir = Path.of(CODE_DIR);

		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}

		switch (language) {

			case JAVA:
				return executeJava(code, input);

			case PYTHON:
				return executePython(code, input);

			case CPP:
				return executeCpp(code, input);

			default:
				throw new RuntimeException("Unsupported language");
		}
	}

	private String executeJava(String code, String input) throws Exception {

		Path sourceFile = Path.of(CODE_DIR, "Solution.java");
		Files.writeString(sourceFile, code);

		ProcessBuilder compile = new ProcessBuilder(
				"docker", "run", "--rm",
				"-v", CODE_DIR + ":/app",
				"eclipse-temurin:17",
				"javac", "/app/Solution.java");

		Process compileProcess = compile.start();
		int compileResult = compileProcess.waitFor();

		if (compileResult != 0) {
			return "Compilation Error";
		}

		ProcessBuilder run = new ProcessBuilder(
				"docker", "run", "--rm",
				"-v", CODE_DIR + ":/app",
				"--memory=256m",
				"--cpus=1",
				"eclipse-temurin:17",
				"java", "-cp", "/app", "Solution");

		return runProcess(run, input);
	}

	private String executePython(String code, String input) throws Exception {

		Path sourceFile = Path.of(CODE_DIR, "Solution.py");
		Files.writeString(sourceFile, code);

		ProcessBuilder run = new ProcessBuilder(
				"docker", "run", "--rm",
				"-v", CODE_DIR + ":/app",
				"--memory=256m",
				"--cpus=1",
				"python:3.10",
				"python", "/app/Solution.py");

		return runProcess(run, input);
	}

	private String executeCpp(String code, String input) throws Exception {

		Path sourceFile = Path.of(CODE_DIR, "Solution.cpp");
		Files.writeString(sourceFile, code);

		ProcessBuilder compile = new ProcessBuilder(
				"docker", "run", "--rm",
				"-v", CODE_DIR + ":/app",
				"gcc:latest",
				"g++", "/app/Solution.cpp", "-o", "/app/a.out");

		Process compileProcess = compile.start();
		int compileResult = compileProcess.waitFor();

		if (compileResult != 0) {
			return "Compilation Error";
		}

		ProcessBuilder run = new ProcessBuilder(
				"docker", "run", "--rm",
				"-v", CODE_DIR + ":/app",
				"--memory=256m",
				"--cpus=1",
				"gcc:latest",
				"/app/a.out");

		return runProcess(run, input);
	}

	private String runProcess(ProcessBuilder processBuilder, String input) throws Exception {

		Process process = processBuilder.start();

		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(process.getOutputStream()));

		bw.write(input);
		bw.flush();
		bw.close();

		boolean finished = process.waitFor(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

		if (!finished) {
			process.destroyForcibly();
			return "Time Limit Exceeded";
		}

		BufferedReader br = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

		StringBuilder output = new StringBuilder();
		String line;

		while ((line = br.readLine()) != null) {
			output.append(line);
		}

		int exitCode = process.exitValue();

		if (exitCode != 0) {
			return "Runtime Error";
		}

		return output.toString().trim();
	}
}