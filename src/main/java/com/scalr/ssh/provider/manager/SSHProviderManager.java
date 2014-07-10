package com.scalr.ssh.provider.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.provider.base.SSHProviderInterface;
import com.scalr.ssh.provider.linux.LinuxGnomeTerminalSSHProvider;
import com.scalr.ssh.provider.linux.LinuxXTermSSHProvider;
import com.scalr.ssh.provider.mac.MacAppleScriptSSHProvider;
import com.scalr.ssh.provider.mac.MacNativeSSHProvider;
import com.scalr.ssh.provider.mac.MacSSHProvider;
import com.scalr.ssh.provider.windows.WindowsOpenSSHProvider;
import com.scalr.ssh.provider.windows.WindowsPuTTYProvider;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Logger;

public class SSHProviderManager {
    private final static Logger logger = Logger.getLogger(SSHProviderManager.class.getName());
    private String platformName;

    public SSHProviderManager(String platformName) {
        this.platformName = platformName.toLowerCase();
    }

    public ArrayList<SSHProviderInterface> getAvailableSSHProviders(SSHConfiguration sshConfiguration) {
        ArrayList<SSHProviderInterface> availableSSHProviders = new ArrayList<SSHProviderInterface>();

        if (platformName.contains("win")) {
            availableSSHProviders.add(new WindowsPuTTYProvider(sshConfiguration));
            availableSSHProviders.add(new WindowsOpenSSHProvider(sshConfiguration));
        } else if (platformName.contains("mac")) {
            availableSSHProviders.add(new MacAppleScriptSSHProvider(sshConfiguration));
            availableSSHProviders.add(new MacSSHProvider(sshConfiguration));
            availableSSHProviders.add(new MacNativeSSHProvider(sshConfiguration));
        } else if (platformName.contains("nux") || platformName.contains("nix")) {
            availableSSHProviders.add(new LinuxGnomeTerminalSSHProvider(sshConfiguration));
            availableSSHProviders.add(new LinuxXTermSSHProvider(sshConfiguration));
        } else {
            // None
        }

        return availableSSHProviders;
    }

    public ArrayList<SSHProviderInterface> getOrderedSSHProviders(SSHConfiguration sshConfiguration, String preferredProvider) {
        ArrayList<SSHProviderInterface> availableProviders = getAvailableSSHProviders(sshConfiguration);
        ArrayList<SSHProviderInterface> orderedProviders = new ArrayList<SSHProviderInterface>();

        if (preferredProvider != null) {
            // Reorder if required.
            logger.info(String.format("Preferred provider is: '%s'", preferredProvider));

            ListIterator<SSHProviderInterface> iter = availableProviders.listIterator();
            int iterIndex;
            SSHProviderInterface iterProviders;

            while (iter.hasNext()) {
                iterIndex = iter.nextIndex();
                iterProviders = iter.next();

                if (iterProviders.getClass().getCanonicalName().equals(preferredProvider)) {
                    logger.fine(String.format("Preferred '%s' was found", preferredProvider));
                    availableProviders.remove(iterIndex);
                    orderedProviders.add(iterProviders);
                    break;
                }
            }

            if (orderedProviders.isEmpty()) {
                logger.warning(String.format("Preferred provider '%s' was not found", preferredProvider));
            }
        }

        orderedProviders.addAll(availableProviders);
        return orderedProviders;
    }
}
