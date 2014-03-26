package com.scalr;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MacOSSSHLauncher implements LocalSSHLauncher {
    File commandFile;

    @Override
    public void setUpEnvironment() throws IOException, EnvironmentSetupException {
        commandFile = File.createTempFile("ssh-command", ".sh");
        //commandFile.deleteOnExit(); // TODO: Find how we handle this

        PrintWriter writer = new PrintWriter(commandFile, "UTF-8");
        writer.println("#!/bin/bash");
        writer.println("ssh orozco.fr");  //TODO: Implement for real
        writer.close();

        if (!commandFile.setExecutable(true, true)) {
            throw new EnvironmentSetupException();
        }
    }

    @Override
    public void tearDownEnvironment() {
    }

    @Override
    public String[] getSSHCommand() throws InvalidEnvironmentException {
        if (commandFile == null) {
            throw new InvalidEnvironmentException();
        }

        String canonicalPath;


        try {
            canonicalPath = commandFile.getCanonicalPath();
        } catch (IOException e) {
            throw new InvalidEnvironmentException();
        }

        return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", canonicalPath};
    }
}
