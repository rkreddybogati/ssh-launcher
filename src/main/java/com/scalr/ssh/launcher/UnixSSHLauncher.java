package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import com.scalr.ssh.manager.UnixSSHManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public abstract class UnixSSHLauncher extends BaseSSHLauncher {

    @Override
    protected void createCommandFile(String sshCommandLine) throws EnvironmentSetupException {
        try {
            commandFile = FileSystemManager.getTemporaryFile("ssh-command", ".sh");
        } catch (IOException e) {
            throw new EnvironmentSetupException("Error creating command file.");
        }
        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(commandFile, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new EnvironmentSetupException("Error writing to command file.");
        } catch (UnsupportedEncodingException e) {
            throw new EnvironmentSetupException("Error writing to command file.");
        }

        writer.println("#!/bin/bash");
        writer.println(sshCommandLine);
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException("Error setting command file executable.");
        }
    }

    @Override
    protected SSHManagerInterface getSSHManager(SSHConfiguration sshConfiguration) {
        return new UnixSSHManager(sshConfiguration);
    }

}
