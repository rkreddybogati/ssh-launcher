package com.scalr.launcher;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.scalr.SSHConfiguration;
import com.scalr.exception.EnvironmentSetupException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MacOSLauncher extends UnixLauncher {
    private NSDictionary getBaseTerminalConfiguration () {
        return AccessController.doPrivileged(new PrivilegedAction<NSDictionary>() {
            @Override
            public NSDictionary run() {
                String[] pathBits = {System.getProperty("user.home"), "Library",
                                     "Preferences", "com.apple.Terminal.plist"};
                File configFile = new File(StringUtils.join(pathBits, File.separator));

                NSDictionary root;

                try {
                    root = (NSDictionary) PropertyListParser.parse(configFile);
                } catch (Exception e) {
                    //TODO: Fix.
                    e.printStackTrace();
                    return new NSDictionary();  // Default configuration
                }

                String defaultConfiguration = root.objectForKey("Default Window Settings").toString();
                // TODO -> This is unsafe.
                NSDictionary configurations = (NSDictionary) root.objectForKey("Window Settings");

                NSObject ret = configurations.objectForKey(defaultConfiguration);

                if (ret == null) {
                    return new NSDictionary();
                }

                return (NSDictionary) ret;

            }
        });
    }

    @Override
    public void setUpEnvironment(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException {
        String sshCommandLine = getSSHCommandLine(sshConfiguration);

        final NSDictionary root = getBaseTerminalConfiguration();
        root.put("CommandString", sshCommandLine);
        root.put("RunCommandAsShell", true);
        root.put("name", "Scalr SSH");
        root.put("shellExitAction", 0);
        root.put("type", "Window Settings");

        //TODO -> Test if a key is already present

        commandFile = getTemporaryFile("ssh-command", ".terminal");
        PropertyListParser.saveAsXML(root, commandFile);
    }

    @Override
    protected String[] getSSHCommandFromPath(String path) {
        //return new String[] {"/usr/bin/open", "--fresh", "--new", "-b", "com.apple.terminal", path};
        return new String[] {"/usr/bin/open", path};
    }
}
