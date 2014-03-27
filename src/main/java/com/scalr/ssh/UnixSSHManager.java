package com.scalr.ssh;

import com.scalr.exception.EnvironmentSetupException;
import com.scalr.fs.FileSystemManager;
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
        String[] pathBits = {FileSystemManager.getUserHome(), ".ssh", "test.pem"};
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
                    System.out.println("KEY PATH");
                    System.out.println(sshFilePath);

                    File sshFile = new File(sshFilePath);
                    // TODO -> Won't be needed when we get a hashed name.
                    if (sshFile.exists()) {
                        sshFile.delete();
                    }
                    try {
                        if (!sshFile.createNewFile()) {
                            System.out.println("CREATE FAIL - CREATE");
                            return null;
                        }
                    } catch (IOException e) {
                        System.out.println("CREATE FAIL - EXC");
                        return null;
                    }
                    if (!sshFile.setWritable(true, true)) {
                        System.out.println("SET WRITE FAIL");
                        return null;
                    }
                    if (!sshFile.setReadable(true, true)) {
                        System.out.println("SET READ FAIL");
                        return null;
                    }
                    return sshFile;
                }
            });

            if (sshFile == null) {
                // We failed to create the file
                throw new EnvironmentSetupException();
            }

            try {
                BufferedWriter output = new BufferedWriter(new FileWriter(sshFile));
                output.write(sshConfiguration.getPrivateKey());
                output.close();
            } catch (IOException e) {
                throw new EnvironmentSetupException();
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
