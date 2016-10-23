package jmhc.processing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmhc.forms.ProgramControler;
import jmhc.forms.ProjectFrame;
import jmhc.objects.DBSettings;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class AllelLoadToDB extends Thread implements Runnable {

    private ProgramControler ProgramControler;
    private JLabel jLabel_statusLabel;
    private JInternalFrame frame;

    public AllelLoadToDB(ProgramControler ProgramControler, JLabel jLabel_statusLabel, JInternalFrame frame) {
        this.ProgramControler = ProgramControler;
        this.jLabel_statusLabel = jLabel_statusLabel;
        this.frame = frame;
    }
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void StartProcessing() {
        start();
    }

    @Override
    public void run() {

        BufferedReader br = null;
        DBSettings mDBSettings = null;
        int insertCounter = 0;

        try {

            //Data base settings
            if (this.ProgramControler.getDBSettings() == null || this.ProgramControler.getDBSettings().getMSQLite() == null) {
                throw new Exception(DBSettings.exc_connectToDataBase);
            } else {
                mDBSettings = this.ProgramControler.getDBSettings();
            }

            //checkpoint:
            mDBSettings.check_ExistingData(DBSettings.allel_tableName, frame);
            if (path == null || path.isEmpty()) {
                throw new Exception("Empty path");
            }


            Pattern mPattern = Pattern.compile("[^a-zA-Z]");

            br = new BufferedReader(new FileReader(path));
            SequenceIterator iter = (SequenceIterator) SeqIOTools.fileToBiojava("fasta", "dna", br);
            Sequence sequence = null;
            while (iter.hasNext()) {
                try {
                    sequence = iter.nextSequence();

                    String name = sequence.getName();
                    String seqAsString = sequence.seqString().toUpperCase();

                    Matcher matcher = mPattern.matcher(seqAsString);
                    seqAsString = matcher.replaceAll("");

                    int length = seqAsString.length();
                    @SuppressWarnings("static-access")
                    String sql = "INSERT INTO " + mDBSettings.allel_tableName + " "
                            + "(" + mDBSettings.allel_header + "," + mDBSettings.allel_sequence + "," + mDBSettings.allel_length + ") "
                            + "VALUES ('" + name + "','" + seqAsString + "'," + length + ")";
                    this.ProgramControler.getDBSettings().getMSQLite().Execute(sql);

                    insertCounter++;
                    if (insertCounter % 100 == 0) {
                        SeqUtils.SetStatus(jLabel_statusLabel, "Loading alleles ..." + insertCounter);
                    }
                } catch (BioException bex) {
                    throw bex;
                } catch (Exception ex) {
                    throw ex;
                }
            }

            //log to info table:
            ProgramControler.getDBSettings().log_InfoTable("alleles import ", "count: " + insertCounter, "");

            /**
            update allel names in extract table if there are any sequences:
            update allels info in extract table based on allel table
             */
            boolean extractDataExists = mDBSettings.check_ExistingData(DBSettings.extr_tableName);
            if (extractDataExists) {
                AllelsTableInfo mAllelsTableInfo = new AllelsTableInfo(ProgramControler, jLabel_statusLabel);
                mAllelsTableInfo.UpdateAllelInfoInExtractTable();
            }

            //allels loaded:
            JOptionPane.showMessageDialog(frame,
                    "loaded: " + insertCounter + " alleles\n" + "alleles names in sequences table updated",
                    frame.getName(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (InterruptedException ex) {
            jLabel_statusLabel.setText(ex.getMessage());
            JOptionPane.showMessageDialog(frame,
                    "Done...", frame.getName(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(frame,
                    ex.getMessage(), frame.getName(), JOptionPane.ERROR_MESSAGE);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
                }
            }
            //this.finished = true;
            ((ProjectFrame) frame).SetsForProcessing(true, "allel");
        }
    }
}
