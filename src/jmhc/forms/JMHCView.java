/*
 * JMHCView.java
 */
package jmhc.forms;

import java.awt.Desktop;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import jmhc.objects.DBSettings;
import jmhc.objects.OSenum;
import org.apache.commons.lang3.SystemUtils;

/**
 * The application's main frame.
 */
public class JMHCView extends FrameView {

    // declare my variable at the top of my Java class
    private Preferences prefs;
    private ProgramControler ProgramControler;
    //windows:
    private ExtractorFrame mExtractingPanel = null;
    private ProjectFrame mProjectFrame = null;
    private FastaExportAlignFrame mAlignFrame = null;

    public JMHCView(SingleFrameApplication app) {
        super(app);

        initComponents();
        ProgramControler = new ProgramControler();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(null);
        statusProgressBar.setVisible(false);
        jLabel_statusLabel.setText("");

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    statusProgressBar.setVisible(true);
                    statusProgressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    statusProgressBar.setVisible(false);
                    statusProgressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    statusProgressBar.setVisible(true);
                    statusProgressBar.setIndeterminate(false);
                    statusProgressBar.setValue(value);
                }
            }
        });

        // create a Preferences instance (somewhere later in the code)
        prefs = Preferences.userNodeForPackage(this.getClass());

        //logger (program) init:
        this.logger_ProgramInit();
        try {
            this.ProgramControler.setDBSettings(new DBSettings());
        } catch (SQLException ex) {
            Logger.getLogger(JMHCView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void showAboutBox() throws IOException {
        if (aboutBox == null) {
            JFrame mainFrame = JMHCApp.getApplication().getMainFrame();
            aboutBox = new JMHCAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        JMHCApp.getApplication().show(aboutBox);
    }

    private void logger_ProgramInit() {

        Logger logger;
        FileHandler fh;

        try {

            logger = Logger.getLogger(DBSettings.loggerProgram);
            String path = System.getProperty("user.dir");

            //check if path is writeable?
            File fi = new File(path);
            if (!fi.canWrite()) {
                path = System.getProperty("java.io.tmpdir");
            }

            // This block configure the logger with handler and formatter
            fh = new FileHandler(path + SystemUtils.FILE_SEPARATOR + "jMHC.log", false);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
        } catch (IOException e) {
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainDesktopPanel = new javax.swing.JDesktopPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        projectMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        extractorMenu = new javax.swing.JMenu();
        extractingMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        jLabel_statusLabel = new javax.swing.JLabel();
        statusProgressBar = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();

        mainPanel.setPreferredSize(new java.awt.Dimension(600, 450));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jmhc.forms.JMHCApp.class).getContext().getResourceMap(JMHCView.class);
        mainDesktopPanel.setBackground(resourceMap.getColor("mainDesktopPanel.background")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainDesktopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainDesktopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N

        projectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        projectMenuItem.setIcon(resourceMap.getIcon("projectMenuItem.icon")); // NOI18N
        projectMenuItem.setText(resourceMap.getString("projectMenuItem.text")); // NOI18N
        projectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(projectMenuItem);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(jmhc.forms.JMHCApp.class).getContext().getActionMap(JMHCView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        extractorMenu.setText(resourceMap.getString("extractorMenu.text")); // NOI18N

        extractingMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        extractingMenuItem.setIcon(resourceMap.getIcon("extractingMenuItem.icon")); // NOI18N
        extractingMenuItem.setText(resourceMap.getString("extractingMenuItem.text")); // NOI18N
        extractingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractingMenuItemActionPerformed(evt);
            }
        });
        extractorMenu.add(extractingMenuItem);

        exportMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        exportMenuItem.setIcon(resourceMap.getIcon("exportMenuItem.icon")); // NOI18N
        exportMenuItem.setText(resourceMap.getString("exportMenuItem.text")); // NOI18N
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        extractorMenu.add(exportMenuItem);

        menuBar.add(extractorMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        helpMenu.add(jMenuItem1);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setPreferredSize(new java.awt.Dimension(515, 30));

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        jLabel_statusLabel.setFont(resourceMap.getFont("jLabel_statusLabel.font")); // NOI18N
        jLabel_statusLabel.setText(resourceMap.getString("jLabel_statusLabel.text")); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 434, Short.MAX_VALUE)
                .addComponent(statusAnimationLabel)
                .addGap(315, 315, 315))
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 194, Short.MAX_VALUE)
                .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(statusMessageLabel)
                        .addComponent(statusAnimationLabel))
                    .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_statusLabel))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 253, Short.MAX_VALUE)
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void extractingMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_extractingMenuItemActionPerformed
        if (mExtractingPanel == null) {
            mExtractingPanel = new ExtractorFrame(jLabel_statusLabel, this.ProgramControler);
            mExtractingPanel.setTitle(extractingMenuItem.getText());
            mainDesktopPanel.add(mExtractingPanel);
        }
        if (!mExtractingPanel.isVisible()) {
            mExtractingPanel.setVisible(true);
        }
    }//GEN-LAST:event_extractingMenuItemActionPerformed

    private void projectMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectMenuItemActionPerformed
        if (mProjectFrame == null) {
            mProjectFrame = new ProjectFrame(this.ProgramControler, jLabel_statusLabel);
            mProjectFrame.setTitle(projectMenuItem.getText());
            mainDesktopPanel.add(mProjectFrame);
        }
        if (!mProjectFrame.isVisible()) {
            mProjectFrame.setVisible(true);
        }
    }//GEN-LAST:event_projectMenuItemActionPerformed

    private void exportMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        if (mAlignFrame == null) {
            mAlignFrame = new FastaExportAlignFrame(this.ProgramControler, jLabel_statusLabel);
            mAlignFrame.setTitle(exportMenuItem.getText());
            mainDesktopPanel.add(mAlignFrame);
        }
        if (!mAlignFrame.isVisible()) {
            mAlignFrame.setVisible(true);
        }
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void jMenuItem1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        Desktop desktop = null;
        try {

            String fileName = "./doc/jMHCmanual.pdf";
            OSenum currentOS = ProgramControler.getOperatingSystem2();

            if (currentOS == OSenum.Windows) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + fileName);
            } else if (currentOS == OSenum.MacOS) {
                Runtime.getRuntime().exec("open " + fileName);
            } else if (currentOS == OSenum.Linux) {
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();

                    if (desktop != null) {
                        desktop.open(new File(fileName));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error" + e);
        }



    }//GEN-LAST:event_jMenuItem1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JMenuItem exportMenuItem;
    javax.swing.JMenuItem extractingMenuItem;
    javax.swing.JMenu extractorMenu;
    javax.swing.JLabel jLabel_statusLabel;
    javax.swing.JMenuItem jMenuItem1;
    javax.swing.JPanel jPanel1;
    javax.swing.JDesktopPane mainDesktopPanel;
    javax.swing.JPanel mainPanel;
    javax.swing.JMenuBar menuBar;
    javax.swing.JMenuItem projectMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    javax.swing.JPanel statusPanel;
    javax.swing.JProgressBar statusProgressBar;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
