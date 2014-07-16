package com.scalr.ssh.controller.extension;

import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;

public interface ControllerExtension {
    public void setupEnvironment() throws LauncherException;
    public String[] getCommandLineOptions() throws InvalidConfigurationException;
}
