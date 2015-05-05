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

public class OpenURIObserver extends Loggable implements OpenURIHandler {

    @Override
    public void openURI(AppEvent.OpenURIEvent openURIEvent) {
        URI uri = openURIEvent.getURI();
        UriLauncherConfiguration launcherConfiguration = new UriLauncherConfiguration(uri);

        launchSshSession(launcherConfiguration);
        updateAppSettings(launcherConfiguration);
    }

    private void launchSshSession (LauncherConfigurationInterface launcherConfiguration) {
        final SSHLauncher sshLauncher = new SSHLauncher(launcherConfiguration);
        new Thread() {
            public void run() {
                sshLauncher.launch();
            }
        }.start();
    }

    private void updateAppSettings (LauncherConfigurationInterface launcherConfiguration) {

        String requestedLogLevel = launcherConfiguration.getOption(SSHLauncher.logLevelParam);
        Level logLevel;

        //noinspection ConstantConditions
        if (requestedLogLevel != null) {
            try {
                logLevel = Level.parse(requestedLogLevel);
            } catch (IllegalArgumentException e) {
                logLevel = Level.INFO;
                getLogger().warning(String.format("logLevel '%s' could not be parsed - defaulting to INFO", requestedLogLevel));
            }

            getLogger().log(Level.CONFIG, String.format("Setting LogLevel to '%s' (requested '%s')", logLevel.getName(), requestedLogLevel));

            Logger launcherLogger = Logger.getLogger("com.scalr.ssh");
            launcherLogger.setLevel(logLevel);
        }

    }
}
