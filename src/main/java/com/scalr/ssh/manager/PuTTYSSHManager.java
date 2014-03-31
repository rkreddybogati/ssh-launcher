package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.fs.FileSystemManager;

import java.io.File;
import java.io.IOException;

public class PuTTYSSHManager extends BaseSSHManager {
    public PuTTYSSHManager(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    public PuTTYSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String getExecutablePath() throws InvalidEnvironmentException {
        String[] candidateLocations = {"Program Files (x86)", "Program Files"};
        String basePath = "C:/";
        String execPath = new File("PuTTY", "putty.exe").getPath();

        File candidateFile;
        for (String candidateLocation : candidateLocations) {
            candidateFile = new File(new File(basePath, candidateLocation), execPath);
            getLogger().fine(String.format("Looking up PuTTY in '%s'", candidateFile.getPath()));

            if (fsManager.fileExists(candidateFile)) {
                getLogger().fine(String.format("Found PuTTY in %s", candidateFile.getPath()));
                try {
                    return candidateFile.getCanonicalPath();
                } catch (IOException e) {
                    throw new InvalidEnvironmentException("Unable to resolve path to PuTTY");
                }
            } else {
                getLogger().warning(String.format("PuTTY not found in '%s'", candidateFile.getPath()));
            }
        }

        getLogger().severe("Unable to find PuTTY");
        throw new InvalidEnvironmentException(String.format("Unable to find PuTTy"));
    }

    @Override
    protected String[] getExecutableExtraOptions() {
        return new String[] {"-ssh"};
    }

    @Override
    protected String getPortOption() {
        return "-P";
    }

    @Override
    protected String getPrivateKeyOption() {
        return "-i";
    }
}
