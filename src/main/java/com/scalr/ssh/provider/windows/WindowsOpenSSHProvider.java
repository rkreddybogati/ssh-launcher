package com.scalr.ssh.provider.windows;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.OpenSSHController;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.provider.base.BaseSSHProvider;

import java.util.ArrayList;
import java.util.Collections;

public class WindowsOpenSSHProvider extends BaseSSHProvider {
    public WindowsOpenSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        OpenSSHController sshController = new OpenSSHController(sshConfiguration);
        sshController.setupEnvironment();

        ArrayList<String> providerCommandLineBits = new ArrayList<String>();

        providerCommandLineBits.add("cmd.exe");
        providerCommandLineBits.add("/c");
        providerCommandLineBits.add("start");
        providerCommandLineBits.add("Scalr SSH Session");
        Collections.addAll(providerCommandLineBits, sshController.getSSHCommandLineBits());

        ArrayList<String> escapedProviderCommandLineBits = new ArrayList<String>();
        for (String sshCommandLineBit : providerCommandLineBits) {
            if (sshCommandLineBit.contains(" ")) {
                escapedProviderCommandLineBits.add("\"" + sshCommandLineBit + "\"");
            } else {
                escapedProviderCommandLineBits.add(sshCommandLineBit);
            }
        }
        return escapedProviderCommandLineBits.toArray(new String[escapedProviderCommandLineBits.size()]);
    }
}
