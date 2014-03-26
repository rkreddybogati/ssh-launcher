package com.scalr;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


public class SSHLauncher {

    public static void main(String args[]) {
        //String[] command = {"/bin/bash", "-c", "ssh", "orozco.fr"};
        System.out.println("Launching SSH Session");

        SSHConfiguration sshConfiguration = new SSHConfiguration("root", "orozco.fr");
        SSHLauncherInterface launcher = new MacOSSSHLauncher();

        try {
            launcher.setUpEnvironment(sshConfiguration);
        } catch (EnvironmentSetupException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String sshCommand[] = launcher.getSSHCommand();
            System.out.println(StringUtils.join(sshCommand, " "));

            ProcessBuilder pb = new ProcessBuilder().inheritIO().command(sshCommand);
            Process p = pb.start();
            p.waitFor();
        } catch (InvalidEnvironmentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        launcher.tearDownEnvironment();

        System.out.println("Exiting");
    }
}
