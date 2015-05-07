package com.scalr.ssh.launcher.gui.generic;

import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;

public interface AppViewInterface {
    public void appSettingsChanged (LauncherConfigurationInterface launcherConfiguration);
    public void appStarts ();
    public void appExits ();
}
