package com.scalr.ssh.provider.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.provider.*;

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
            availableSSHProviders.add(new MacNativeSSHProvider(sshConfiguration));
            availableSSHProviders.add(new MacSSHProvider(sshConfiguration));
        } else if (platformName.contains("nux") || platformName.contains("nix")) {
            // None
        } else {
            // None
        }

        return availableSSHProviders;
    }

    public ArrayList<SSHProviderInterface> getOrderedSSHProviders(SSHConfiguration sshConfiguration, String preferredProvider) throws InvalidEnvironmentException {
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
