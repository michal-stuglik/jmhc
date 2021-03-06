/*
 * OpenFileJPanel.java
 *
 * Created on 2010-04-25, 12:08:25
 */
package jmhc.forms.panels;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import jmhc.objects.DBSettings;

/**
 *
 * @author Michal Stuglik
 */
public class OpenFileJPanel extends javax.swing.JPanel {

    /** Creates new form OpenFileJPanel */
    public OpenFileJPanel() {
        initComponents();
    }
    private FileFilter jFileFilter;

    public FileFilter getJFileFilter() {
        if (jFileFilter == null) {
            jFileFilter = new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return true;
                }

                @Override
                public String getDescription() {
                    return "All Files (*.*)";
                }
            };
        }

        return jFileFilter;
    }

    public void setJFileFilter(FileFilter jFileFilter) {
        this.jFileFilter = jFileFilter;
    }
    private String LAST_OUTPUT_DIR;

    public String getLAST_OUTPUT_DIR() {
        return LAST_OUTPUT_DIR;
    }

    public void setLAST_OUTPUT_DIR(String LAST_OUTPUT_DIR) {
        this.LAST_OUTPUT_DIR = LAST_OUTPUT_DIR;
    }
    private String Path = "";

    public String getPath() {
        return Path;
    }
    private int jFileChooserOption = JFileChooser.FILES_ONLY;

    public int getJFileChooserOption() {
        return jFileChooserOption;
    }

    public void setJFileChooserOption(int jFileChooserOption) {
        this.jFileChooserOption = jFileChooserOption;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton = new javax.swing.JButton();
        jTextField_PATH = new javax.swing.JTextField();

        setMaximumSize(new java.awt.Dimension(32767, 23));
        setMinimumSize(new java.awt.Dimension(198, 23));
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jmhc.forms.JMHCApp.class).getContext().getResourceMap(OpenFileJPanel.class);
        jButton.setIcon(resourceMap.getIcon("jButton.icon")); // NOI18N
        jButton.setText(resourceMap.getString("jButton.text")); // NOI18N
        jButton.setName("jButton"); // NOI18N
        jButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonActionPerformed(evt);
            }
        });

        jTextField_PATH.setEditable(false);
        jTextField_PATH.setText(resourceMap.getString("jTextField_PATH.text")); // NOI18N
        jTextField_PATH.setName("jTextField_PATH"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_PATH, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton)
                .addComponent(jTextField_PATH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void OpenAction() {

        JFileChooser chooser = null;
        try {
            chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(this.getJFileChooserOption());
            chooser.setFileFilter(this.getJFileFilter());
            String lastDir = this.LAST_OUTPUT_DIR;
            if (lastDir != null) {
                chooser.setCurrentDirectory(new File(lastDir));
            }

            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.exists()) {
                    throw new Exception("no such file");
                }
                this.jTextField_PATH.setText(file.getAbsolutePath());
            } else {
                jTextField_PATH.setText("");
            }

        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this,
                    ex.toString(), this.getName(), JOptionPane.ERROR_MESSAGE);
        } finally {
            this.Path = jTextField_PATH.getText();
        }
    }

    private void jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonActionPerformed
        this.OpenAction();
    }//GEN-LAST:event_jButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton;
    protected javax.swing.JTextField jTextField_PATH;
    // End of variables declaration//GEN-END:variables
}
