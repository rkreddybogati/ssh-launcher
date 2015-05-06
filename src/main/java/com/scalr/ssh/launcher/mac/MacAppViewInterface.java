package com.scalr.ssh.launcher.mac;

import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;

public interface MacAppViewInterface {
    public void appSettingsChanged (LauncherConfigurationInterface launcherConfiguration);
    public void appStarts ();
}
