package jmhc.tools;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import jmhc.forms.ProgramControler;
import jmhc.objects.DBSettings;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.*;

/**
 *
 * @author Michal Stuglik
 */
public class SeqUtils {

    private static List<Character> nonAmbiguity = null;

    public static List<Character> getNonAmbiguity() {
        if (nonAmbiguity == null) {
            nonAmbiguity = new ArrayList<Character>();
            Collections.addAll(nonAmbiguity, 'C', 'G', 'A', 'T');
        }
        return nonAmbiguity;
    }

    public static void setNonAmbiguity(List<Character> nonAmbiguity) {
        SeqUtils.nonAmbiguity = nonAmbiguity;
    }

    public static Boolean hasAmbiquity(SymbolList seq) throws IllegalSymbolException, Exception {
        try {

            String seqstring = seq.seqString().toUpperCase();
            char[] c = seqstring.toCharArray();

            for (int i = 0; i < c.length; i++) {
                char d = c[i];

                if (!getNonAmbiguity().contains(d)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime2() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static List<Sequence> FastaToSequenceList(String fastaFile) throws BioException {

        BufferedReader br = null;
        List<Sequence> list = null;

        try {
            list = new ArrayList<Sequence>();
            br = new BufferedReader(new FileReader(fastaFile));
            SequenceIterator iter = (SequenceIterator) SeqIOTools.fileToBiojava("fasta", "DNA", br);

            while (iter.hasNext()) {
                list.add(iter.nextSequence());
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DBSettings.loggerDataBase).log(Level.WARNING, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(DBSettings.loggerDataBase).log(Level.WARNING, null, ex);
                }
                br = null;
            }
        }
        return list;
    }

    public static List<String> getSeqOrder(String f) throws BioException {
        List<String> order = null;
        BufferedReader br = null;
        Sequence sequence = null;
        try {

            order = new ArrayList<String>();

            br = new BufferedReader(new FileReader(f));
            SequenceIterator iter = (SequenceIterator) SeqIOTools.fileToBiojava("fasta", "DNA", br);

            while (iter.hasNext()) {
                sequence = iter.nextSequence();

                if (sequence != null) {
                    order.add(sequence.getName());
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DBSettings.loggerDataBase).log(Level.WARNING, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(DBSettings.loggerDataBase).log(Level.WARNING, null, ex);
                }
                br = null;
            }
        }
        return order;
    }

    public static void reOrderSeqFastaToFasta(String orinalFile, String newFile) throws BioException {
        List<String> seqList = SeqUtils.getSeqOrder(orinalFile);
        SeqUtils.setNewSeqOrderInFasta(seqList, newFile);
    }

    public static void setNewSeqOrderInFasta(List<String> oryginalOrder, String filePath) throws BioException {
        List<String> changedOrder = null;
        try {
            changedOrder = SeqUtils.getSeqOrder(filePath);
            List<Sequence> seqList = SeqUtils.FastaToSequenceList(filePath);

            if (seqList.isEmpty()) {
                return;
            }

            OutputStream output = null;
            String oderName = filePath.substring(0, filePath.lastIndexOf("_align")) + "_ordered" + DBSettings.fastaExtesion;
            try {
                output = new FileOutputStream(oderName);

                for (int i = 0; i < oryginalOrder.size(); i++) {
                    String seqName = oryginalOrder.get(i);

                    int idx = changedOrder.indexOf(seqName);
                    if (idx == -1) {
                        continue;
                    }
                    try {
                        String seq = seqList.get(idx).seqString().toUpperCase();
                        SeqIOTools.writeFasta(output, seqList.get(idx));
                    } catch (IOException ex) {
                        Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (BioException ex) {
            throw ex;
        } finally {
            changedOrder = null;
        }
    }

    public static String GetNextNumberAsString(int counter, int digits) {
        String prefix = "";
        int counterLength = String.valueOf(counter).length();
        if (digits - counterLength > 0) {
            for (int i = 0; i < (digits - counterLength); i++) {
                prefix += "0";
            }
        }
        return prefix + String.valueOf(counter);
    }

    public static String GetNextFileName(String fName) {

        File fileCheck = new File(fName);

        boolean ex = true;
        int counter = 0;

        String folder = fileCheck.getParent();
        String f = fileCheck.getName();
        String ext = f.substring(f.lastIndexOf("."));

        while (ex) {
            counter++;
            if (fileCheck.exists()) {
                File newName = new File(folder + File.separator + f.substring(0, f.lastIndexOf(ext)) + counter + ext);
                if (!newName.exists()) {
                    fName = newName.getAbsolutePath();
                    break;
                }
            } else {
                break;
            }
        }
        return fName;
    }

    public static void SetStatus(JLabel label, String status) {
        try {
            label.setText(status);
            Rectangle progressRect = label.getBounds();
            progressRect.x = 0;
            progressRect.y = 0;
            label.paintImmediately(progressRect);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.WARNING, null, ex);
        }
    }

    public static Boolean dbIndexService(ProgramControler mProgramControler, String idx_name, String table, String field) throws SQLException, ClassNotFoundException {
        boolean idx_bool = false;
        try {
            String sql = "DROP INDEX IF EXISTS " + idx_name;
            idx_bool = mProgramControler.getDBSettings().getMSQLite().Execute(sql);
            sql = "CREATE INDEX " + idx_name + " on " + table + " ( " + field + " ASC)";
            idx_bool = mProgramControler.getDBSettings().getMSQLite().Execute(sql);

        } catch (SQLException ex) {
            throw ex;
        }
        return idx_bool;
    }

    public static Boolean dbIndexService(ProgramControler mProgramControler, String idx_name, String table, String field1, String field2) throws SQLException, ClassNotFoundException {
        return dbIndexService(mProgramControler, idx_name, table, field1 + "," + field2);
    }
    private static List<String> fileNamesInExport = null;
    public static String fileNameOpt_amplicon = "amplicon";
    public static String fileNameOpt_tag = "tag";
    public static String fileNameOpt_file = "file";
    public static String fileNameOpt_coverage = "coverage";

    public static List<String> getFileNamesInExport() {
        if (fileNamesInExport == null) {
            fileNamesInExport = new ArrayList<String>();
            fileNamesInExport.add("");
            fileNamesInExport.add(fileNameOpt_amplicon);
            fileNamesInExport.add(fileNameOpt_tag);
            fileNamesInExport.add(fileNameOpt_file);
            fileNamesInExport.add(fileNameOpt_coverage);
        }
        return fileNamesInExport;
    }
}
