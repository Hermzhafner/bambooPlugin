package com.Ranorex.hermz.bamboo.junittask.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.Ranorex.hermz.bamboo.junittask.api.MyPluginComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({MyPluginComponent.class})
@Named ("myPluginComponent")
public class MyPluginComponentImpl implements MyPluginComponent
{
    @ComponentImport
    private final ApplicationProperties applicationProperties;
    
    public String path = "";
    int result;
    
    @Inject
    public MyPluginComponentImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        
        File f = applicationProperties.getHomeDirectory();
        String s = f.getParentFile().getParentFile().getName() + ".exe";
        String execFile = f.getAbsolutePath() + File.pathSeparator + s;
        File jFile = null;
        
        
        
        try {
			Process process = new ProcessBuilder(execFile).start();
			while (process.isAlive())
			{
				this.wait(1000);
			}
			result = process.exitValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for (File currentfile : f.listFiles()) {
        	if (currentfile.getName().contains(".junit.xml")) {
        		jFile = currentfile;
        	}
        }
        
        try {
			while (Files.readAllBytes(jFile.toPath()).length == 3) {
				this.wait(5000);
			}
			byte[] b = Files.readAllBytes(jFile.toPath());
			byte[] newB = new byte[b.length - 3];
			
			for (int i = 0; i < newB.length; i++) {
				newB[i] = b[i+3];
			}
			
			Path p =Paths.get(jFile.getAbsolutePath().replaceAll(".junit.xml", "_fixed.junit.xml"));
			Files.write(p, newB, (OpenOption) null);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    
    
    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
    
}