package com.scalr.ssh.controller.extension.putty;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.BaseKeyAuthControllerExtension;
import com.scalr.ssh.filesystem.FileSystemManager;

public class PuttyKeyAuthControllerExtension extends BaseKeyAuthControllerExtension {

    public PuttyKeyAuthControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getPrivateKeyOption() {
        return "-i";
    }

    @Override
    protected String getPrivateKey() {
        return sshConfiguration.getPuttySSHPrivateKey();
    }

    @Override
    protected String getPrivateKeyExtension() {
        return "ppk";
    }
}
