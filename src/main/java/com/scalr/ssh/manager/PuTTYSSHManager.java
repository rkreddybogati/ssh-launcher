package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PuTTYSSHManager extends BaseSSHManager {
    public PuTTYSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    private String getPuTTYPath() throws InvalidEnvironmentException {
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
            }
        }

        getLogger().severe("Unable to find PuTTY");
        throw new InvalidEnvironmentException(String.format("Unable to find PuTTy"));
    }

    @Override
    public String[] getSSHCommandLineBits() throws InvalidEnvironmentException {
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add(getPuTTYPath());
        sshCommandLineBits.add("-ssh");

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-P");
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.getPrivateKey() != null) {
            sshCommandLineBits.add("-i");
            try {
                sshCommandLineBits.add(getSSHPrivateKeyFilePath());
            } catch (IOException e) {
                throw new InvalidEnvironmentException("Unable to resolve SSH Key file path");
            }
        }

        sshCommandLineBits.add(getDestination());

        return sshCommandLineBits.toArray(new String[sshCommandLineBits.size()]);
    }
}
