package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.fs.FileSystemManager;

import java.io.IOException;
import java.util.ArrayList;

public class UnixSSHManager extends BaseSSHManager {
    public UnixSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public UnixSSHManager(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getSSHCommandLineBits() throws InvalidEnvironmentException {
        // TODO --> Share code with PuTTY Manager
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add("ssh");  // TODO -> Where is this installed?

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-p");
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
