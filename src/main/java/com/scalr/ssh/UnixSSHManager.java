package com.scalr.ssh;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.fs.FileSystemManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

public class UnixSSHManager extends BaseSSHManager {
    public UnixSSHManager(SSHConfiguration sshConfiguration) {
        super(sshConfiguration);
    }

    private String getSSHPrivateKeyFilePath() {
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
            //TODO -> Refactor  this mess.
            final String sshFilePath = getSSHPrivateKeyFilePath();

            File sshFile = AccessController.doPrivileged(new PrivilegedAction<File>() {
                @Override
                public File run() {
                    File sshFile = new File(sshFilePath);

                    if (sshFile.exists()) {
                        // The key file names are derived from their contents, if the file is there,
                        // if must be correct.
                        return sshFile;
                    }

                    //
                    try {
                        if (!sshFile.createNewFile()) {
                            System.out.println("Failed to create SSH key file.");
                            return null;
                        }
                    } catch (IOException e) {
                        System.out.println("Error creating SSH Key file.");
                        return null;
                    }
                    if (!sshFile.setWritable(true, true)) {
                        System.out.println("Failed to set SSH key file to writeable by owner.");
                        return null;
                    }
                    if (!sshFile.setReadable(true, true)) {
                        System.out.println("Failed to set SSH key file to readable by owner.");
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
                throw new EnvironmentSetupException("Error writing private key to the filesystem.");
            }
        }
    }

    @Override
    public String getSSHCommandLine() {
        //TODO: Get me that SSH PK path
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();

        sshCommandLineBits.add("ssh");

        if (sshConfiguration.getPort() != null) {
            sshCommandLineBits.add("-p");
            sshCommandLineBits.add(sshConfiguration.getPort().toString());
        }

        if (sshConfiguration.getPrivateKey() != null) {
            sshCommandLineBits.add("-i");
            sshCommandLineBits.add(getSSHPrivateKeyFilePath());
        }

        String[] destinationBits = {sshConfiguration.getUsername(), "@", sshConfiguration.getHost()};
        String   destination = StringUtils.join(destinationBits, "");
        sshCommandLineBits.add(destination);

        return StringUtils.join(sshCommandLineBits, " ");
    }
}
