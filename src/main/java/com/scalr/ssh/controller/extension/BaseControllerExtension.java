package com.scalr.ssh.controller.extension;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.logging.Loggable;

abstract public class BaseControllerExtension extends Loggable implements ControllerExtension {
    protected SSHConfiguration sshConfiguration;
    protected final FileSystemManager fsManager;


    public BaseControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        this.sshConfiguration = sshConfiguration;
        this.fsManager = fsManager;
    }

    @Override
    public void setupEnvironment() throws LauncherException {}

}
