/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.cms7.launcher;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

public class LoggingPanel extends JPanel {

    private final PlainDocument loggingDocument;
    private final JTextArea loggingTextArea;
    private final JScrollPane loggingScrollPane;

    LoggingPanel() {

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        loggingDocument = new PlainDocument();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(2, 4, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        final JLabel infoMessge = new JLabel("Application server log, most recent line at the top");
        add(infoMessge, constraints);

        loggingTextArea = new JTextArea(loggingDocument);
        loggingTextArea.setEditable(false);
        loggingScrollPane = new JScrollPane(loggingTextArea);
        loggingScrollPane.setPreferredSize(new Dimension(600, 200));
        loggingScrollPane.setAutoscrolls(true);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.CENTER;
        add(loggingScrollPane, constraints);
    }

    private File getCatalinaLogFile() {
        final File tomcatLogDir = new File(getTomcatDirectory() + File.separator + "logs");
        final String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final String catalinaFileName = String.format("catalina.%s.log", currentDate);
        return new File(tomcatLogDir, catalinaFileName);
    }

    private static String getTomcatDirectory() {
        final String tomcatDir = System.getProperty("tomcat.dir");
        if (tomcatDir != null && !tomcatDir.trim().equals("")) {
            return tomcatDir;
        }
        return Launcher.getLauncherDir() + File.separator + "tomcat";
    }

    private void startPolling() {
        final TailerListener listener = new TailerListenerAdapter() {
            @Override
            public void handle(final String line) {
                try {
                    loggingDocument.insertString(0, System.lineSeparator(), null);
                    loggingDocument.insertString(0, line, null);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        final File catalinaLogFile = getCatalinaLogFile();
        final Executor logFilePollingExecutor = Executors.newSingleThreadExecutor();
        logFilePollingExecutor.execute(Tailer.create(catalinaLogFile, listener, 100L));
    }

    JFrame createAndShow() {
        JFrame frame = new JFrame("Server log");
        frame.setLocationRelativeTo(null);
        frame.setSize(600, 200);
        frame.add(this);
        frame.pack();
        frame.setResizable(false);
        startPolling();
        return frame;
    }

}
