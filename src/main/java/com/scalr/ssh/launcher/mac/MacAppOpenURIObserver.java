package com.scalr.ssh.launcher.mac;

import com.apple.eawt.AppEvent;
import com.apple.eawt.OpenURIHandler;
import com.scalr.ssh.launcher.SSHLauncher;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.logging.Loggable;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacAppOpenURIObserver extends Loggable implements OpenURIHandler {
    private final MacAppController appController;

    public MacAppOpenURIObserver (MacAppController appController) {
        this.appController = appController;
    }

    @Override
    public void openURI(AppEvent.OpenURIEvent openURIEvent) {
        URI uri = openURIEvent.getURI();
        UriLauncherConfiguration launcherConfiguration = new UriLauncherConfiguration(uri);
        appController.launchSshSession(launcherConfiguration);
    }

}
