package com.scalr.launcher;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.fs.FileSystemManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class LinuxSSHLauncher extends UnixSSHLauncher {

    @Override
    void createCommandFile(String sshCommandLine) throws EnvironmentSetupException {
        try {
            commandFile = FileSystemManager.getTemporaryFile("ssh-command", ".sh");
        } catch (IOException e) {
            e.printStackTrace();
            throw new EnvironmentSetupException();
        }
        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(commandFile, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new EnvironmentSetupException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new EnvironmentSetupException();
        }
        writer.println("#!/bin/bash");
        writer.println(sshCommandLine);  //TODO: Implement for real
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException();
        }
    }


    @Override
    protected String[] getSSHCommandFromPath(String path) {
        //TODO
        return new String[0];
    }


}
