package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.MacNativeSSHLauncher;
import com.scalr.ssh.launcher.SSHLauncherInterface;
import com.scalr.ssh.launcher.WindowsPuTTYLauncher;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SSHLauncher {
    private final static Logger logger = Logger.getLogger(SSHLauncher.class.getName());

    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ssh-launcher", options);
        System.exit(1);
    }

    private static SSHLauncherInterface getSSHLauncher(SSHConfiguration sshConfiguration) throws InvalidEnvironmentException {
        String osName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name").toLowerCase();
            }
        });

        logger.info(String.format("Detected Platform: '%s'", osName));

        if (osName.contains("win")) {
            return new WindowsPuTTYLauncher(sshConfiguration);

        } else if (osName.contains("mac")) {
            return new MacNativeSSHLauncher(sshConfiguration);

        } else if (osName.contains("nux") || osName.contains("nix")) {
            return new WindowsPuTTYLauncher(sshConfiguration);  // TODO -> This is bad.
        }

        logger.severe(String.format("Platform '%s' is not supported", osName));
        throw new InvalidEnvironmentException(String.format("No SSH Launcher for platform: %s", osName));
    }

    public static void launchSSHFromConfiguration(SSHConfiguration sshConfiguration) throws LauncherException {
        SSHLauncherInterface launcher = getSSHLauncher(sshConfiguration);

        String sshCommand[] = launcher.getSSHCommand();

        logger.info(String.format("Launcher Command Line: '%s'", StringUtils.join(sshCommand, " ")));

        ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);

        try {
            pb.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to create SSH Session", e);
            throw new LauncherException(String.format("Unable to start process: %s", e));
        }

        logger.info("Assuming SSH session was launched. Exiting.");
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
        launchSSHFromConfiguration(sshConfiguration);

        System.out.println("Exiting");
    }
}
