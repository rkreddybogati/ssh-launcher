package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private String getSSHPrivateKeyFilePath() throws IOException {
        // We compute the filename based on the SSH key contents.
        // If an existing file is there, we'll be able to safely ignore it.

        if (getPrivateKey() == null) {
            getLogger().finer("No private key was defined");
            return null;
        }

        String sshKeyName = sshConfiguration.getSSHKeyName();

        if (sshKeyName == null) {
            // Use a default, auto-generated, key name
            sshKeyName = String.format("scalr-key-%s", DigestUtils.sha256Hex(getPrivateKey()));
        } else {
            // Use the SSH Key Name if provided, but ensure it is not relative, as we do not want to create a
            // security hole.
            if (Paths.get(sshKeyName).getFileName() != Paths.get(sshKeyName)) {
                throw new SecurityException("SSH Key path may not be relative");
            }
        }

        // Add the extension and path to the key

        File retFile = new File(fsManager.getUserHome());
        String[] pathBits = {".ssh", "scalr-ssh-keys", String.format("%s.%s", sshKeyName, getPrivateKeyExtension())};

        for (String pathBit : pathBits) {
            retFile = new File(retFile, pathBit);
        }

        return retFile.getCanonicalPath();
    }

    @Override
    public void setUpSSHEnvironment() throws EnvironmentSetupException {
        if (getPrivateKey() != null) {
            final String sshFilePath;

            try {
                sshFilePath = getSSHPrivateKeyFilePath();
            } catch (IOException e) {
                throw new EnvironmentSetupException("Unable to resolve path to SSH key");
            }

            if (sshFilePath == null ) {
                getLogger().finer("No SSH private key will be written to disk.");
                return;
            }

            File sshFile = AccessController.doPrivileged(new PrivilegedAction<File>() {
                @Override
                public File run() {
                    File sshFile = new File(sshFilePath);

                    if (sshFile.exists()) {
                        getLogger().finer(String.format("SSH file '%s' already exists - not replacing", sshFilePath));
                        // The key file names are derived from their contents, if the file is there,
                        // if must be correct.
                        return sshFile;
                    }

                    if (!sshFile.getParentFile().exists() && !sshFile.getParentFile().mkdirs()) {
                        getLogger().severe(String.format("Failed to create directory tree for SSH File '%s'",
                                sshFilePath));
                        return null;
                    }

                    try {
                        getLogger().finer(String.format("Creating new SSH file: '%s'", sshFilePath));
                        if (!sshFile.createNewFile()) {
                            getLogger().severe(String.format("Failed to create SSH key file: %s", sshFilePath));
                            return null;
                        }
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, String.format("Error creating SSH File '%s'", sshFilePath), e);
                        return null;
                    }

                    if (!sshFile.setWritable(false, false) || !sshFile.setWritable(true, true)) {
                        getLogger().warning(String.format("Failed to make SSH File '%s' writable", sshFilePath));
                    }

                    if (!sshFile.setReadable(false, false) || !sshFile.setReadable(true, true)) {
                        getLogger().warning(String.format("Failed to make SSH File '%s' writable", sshFilePath));
                    }

                    return sshFile;
                }
            });

            if (sshFile == null) {
                throw new EnvironmentSetupException("Error creating SSH Key File");
            }

            try {
                BufferedWriter output = new BufferedWriter(new FileWriter(sshFile));
                output.write(getPrivateKey());
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
    public String[] getSSHCommandLineBits() throws InvalidEnvironmentException {
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add(getExecutablePath());
        Collections.addAll(sshCommandLineBits, getExecutableExtraOptions());

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add(getPortOption());
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (getPrivateKey() != null) {
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
