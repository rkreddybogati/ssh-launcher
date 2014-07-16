package com.scalr.ssh.controller;

import com.scalr.ssh.configuration.SSHConfiguration;
import com.scalr.ssh.controller.extension.ControllerExtension;
import com.scalr.ssh.controller.extension.shared.DestinationControllerExtension;
import com.scalr.ssh.exception.InvalidEnvironmentException;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.filesystem.FileSystemManager;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;

abstract public class BaseSSHController extends Loggable implements SSHController {
    protected final SSHConfiguration sshConfiguration;
    protected final FileSystemManager fsManager;

    private ControllerExtension[] controllerExtensions;

    public BaseSSHController(final SSHConfiguration sshConfiguration, final FileSystemManager fsManager) {
        this.sshConfiguration = sshConfiguration;
        this.fsManager = fsManager;


        // Create the Controller Extensions for this controller.
        final ArrayList<ControllerExtension> controllerExtensionArrayList = new ArrayList<ControllerExtension>();

        Collections.addAll(controllerExtensionArrayList, getExtraControllerExtensions());
        if (sshConfiguration.useKeyAuth()) {
            controllerExtensionArrayList.add(getKeyAuthControllerExtension());
        }
        if (sshConfiguration.getIgnoreHostKeys()) {
            controllerExtensionArrayList.add(getIgnoreHostKeysControllerExtension());
        }
        controllerExtensionArrayList.add(getPortControllerExtension());
        controllerExtensionArrayList.add(getDestinationControllerExtension());

        // Initialize Controller Extensions as an array
        controllerExtensions = new ControllerExtension[controllerExtensionArrayList.size()];
        controllerExtensionArrayList.toArray(controllerExtensions);
    }

    public BaseSSHController(SSHConfiguration sshConfiguration) {
        this(sshConfiguration, new FileSystemManager());
    }

    // Hooks for Controller Extensions

    abstract protected ControllerExtension getKeyAuthControllerExtension ();
    abstract protected ControllerExtension getPortControllerExtension ();
    abstract protected ControllerExtension getIgnoreHostKeysControllerExtension ();

    protected ControllerExtension[] getExtraControllerExtensions () {
        return new ControllerExtension[0];
    }

    protected ControllerExtension getDestinationControllerExtension () {
        return new DestinationControllerExtension(sshConfiguration, fsManager);
    }

    // Hooks for executable search

    abstract protected File[] getExecutableExtraSearchPaths();
    abstract protected String[] getExecutableSearchNames();

    // Actual logic

    private File[] getExecutableSearchPathsFromEnvironment () {
        // Load the user's path
        String pathEnvVar= AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getenv("PATH");
            }
        });

        if (pathEnvVar == null) {
            pathEnvVar = "";
        }

        ArrayList<File> files = new ArrayList<File>();
        for (String pathElement : pathEnvVar.split(File.pathSeparator)) {
            files.add(new File(pathElement));
        }

        return files.toArray(new File[files.size()]);
    }

    private File[] getExecutableSearchPaths () {
        return ArrayUtils.addAll(getExecutableSearchPathsFromEnvironment(), getExecutableExtraSearchPaths());
    }

    protected File getExecutablePath() throws InvalidEnvironmentException {
        File[] candidateLocations = getExecutableSearchPaths();
        String[] candidateNames = getExecutableSearchNames();

        File sshExecutable;
        for (String candidateName : candidateNames) {
            sshExecutable = fsManager.findInPaths(candidateLocations, candidateName);
            if (sshExecutable != null) {
                return sshExecutable;
            }
        }

        String errorMsg = String.format("Unable to locate: %s", StringUtils.join(candidateNames, ", "));
        throw new InvalidEnvironmentException(errorMsg);
    }

    @Override
    public void setupEnvironment() throws LauncherException {
        for (ControllerExtension extension : controllerExtensions) {
            extension.setupEnvironment();
        }
    }

    @Override
    public String[] getSSHCommandLineBits() throws LauncherException {

        // Find SSH executable
        File sshExecutableFile = getExecutablePath();
        String sshExecutablePath;
        try {
            sshExecutablePath = getExecutablePath().getCanonicalPath();
        } catch (IOException e) {
            throw new InvalidEnvironmentException(String.format("Unable to resolve path to: %s", sshExecutableFile));
        }

        // Create command line
        ArrayList<String> sshCommandLineBits = new ArrayList<String>();
        sshCommandLineBits.add(sshExecutablePath);
        for (ControllerExtension controllerExtension : controllerExtensions) {
            Collections.addAll(sshCommandLineBits, controllerExtension.getCommandLineOptions());
        }

        getLogger().info(String.format("SSH Command Line: '%s'", StringUtils.join(sshCommandLineBits, " ")));
        return sshCommandLineBits.toArray(new String[sshCommandLineBits.size()]);
    }

}
