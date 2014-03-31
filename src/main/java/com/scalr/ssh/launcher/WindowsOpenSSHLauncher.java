package com.scalr.ssh.launcher;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.manager.OpenSSHManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

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

        ArrayList<String> sshCommandBits = new ArrayList<String>();
        sshCommandBits.add("cmd.exe");
        sshCommandBits.add("/c");
        sshCommandBits.add("\"\"" + StringUtils.join(sshManager.getSSHCommandLineBits(), "\" \"") + "\"\"");
        return sshCommandBits.toArray(new String[sshCommandBits.size()]);
    }
}
