package com.scalr.ssh.controller.extension.shared;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BaseControllerExtension;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.filesystem.FileSystemManager;

public class NoopControllerExtension extends BaseControllerExtension {
    public NoopControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getCommandLineOptions() throws InvalidConfigurationException {
        return new String[0];
    }
}
