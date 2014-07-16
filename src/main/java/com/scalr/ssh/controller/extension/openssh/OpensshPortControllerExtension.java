package com.scalr.ssh.controller.extension.openssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BasePortControllerExtension;
import com.scalr.ssh.filesystem.FileSystemManager;

public class OpensshPortControllerExtension extends BasePortControllerExtension {

    public OpensshPortControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getPortOption() {
        return "-p";
    }
}
