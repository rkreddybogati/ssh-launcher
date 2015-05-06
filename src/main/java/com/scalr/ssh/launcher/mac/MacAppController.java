package com.scalr.ssh.launcher.mac;

import com.apple.eawt.Application;
import com.apple.eawt.QuitStrategy;
import com.scalr.ssh.launcher.SSHLauncher;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.logging.Loggable;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



public class MacAppController extends Loggable {
    private LauncherConfigurationInterface launcherConfiguration;
    private ArrayList<MacAppViewInterface> views;

    public MacAppController () {
        views = new ArrayList<MacAppViewInterface>();
    }

    public void registerView (MacAppViewInterface view) {
        views.add(view);
    }

    public void start () {
        Application.getApplication().disableSuddenTermination();
        Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);

        for (MacAppViewInterface view: views) {
            view.appStarts();
        }
    }

    public void launchSshSession() {
        if (launcherConfiguration == null) {
            getLogger().warning("You must launch at least one SSH session to be able to use this feature.");
            return;
        }

        final SSHLauncher sshLauncher = new SSHLauncher(launcherConfiguration);
        new Thread() {
            public void run() {
                sshLauncher.launch();
            }
        }.start();
    }

    public void launchSshSession (LauncherConfigurationInterface launcherConfiguration) {
        updateAppSettings(launcherConfiguration);
        launchSshSession();
    }


    private void updateAppSettings (LauncherConfigurationInterface launcherConfiguration) {
        // Update to the last configuration
        this.launcherConfiguration = launcherConfiguration;

        // Notify views
        for (MacAppViewInterface view: views) {
            view.appSettingsChanged(this.launcherConfiguration);
        }

        // Update log settings
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
