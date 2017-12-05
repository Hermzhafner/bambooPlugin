package com.Ranorex.hermz.bamboo.junittask.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;

public class ExecuteRanorexTask implements TaskType {
	private static final String RANOREX_SOLUTION_RXSLN_FILE_NOT_FOUND = "Ranorex Solution (*.rxsln) - File not found!";
	private static final String JUNIT_XML = ".junit.xml";
	private static final String TEST_EXECUTED_RESULT = "Test executed. Result: ";
	private static final String REPORT_RXLOG = "report.rxlog";
	private static final String EXE_RF = ".exe /rf:";
	private static final String DEBUG = "Debug";
	private static final String BIN = "bin";
	private static final String RXSLN = ".rxsln";
	private static final String END_RANOREX_TEST = "End Ranorex Test";
	private static final String START_RANOREX_TEST = "Start Ranorex Test";

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
		File rootDir = taskContext.getRootDirectory();
		File jUnitReportFile = null;
		int result = -1;
		String execFile = getRXexecPath(taskContext, rootDir);
		
		// ends everything.
		if (execFile == null) {
			return 1;
		}
		
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(execFile);

		while (process.isAlive()) {
			this.wait(1000);
		}
		
		result = process.exitValue();
		taskContext.getBuildLogger().addBuildLogEntry(TEST_EXECUTED_RESULT + result);
		jUnitReportFile = getjUnitReportFile(rootDir, jUnitReportFile);
		removeBOMfromjUnitReport(jUnitReportFile);

		return result;
	}

	private String getRXexecPath(TaskContext taskContext, File f) {
		String slName = getSolutionPath(f);
		if (slName == null) {
			taskContext.getBuildLogger().addBuildLogEntry(RANOREX_SOLUTION_RXSLN_FILE_NOT_FOUND);
			return null;
		} 
		return f.getAbsolutePath() + File.separator + slName + File.separator + BIN + File.separator + DEBUG
				+ File.separator + slName + EXE_RF + taskContext.getRootDirectory() + File.separator + REPORT_RXLOG;
	}

	private String getSolutionPath(File f) {
		for (File file : f.listFiles()) {
			if (file.getName().endsWith(RXSLN)) {
				return file.getName().replaceAll(RXSLN, "");
			}
		}
		return null;
	}
	
	private File getjUnitReportFile(File f, File jFile) {
		for (File currentfile : f.listFiles()) {
			if (currentfile.getName().contains(JUNIT_XML)) {
				jFile = currentfile;
			}
		}
		return jFile;
	}

	private void removeBOMfromjUnitReport(File reportFile) throws IOException, InterruptedException {
		if (reportFile != null) {
			while (Files.readAllBytes(reportFile.toPath()).length == 3) {
				this.wait(5000);
			}
			byte[] existingReport = Files.readAllBytes(reportFile.toPath());
			byte[] newReport = new byte[existingReport.length - 3];

			for (int i = 0; i < newReport.length; i++) {
				newReport[i] = existingReport[i + 3];
			}

			Path reportPath = Paths.get(reportFile.getAbsolutePath());
			Files.delete(reportPath);
			Files.write(reportPath, newReport);
		}
	}
}
