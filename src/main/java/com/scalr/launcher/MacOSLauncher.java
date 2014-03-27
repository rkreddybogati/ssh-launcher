package com.scalr.launcher;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.scalr.SSHConfiguration;
import com.scalr.exception.EnvironmentSetupException;

import java.io.IOException;

public class MacOSLauncher extends UnixLauncher {
    @Override
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException {
        String sshCommandLine = getSSHCommandLine(sshConfiguration);

        final NSDictionary root = new NSDictionary();
        root.put("CommandString", sshCommandLine);
        root.put("RunCommandAsShell", true);
        root.put("name", "Scalr SSH");
        root.put("shellExitAction", 0);
        root.put("type", "Window Settings");

        commandFile = getTemporaryFile("ssh-command", ".terminal");
        PropertyListParser.saveAsXML(root, commandFile);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        //return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", path};
        return new String[] {"/usr/bin/open", path};
    }
}
