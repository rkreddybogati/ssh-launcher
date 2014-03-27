package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class WindowsSSHManager extends BaseSSHManager {
    public WindowsSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String getSSHCommandLine() {
        //TODO --> Reuse code shared with UNIX
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        //TODO --> How to I handle uncommon installation locations?
        sshCommandLineBits.add("\"C:\\Program Files (x86)\\PuTTY\\putty.exe\"");
        sshCommandLineBits.add("-ssh");

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-P");
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.getPrivateKey() != null) {
            sshCommandLineBits.add("-i");
            sshCommandLineBits.add(getSSHPrivateKeyFilePath());
        }

        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        String   destination = StringUtils.join(destinationBits, "");
        sshCommandLineBits.add(destination);

        return StringUtils.join(sshCommandLineBits, " ");
    }
}
