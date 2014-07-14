/*
 *  Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.cms7.launcher;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import com.centerkey.utils.BareBonesBrowserLaunch;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.Tomcat7xInstalledLocalContainer;
import org.codehaus.cargo.container.tomcat.TomcatExistingLocalConfiguration;

public class Launcher extends JPanel implements ActionListener {

    private static final String START_TEXT = "Start Hippo CMS";
    private static final String STOP_TEXT = "Stop Hippo CMS";

    public static final String START_URL = "http://localhost:8080/";

    private static final String STOPPED_MESSAGE = " ";
    private static final String STARTING_MESSAGE = "Starting Hippo CMS, this may take a few minutes";
    private static final String STARTED_MESSAGE = "<html>Hippo CMS is running - <a href='" + START_URL + "'>open instructions</a></html>";
    private static final String STOPPING_MESSAGE = "Stopping Hippo CMS, this may take a few minutes";

    private static final String PORT_IN_USE_MESSAGE = "Cannot start Hippo CMS: port %d is already in use";
    private static final String INCORRECT_JAVA_VERSION_OR_VENDOR = "Cannot start Hippo CMS: you need to have Oracle Java 7 installed";

    private final Tomcat7xInstalledLocalContainer container;
    private final JLabel message;
    private final JButton button;

    private Launcher() {

        final LocalConfiguration config = new TomcatExistingLocalConfiguration(getTomcatDirectory());
        container = new Tomcat7xInstalledLocalContainer(config);
        container.setHome(getTomcatDirectory());
        container.setTimeout(TimeUnit.MILLISECONDS.convert(2L, TimeUnit.MINUTES));
        Map<String, String> systemProperties = new HashMap<String, String>();
        systemProperties.put("log4j.configuration", getLog4jFile());
        container.setSystemProperties(systemProperties);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel pic = new JLabel();
        try {
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("background.png"));
            pic.setIcon(new ImageIcon(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        add(pic, constraints);

        constraints.gridy = 2;
        constraints.gridx = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.insets = new Insets(0, 0, 2, 2);
        button = new JButton(START_TEXT);
        button.addActionListener(this);
        add(button, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 0);
        message = new JLabel(STOPPED_MESSAGE);
        message.setLabelFor(button);
        message.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                BareBonesBrowserLaunch.openURL(START_URL);
            }
        });
        add(message, constraints);

        final LoggingPanel loggingPanel = new LoggingPanel();
        final JFrame loggingDisplayFrame = loggingPanel.createAndShow();
        loggingDisplayFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        final JCheckBox loggingToggle = new JCheckBox("Show server log");
        loggingToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                loggingDisplayFrame.setVisible(loggingToggle.isSelected());
            }
        });

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(0, 2, 2, 0);
        add(loggingToggle, constraints);
    }

    private static String getLog4jFile() {
        return "file:" + getTomcatDirectory() + File.separator + "conf" + File.separator + "log4j.xml";
    }

    private static String getTomcatDirectory() {
        final String tomcatDir = System.getProperty("tomcat.dir");
        if (tomcatDir != null && !tomcatDir.trim().equals("")) {
            return tomcatDir;
        }
        return getLauncherDir() + File.separator + "tomcat";
    }

    static String getLauncherDir() {
        final String classFile = Launcher.class.getResource("Launcher.class").getPath();
        if (classFile.contains(".jar!")) {
            final String jarFile = classFile.substring(0, classFile.indexOf('!'));
            String appDir = new File(jarFile).getParentFile().getPath();
            if (appDir.startsWith("file:/")) {
                appDir = appDir.substring(5);
            } else if (appDir.startsWith("file:\\")) {
                appDir = appDir.substring(6);
            }
            try {
                appDir = URLDecoder.decode(appDir, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return appDir;
        }
        return System.getProperty("user.dir");
    }

    private static String getVersion() {
        final Properties properties = new Properties();
        try {
            properties.load(new FileReader(getLauncherDir() + File.separator + "launcher.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty("version", "??? version ???");
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Hippo CMS " + getVersion());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        final Launcher launcher = new Launcher();
        frame.add(launcher);
        try {
            frame.setIconImage(ImageIO.read(Launcher.class.getResourceAsStream("icon_128.gif")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.pack();
        frame.setVisible(true);
    }

    private static boolean isPortAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignore) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException ignore) {
                }
            }
        }

        return false;
    }

    private static boolean isRequiredJavaVersion() {
        final String javaVersion = System.getProperty("java.version", "");
        final int minorVersion = Integer.parseInt(javaVersion.split("\\.")[1]);
        return minorVersion >= 7;
    }

    private static boolean isRequiredJavaVendor() {
        final String javaVendor = System.getProperty("java.vendor", "");
        return javaVendor.toLowerCase().contains("oracle");
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShow();
            }
        });
    }


    public void actionPerformed(final ActionEvent event) {
        if (container.getState() != State.STARTED) {
            if (!isRequiredJavaVersion()) {
                error(INCORRECT_JAVA_VERSION_OR_VENDOR);
            } else if (!isRequiredJavaVendor()) {
                error(INCORRECT_JAVA_VERSION_OR_VENDOR);
            } else if (!isPortAvailable(8080)) {
                error(String.format(PORT_IN_USE_MESSAGE, 8080));
            } else if (!isPortAvailable(8009)) {
                error(String.format(PORT_IN_USE_MESSAGE, 8009));
            } else if (!isPortAvailable(8005)) {
                error(String.format(PORT_IN_USE_MESSAGE, 8005));
            } else {
                message.setText(STARTING_MESSAGE);
                button.setEnabled(false);
                new StartupTask().execute();
            }
        } else if (container.getState() == State.STARTED) {
            button.setEnabled(false);
            message.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            new ShutdownTask().execute();
            message.setText(STOPPING_MESSAGE);
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private class StartupTask extends SwingWorker<Void, Void> {

        private volatile boolean success;
        private volatile Exception exception;

        @Override
        protected Void doInBackground() throws Exception {
            try {
                container.start();
                success = true;
            } catch (ContainerException e) {
                e.printStackTrace();
                success = false;
                exception = e;
            }
            return null;
        }

        @Override
        protected void done() {
            button.setEnabled(true);
            if (!success) {
                error("Error during startup: " + exception);
            }
            if (container.getState() == State.STARTED) {
                button.setText(STOP_TEXT);
                message.setText(STARTED_MESSAGE);
                message.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                BareBonesBrowserLaunch.openURL(START_URL);
            }
        }
    }

    private class ShutdownTask extends SwingWorker<Void, Void> {

        private volatile boolean success;
        private volatile Exception exception;

        @Override
        protected Void doInBackground() throws Exception {
            try {
                container.stop();
                //loggingTailer.stop();
                success = true;
            } catch (ContainerException e) {
                e.printStackTrace();
                success = false;
                exception = e;
            }
            return null;
        }

        @Override
        protected void done() {
            button.setEnabled(true);
            if (!success) {
                error("Error during shutdown: " + exception);
            }
            if (container.getState() == State.STOPPED) {
                button.setText(START_TEXT);
                message.setText(STOPPED_MESSAGE);
            }
        }
    }
}
