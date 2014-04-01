package com.scalr.ssh;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.launcher.*;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Logger;

public class SSHLauncherManager {
    private final static Logger logger = Logger.getLogger(SSHLauncher.class.getName());
    private String platformName;

    public SSHLauncherManager (String platformName) {
        this.platformName = platformName.toLowerCase();
    }

    public ArrayList<SSHLauncherInterface> getAvailableSSHLaunchers (SSHConfiguration sshConfiguration) {
        ArrayList<SSHLauncherInterface> availableSSHLaunchers = new ArrayList<SSHLauncherInterface>();

        if (platformName.contains("win")) {
            availableSSHLaunchers.add(new WindowsPuTTYLauncher(sshConfiguration));
            availableSSHLaunchers.add(new WindowsOpenSSHLauncher(sshConfiguration));
        } else if (platformName.contains("mac")) {
            availableSSHLaunchers.add(new MacNativeSSHLauncher(sshConfiguration));
            availableSSHLaunchers.add(new MacSSHLauncher(sshConfiguration));
        } else if (platformName.contains("nux") || platformName.contains("nix")) {
            // None
        } else {
            // None
        }

        return availableSSHLaunchers;
    }

    public ArrayList<SSHLauncherInterface> getOrderedSSHLaunchers (SSHConfiguration sshConfiguration, String preferredLauncher) throws InvalidEnvironmentException {
        ArrayList<SSHLauncherInterface> availableLaunchers = getAvailableSSHLaunchers(sshConfiguration);
        ArrayList<SSHLauncherInterface> orderedLaunchers = new ArrayList<SSHLauncherInterface>();

        if (preferredLauncher != null) {
            // Reorder if required.
            logger.info(String.format("Preferred launcher is: '%s'", preferredLauncher));

            ListIterator<SSHLauncherInterface> iter = availableLaunchers.listIterator();
            int iterIndex;
            SSHLauncherInterface iterLauncher;

            while (iter.hasNext()) {
                iterIndex = iter.nextIndex();
                iterLauncher = iter.next();

                if (iterLauncher.getClass().getCanonicalName().equals(preferredLauncher)) {
                    logger.fine(String.format("Preferred '%s' was found", preferredLauncher));
                    availableLaunchers.remove(iterIndex);
                    orderedLaunchers.add(iterLauncher);
                    break;
                }
            }

            if (orderedLaunchers.isEmpty()) {
                logger.warning(String.format("Preferred launcher '%s' was not found", preferredLauncher));
            }
        }

        orderedLaunchers.addAll(availableLaunchers);
        return orderedLaunchers;
    }
}
