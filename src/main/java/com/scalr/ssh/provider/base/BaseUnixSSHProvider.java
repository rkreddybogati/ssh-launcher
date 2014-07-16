package com.scalr.ssh.provider.base;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.OpenSSHController;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.controller.SSHController;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

public abstract class BaseUnixSSHProvider extends BaseSSHProvider {
    public BaseUnixSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    protected File createCommandFile(String sshCommandLine) throws EnvironmentSetupException {
        File commandFile;

        try {
            commandFile = fsManager.getTemporaryFile("ssh-command", ".sh");
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
        writer.println("read -p 'Press enter to exit'"); // TODO -> "Enter to exit"
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException("Error setting command file executable.");
        }

        return commandFile;
    }

    protected SSHController getSSHController(SSHConfiguration sshConfiguration) {
        return new OpenSSHController(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHController sshController = getSSHController(sshConfiguration);
        sshController.setupEnvironment();

        String[] sshCommandLineBits = sshController.getSSHCommandLineBits();
        String   sshCommandLine = StringUtils.join(sshCommandLineBits, " ");

        File commandFile = createCommandFile(sshCommandLine);
        String canonicalPath;
        try {
            canonicalPath = commandFile.getCanonicalPath();
        } catch (IOException e) {
            throw new InvalidEnvironmentException("Command file has no canonical path");
        }

        return getSSHCommandFromPath(canonicalPath);
    }

    protected abstract String[] getSSHCommandFromPath(String canonicalPath);
}
