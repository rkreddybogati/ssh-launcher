package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.fs.FileSystemManager;

public class UnixSSHManager extends BaseSSHManager {
    public UnixSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public UnixSSHManager(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
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
