package com.scalr.ssh.provider.linux;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.filesystem.FileSystemManager;

public class LinuxXTermSSHProvider extends LinuxBaseSSHProvider {
    public LinuxXTermSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public LinuxXTermSSHProvider(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    protected String getTerminalEmulatorName() {
        return "xterm";
    }

    @Override
    protected String getTerminalEmulatorCommandFlag() {
        return "-e";
    }
}
