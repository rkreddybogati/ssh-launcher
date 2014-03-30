package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.fs.FileSystemManager;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;

abstract public class BaseSSHManager extends Loggable implements SSHManagerInterface {
    protected SSHConfiguration sshConfiguration;

    public BaseSSHManager (SSHConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }

    protected String getSSHPrivateKeyFilePath() {
        // We compute the filename based on the SSH key contents.
        // If an existing file is there, we'll be able to safely ignore it.

        if (this.sshConfiguration.getPrivateKey() == null) {
            getLogger().finer("No private key was defined");
            return null;
        }

        String[] keyNameBits = {"scalr-key-", DigestUtils.sha256Hex(this.sshConfiguration.getPrivateKey()), ".pem"};
        String keyName = StringUtils.join(keyNameBits, "");

        String[] pathBits = {FileSystemManager.getUserHome(), ".ssh", keyName};
        return StringUtils.join(pathBits, File.separator);
    }

    @Override
    public void setUpSSHEnvironment() throws EnvironmentSetupException {
        if (sshConfiguration.getPrivateKey() != null) {
            //TODO What if .ssh does not exist?
            final String sshFilePath = getSSHPrivateKeyFilePath();

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

                    try {
                        getLogger().finer(String.format("Creating new SSH file: '%s'", sshFilePath));
                        if (!sshFile.createNewFile()) {
                            getLogger().severe("Failed to create SSH key file.");
                            return null;
                        }
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Error creating SSH File", e);
                        return null;
                    }
                    if (!sshFile.setWritable(true, true)) {
                        getLogger().severe(String.format("Failed to make SSH File '%s' writable", sshFilePath));
                        return null;
                    }
                    if (!sshFile.setReadable(true, true)) {
                        getLogger().severe(String.format("Failed to make SSH File '%s' readable", sshFilePath));
                        return null;
                    }

                    return sshFile;
                }
            });

            if (sshFile == null) {
                // We failed to create the file
                throw new EnvironmentSetupException("Error setting up SSH configuration.");
            }

            try {
                BufferedWriter output = new BufferedWriter(new FileWriter(sshFile));
                output.write(sshConfiguration.getPrivateKey());
                output.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Error writing private key to SSH File", e);
                throw new EnvironmentSetupException("Error writing private key to the filesystem.");
            }
        }
    }

    protected String getDestination() {
        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        return StringUtils.join(destinationBits, "");
    }
}
