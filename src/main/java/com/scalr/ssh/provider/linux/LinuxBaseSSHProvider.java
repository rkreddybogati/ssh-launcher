package com.scalr.ssh.provider.linux;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.SSHManager;
import com.scalr.ssh.provider.base.BaseSSHProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

abstract public class LinuxBaseSSHProvider extends BaseSSHProvider {

    public LinuxBaseSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public LinuxBaseSSHProvider(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHManager sshManager = new OpenSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();

        // Note: we wrap ssh into a command line so that we can keep the terminal window open when
        //       SSH exits (gnome-terminal does not let us do that)

        String sshCommand = StringUtils.join(sshManager.getSSHCommandLineBits(), " ");
        String scriptCommand = String.format("clear ; echo '%s' ; %s ; echo 'Hit enter to exit' ; read ; exit", sshCommand, sshCommand);
        // TODO -> Use exit in Mac OS, too?

        ArrayList<String> commandBits = new ArrayList<String>();
        commandBits.add(getTerminalEmulator());
        commandBits.add(getTerminalEmulatorCommandFlag());
        commandBits.add("/bin/bash");  // /bin/sh messes up when we use `read`
        commandBits.add("-c");
        commandBits.add(scriptCommand);

        return commandBits.toArray(new String[commandBits.size()]);
    }

    private String getTerminalEmulator() throws InvalidEnvironmentException {
        String terminalEmulatorName = getTerminalEmulatorName();

        File terminalEmulatorExecutable = fsManager.findInPaths(
                new File[]{new File("/bin"), new File("/usr/bin"), new File("/usr/local/bin")},
                new File(terminalEmulatorName).getPath()
        );

        if (terminalEmulatorExecutable == null) {
            getLogger().severe(String.format("Unable to locate %s executable", terminalEmulatorName));
            throw new InvalidEnvironmentException(String.format("Unable to find %s. Is it installed?", terminalEmulatorName));
        }

        // TODO -> This logic should be abstracted away in a single wrapper
        // We use it to find PuTTY, to find OpenSSH, and to find out Terminal Emulator
        // We should also use it to find osascript
        // We should also have it use the $PATH!

        try {
            return terminalEmulatorExecutable.getCanonicalPath();
        } catch (IOException e) {
            throw new InvalidEnvironmentException(String.format("Unable to resolve path to %s", terminalEmulatorName));
        }
    }


    abstract protected String getTerminalEmulatorName();
    abstract protected String getTerminalEmulatorCommandFlag();
}
