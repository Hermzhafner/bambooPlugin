package com.Ranorex.hermz.bamboo.junittask.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ExecuteRanorexTask implements TaskType {
	private static final String _FIXED_JUNIT_XML = "_fixed.junit.xml";
	private static final String JUNIT_XML = ".junit.xml";
	private static final String TEST_EXECUTED_RESULT = "Test executed. Result: ";
	private static final String REPORT_RXLOG = "report.rxlog";
	private static final String EXE_RF = ".exe /rf:";
	private static final String DEBUG = "Debug";
	private static final String BIN = "bin";
	private static final String RXSLN = ".rxsln";
	private static final String END_RANOREX_TEST = "End Ranorex Test";
	private static final String START_RANOREX_TEST = "Start Ranorex Test";
	static String slName = "";

	@SuppressWarnings("deprecation")
	public TaskResult execute(TaskContext taskContext) throws TaskException {
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		int result = -1;
		buildLogger.addBuildLogEntry(START_RANOREX_TEST);

		try {
			result = executeTest(taskContext);
			if (result == 0) {
				return TaskResultBuilder.create(taskContext).success().build();
			} else {
				return TaskResultBuilder.create(taskContext).failed().build();
			}
		} catch (IOException e) {
			buildLogger.addBuildLogEntry(e.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		} catch (InterruptedException e) {
			buildLogger.addBuildLogEntry(e.getMessage());
			return TaskResultBuilder.create(taskContext).failedWithError().build();
		} finally {
			buildLogger.addBuildLogEntry(END_RANOREX_TEST);
		}
	}

	private synchronized int executeTest(TaskContext taskContext) throws IOException, InterruptedException {
		taskContext.getBuildLogger().addBuildLogEntry(taskContext.getRootDirectory().getAbsolutePath());
		File f = taskContext.getRootDirectory();
		File jFile = null;
		int result = -1;
		String execFile = extracted(taskContext, f);
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(execFile);

		while (process.isAlive()) {
			this.wait(1000);
		}
		result = process.exitValue();
		taskContext.getBuildLogger().addBuildLogEntry(TEST_EXECUTED_RESULT + result);
		jFile = extracted(f, jFile);
		extracted(jFile);

		return result;
	}

	private String extracted(TaskContext taskContext, File f) {
		return f.getAbsolutePath() + File.separator + slName + File.separator + BIN + File.separator + DEBUG
				+ File.separator + slName + EXE_RF + taskContext.getRootDirectory() + File.separator + REPORT_RXLOG;
	}

	private File extracted(File f, File jFile) {
		for (File file : f.listFiles()) {
			if (file.getName().endsWith(RXSLN)) {
				slName = file.getName().replaceAll(RXSLN, "");
			}
		}
		for (File currentfile : f.listFiles()) {
			if (currentfile.getName().contains(JUNIT_XML)) {
				jFile = currentfile;
			}
		}
		return jFile;
	}

	private void extracted(File jFile) throws IOException, InterruptedException {
		if (jFile != null) {
			while (Files.readAllBytes(jFile.toPath()).length == 3) {
				this.wait(5000);
			}
			byte[] b = Files.readAllBytes(jFile.toPath());
			byte[] newB = new byte[b.length - 3];

			for (int i = 0; i < newB.length; i++) {
				newB[i] = b[i + 3];
			}

			Path p = Paths.get(jFile.getAbsolutePath().replaceAll(JUNIT_XML, _FIXED_JUNIT_XML));
			Files.write(p, newB);
		}
	}
}
