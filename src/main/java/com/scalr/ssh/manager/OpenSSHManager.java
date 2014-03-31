package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.fs.FileSystemManager;

public class OpenSSHManager extends BaseSSHManager {
    public OpenSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String getPrivateKey() {
        return sshConfiguration.getOpenSSHPrivateKey();
    }

    @Override
    protected String getPrivateKeyExtension() {
        return "pem";
    }

    public OpenSSHManager(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getExecutablePath() throws InvalidEnvironmentException {
        return "ssh";
    }

    @Override
    protected String[] getExecutableExtraOptions() {
        return new String[0];
    }

    @Override
    protected String getPortOption() {
        return "-p";
    }

    @Override
    protected String getPrivateKeyOption() {
        return "-i";
    }
}
