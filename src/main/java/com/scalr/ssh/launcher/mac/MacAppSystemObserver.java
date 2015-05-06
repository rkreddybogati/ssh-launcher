package com.scalr.ssh.launcher.mac;

import com.apple.eawt.*;
import com.scalr.ssh.launcher.SSHLauncher;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.logging.Loggable;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacAppSystemObserver extends Loggable implements OpenURIHandler, MacAppViewInterface {
    private final MacAppController appController;

    public MacAppSystemObserver(MacAppController appController) {
        this.appController = appController;
    }

    @Override
    public void openURI(AppEvent.OpenURIEvent openURIEvent) {
        URI uri = openURIEvent.getURI();
        UriLauncherConfiguration launcherConfiguration = new UriLauncherConfiguration(uri);
        appController.launchSshSession(launcherConfiguration);
    }

    @Override
    public void appSettingsChanged(LauncherConfigurationInterface launcherConfiguration) {

    }

    @Override
    public void appStarts() {
        Application.getApplication().setOpenURIHandler(this);
    }
}
