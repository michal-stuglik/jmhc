/*
 * ExtractorFrame.java
 *
 * Created on 2010-04-14, 22:14:11
 */
package jmhc.forms;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmhc.processing.FastFileFilter;
import jmhc.objects.DBSettings;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ListModel;
import jmhc.processing.DBExtractor;
import jmhc.processing.SeqExtractor;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class ExtractorFrame extends javax.swing.JInternalFrame {

    // declare my variable at the top of my Java class
    private Preferences prefs;
    private ProgramControler mProgramControler;
    private long startTime = 0;
    private long stopTime = 0;
    private SeqExtractor mSeqExtractor = null;
    private DBExtractor mDBExtractor = null;

    /** Creates new form ExtractorFrame */
    public ExtractorFrame(JLabel jLabel_statusLabel, ProgramControler mProgramControler) {
        initComponents();

        this.jLabel_statusLabel = jLabel_statusLabel;
        this.mProgramControler = mProgramControler;

        // create a Preferences instance (somewhere later in the code)
        prefs = Preferences.userNodeForPackage(this.getClass());
    }
    private JLabel jLabel_statusLabel;

    public JLabel getJLabel_statusLabel() {
        return jLabel_statusLabel;
    }

    public void ClickedButton() {

        try {

            if (jButton_Start.getText().equals("start")) {

                //checkpoint: check if there are any data inside|:
                mProgramControler.getDBSettings().check_ExistingData(DBSettings.extr_tableName, this);

                SetsForProcessing(true, jButton_Start);

                //extracting sequences:
                this.ExtractSequences();

            } else if (jButton_Start.getText().equals("stop")) {
                jButton_Start.setEnabled(Boolean.FALSE);
                mSeqExtractor.interrupt();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
            SetsForProcessing(false, jButton_Start);
        } finally {
        }
    }

    public void SetsForProcessing(boolean set, JButton buttonStart) {
        if (set) {
            buttonStart.setText("stop");
            buttonStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jmhc/forms/resources/stop16.gif")));
            jButton_close.setEnabled(false);

            stopTime = 0;
            startTime = 0;

            startTime = System.currentTimeMillis();
        } else {
            buttonStart.setEnabled(!set);
            buttonStart.setText("start");
            buttonStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jmhc/forms/resources/play16.gif")));
            jButton_close.setEnabled(true);
            stopTime = System.currentTimeMillis();

            //statusbar clear
            SeqUtils.SetStatus(jLabel_statusLabel, "                                                                      ");
            SetElapsedTime();
        }
    }

    private void SetElapsedTime() {
        long elapsedTimeMillis = stopTime - startTime;
        if (elapsedTimeMillis > 0) {

//            float elapsedTimeSec = elapsedTimeMillis / 1000F;
//            float elapsedTimeMin = elapsedTimeMillis / (60 * 1000F);
//            float elapsedTimeHour = elapsedTimeMillis / (60 * 60 * 1000F);

            long secondInMillis = 1000;
            long minuteInMillis = secondInMillis * 60;
            long hourInMillis = minuteInMillis * 60;
            long dayInMillis = hourInMillis * 24;
            long yearInMillis = dayInMillis * 365;

            long diff = stopTime - startTime;
            long elapsedYears = diff / yearInMillis;
            diff = diff % yearInMillis;
            long elapsedDays = diff / dayInMillis;
            diff = diff % dayInMillis;
            long elapsedHours = diff / hourInMillis;
            diff = diff % hourInMillis;
            long elapsedMinutes = diff / minuteInMillis;
            diff = diff % minuteInMillis;
            long elapsedSeconds = diff / secondInMillis;

            SeqUtils.SetStatus(jLabel_statusLabel, "Elapsed time [hh:mm:ss] " + PrepareTime(elapsedHours) + ":" + PrepareTime(elapsedMinutes) + ":" + PrepareTime(elapsedSeconds));
        }
        stopTime = 0;
        startTime = 0;
    }

    private String PrepareTime(long l) {
        String sl = String.valueOf(l);
        if (sl.length() == 1) {
            sl = "0" + sl;
        }
        return sl;
    }

    private void ExtractSequences() throws Exception {
        try {

            ListModel model = jList1_files.getModel();
            List<String> fileList = new ArrayList<String>();

            for (int i = 0; i < model.getSize(); i++) {
                String filePath = (String) model.getElementAt(i);
                fileList.add(filePath);
            }

            if (fileList.isEmpty()) {
                throw new Exception("No files selected!");
            }

            if (this.mProgramControler.getDBSettings() == null || this.mProgramControler.getDBSettings().getMSQLite() == null) {
                throw new Exception("Connect to database");
            }

            if (!jCheckBox_F.isSelected() && !jCheckBox_R.isSelected()) {
                throw new Exception("Select search direction");
            }

            int fixedLength = 0;
            if (jCheckBox_fixedLength.isSelected()) {
                try {
                    fixedLength = Integer.parseInt(jTextField_fixedLength.getText());
                    if (fixedLength < 0) {
                        throw new Exception("Sequence length must be > 0");
                    }
                } catch (NumberFormatException e) {
                    throw new Exception(DBSettings.mess_ValueMustBeInteger);
                }
            }


            int primerRlength = jTextField_PrimerR.getText().trim().length();
            int primerFlength = jTextField_PrimerF.getText().trim().length();
            int primerRCutoff = 0;

            if (jCheckBox_cutoff.isSelected() && !jTextField_PrimerRCutoff.getText().trim().isEmpty()) {
                try {
                    primerRCutoff = Integer.parseInt(jTextField_PrimerRCutoff.getText().trim());
                    if (primerRCutoff <= 0 || primerRCutoff >= primerRlength) {
                        throw new Exception("Cutoff must be > 0 and =< primer R length");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            DBSettings.mess_ValueMustBeInteger, this.getName(), JOptionPane.INFORMATION_MESSAGE);
                }
            }

            if (primerFlength == 0) {
                throw new Exception(mProgramControler.DBSettings.exc_pleaseSet + "Primer F");
            }

            if (!jCheckBox_fixedLength.isSelected() && primerRlength == 0) {
                throw new Exception(mProgramControler.DBSettings.exc_pleaseSet + "Primer R");
            }

            int taglength = 0;
            if (!jTextField_TAGLength.getText().trim().isEmpty()) {
                taglength = Integer.parseInt(jTextField_TAGLength.getText().trim());
            }

            mSeqExtractor = new SeqExtractor(mProgramControler, jLabel_statusLabel, this);
            mSeqExtractor.setCutoff(jCheckBox_cutoff.isSelected());
            mSeqExtractor.setPrimerRcutoff(primerRCutoff);
            mSeqExtractor.setPrimerF(jTextField_PrimerF.getText().trim());
            mSeqExtractor.setPrimerR(jTextField_PrimerR.getText().trim());

            mSeqExtractor.setTAGlength(taglength);
            mSeqExtractor.setFileList(fileList);
            mSeqExtractor.setStrain_F(jCheckBox_F.isSelected());
            mSeqExtractor.setStrain_R(jCheckBox_R.isSelected());
            mSeqExtractor.setMustStartWith(jCheckBox_ExtractOnly.isSelected());
            mSeqExtractor.setSeqBeginerMustBe(jTextField_ExtractSeq.getText());

            mSeqExtractor.setOneSideTags(!jCheckBox_2sideTAG.isSelected());
            mSeqExtractor.setFixedSequenceLength(fixedLength);
            mSeqExtractor.setAllowOneMismatchInTag(jCheckBox_allowOneMismatch.isSelected());

            mSeqExtractor.StartProcessing();

        } catch (Exception e) {
            throw e;
        } finally {
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

        jPanel_Main = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1_files = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField_PrimerR = new javax.swing.JTextField();
        jTextField_PrimerF = new javax.swing.JTextField();
        jCheckBox_cutoff = new javax.swing.JCheckBox();
        jTextField_PrimerRCutoff = new javax.swing.JTextField();
        jTextField_fixedLength = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField_TAGLength = new javax.swing.JTextField();
        jCheckBox_fixedLength = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox_2sideTAG = new javax.swing.JCheckBox();
        jCheckBox_allowOneMismatch = new javax.swing.JCheckBox();
        jCheckBox_F = new javax.swing.JCheckBox();
        jCheckBox_R = new javax.swing.JCheckBox();
        jCheckBox_ExtractOnly = new javax.swing.JCheckBox();
        jTextField_ExtractSeq = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jButton_Start = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox_NoTAGs = new javax.swing.JCheckBox();
        jCheckBox_atLeastNumberVariants = new javax.swing.JCheckBox();
        jTextField_variantsNum = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jButtonSaveFile = new javax.swing.JButton();
        jTextField_OutPath = new javax.swing.JTextField();
        jButton_StartOutput = new javax.swing.JButton();
        jButton_close = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jmhc.forms.JMHCApp.class).getContext().getResourceMap(ExtractorFrame.class);
        setForeground(resourceMap.getColor("Import/Extracting.foreground")); // NOI18N
        setIconifiable(true);
        setTitle(resourceMap.getString("Import/Extracting.title")); // NOI18N
        setMaximumSize(null);
        setName("Import/Extracting"); // NOI18N
        setPreferredSize(new java.awt.Dimension(810, 549));

        jPanel_Main.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel_Main.setName("jPanel_Main"); // NOI18N
        jPanel_Main.setOpaque(false);

        jSplitPane2.setDividerSize(2);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(-16777216,true), 1, true), "Import to database", 2, 0));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setRequestFocusEnabled(false);

        jSplitPane1.setDividerSize(2);
        jSplitPane1.setAutoscrolls(true);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setRequestFocusEnabled(false);

        jPanel5.setName("jPanel5"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList1_files.setName("jList1_files"); // NOI18N
        jScrollPane2.setViewportView(jList1_files);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jmhc/forms/resources/open16.gif"))); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButton2))
                .addGap(19, 19, 19)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel5);

        jPanel6.setName("jPanel6"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextField_PrimerR.setText(resourceMap.getString("jTextField_PrimerR.text")); // NOI18N
        jTextField_PrimerR.setName("jTextField_PrimerR"); // NOI18N

        jTextField_PrimerF.setText(resourceMap.getString("jTextField_PrimerF.text")); // NOI18N
        jTextField_PrimerF.setName("jTextField_PrimerF"); // NOI18N

        jCheckBox_cutoff.setText(resourceMap.getString("jCheckBox_cutoff.text")); // NOI18N
        jCheckBox_cutoff.setName("jCheckBox_cutoff"); // NOI18N
        jCheckBox_cutoff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBox_cutoffMouseClicked(evt);
            }
        });

        jTextField_PrimerRCutoff.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField_PrimerRCutoff.setText(resourceMap.getString("jTextField_PrimerRCutoff.text")); // NOI18N
        jTextField_PrimerRCutoff.setEnabled(false);
        jTextField_PrimerRCutoff.setName("jTextField_PrimerRCutoff"); // NOI18N

        jTextField_fixedLength.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField_fixedLength.setText(resourceMap.getString("jTextField_fixedLength.text")); // NOI18N
        jTextField_fixedLength.setEnabled(false);
        jTextField_fixedLength.setName("jTextField_fixedLength"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextField_TAGLength.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField_TAGLength.setText(resourceMap.getString("jTextField_TAGLength.text")); // NOI18N
        jTextField_TAGLength.setName("jTextField_TAGLength"); // NOI18N

        jCheckBox_fixedLength.setText(resourceMap.getString("jCheckBox_fixedLength.text")); // NOI18N
        jCheckBox_fixedLength.setName("jCheckBox_fixedLength"); // NOI18N
        jCheckBox_fixedLength.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox_fixedLengthStateChanged(evt);
            }
        });

        jPanel3.setName("jPanel3"); // NOI18N

        jCheckBox_2sideTAG.setText(resourceMap.getString("jCheckBox_2sideTAG.text")); // NOI18N
        jCheckBox_2sideTAG.setName("jCheckBox_2sideTAG"); // NOI18N
        jCheckBox_2sideTAG.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBox_2sideTAGMouseClicked(evt);
            }
        });

        jCheckBox_allowOneMismatch.setLabel(resourceMap.getString("jCheckBox_allowOneMismatch.label")); // NOI18N
        jCheckBox_allowOneMismatch.setName("jCheckBox_allowOneMismatch"); // NOI18N

        jCheckBox_F.setText(resourceMap.getString("jCheckBox_F.text")); // NOI18N
        jCheckBox_F.setName("jCheckBox_F"); // NOI18N

        jCheckBox_R.setText(resourceMap.getString("jCheckBox_R.text")); // NOI18N
        jCheckBox_R.setName("jCheckBox_R"); // NOI18N
        jCheckBox_R.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBox_RMouseClicked(evt);
            }
        });

        jCheckBox_ExtractOnly.setText(resourceMap.getString("jCheckBox_ExtractOnly.text")); // NOI18N
        jCheckBox_ExtractOnly.setName("jCheckBox_ExtractOnly"); // NOI18N
        jCheckBox_ExtractOnly.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox_ExtractOnlyStateChanged(evt);
            }
        });

        jTextField_ExtractSeq.setText(resourceMap.getString("jTextField_ExtractSeq.text")); // NOI18N
        jTextField_ExtractSeq.setEnabled(false);
        jTextField_ExtractSeq.setName("jTextField_ExtractSeq"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox_2sideTAG)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBox_ExtractOnly)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField_ExtractSeq, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox_allowOneMismatch)
                    .addComponent(jCheckBox_F, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox_R, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox_2sideTAG)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_allowOneMismatch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_F)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_R)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_ExtractOnly)
                    .addComponent(jTextField_ExtractSeq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setName("jPanel7"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(jmhc.forms.JMHCApp.class).getContext().getActionMap(ExtractorFrame.class, this);
        jButton_Start.setAction(actionMap.get("ClickedButton")); // NOI18N
        jButton_Start.setIcon(resourceMap.getIcon("jButton_Start.icon")); // NOI18N
        jButton_Start.setText(resourceMap.getString("jButton_Start.text")); // NOI18N
        jButton_Start.setName("jButton_Start"); // NOI18N
        jButton_Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_StartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jButton_Start)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_Start))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField_PrimerR, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField_PrimerF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox_cutoff)
                            .addComponent(jCheckBox_fixedLength))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField_fixedLength, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                            .addComponent(jTextField_PrimerRCutoff, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_TAGLength, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextField_PrimerRCutoff, jTextField_TAGLength, jTextField_fixedLength});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_PrimerF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jCheckBox_fixedLength)
                    .addComponent(jTextField_fixedLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_PrimerR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jCheckBox_cutoff)
                    .addComponent(jTextField_PrimerRCutoff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField_TAGLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jTextField_PrimerRCutoff, jTextField_TAGLength, jTextField_fixedLength});

        jSplitPane1.setRightComponent(jPanel6);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setLeftComponent(jPanel2);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(-16777216,true), 1, true), resourceMap.getString("jPanel1.border.title"), 2, 0)); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jCheckBox_NoTAGs.setText(resourceMap.getString("jCheckBox_NoTAGs.text")); // NOI18N
        jCheckBox_NoTAGs.setName("jCheckBox_NoTAGs"); // NOI18N

        jCheckBox_atLeastNumberVariants.setText(resourceMap.getString("jCheckBox_atLeastNumberVariants.text")); // NOI18N
        jCheckBox_atLeastNumberVariants.setName("jCheckBox_atLeastNumberVariants"); // NOI18N
        jCheckBox_atLeastNumberVariants.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBox_atLeastNumberVariantsMouseClicked(evt);
            }
        });

        jTextField_variantsNum.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField_variantsNum.setEnabled(false);
        jTextField_variantsNum.setName("jTextField_variantsNum"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox_NoTAGs)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jCheckBox_atLeastNumberVariants)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_variantsNum, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel7)))
                .addContainerGap(413, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_NoTAGs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_atLeastNumberVariants)
                    .addComponent(jTextField_variantsNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap())
        );

        jPanel8.setName("jPanel8"); // NOI18N

        jButtonSaveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jmhc/forms/resources/save16.gif"))); // NOI18N
        jButtonSaveFile.setText(resourceMap.getString("jButtonSaveFile.text")); // NOI18N
        jButtonSaveFile.setName("jButtonSaveFile"); // NOI18N
        jButtonSaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveFileActionPerformed(evt);
            }
        });

        jTextField_OutPath.setBackground(resourceMap.getColor("jTextField_OutPath.background")); // NOI18N
        jTextField_OutPath.setEditable(false);
        jTextField_OutPath.setText(resourceMap.getString("jTextField_OutPath.text")); // NOI18N
        jTextField_OutPath.setName("jTextField_OutPath"); // NOI18N

        jButton_StartOutput.setAction(actionMap.get("ClickedButton")); // NOI18N
        jButton_StartOutput.setIcon(resourceMap.getIcon("jButton_StartOutput.icon")); // NOI18N
        jButton_StartOutput.setText(resourceMap.getString("jButton_StartOutput.text")); // NOI18N
        jButton_StartOutput.setName("jButton_StartOutput"); // NOI18N
        jButton_StartOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_StartOutputActionPerformed(evt);
            }
        });

        jButton_close.setIcon(resourceMap.getIcon("jButton_close.icon")); // NOI18N
        jButton_close.setText(resourceMap.getString("jButton_close.text")); // NOI18N
        jButton_close.setMaximumSize(new java.awt.Dimension(81, 25));
        jButton_close.setMinimumSize(new java.awt.Dimension(81, 25));
        jButton_close.setName("jButton_close"); // NOI18N
        jButton_close.setPreferredSize(new java.awt.Dimension(81, 25));
        jButton_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_closeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButtonSaveFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_OutPath, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButton_StartOutput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 585, Short.MAX_VALUE)
                        .addComponent(jButton_close, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSaveFile)
                    .addComponent(jTextField_OutPath, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jButton_StartOutput))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jButton_close, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel4, jPanel8});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel1);
        jPanel1.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel1.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout jPanel_MainLayout = new javax.swing.GroupLayout(jPanel_Main);
        jPanel_Main.setLayout(jPanel_MainLayout);
        jPanel_MainLayout.setHorizontalGroup(
            jPanel_MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel_MainLayout.setVerticalGroup(
            jPanel_MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_Main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_Main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        DefaultListModel listModel = null;
        JFileChooser chooser = null;

        try {

            listModel = new DefaultListModel();

            chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileFilter(new FastFileFilter());
            String lastDir = prefs.get("LAST_OUTPUT_DIR", null);
            if (lastDir != null && !lastDir.isEmpty()) {
                chooser.setCurrentDirectory(new File(lastDir));
            }

            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();

                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    listModel.addElement(file.getAbsolutePath());
                    prefs.put("LAST_OUTPUT_DIR", file.getAbsolutePath());
                }
                jList1_files.setModel(listModel);
            }

        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox_ExtractOnlyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox_ExtractOnlyStateChanged
        jTextField_ExtractSeq.setEnabled(jCheckBox_ExtractOnly.isSelected());
}//GEN-LAST:event_jCheckBox_ExtractOnlyStateChanged

    private void jButton_StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_StartActionPerformed
        try {
            if (mProgramControler.DBSettings.getMSQLite() == null) {
                throw new Exception(mProgramControler.DBSettings.exc_connectToDataBase);
            }
            this.ClickedButton();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton_StartActionPerformed

    private void jButtonSaveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveFileActionPerformed
        try {
            //String filename = File.separator + "tmp";
            JFileChooser fc = new JFileChooser();
            String lastDir = prefs.get("LAST_OUTPUT_DIR", null);
            if (lastDir != null && !lastDir.isEmpty()) {
                fc.setCurrentDirectory(new File(lastDir));
            }

            int sd = fc.showSaveDialog(this);
            String fullPath = "";
            if (sd == 0) {
                File selFile = fc.getSelectedFile();
                if (!selFile.getName().endsWith(DBSettings.outputExtesion)) {
                    fullPath = selFile.getAbsolutePath() + DBSettings.outputExtesion;
                } else {
                    fullPath = selFile.getAbsolutePath();
                }

                File f = new File(fullPath);

                this.jTextField_OutPath.setText(fullPath);
                prefs.put("LAST_OUTPUT_DIR", f.getParent());
            } else {
                this.jTextField_OutPath.setText("");
            }
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSaveFileActionPerformed

    private void jCheckBox_2sideTAGMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox_2sideTAGMouseClicked

        if (jCheckBox_2sideTAG.isSelected()) {
            jCheckBox_fixedLength.setSelected(!jCheckBox_2sideTAG.isSelected());
            jCheckBox_cutoff.setSelected(!jCheckBox_2sideTAG.isSelected());

            jCheckBox_fixedLengthStateChanged(null);
            jCheckBox_cutoffMouseClicked(null);
        }
    }//GEN-LAST:event_jCheckBox_2sideTAGMouseClicked

    private void jCheckBox_RMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox_RMouseClicked
        if (jCheckBox_R.isSelected()) {
            jCheckBox_fixedLength.setSelected(Boolean.FALSE);
        }
    }//GEN-LAST:event_jCheckBox_RMouseClicked

    private void jCheckBox_cutoffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox_cutoffMouseClicked
        if (jCheckBox_cutoff.isSelected()) {
            jCheckBox_2sideTAG.setSelected(!jCheckBox_cutoff.isSelected());
            jCheckBox_2sideTAGMouseClicked(null);
        }
        jTextField_PrimerRCutoff.setEnabled(jCheckBox_cutoff.isSelected());
    }//GEN-LAST:event_jCheckBox_cutoffMouseClicked

    private void jCheckBox_atLeastNumberVariantsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox_atLeastNumberVariantsMouseClicked
        jTextField_variantsNum.setEnabled(jCheckBox_atLeastNumberVariants.isSelected());
    }//GEN-LAST:event_jCheckBox_atLeastNumberVariantsMouseClicked

    private void jButton_StartOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_StartOutputActionPerformed
        try {

            if (mProgramControler.DBSettings.getMSQLite() == null) {
                throw new Exception(mProgramControler.DBSettings.exc_connectToDataBase);
            }

            if (jButton_StartOutput.getText().equals("start")) {

                SetsForProcessing(true, jButton_StartOutput);

                //extracting sequences:
                this.GenerateOutPut();

            } else if (jButton_StartOutput.getText().equals("stop")) {
                jButton_StartOutput.setEnabled(Boolean.FALSE);
                mDBExtractor.interrupt();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getName(), JOptionPane.ERROR_MESSAGE);
            SetsForProcessing(false, jButton_StartOutput);
        } finally {
        }
    }//GEN-LAST:event_jButton_StartOutputActionPerformed

    private void jCheckBox_fixedLengthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox_fixedLengthStateChanged
        if (jCheckBox_fixedLength.isSelected()) {
            jCheckBox_2sideTAG.setSelected(!jCheckBox_fixedLength.isSelected());
            jCheckBox_2sideTAGMouseClicked(null);
        }
        jTextField_fixedLength.setEnabled(jCheckBox_fixedLength.isSelected());

    }//GEN-LAST:event_jCheckBox_fixedLengthStateChanged

    private void jButton_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_closeActionPerformed

        this.setVisible(false);     }//GEN-LAST:event_jButton_closeActionPerformed

    private void GenerateOutPut() throws Exception {
        try {

            if (jTextField_OutPath.getText().equals("")) {
                throw new Exception("Select output file!");
            }

            int variantsNumber = 0;
            if (jCheckBox_atLeastNumberVariants.isSelected()) {
                try {
                    variantsNumber = Integer.parseInt(jTextField_variantsNum.getText());
                    if (variantsNumber < 0) {
                        throw new Exception("Variants number length must be > 0");
                    }
                } catch (NumberFormatException e) {
                    throw new Exception(DBSettings.mess_ValueMustBeInteger);
                }
            }


            //db indexing, producing output (db extracting)
            mDBExtractor = new DBExtractor(this.mProgramControler, jLabel_statusLabel, this);
            mDBExtractor.setOutPutFile(jTextField_OutPath.getText());
            mDBExtractor.SetAllesUpdate(true);//TODO: check this property
            mDBExtractor.setGenerateNo_TAGS(!jCheckBox_NoTAGs.isSelected());
            mDBExtractor.setVariantsNumber(variantsNumber);
            

            mDBExtractor.StartProcessing();

        } catch (Exception e) {
            throw e;
        } finally {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonSaveFile;
    public javax.swing.JButton jButton_Start;
    public javax.swing.JButton jButton_StartOutput;
    public javax.swing.JButton jButton_close;
    private javax.swing.JCheckBox jCheckBox_2sideTAG;
    private javax.swing.JCheckBox jCheckBox_ExtractOnly;
    private javax.swing.JCheckBox jCheckBox_F;
    private javax.swing.JCheckBox jCheckBox_NoTAGs;
    private javax.swing.JCheckBox jCheckBox_R;
    private javax.swing.JCheckBox jCheckBox_allowOneMismatch;
    private javax.swing.JCheckBox jCheckBox_atLeastNumberVariants;
    private javax.swing.JCheckBox jCheckBox_cutoff;
    private javax.swing.JCheckBox jCheckBox_fixedLength;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList1_files;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel_Main;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTextField jTextField_ExtractSeq;
    private javax.swing.JTextField jTextField_OutPath;
    private javax.swing.JTextField jTextField_PrimerF;
    private javax.swing.JTextField jTextField_PrimerR;
    private javax.swing.JTextField jTextField_PrimerRCutoff;
    private javax.swing.JTextField jTextField_TAGLength;
    private javax.swing.JTextField jTextField_fixedLength;
    private javax.swing.JTextField jTextField_variantsNum;
    // End of variables declaration//GEN-END:variables
}
