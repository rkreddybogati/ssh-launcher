package com.scalr.ssh.controller.extension.shared;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BaseControllerExtension;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.filesystem.FileSystemManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class DestinationControllerExtension extends BaseControllerExtension {
    public DestinationControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getCommandLineOptions() throws InvalidConfigurationException {
        ArrayList<String> destinationBits = new ArrayList<String>();
        if (sshConfiguration.getUsername() != null) {
            destinationBits.add(sshConfiguration.getUsername());
            destinationBits.add("@");
        }
        destinationBits.add(sshConfiguration.getHost());
        return new String[]{StringUtils.join(destinationBits, "")};
    }
}
