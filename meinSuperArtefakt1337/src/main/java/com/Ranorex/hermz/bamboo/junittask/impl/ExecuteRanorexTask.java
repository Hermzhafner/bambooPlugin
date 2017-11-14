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

	@SuppressWarnings("deprecation")
	public TaskResult execute(TaskContext taskContext) throws TaskException {
		final BuildLogger buildLogger = taskContext.getBuildLogger();

		buildLogger.addBuildLogEntry("Start Ranorex Test");

		int result = -1;
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
			buildLogger.addBuildLogEntry("End Ranorex Test");
		}
	}

	static String slName = "";

	private synchronized int executeTest(TaskContext taskContext) throws IOException, InterruptedException {
		taskContext.getBuildLogger().addBuildLogEntry(taskContext.getRootDirectory().getAbsolutePath());
		File f = taskContext.getRootDirectory();

		for (File file : f.listFiles()) {
			if (file.getName().endsWith(".rxsln")) {
				slName = file.getName().replaceAll(".rxsln", "");
			}
		}
		
				
		String execFile = f.getAbsolutePath() + "\\" + slName + "\\bin" + "\\Debug\\" + slName + ".exe /rf:" + taskContext.getRootDirectory() + "\\report.rxlog";
		File jFile = null;
		int result = -1;

//		Process process = new ProcessBuilder(execFile).start();
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(execFile);
		
		while (process.isAlive()) {
			this.wait(1000);
		}
		result = process.exitValue();

		taskContext.getBuildLogger().addBuildLogEntry("Test executed. Result: " + result);
		
//		File debugfolder = new File(f.getAbsolutePath() + "\\" + slName + "\\bin" + "\\Debug");

		for (File currentfile : f.listFiles()) {
			if (currentfile.getName().contains(".junit.xml")) {
				jFile = currentfile;
			}
		}

		if (jFile != null) {
			while (Files.readAllBytes(jFile.toPath()).length == 3) {
				this.wait(5000);
			}
			byte[] b = Files.readAllBytes(jFile.toPath());
			byte[] newB = new byte[b.length - 3];
			
			for (int i = 0; i < newB.length; i++) {
				newB[i] = b[i + 3];
			}
			
			Path p = Paths.get(jFile.getAbsolutePath().replaceAll(".junit.xml", "_fixed.junit.xml"));
			Files.write(p, newB);
		}

		return result;
	}
}
