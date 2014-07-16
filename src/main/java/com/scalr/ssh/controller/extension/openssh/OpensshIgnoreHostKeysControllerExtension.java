package com.scalr.ssh.controller.extension.openssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BaseControllerExtension;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.filesystem.FileSystemManager;

public class OpensshIgnoreHostKeysControllerExtension extends BaseControllerExtension {
    public OpensshIgnoreHostKeysControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getCommandLineOptions() throws InvalidConfigurationException {
        return new String[] {"-o", "UserKnownHostsFile=/dev/null", "-o", "CheckHostIP=no",  "-o", "StrictHostKeyChecking=no"};
    }
}
