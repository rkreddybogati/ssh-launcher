package com.scalr.ssh.controller.extension.openssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BaseKeyAuthControllerExtension;
import com.scalr.ssh.filesystem.FileSystemManager;

public class OpensshKeyAuthControllerExtension extends BaseKeyAuthControllerExtension {

    public OpensshKeyAuthControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getPrivateKeyOption() {
        return "-i";
    }

    @Override
    protected String getPrivateKey() {
        return sshConfiguration.getOpenSSHPrivateKey();
    }

    @Override
    protected String getPrivateKeyExtension() {
        return "pem";
    }
}
