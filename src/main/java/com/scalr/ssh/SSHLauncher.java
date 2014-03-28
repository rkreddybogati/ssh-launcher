package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
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

    private static SSHLauncherInterface getSSHLauncher(SSHConfiguration sshConfiguration) throws InvalidEnvironmentException {
        String osName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name").toLowerCase();
            }
        });

        System.out.println("Platform detected: " + osName);

        if (osName.contains("win")) {
            return new WindowsSSHLauncher(sshConfiguration);

        } else if (osName.contains("mac")) {
            return new MacSSHLauncher(sshConfiguration);

        } else if (osName.contains("nux") || osName.contains("nix")) {
            return new WindowsSSHLauncher(sshConfiguration);
        }

        throw new InvalidEnvironmentException(String.format("No SSH Launcher for platform: %s", osName));
    }

    public static void launchSSHFromConfiguration(SSHConfiguration sshConfiguration) throws LauncherException {
        SSHLauncherInterface launcher = getSSHLauncher(sshConfiguration);

        String sshCommand[] = launcher.getSSHCommand();

        System.out.println("Launching SSH Session");
        System.out.println(StringUtils.join(sshCommand, " "));

        //ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);

        try {
            Process p = Runtime.getRuntime().exec(sshCommand);
            p.waitFor();
        } catch (IOException e) {
            throw new LauncherException(String.format("Unable to start process: %s", e));
        } catch (InterruptedException e) {
            throw new LauncherException(String.format("Process was interrupted: %s", e));
        }

        //launcher.tearDownEnvironment(); //TODO
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

        SSHConfiguration sshConfiguration = new SSHConfiguration(cmd.getOptionValue("username"),cmd.getOptionValue("host"));
        launchSSHFromConfiguration(sshConfiguration);

        System.out.println("Exiting");
    }
}
