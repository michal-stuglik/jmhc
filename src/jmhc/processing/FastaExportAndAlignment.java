package jmhc.processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmhc.forms.FastaExportAlignFrame;
import jmhc.forms.ProgramControler;
import jmhc.objects.DBSettings;
import jmhc.objects.MuscleEnum;
import jmhc.objects.OSenum;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class FastaExportAndAlignment extends Thread implements Runnable {

    private ProgramControler ProgramControler;
    private JLabel jLabel_statusLabel;
    private static String separator = "D3845A44-035D-49F5-B229-1C2C98CA5197";
    private int minLength;
    private FastaExportAlignFrame parentFrame;
    private int totalLineCounter;
    private OSenum currentOS;

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        if (minLength <= 0) {
            minLength = 0;
        }
        this.minLength = minLength;
    }
    private int maxLength;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        if (maxLength <= 0) {
            maxLength = 0;
        }
        this.maxLength = maxLength;
    }
    private String folderPath;

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    private String muscleFile;

    public String getMuscleFile() {
        return muscleFile;
    }

    public void setMuscleFile(String muscleFile) {
        this.muscleFile = muscleFile;
    }
    private int minNumberOfSeq = 1;

    public void setMinNumberOfSeq(int minNumberOfSeq) {
        this.minNumberOfSeq = minNumberOfSeq;
    }

    public int getMinNumberOfSeq() {
        return minNumberOfSeq;
    }
    private boolean reOrderAlign;

    public boolean isReOrderAlign() {
        return reOrderAlign;
    }

    public void setReOrderAlign(boolean reOrderAlign) {
        this.reOrderAlign = reOrderAlign;
    }
    private int minVarianCount = 1;

    public int getMinVarianCount() {
        return minVarianCount;
    }

    public void setMinVarianCount(int minVarianCount) {
        this.minVarianCount = minVarianCount;
    }
    private List<Object> nameOptions;

    public List<Object> getNameOptions() {
        return nameOptions;
    }

    public void setNameOptions(List<Object> nameOptions) {
        this.nameOptions = nameOptions;
    }

    /** Creates new form FastaExportAlignFrame */
    public FastaExportAndAlignment(ProgramControler mProgramControler, JLabel jLabel_statusLabel, FastaExportAlignFrame parentFrame) {

        this.ProgramControler = mProgramControler;
        this.jLabel_statusLabel = jLabel_statusLabel;
        this.parentFrame = parentFrame;
    }

    private ArrayList<String> Extract_TagFileDiversityFormExtractTable() throws SQLException, ClassNotFoundException {

        ArrayList<String> TagFile = null;
        ResultSet rs = null;
        String sql = "";

        //SELECT tag, file from extract group by tag, file
        sql = "SELECT " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField
                + " from " + DBSettings.extr_tableName
                + " group by " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField;

        try {
            TagFile = new ArrayList<String>();
            rs = ProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
            while (rs.next()) {
                TagFile.add(rs.getString(1) + separator + rs.getString(2));
            }
        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        }
        return TagFile;
    }

    private HashMap<String, String> Extract_TagFileSpeciesDiversityFromTagTable() throws SQLException, ClassNotFoundException {

        HashMap<String, String> map = null;
        ResultSet rs = null;
        String sql = "";

        //SELECT tag, file, species from tags group by tag, file
        sql = "SELECT " + DBSettings.tags_tag + ", " + DBSettings.tags_file + ", " + DBSettings.tags_species
                + " from " + DBSettings.tags_tableName
                + " group by " + DBSettings.tags_tag + ", " + DBSettings.tags_file;

        try {
            map = new HashMap<String, String>();
            rs = ProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
            while (rs.next()) {
                if (!map.containsKey(rs.getString(1) + separator + rs.getString(2))) {
                    map.put(rs.getString(1) + separator + rs.getString(2), rs.getString(3));
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        }
        return map;
    }
    private boolean useTag;

    public boolean isUseTag() {
        return useTag;
    }

    public void setUseTag(boolean useTag) {
        this.useTag = useTag;
    }
    private boolean useFile;

    public boolean isUseFile() {
        return useFile;
    }

    public void setUseFile(boolean useFile) {
        this.useFile = useFile;
    }
    private boolean useSpecies;

    public boolean isUseSpecies() {
        return useSpecies;
    }

    public void setUseSpecies(boolean useSpecies) {
        this.useSpecies = useSpecies;
    }
    private boolean useSeqNumber;

    public boolean isUseSeqNumber() {
        return useSeqNumber;
    }

    public void setUseSeqNumber(boolean useSeqNumber) {
        this.useSeqNumber = useSeqNumber;
    }
    private boolean alignment;

    public boolean isAlignment() {
        return alignment;
    }

    public void setAlignment(boolean alignment) {
        this.alignment = alignment;
    }
    private Dictionary MuscleOpt_dic;
    private String muscle_optionsString = "";

    public void setMuscleOption(Dictionary MuscleOpt_dic) {
        this.MuscleOpt_dic = MuscleOpt_dic;

        if (MuscleOpt_dic != null && !MuscleOpt_dic.isEmpty()) {

            StringBuilder sb = null;
            sb = new StringBuilder();

            Enumeration enumerator = MuscleOpt_dic.keys();
            while (enumerator.hasMoreElements()) {
                Object o = enumerator.nextElement();
                if ((Boolean) MuscleOpt_dic.get(o)) {
                    if (o.equals(MuscleEnum.noanchors)) {
                        sb.append(" -noanchors");
                    } else if (o.equals(MuscleEnum.brenner)) {
                        sb.append(" -brenner");
                    } else if (o.equals(MuscleEnum.cluster)) {
                        sb.append(" -cluster");
                    } else if (o.equals(MuscleEnum.diags)) {
                        sb.append(" -diags");
                    } else if (o.equals(MuscleEnum.dimer)) {
                        sb.append(" -dimer");
                    } else {
                    }
                }
            }
            muscle_optionsString = sb.toString();
        }
    }

    private String fastaName(String tag, String file, String species, int seqNumber) {

        String t = "";
        String f = "";
        String s = "";
        String n = "";

        if (isUseTag()) {
            t = "_" + tag;
        }
        if (isUseFile()) {
            f = "_" + file;
        }
        if (isUseSpecies()) {
            s = "_" + species;
        }
        if (isUseSeqNumber()) {
            n = "_" + seqNumber;
        }
        String returnName = getPrefix() + s + t + f + n + DBSettings.fastaExtesion;
        if (returnName.trim().startsWith("_")) {
            returnName = returnName.trim().substring(1);
        }

        return returnName;
    }

    private String fastaName(String tag, String file, String amplicon, int seqNumber, List<Object> l) {

        String name = "";

        for (int i = 0; i < l.size(); i++) {
            String o = String.valueOf(l.get(i));

            if (o.equals(SeqUtils.fileNameOpt_amplicon)) {
                name += "_" + amplicon;
            } else if (o.equals(SeqUtils.fileNameOpt_tag)) {
                name += "_" + tag;
            } else if (o.equals(SeqUtils.fileNameOpt_file)) {
                name += "_" + file;
            } else if (o.equals(SeqUtils.fileNameOpt_coverage)) {
                name += "_" + seqNumber;
            } else {
            }
        }

        String returnName = getPrefix() + name + DBSettings.fastaExtesion;
        if (returnName.trim().startsWith("_")) {
            returnName = returnName.trim().substring(1);
        }
        return returnName;
    }

    public void StartProcessing() {
        start();
    }

    @Override
    public void run() {
        try {

            //os specific add.
            InitialOSAddjust();

            currentOS = ProgramControler.getOperatingSystem2();
            if (currentOS == OSenum.MacOS && isAlignment()) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Attention! Alignment process is not suppported for Mac OS", this.parentFrame.getName(),
                        JOptionPane.INFORMATION_MESSAGE);
            }

            ArrayList<String> TagFileDiversityFormExtractTable = Extract_TagFileDiversityFormExtractTable();
            HashMap<String, String> TagFileSpeciesDiversity = Extract_TagFileSpeciesDiversityFromTagTable();

            String pathSep = System.getProperty("file.separator");
            parentFrame.jTextArea1.removeAll();
            totalLineCounter = 0;

            for (int i = 0; i < TagFileDiversityFormExtractTable.size(); i++) {

                if (FastaExportAndAlignment.interrupted()) {
                    SeqUtils.SetStatus(jLabel_statusLabel, "Process is finishing...");
                    throw new InterruptedException("Import canceled");
                }

                boolean fastaFileIsNotEmpty = false;
                String tag_file = TagFileDiversityFormExtractTable.get(i);
                String[] tagfilearr = tag_file.split(separator);
                String tag = tagfilearr[0];
                String file = tagfilearr[1];

                //file name;
                String species = "";
                if (TagFileSpeciesDiversity.containsKey(tag_file)) {
                    species = TagFileSpeciesDiversity.get(tag_file);
                }

                //query about total number of sequences:
                ResultSet rs = null;
                String sql = "SELECT " + "COUNT(" + DBSettings.extr_sequenceField + ") as " + DBSettings.seqNumber
                        + " FROM " + DBSettings.extr_tableName
                        + " WHERE " + DBSettings.extr_tagField + " = '" + tag + "' AND " + DBSettings.extr_fileField + " = '" + file + "'";
                int totalSeqNumber = 0;

                SeqUtils.SetStatus(jLabel_statusLabel, "Analysing multifasta for tag/file: " + tag + "/" + file);

                try {
                    rs = ProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
                    while (rs.next()) {
                        totalSeqNumber = rs.getInt(DBSettings.seqNumber);
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                }

                //checkpoint for minimal number of seq for search;
                if (totalSeqNumber < getMinNumberOfSeq()) {
                    continue;
                }

                //String fastaName = this.fastaName(tag, file, species, totalSeqNumber);
                String fastaName = this.fastaName(tag, file, species, totalSeqNumber, getNameOptions());

                BufferedWriter bufferedWriter = null;
                String fastaPath = this.getFolderPath() + pathSep + fastaName;
                fastaPath = SeqUtils.GetNextFileName(fastaPath);//to ensure unique name

                bufferedWriter = new BufferedWriter(new FileWriter(fastaPath));

                sql = "SELECT " + DBSettings.extr_allelName + ", " + DBSettings.extr_sequenceField + ", COUNT(" + DBSettings.extr_sequenceField + ") as " + DBSettings.seqNumber
                        + " FROM " + DBSettings.extr_tableName
                        + " WHERE " + DBSettings.extr_tagField + " = '" + tag + "' AND " + DBSettings.extr_fileField + " = '" + file + "'"
                        + " GROUP BY " + DBSettings.extr_sequenceField
                        + " ORDER BY " + DBSettings.seqNumber + " DESC";


                try {

                    rs = ProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
                    while (rs.next()) {

                        String seq = rs.getString(DBSettings.extr_sequenceField);
                        String name = rs.getString(DBSettings.extr_allelName);
                        int count = Integer.parseInt(rs.getString(DBSettings.seqNumber));

                        //sequence length condition:
                        if (getMaxLength() > 0) {
                            if (seq.length() > getMaxLength()) {
                                continue;
                            }
                        }

                        if (getMinLength() > 0) {
                            if (seq.length() < getMinLength()) {
                                continue;
                            }
                        }

                        if (count < getMinVarianCount()) {
                            continue;
                        } else {
                            fastaFileIsNotEmpty = true;
                        }
                        
                        bufferedWriter.write(">" + name + "_" + seq.length() + "_" + count + "\n");
                        bufferedWriter.write(seq + "\n");
                    }

                } catch (Exception e) {
                    throw e;
                } finally {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                    //Close the BufferedWriter
                    if (bufferedWriter != null) {
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    }

                    //remove empty files
                    if (!fastaFileIsNotEmpty) {
                        File fi = new File(fastaPath);
                        fi.delete();
                        fi = null;
                    }
                }

                //Alignment:
                if (isAlignment() && fastaFileIsNotEmpty) {
                    this.ProcessAlignment(fastaPath);
                }
            }

            SeqUtils.SetStatus(jLabel_statusLabel, "Done...");
            JOptionPane.showMessageDialog(parentFrame,
                    "Done...", this.parentFrame.getName(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (InterruptedException ex) {
            jLabel_statusLabel.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this.parentFrame,
                    ex.getMessage(), this.parentFrame.getName(), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(parentFrame,
                    ex.getMessage(), parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
        } finally {
            parentFrame.SetsForProcessing(false);
        }
    }

    private void ProcessAlignment(String fastaFile) throws Exception {
        try {
            Boolean interrupted = false;
            if (isAlignment()) {
                if (!fastaFile.endsWith(DBSettings.fastaExtesion)) {
                    return;
                }

                SeqUtils.SetStatus(jLabel_statusLabel, "Alignment: " + fastaFile);
                String alignFile = fastaFile.substring(0, fastaFile.indexOf(DBSettings.fastaExtesion)) + "_align" + DBSettings.fastaExtesion;
                String cmd = muscleFile + " -in " + fastaFile + " -out " + alignFile + muscle_optionsString;// + " -diags";// -log " + logFilePath;

                Process p = null;
                try {

                    /**
                     * OS specific implementation needed
                     */
                    ProcessBuilder pb = null;

                    if (currentOS == OSenum.Windows) {
                        pb = new ProcessBuilder("cmd.exe", "/c", cmd);
                    } else if (currentOS == OSenum.Linux) {
                        pb = new ProcessBuilder("bash", "-c", cmd);
                    }

                    pb.redirectErrorStream(true);
                    p = pb.start();

                    InputStreamReader isr = new InputStreamReader(p.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String line;
                    int lineCounter = 0;

                    while ((line = br.readLine()) != null) {
                        totalLineCounter++;
                        lineCounter++;

                        //if (lineCounter > 3) {
                        parentFrame.jTextArea1.insert(totalLineCounter + "\t" + line + "\n", 1);
                        //}

                        if (FastaExportAndAlignment.interrupted()) {
                            interrupted = true;
                            p.destroy();
                            SeqUtils.SetStatus(jLabel_statusLabel, "Process is finishing...");
                        }
                    }

                    if (FastaExportAndAlignment.interrupted() || interrupted) {
                        p.destroy();
                        SeqUtils.SetStatus(jLabel_statusLabel, "Process is finishing...");
                        p.waitFor();
                        throw new InterruptedException("Import canceled");
                    }

                } catch (Exception ioe) {
                    throw ioe;
                } finally {
                    try {
                        p.waitFor();
                        p.getInputStream().close();
                        p.getOutputStream().close();
                        p.getErrorStream().close();
                    } catch (Exception ioe) {
                    }
                }

                //sort in original ordersq
                if (this.isReOrderAlign()) {// && wait == Boolean.FALSE) {
                    SeqUtils.reOrderSeqFastaToFasta(fastaFile, alignFile);
                }

                if (FastaExportAndAlignment.interrupted() || interrupted) {
                    throw new InterruptedException("Import canceled");
                }

            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void InitialOSAddjust() {
        OSenum os = ProgramControler.getOperatingSystem2();
        if (os == OSenum.Windows) {
        } else if (os == OSenum.Linux) {
        } else if (os == OSenum.MacOS) {
        }
    }
}
