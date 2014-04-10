package com.scalr.ssh.provider.mac;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.manager.OpenSSHManager;
import com.scalr.ssh.manager.SSHManagerInterface;
import com.scalr.ssh.provider.base.BaseSSHProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class MacAppleScriptSSHProvider extends BaseSSHProvider {
    public MacAppleScriptSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    public MacAppleScriptSSHProvider(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    private String[] getAppleScriptLines(SSHManagerInterface sshManager) throws InvalidEnvironmentException {
        String scriptCommand = String.format("%s ; echo 'Hit enter to exit' ; read ; logout", StringUtils.join(sshManager.getSSHCommandLineBits(), " "));

        ArrayList<String> appleScript = new ArrayList<String>();

        appleScript.add("tell application \"System Events\"");
        appleScript.add("if (count (processes whose bundle identifier is \"com.apple.Terminal\")) is 0 then");
        appleScript.add("tell application \"/Applications/Utilities/Terminal.app\"");
        appleScript.add("activate");
        appleScript.add(String.format("do script \"%s\" in window 0", scriptCommand));
        appleScript.add("end tell");
        appleScript.add("else");
        appleScript.add("tell application \"/Applications/Utilities/Terminal.app\"");
        appleScript.add(String.format("do script \"%s\"", scriptCommand));
        appleScript.add("activate");
        appleScript.add("end tell");
        appleScript.add("end if");
        appleScript.add("end tell");

        return appleScript.toArray(new String[appleScript.size()]);
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHManagerInterface sshManager = new OpenSSHManager(sshConfiguration);
        sshManager.setUpSSHEnvironment();

        ArrayList<String> commandBits = new ArrayList<String>();
        commandBits.add("/usr/bin/osascript");

        for (String appleScriptLine: getAppleScriptLines(sshManager)) {
            commandBits.add("-e");
            commandBits.add(appleScriptLine);
        }

        return commandBits.toArray(new String[commandBits.size()]);
    }
}
