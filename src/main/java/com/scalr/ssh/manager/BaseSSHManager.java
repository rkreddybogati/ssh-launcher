package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidConfigurationException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

abstract public class BaseSSHManager extends Loggable implements SSHManagerInterface {
    protected final SSHConfiguration sshConfiguration;
    protected final FileSystemManager fsManager;

    public BaseSSHManager (SSHConfiguration sshConfiguration, FileSystemManager fsManager) {
        this.sshConfiguration = sshConfiguration;
        this.fsManager = fsManager;
    }

    public BaseSSHManager (SSHConfiguration sshConfiguration) {
        this(sshConfiguration, new FileSystemManager());
    }

    abstract protected String getPrivateKey ();
    abstract protected String getPrivateKeyExtension();

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

    private String getSSHPrivateKeyFilePath() throws IOException, InvalidConfigurationException {
        // We compute the filename based on the SSH key contents.
        // If an existing file is there, we'll be able to safely ignore it.

        String sshKeyName = getSSHKeyName();

        // Add the extension and path to the key
        File retFile = new File(fsManager.getUserHome());
        String[] pathBits = {".ssh", "scalr-ssh-keys", String.format("%s.%s", sshKeyName, getPrivateKeyExtension())};

        // TODO - Path join method
        for (String pathBit : pathBits) {
            retFile = new File(retFile, pathBit);
        }

        return retFile.getCanonicalPath();
    }

    @Override
    public void setUpSSHEnvironment() throws LauncherException {
        if (sshConfiguration.useKeyAuth()) {
            final String keyPath;

            // Figure out where the key is supposed to go.
            try {
                keyPath = getSSHPrivateKeyFilePath();
            } catch (IOException e) {
                throw new EnvironmentSetupException("Unable to resolve path to SSH key");
            }

            // Check if the file happens to already be there. We don't want to override the
            // key, in order to maximize user flexibility.
            getLogger().info(String.format("Looking up a local key in SSH Key File '%s'", keyPath));

            Boolean sshFileExists = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return new File(keyPath).exists();
                }
            });

            if (sshFileExists) {
                getLogger().info(String.format("SSH Key File '%s' already exists. Not replacing.", keyPath));
                return;
            }

            // Check we have a private key to write before opening the file
            // We don't want to create an empty file.
            String privateKey = getAndCheckPrivateKey();

            // TODO - Refactor not to ov the key if it exists!
            File sshFile = AccessController.doPrivileged(new PrivilegedAction<File>() {
                @Override
                public File run() {
                    File sshFile = new File(keyPath);

                    if (!sshFile.getParentFile().exists() && !sshFile.getParentFile().mkdirs()) {
                        getLogger().severe(String.format("Failed to create directory tree for SSH File '%s'",
                                keyPath));
                        return null;
                    }

                    try {
                        getLogger().finer(String.format("Creating new SSH file: '%s'", keyPath));
                        if (!sshFile.createNewFile()) {
                            getLogger().severe(String.format("Failed to create SSH key file: %s", keyPath));
                            return null;
                        }
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, String.format("Error creating SSH File '%s'", keyPath), e);
                        return null;
                    }

                    if (!sshFile.setWritable(false, false) || !sshFile.setWritable(true, true)) {
                        getLogger().warning(String.format("Failed to make SSH File '%s' writable", keyPath));
                    }

                    if (!sshFile.setReadable(false, false) || !sshFile.setReadable(true, true)) {
                        getLogger().warning(String.format("Failed to make SSH File '%s' writable", keyPath));
                    }

                    return sshFile;
                }
            });

            if (sshFile == null) {
                throw new EnvironmentSetupException("Error creating SSH Key File");
            }

            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(sshFile), "UTF-8");
                BufferedWriter output = new BufferedWriter(writer);
                output.write(privateKey);
                output.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Error writing private key to SSH File", e);
                throw new EnvironmentSetupException("Error writing private key to the filesystem.");
            }
        }
    }

    private String getDestination() {
        ArrayList<String> destinationBits = new ArrayList<String>();
        if (sshConfiguration.getUsername() != null) {
            destinationBits.add(sshConfiguration.getUsername());
            destinationBits.add("@");
        }
        destinationBits.add(sshConfiguration.getHost());
        return StringUtils.join(destinationBits, "");
    }

    abstract protected String   getExecutablePath () throws InvalidEnvironmentException;
    abstract protected String[] getExecutableExtraOptions ();
    abstract protected String   getPortOption ();
    abstract protected String   getPrivateKeyOption ();

    @Override
    public String[] getSSHCommandLineBits() throws LauncherException {
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add(getExecutablePath());
        Collections.addAll(sshCommandLineBits, getExecutableExtraOptions());

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add(getPortOption());
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.useKeyAuth()) {
            sshCommandLineBits.add(getPrivateKeyOption());
            try {
                sshCommandLineBits.add(getSSHPrivateKeyFilePath());
            } catch (IOException e) {
                throw new InvalidEnvironmentException("Unable to resolve SSH Key file path");
            }
        }

        sshCommandLineBits.add(getDestination());

        getLogger().info(String.format("SSH Command Line: '%s'", StringUtils.join(sshCommandLineBits, " ")));

        return sshCommandLineBits.toArray(new String[sshCommandLineBits.size()]);
    }
}
