package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.launcher.MacSSHLauncher;
import com.scalr.ssh.launcher.SSHLauncherInterface;
import com.scalr.ssh.launcher.WindowsSSHLauncher;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class SSHLauncher {
    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ssh-launcher", options);
        System.exit(1);
    }

    private static SSHLauncherInterface getSSHLauncher() throws InvalidEnvironmentException {
        String osName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name").toLowerCase();
            }
        });

        System.out.println("Platform detected: " + osName);

        if (osName.contains("win")) {
            return new WindowsSSHLauncher();

        } else if (osName.contains("mac")) {
            return new MacSSHLauncher();

        } else if (osName.contains("nux") || osName.contains("nix")) {
            return new WindowsSSHLauncher();
        }

        throw new InvalidEnvironmentException();
    }

    public static void launchSSHFromConfiguration(SSHConfiguration sshConfiguration) throws IOException, EnvironmentSetupException, InvalidEnvironmentException, InterruptedException {
        SSHLauncherInterface launcher = getSSHLauncher();

        launcher.setUpEnvironment(sshConfiguration);
        String sshCommand[] = launcher.getSSHCommand();

        System.out.println("Launching SSH Session");
        System.out.println(StringUtils.join(sshCommand, " "));

        ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
        Process p = pb.start();
        p.waitFor();

        launcher.tearDownEnvironment();
    }

    public static void main(String args[]) throws IOException, EnvironmentSetupException, InterruptedException, InvalidEnvironmentException {
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

        SSHConfiguration sshConfiguration = new SSHConfiguration(cmd.getOptionValue("username"),cmd.getOptionValue("host"));
        launchSSHFromConfiguration(sshConfiguration);

        System.out.println("Exiting");
    }
}
