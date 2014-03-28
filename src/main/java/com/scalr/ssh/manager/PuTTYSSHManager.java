package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class PuTTYSSHManager extends BaseSSHManager {
    public PuTTYSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    private String getPuTTYPath() {
        String[] pathBits = {"C:/", "Program Files (x86)", "PuTTY", "putty.exe"};
        ArrayList<String> escapedPathBits = new ArrayList<String>();

        for (String pathBit : pathBits) {
            if (pathBit.contains(" ")) {
                escapedPathBits.add(String.format("\"%s\"", pathBit));
            } else {
                escapedPathBits.add(pathBit);
            }
        }

        return StringUtils.join(pathBits, "\\");
    }

    @Override
    public String[] getSSHCommandLineBits() {
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add(getPuTTYPath());
        sshCommandLineBits.add("-ssh");

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-P");
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.getPrivateKey() != null) {
            sshCommandLineBits.add("-i");
            sshCommandLineBits.add(getSSHPrivateKeyFilePath());
        }

        sshCommandLineBits.add(getDestination());

        return sshCommandLineBits.toArray(new String[sshCommandLineBits.size()]);
    }
}
