package com.scalr.ssh.launcher;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenURIHandler;
import com.scalr.ssh.exception.LauncherException;
import com.scalr.ssh.launcher.configuration.CLILauncherConfiguration;
import com.scalr.ssh.launcher.configuration.LauncherConfigurationInterface;
import com.scalr.ssh.launcher.configuration.UriLauncherConfiguration;
import com.scalr.ssh.logging.Loggable;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.*;

public class SSHLauncherCLI extends Loggable {
    private final static Logger logger = Logger.getLogger(SSHLauncherCLI.class.getName());

    class OpenURIObserver implements OpenURIHandler {

        @Override
        public void openURI(AppEvent.OpenURIEvent openURIEvent) {

        }
    }

    private static UriLauncherConfiguration getUriLauncherConfigurationMacOs() {
        logger.info("Trying information from URI");

        final String[] uriArr = new String[1];
        final Object lock = new Object();

        // This should run the Uri handler.
        Application.getApplication().setOpenURIHandler(
                new OpenURIHandler() {
                    @Override
                    public void openURI(final AppEvent.OpenURIEvent pEvent) {
                        uriArr[0] = pEvent.getURI().toString();
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }
        );

        synchronized (lock) {
            try {
                logger.info("LOCK STARTED");
                lock.wait(5000);
                logger.info("LOCK ENDED");
            } catch (InterruptedException ignored) {
                logger.info("LOCK INTERRUPTED");
            }
        }

        String uri = uriArr[0];

        if (uri != null) {
            logger.info(String.format("Received uri: '%s'", uriArr[0]));
            try {
                return new UriLauncherConfiguration(new URI(uri));
            } catch (URISyntaxException e) {
                logger.warning("URI could not be parsed!");
            }
        }

        logger.info("No Mac OS URI received");
        return null;
    }



    private static Options getCommandLineOptions () {
        Options options = new Options();

        String optName;
        String optDescription;

        for (String[] launcherOpt : SSHLauncher.getParameterInfo()) {
            optName = launcherOpt[0];
            optDescription = launcherOpt[1];
            options.addOption(new Option(optName, true, optDescription));
        }

        return options;
    }

    private static CLILauncherConfiguration getCliLauncherConfiguration(String args[]) {
        logger.info("Trying information from Args");

        /// More
        CommandLineParser parser = new GnuParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(getCommandLineOptions(), args);
            return new CLILauncherConfiguration(cmd);
        } catch (ParseException e) {
            logger.warning("Command Line Arguments cannot be parsed");
            return null;
        }
    }

    public static void printHelp () {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ssh-provider", getCommandLineOptions());
        System.exit(1);
    }

    public static void main(String args[]) throws IOException, LauncherException, InterruptedException {
        Logger rootLogger = Logger.getLogger("");

        // TODO - fixme
        FileHandler fh = new FileHandler("/tmp/AppletLog.log");

        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        rootLogger.addHandler(fh);


        // We try the CliLauncherConfiguration first, because it's fast.
        // If that doesn't work, we try UriLauncherConfigurationMacOs, which takes more time to run (because Apple
        // Events are not always delivered immediately)

        LauncherConfigurationInterface launcherConfiguration;
        launcherConfiguration = getCliLauncherConfiguration(args);
        if (launcherConfiguration == null) {
            launcherConfiguration = getUriLauncherConfigurationMacOs();
        }
        if (launcherConfiguration == null) {
            printHelp();
            System.exit(1);
        }

        // Set logging
        // TODO - Share code with applet
        String requestedLogLevel = launcherConfiguration.getOption(SSHLauncher.logLevelParam);
        Level logLevel;

        if (requestedLogLevel != null) {
            try {
                logLevel = Level.parse(requestedLogLevel);
            } catch (IllegalArgumentException e) {
                logLevel = Level.INFO;
            }

            Logger launcherLogger = Logger.getLogger("com.scalr.ssh");
            launcherLogger.setLevel(logLevel);

            Logger root = Logger.getLogger("");
            Handler[] handlers = root.getHandlers();
            for(Handler h: handlers){
                h.setLevel(logLevel);
            }
        }

        // Actually launch
        SSHLauncher sshLauncher = new SSHLauncher(launcherConfiguration);
        boolean hasSucceeded = sshLauncher.launch();

        if (!hasSucceeded) {
           System.out.println("Failed to launch SSH session");
        }

        System.out.println("Exiting");
    }

}
