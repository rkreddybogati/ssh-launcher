package com.scalr.ssh.provider.mac;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.OpenSSHController;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.controller.SSHController;
import com.scalr.ssh.provider.base.BaseSSHProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacNativeSSHProvider extends BaseSSHProvider {


    public MacNativeSSHProvider(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    private NSDictionary getBaseTerminalConfiguration () {
        final Logger logger = getLogger();
        logger.fine("Querying default Terminal configuration");

        return AccessController.doPrivileged(new PrivilegedAction<NSDictionary>() {
            final String[] pathBits = {fsManager.getUserHome(), "Library", "Preferences",
                                       "com.apple.Terminal.plist"};

            @Override
            public NSDictionary run() {
                File configFile = new File(StringUtils.join(pathBits, File.separator));

                NSDictionary root;

                try {
                    logger.finer(String.format("Parsing Terminal configuration at '%s'", configFile.getPath()));
                    root = (NSDictionary) PropertyListParser.parse(configFile);
                } catch (Exception e) {
                    //TODO: Fix.
                    logger.log(Level.WARNING, "Unable to parse Terminal configuration", e);
                    return new NSDictionary();  // Default configuration
                }

                String defaultConfiguration = root.objectForKey("Default Window Settings").toString();


                try {
                    NSDictionary configurations = (NSDictionary) root.objectForKey("Window Settings");
                    NSObject ret = configurations.objectForKey(defaultConfiguration);

                    if (ret == null) {
                        logger.warning("Unable to find Terminal window configuration");
                        return new NSDictionary();
                    }

                    return (NSDictionary) ret;
                } catch (ClassCastException e) {
                    logger.log(Level.WARNING, "An error occcured parsing Terminal configuration", e);
                    return new NSDictionary();
                }
            }
        });
    }

    @Override
    public String[] getSSHCommand() throws LauncherException {
        SSHController sshController = new OpenSSHController(sshConfiguration);
        sshController.setupEnvironment();

        String[] sshCommandLineBits = sshController.getSSHCommandLineBits();
        String   sshCommandLine = StringUtils.join(sshCommandLineBits, " ");

        final NSDictionary root = getBaseTerminalConfiguration();
        root.put("CommandString", sshCommandLine);
        root.put("RunCommandAsShell", true);
        root.put("name", "Scalr SSH");
        root.put("shellExitAction", 2);  // Do not close the window.
        root.put("type", "Window Settings");

        File commandFile;

        try {
            commandFile = fsManager.getTemporaryFile("ssh-command", ".terminal");
        } catch (IOException e) {
            throw new LauncherException("Unable to provision a temporary file for Terminal command.");
        }

        try {
            PropertyListParser.saveAsXML(root, commandFile);
        } catch (IOException e) {
            throw new LauncherException("Unable to write Terminal command to temporary file.");
        }

        String canonicalPath;
        try {
            canonicalPath = commandFile.getCanonicalPath();
        } catch (IOException e) {
            throw new LauncherException("Terminal command file has no canonical path");
        }

        ProcessBuilder pb = new ProcessBuilder().command("/usr/bin/xattr", "-d", "com.apple.quarantine", canonicalPath);
        try {
            Process p = pb.start();
            Integer ret = p.waitFor();

            if (ret != 0) {
                getLogger().warning(String.format("May have failed to remove Terminal command file '%s' from quarantine: return was '%s'", canonicalPath, ret));
            }
        } catch (IOException e) {
            throw new LauncherException("xattr not found.");
        } catch (InterruptedException e) {
            throw new LauncherException("Interrupted when removing Terminal command file from quarantine.");
        }

        return new String[] {"/usr/bin/open", canonicalPath};
    }
}
