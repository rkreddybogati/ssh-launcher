package com.scalr.ssh.manager;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.exception.EnvironmentSetupException;
import com.scalr.ssh.fs.FileSystemManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class BaseSSHManager implements SSHManagerInterface {
    private final static Logger logger = Logger.getLogger(BaseSSHManager.class.getName());

    protected SSHConfiguration sshConfiguration;

    public BaseSSHManager (SSHConfiguration sshConfiguration) {
        this.sshConfiguration = sshConfiguration;
    }

    protected String getSSHPrivateKeyFilePath() {
        // We compute the filename based on the SSH key contents.
        // If an existing file is there, we'll be able to safely ignore it.

        if (this.sshConfiguration.getPrivateKey() == null) {
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

            File sshFile = AccessController.doPrivileged(new PrivilegedAction<File>() {
                @Override
                public File run() {
                    File sshFile = new File(sshFilePath);

                    if (sshFile.exists()) {
                        logger.log(Level.FINER, "SSH File '{0}' already exists - aborting", sshFilePath);
                        // The key file names are derived from their contents, if the file is there,
                        // if must be correct.
                        return sshFile;
                    }

                    try {
                        logger.log(Level.FINER, "Creating new SSH File '{0}'", sshFilePath);
                        if (!sshFile.createNewFile()) {
                            System.out.println("Failed to create SSH key file.");
                            return null;
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error creating SSH File", e);
                        return null;
                    }
                    if (!sshFile.setWritable(true, true)) {
                        logger.log(Level.SEVERE, "Failed to make SSH File '{0}' writable", sshFile);
                        return null;
                    }
                    if (!sshFile.setReadable(true, true)) {
                        logger.log(Level.SEVERE, "Failed to make SSH File '{0}' readable", sshFile);
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
                logger.log(Level.SEVERE, "Error writing private key to SSH File", e);
                throw new EnvironmentSetupException("Error writing private key to the filesystem.");
            }
        }
    }

    protected String getDestination() {
        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        return StringUtils.join(destinationBits, "");
    }
}
