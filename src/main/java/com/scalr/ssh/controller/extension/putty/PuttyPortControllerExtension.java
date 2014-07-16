package com.scalr.ssh.controller.extension.putty;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BasePortControllerExtension;
import com.scalr.ssh.filesystem.FileSystemManager;

public class PuttyPortControllerExtension extends BasePortControllerExtension {

    public PuttyPortControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getPortOption() {
        return "-P";
    }
}
