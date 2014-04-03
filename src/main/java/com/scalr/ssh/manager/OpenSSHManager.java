package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.filesystem.FileSystemManager;

import java.io.File;
import java.io.IOException;

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
        // TODO --> Refactor and reuse code from PuTTY
        File[] candidateLocations = new File[] {
                fsManager.pathJoin("/usr", "bin"), fsManager.pathJoin("/usr", "local", "bin"), new File("/bin"),
                fsManager.pathJoin("C:/", "Program Files (x86)", "OpenSSH", "bin"),
                fsManager.pathJoin("C:/", "Program Files", "OpenSSH", "bin")
        };
        String[] candidateNames = new String[] { "ssh", "ssh.exe"};

        File sshExecutable;
        for (String candidateName : candidateNames) {
            sshExecutable = fsManager.findInPaths(candidateLocations, candidateName);
            if (sshExecutable != null) {
                try {
                    return sshExecutable.getCanonicalPath();
                } catch (IOException e) {
                    throw new InvalidEnvironmentException("Unable to resolve path to SSH");
                }
            }
        }

        getLogger().severe("Unable to locate ssh executable");
        throw new InvalidEnvironmentException("Unable to find ssh. Is it installed?");
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
