package com.scalr.ssh.logging;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class JTextAreaHandler extends Handler {

    private JTextArea textArea;

    public JTextAreaHandler(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void publish(final LogRecord record) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                StringWriter text = new StringWriter();
                PrintWriter out = new PrintWriter(text);
                out.println(textArea.getText());
                out.printf("[%s]: %s:%s -> %s", record.getLevel(), record.getLoggerName(), record.getSourceMethodName(),
                        record.getMessage());
                textArea.setText(text.toString());
            }

        });
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
