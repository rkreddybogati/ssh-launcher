package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.manager.OpenSSHManager;

import java.util.ArrayList;
import java.util.Collections;

public class WindowsOpenSSHLauncher extends BaseSSHLauncher {
    public WindowsOpenSSHLauncher(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    public WindowsOpenSSHLauncher(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        OpenSSHManager sshManager = new OpenSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();

        ArrayList<String> launcherCommandLineBits = new ArrayList<String>();

        launcherCommandLineBits.add("cmd.exe");
        launcherCommandLineBits.add("/c");
        launcherCommandLineBits.add("start");
        launcherCommandLineBits.add("Scalr SSH Session");
        Collections.addAll(launcherCommandLineBits, sshManager.getSSHCommandLineBits());

        ArrayList<String> escapedLauncherCommandLineBits = new ArrayList<String>();
        for (String sshCommandLineBit : launcherCommandLineBits) {
            if (sshCommandLineBit.contains(" ")) {
                escapedLauncherCommandLineBits.add("\"" + sshCommandLineBit + "\"");
            } else {
                escapedLauncherCommandLineBits.add(sshCommandLineBit);
            }
        }
        return escapedLauncherCommandLineBits.toArray(new String[escapedLauncherCommandLineBits.size()]);
    }
}
