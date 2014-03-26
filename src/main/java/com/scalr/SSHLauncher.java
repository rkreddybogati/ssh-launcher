package com.scalr;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.exception.InvalidEnvironmentException;
import com.scalr.launcher.MacOSLauncher;
import com.scalr.launcher.SSHLauncherInterface;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


public class SSHLauncher {
    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ssh-launcher", options);
        System.exit(1);
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
        SSHLauncherInterface launcher = new MacOSLauncher();


        launcher.setUpEnvironment(sshConfiguration);
        String sshCommand[] = launcher.getSSHCommand();

        System.out.println("Launching SSH Session");
        System.out.println(StringUtils.join(sshCommand, " "));

        ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
        Process p = pb.start();
        p.waitFor();

        launcher.tearDownEnvironment();

        System.out.println("Exiting");
    }
}
