package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.SSHLauncherInterface;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SSHLauncher {
    private final static Logger logger = Logger.getLogger(SSHLauncher.class.getName());

    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ssh-launcher", options);
        System.exit(1);
    }

    public static void launchSSHFromConfiguration(SSHConfiguration sshConfiguration, String preferredLauncher) throws LauncherException {
        String platformName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name");
            }
        });

        logger.info(String.format("Detected Platform: '%s'", platformName));
        SSHLauncherManager sshLauncherManager = new SSHLauncherManager(platformName);

        ArrayList<SSHLauncherInterface> sshLaunchers = sshLauncherManager.getOrderedSSHLaunchers(sshConfiguration, preferredLauncher);
        if (sshLaunchers.isEmpty()) {
            logger.severe(String.format("No SSH Launcher available for platform '%s'", platformName));
        }

        for (SSHLauncherInterface sshLauncher: sshLaunchers) {
            logger.info(String.format("Creating SSH Session with launcher: '%s'", sshLauncher.getClass().getCanonicalName()));
            try {
                String[] sshCommand = sshLauncher.getSSHCommand();
                logger.info(String.format("Launcher Command Line: '%s'", StringUtils.join(sshCommand, " ")));

                ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
                pb.start();

                logger.info("Assuming SSH session was launched. Exiting.");
                return;
            } catch (LauncherException e) {
                logger.log(Level.WARNING, String.format("Launcher '%s' failed to prepare SSH", sshLauncher.getClass().getCanonicalName()), e);
            } catch (IOException e) {
                logger.log(Level.WARNING, String.format("Launcher '%s' failed to launch SSH", sshLauncher.getClass().getCanonicalName()), e);
            }
        }

        throw new LauncherException("All launchers failed to launch SSH");
    }

    public static void main(String args[]) throws IOException, LauncherException, InterruptedException {
        Options options = new Options();
        options.addOption("u", "username", true, "SSH user to login as");
        options.addOption("h", "host", true, "Host to connect to");
        CommandLineParser parser = new GnuParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp(options);
            System.exit(1);
        }

        String username = cmd.getOptionValue("username");
        String host     = cmd.getOptionValue("host");

        if (username == null || host == null)  {
            printHelp(options);
            System.exit(1);
        }

        SSHConfiguration sshConfiguration = new SSHConfiguration(cmd.getOptionValue("host"));
        sshConfiguration.setUsername(username);
        launchSSHFromConfiguration(sshConfiguration, null);

        System.out.println("Exiting");
    }
}
