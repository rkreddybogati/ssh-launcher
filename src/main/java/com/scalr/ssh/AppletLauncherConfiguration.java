package com.scalr.ssh;

import java.applet.Applet;

public class AppletLauncherConfiguration implements LauncherConfigurationInterface {

    Applet applet;

    public AppletLauncherConfiguration (Applet applet) {
        this.applet = applet;
    }

    @Override
    public String getOption(String optionName) {
        return applet.getParameter(optionName);
    }
}
