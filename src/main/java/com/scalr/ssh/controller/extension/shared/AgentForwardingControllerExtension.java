package com.scalr.ssh.controller.extension.shared;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BaseControllerExtension;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.filesystem.FileSystemManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class AgentForwardingControllerExtension extends BaseControllerExtension {
    public AgentForwardingControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getCommandLineOptions() throws InvalidConfigurationException {
        return new String[] {"-A"};
    }
}
