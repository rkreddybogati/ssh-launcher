package com.scalr.ssh.launcher.mac;

import com.scalr.ssh.launcher.SSHLauncher;
import com.scalr.ssh.logging.JTextAreaHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class AppFrame extends JFrame {
    private final static Logger logger = Logger.getLogger(AppFrame.class.getName());

    private static String getAppName () {
        return "Scalr SSH Launcher";
    }

    private static String getAppVersion () {
        return SSHLauncher.class.getPackage().getImplementationVersion();
    }

    public AppFrame() {
        super(String.format("%s %s", getAppName(), getAppVersion()));
        setSize(800, 600);

        // Close Behavior
        addWindowListener(new WindowCloseListener(this));

        // Log window
        JTextArea textArea  = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Handler textAreaHandler = new JTextAreaHandler(textArea);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(textAreaHandler);

        // Quit Button
        JButton button = new JButton("Quit");
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dispose();
                    }
                });
            }
        });

        // Layout
        setLayout(new BorderLayout());

        Container buttonContainer = new Container();
        buttonContainer.setLayout(new GridLayout(1, 3));
        buttonContainer.setPreferredSize(new Dimension(0, 50));

        buttonContainer.add(new Label());
        buttonContainer.add(button);
        buttonContainer.add(new Label());

        add(jScrollPane, BorderLayout.CENTER);
        //add(buttonContainer, BorderLayout.PAGE_END);

        // Info
        logger.info(String.format("Loaded: %s %s", getAppName(), getAppVersion()));
    }

}
