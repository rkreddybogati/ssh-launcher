package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.filesystem.FileSystemManager;

import java.io.File;
import java.io.IOException;

public class PuTTYManager extends BaseSSHManager {
    public PuTTYManager(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    public PuTTYManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    protected String getPrivateKey() {
        return sshConfiguration.getPuttySSHPrivateKey();
    }

    @Override
    protected String getPrivateKeyExtension() {
        return "ppk";
    }

    @Override
    protected String getExecutablePath() throws InvalidEnvironmentException {
        File[] candidateLocations = new File[] {new File("C:/", "Program Files (x86)"), new File("C:/", "Program Files")};
        String execPath = new File("PuTTY", "putty.exe").getPath();

        File puttyExecutable = fsManager.findInPaths(candidateLocations, execPath);

        if (puttyExecutable == null) {
            getLogger().severe("Unable to locate PuTTY executable");
            throw new InvalidEnvironmentException(String.format("Unable to find PuTTy. Is it installed?"));
        }

        try {
            return puttyExecutable.getCanonicalPath();
        } catch (IOException e) {
            throw new InvalidEnvironmentException("Unable to resolve path to PuTTY");
        }
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
