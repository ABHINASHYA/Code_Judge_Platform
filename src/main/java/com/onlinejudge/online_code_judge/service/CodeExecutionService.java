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
	// Includes container startup + program execution time. 5s was causing false TLE on small inputs.
	private static final int TIME_LIMIT_SECONDS = 12;

	public String executeCode(String code, String input, Language language) throws Exception {
		String sanitizedCode = sanitizeSource(code);
		String sanitizedInput = input == null ? "" : input;

		Path dir = Path.of(CODE_DIR);

		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}

		switch (language) {

			case JAVA:
				return executeJava(sanitizedCode, sanitizedInput);

			case PYTHON:
				return executePython(sanitizedCode, sanitizedInput);

			case CPP:
				return executeCpp(sanitizedCode, sanitizedInput);

			default:
				throw new RuntimeException("Unsupported language");
		}
	}

	private String executeJava(String code, String input) throws Exception {
		if (isCommandAvailable("javac") && isCommandAvailable("java")) {
			return executeJavaLocal(code, input);
		}
		if (!isDockerAvailable()) {
			return "Internal Error: Docker is unavailable and local Java runtime was not found";
		}
		return executeJavaDocker(code, input);
	}

	private String executeJavaLocal(String code, String input) throws Exception {
		Path sourceFile = Path.of(CODE_DIR, "Solution.java");
		Files.writeString(sourceFile, code);

		ProcessBuilder compile = new ProcessBuilder(
				"javac", "-encoding", "UTF-8", sourceFile.toString());

		Process compileProcess = compile.start();
		int compileResult = compileProcess.waitFor();

		if (compileResult != 0) {
			String stderr = readStream(compileProcess.getErrorStream());
			return withDetails("Compilation Error", stderr);
		}

		ProcessBuilder run = new ProcessBuilder(
				"java", "-cp", CODE_DIR, "Solution");

		return runProcess(run, input);
	}

	private String executeJavaDocker(String code, String input) throws Exception {
		Path sourceFile = Path.of(CODE_DIR, "Solution.java");
		Files.writeString(sourceFile, code);

		ProcessBuilder compile = new ProcessBuilder(
				"docker", "run", "--rm",
				"--pull=never",
				"-v", CODE_DIR + ":/app",
				"eclipse-temurin:17",
				"javac", "-encoding", "UTF-8", "/app/Solution.java");

		Process compileProcess = compile.start();
		int compileResult = compileProcess.waitFor();

		if (compileResult != 0) {
			String stderr = readStream(compileProcess.getErrorStream());
			return withDetails("Compilation Error", stderr);
		}

		ProcessBuilder run = new ProcessBuilder(
				"docker", "run", "--rm",
				"-i",
				"--pull=never",
				"-v", CODE_DIR + ":/app",
				"--memory=256m",
				"--cpus=1",
				"eclipse-temurin:17",
				"java", "-cp", "/app", "Solution");

		return runProcess(run, input);
	}

	private String executePython(String code, String input) throws Exception {
		if (isCommandAvailable("python")) {
			return executePythonLocal(code, input);
		}
		if (!isDockerAvailable()) {
			return "Internal Error: Docker is unavailable and local Python runtime was not found";
		}
		return executePythonDocker(code, input);
	}

	private String executePythonLocal(String code, String input) throws Exception {
		Path sourceFile = Path.of(CODE_DIR, "Solution.py");
		Files.writeString(sourceFile, code);

		ProcessBuilder run = new ProcessBuilder(
				"python", sourceFile.toString());

		return runProcess(run, input);
	}

	private String executePythonDocker(String code, String input) throws Exception {
		Path sourceFile = Path.of(CODE_DIR, "Solution.py");
		Files.writeString(sourceFile, code);

		ProcessBuilder run = new ProcessBuilder(
				"docker", "run", "--rm",
				"-i",
				"--pull=never",
				"-v", CODE_DIR + ":/app",
				"--memory=256m",
				"--cpus=1",
				"python:3.10",
				"python", "/app/Solution.py");

		return runProcess(run, input);
	}

	private String executeCpp(String code, String input) throws Exception {
		if (isCommandAvailable("g++")) {
			return executeCppLocal(code, input);
		}
		if (!isDockerAvailable()) {
			return "Internal Error: Docker is unavailable and local C++ compiler was not found";
		}
		return executeCppDocker(code, input);
	}

	private String executeCppLocal(String code, String input) throws Exception {
		Path sourceFile = Path.of(CODE_DIR, "Solution.cpp");
		Files.writeString(sourceFile, code);

		ProcessBuilder compile = new ProcessBuilder(
				"g++", sourceFile.toString(), "-O2", "-std=c++17", "-o", Path.of(CODE_DIR, "a.out").toString());

		Process compileProcess = compile.start();
		int compileResult = compileProcess.waitFor();

		if (compileResult != 0) {
			String stderr = readStream(compileProcess.getErrorStream());
			return withDetails("Compilation Error", stderr);
		}

		ProcessBuilder run = new ProcessBuilder(
				Path.of(CODE_DIR, "a.out").toString());

		return runProcess(run, input);
	}

	private String executeCppDocker(String code, String input) throws Exception {
		Path sourceFile = Path.of(CODE_DIR, "Solution.cpp");
		Files.writeString(sourceFile, code);

		ProcessBuilder compile = new ProcessBuilder(
				"docker", "run", "--rm",
				"--pull=never",
				"-v", CODE_DIR + ":/app",
				"gcc:latest",
				"g++", "/app/Solution.cpp", "-o", "/app/a.out");

		Process compileProcess = compile.start();
		int compileResult = compileProcess.waitFor();

		if (compileResult != 0) {
			String stderr = readStream(compileProcess.getErrorStream());
			return withDetails("Compilation Error", stderr);
		}

		ProcessBuilder run = new ProcessBuilder(
				"docker", "run", "--rm",
				"-i",
				"--pull=never",
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
			if (output.length() > 0) {
				output.append('\n');
			}
			output.append(line);
		}

		int exitCode = process.exitValue();

		if (exitCode != 0) {
			String stderr = readStream(process.getErrorStream());
			return withDetails("Runtime Error", stderr);
		}

		return output.toString().trim();
	}

	private boolean isCommandAvailable(String command) {
		try {
			String os = System.getProperty("os.name", "").toLowerCase();
			String resolver = os.contains("win") ? "where" : "which";
			Process process = new ProcessBuilder(resolver, command).start();
			boolean finished = process.waitFor(2, TimeUnit.SECONDS);
			return finished && process.exitValue() == 0;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean isDockerAvailable() {
		try {
			Process process = new ProcessBuilder("docker", "info").start();
			boolean finished = process.waitFor(3, TimeUnit.SECONDS);
			return finished && process.exitValue() == 0;
		} catch (Exception ex) {
			return false;
		}
	}

	private String readStream(InputStream stream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (content.length() > 0) {
					content.append('\n');
				}
				content.append(line);
			}
			return content.toString().trim();
		}
	}

	private String withDetails(String prefix, String details) {
		if (details == null || details.isBlank()) {
			return prefix;
		}

		String trimmed = details.trim();
		int maxLen = 1000;
		if (trimmed.length() > maxLen) {
			trimmed = trimmed.substring(0, maxLen) + "...";
		}

		return prefix + ": " + trimmed;
	}

	private String sanitizeSource(String source) {
		if (source == null) {
			return "";
		}

		String sanitized = source.replace("\uFEFF", "");
		if (!sanitized.isEmpty() && sanitized.charAt(0) == '\u200B') {
			sanitized = sanitized.substring(1);
		}
		return sanitized;
	}
}
