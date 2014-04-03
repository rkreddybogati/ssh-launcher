package com.scalr.ssh.launcher.configuration;

import org.apache.commons.cli.CommandLine;

public class CLILauncherConfiguration implements LauncherConfigurationInterface {
    CommandLine cmd;


    public CLILauncherConfiguration (CommandLine cmd) {
        this.cmd = cmd;
    }

    @Override
    public String getOption(String optionName) {
        return cmd.getOptionValue(optionName);
    }
}
