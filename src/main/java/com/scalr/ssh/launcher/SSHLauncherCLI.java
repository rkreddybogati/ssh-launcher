package com.scalr.ssh.launcher;

import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.configuration.CLILauncherConfiguration;
import org.apache.commons.cli.*;

import java.io.IOException;

public class SSHLauncherCLI {

    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ssh-provider", options);
        System.exit(1);
    }

    public static void main(String args[]) throws IOException, LauncherException, InterruptedException {
        Options options = new Options();

        String optName;
        String optDescription;

        for (String[] launcherOpt : SSHLauncher.getParameterInfo()) {
            optName = launcherOpt[0];
            optDescription = launcherOpt[1];
            options.addOption(new Option(optName, true, optDescription));
        }

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp(options);
            System.exit(1);
        }

        SSHLauncher sshLauncher = new SSHLauncher(new CLILauncherConfiguration(cmd));
        boolean hasSucceeded = sshLauncher.launch();

        if (!hasSucceeded) {
           System.out.println("Failed to launch SSH session");
        }

        System.out.println("Exiting");
    }

}
