package com.scalr.ssh.controller.extension;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileExistsPrivilegedAction;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.filesystem.WritePrivateKeyPrivilegedAction;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.AccessController;

abstract public class BaseKeyAuthControllerExtension extends BaseControllerExtension {
    public BaseKeyAuthControllerExtension(SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        super(sshConfiguration, fsManager);
    }

    private File getPrivateKeyFile() throws InvalidConfigurationException {
        // We compute the filename based on the SSH key contents.
        // If an existing file is there, we'll be able to safely ignore it.
        String sshKeyName = getSSHKeyName();

        // Add the extension and path to the key
        File retFile = new File(fsManager.getUserHome());
        String[] pathBits = {".ssh", "scalr-ssh-keys", String.format("%s.%s", sshKeyName, getPrivateKeyExtension())};

        // Join all of this!
        for (String pathBit : pathBits) {
            retFile = new File(retFile, pathBit);
        }

        return retFile;
    }

    private String getAndCheckPrivateKey () throws InvalidConfigurationException {
        String privateKey = getPrivateKey();
        if (privateKey == null) {
            throw new InvalidConfigurationException("Private Key Auth is enabled, but no private key was provided. " +
                                                    "Disable Private Key Auth, use a local key, or provide one.");
        }
        return privateKey;
    }

    private String getSSHKeyName () throws InvalidConfigurationException {
        // Return the name of the "leaf file"
        String sshKeyName = sshConfiguration.getSSHKeyName();

        // Check we have a key name, or try to default to a hash of the key
        if (sshKeyName == null) {
            sshKeyName = String.format("scalr-key-%s", DigestUtils.sha256Hex(getAndCheckPrivateKey()));
        }

        // Check the key name is not empty
        if (sshKeyName.isEmpty()) {
            throw new InvalidConfigurationException("SSH Key Auth is enabled, but an empty SSH Key Name was " +
                    "provided.");
        }

        // Ensure our path is not relative to avoid creating a security hole.
        if (!Paths.get(sshKeyName).getFileName().equals(Paths.get(sshKeyName))) {
            getLogger().severe(String.format("Invalid SSH Key Path. Actual name: %s, File name: %s", Paths.get(sshKeyName), Paths.get(sshKeyName).getFileName()));
            throw new SecurityException(String.format("SSH Key path can not be relative. Received: %s", sshKeyName));
        }

        return sshKeyName;
    }

    private void createKeyFile(final File keyFile) throws LauncherException {
        // TODO - Fix exception class hierarchy
        // Check we have a private key to write before opening the file
        // We don't want to create an empty file.
        String privateKey = getAndCheckPrivateKey();

        Boolean keyFileCreated = AccessController.doPrivileged(new WritePrivateKeyPrivilegedAction(keyFile, privateKey));
        if (!keyFileCreated) {
            throw new EnvironmentSetupException("Unable to create SSH Key File");
        }
    }

    public void setupEnvironment() throws LauncherException {
        final File keyFile;

        // Figure out where the key is supposed to go.
        keyFile = getPrivateKeyFile();

        // Check if the file happens to already be there. We don't want to override the
        // key, in order to maximize user flexibility.
        getLogger().info(String.format("Looking up a local key in SSH Key File '%s'", keyFile));
        Boolean keyFileExists = AccessController.doPrivileged(new FileExistsPrivilegedAction(keyFile));

        if (keyFileExists) {
            getLogger().info(String.format("SSH Key File '%s' already exists. Not replacing.", keyFile));
        } else {
            createKeyFile(keyFile);
        }
    }

    public String[] getCommandLineOptions() throws InvalidConfigurationException {
        try {
            return new String[] {getPrivateKeyOption(), getPrivateKeyFile().getCanonicalPath()};
        } catch (IOException e) {
            throw new InvalidConfigurationException("Unable to resolve path to private key file");
        }
    }

    abstract protected String getPrivateKeyOption ();
    abstract protected String getPrivateKey ();
    abstract protected String getPrivateKeyExtension();
}
