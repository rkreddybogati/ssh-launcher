package com.scalr.ssh.controller.extension;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.filesystem.FileSystemManager;

abstract public class BasePortControllerExtension extends BaseControllerExtension {

    public BasePortControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getCommandLineOptions() {
        Integer port = sshConfiguration.getPort();
        if (port != null) {
            return new String[] {getPortOption(), port.toString()};
        }
        return new String[] {};
    }

    abstract protected String getPortOption ();
}
