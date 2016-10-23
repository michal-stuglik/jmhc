package jmhc.processing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmhc.forms.ExtractorFrame;
import jmhc.forms.ProgramControler;
import jmhc.objects.DBSettings;
import jmhc.objects.ProgramOptionEnum;
import jmhc.objects.TagFileObject;
import jmhc.objects.TagFileObjectSortByIndividual;
import jmhc.tools.SQLite;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class DBExtractor extends Thread implements Runnable {

    private String tableName;
    private ProgramControler mProgramControler;
    private JLabel jLabel_statusLabel;
    private static String ArtificialIndiviudal = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz74E5C800-C18C-42F4-BBC7-04565247EB58z";
    private ExtractorFrame parentFrame;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    private SQLite sQLlite;

    public SQLite getSQLlite() {
        return sQLlite;
    }

    public void setSQLlite(SQLite sQLlite) {
        this.sQLlite = sQLlite;
    }
    private Statement statement;

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
    private DBSettings mDBSettings;

    public DBSettings getMDBSettings() {
        return mDBSettings;
    }

    public void setMDBSettings(DBSettings mDBSettings) {
        this.mDBSettings = mDBSettings;
    }
    private String tagFile;

    public String getTagFile() {
        return tagFile;
    }

    public void setTagFile(String tagFile) {
        this.tagFile = tagFile;
    }
    private String outPutFile = "";

    public String getOutPutFile() {
        return outPutFile;
    }

    public void setOutPutFile(String outPutFile) {
        this.outPutFile = outPutFile;
    }
    private boolean allesUpdate;

    public boolean isAllesUpdate() {
        return allesUpdate;
    }

    public void SetAllesUpdate(boolean allesUpdate) {
        this.allesUpdate = allesUpdate;
    }
    private boolean finished = false;

    public boolean isFinished() {
        return finished;
    }
    protected String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
    private boolean generateNo_TAGS = true;

    public boolean isGenerateNo_TAGS() {
        return generateNo_TAGS;
    }

    public void setGenerateNo_TAGS(boolean generateNo_TAGS) {
        this.generateNo_TAGS = generateNo_TAGS;
    }
    private int variantsNumber = 0;

    public int getVariantsNumber() {
        return variantsNumber;
    }

    public void setVariantsNumber(int variantsNumber) {
        this.variantsNumber = variantsNumber;
    }
    
    
    //report vars:
    private int REPORT_NO_TAG_counter = 0;
    private int REPORT_variant_counter = 0;
    private List<TagFileObject> tags = null;

    public DBExtractor(ProgramControler mProgramControler, JLabel jLabel_statusLabel, ExtractorFrame parentFrame) {
        this.mProgramControler = mProgramControler;
        this.mDBSettings = mProgramControler.getDBSettings();
        this.jLabel_statusLabel = jLabel_statusLabel;
        this.parentFrame = parentFrame;
    }

    public DBExtractor(String tableName) {
        this.tableName = tableName;
    }

    public void StartProcessing() {
        start();
    }

    @Override
    @SuppressWarnings("static-access")
    public void run() /* throws InterruptedException*/ {

        String sql = "";

        try {

            //status:
            SeqUtils.SetStatus(jLabel_statusLabel, "Updating tag field");

            //merge TAG field into one
            sql = "UPDATE " + DBSettings.extr_tableName + " SET " + DBSettings.extr_tagField + " = " + DBSettings.extr_tagFField + " || " + DBSettings.extr_tagRField;
            boolean b = this.mProgramControler.getDBSettings().getMSQLite().Execute(sql);

            //cancel checkpoint:
            this.CancelCheckpoint();

            //db fields indexing
            boolean idx_bool = false;
            SeqUtils.SetStatus(jLabel_statusLabel, "Indexing database: tag field");
            //idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_ext_tag", DBSettings.extr_tableName, DBSettings.extr_tagField);
            //idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_ext_file", DBSettings.extr_tableName, DBSettings.extr_fileField);
            idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_ext_file", DBSettings.extr_tableName, DBSettings.extr_tagField, DBSettings.extr_fileField);
            
            //cancel checkpoint:
            this.CancelCheckpoint();

            //idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_ext_tagF", DBSettings.extr_tableName, DBSettings.extr_tagFField);
            //idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_ext_tagR", DBSettings.extr_tableName, DBSettings.extr_tagRField);
            SeqUtils.SetStatus(jLabel_statusLabel, "Indexing database: seq field");
            idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_ext_seq", DBSettings.extr_tableName, DBSettings.extr_sequenceField);

            //cancel checkpoint:
            this.CancelCheckpoint();

            //update allels info in extract  table based on allel table
            AllelsTableInfo mAllelsTableInfo = new AllelsTableInfo(mProgramControler, jLabel_statusLabel);
            mAllelsTableInfo.UpdateAllelInfoInExtractTable(this.getVariantsNumber());

            //cancel checkpoint:
            this.CancelCheckpoint();

            String datetime = SeqUtils.getDateTime2();

            String outFile_1type = this.GetFileName(this.getOutPutFile(), datetime, "1");
            String outFile_2type = this.GetFileName(this.getOutPutFile(), datetime, "2");
            String outFile_report = this.GetFileName(this.getOutPutFile(), datetime, "report");

            //status:
            SeqUtils.SetStatus(jLabel_statusLabel, "Extracting to: " + outFile_1type);
            Extract_Type1(outFile_1type);

            //status:
            SeqUtils.SetStatus(jLabel_statusLabel, "Extracting to: " + outFile_2type);
            Extract_Type2(outFile_2type);

            //report:
            SeqUtils.SetStatus(jLabel_statusLabel, "Generating report to: " + outFile_report);
            Extract_Report(outFile_report);

            //log into table:
            mProgramControler.getDBSettings().log_InfoTable("generate output", "generate output files", "");
            this.setInfo("Output files generated.");//+ totalCounter + ", imported: " + insertCounter);
            SeqUtils.SetStatus(jLabel_statusLabel, "success!" + " " + this.getInfo());
            JOptionPane.showMessageDialog(this.parentFrame, "success!" + " " + this.getInfo(), this.parentFrame.getName(), JOptionPane.INFORMATION_MESSAGE);

        } catch (InterruptedException ex) {
            SeqUtils.SetStatus(jLabel_statusLabel, ex.getMessage());
            Logger.getLogger(DBSettings.loggerProgram).log(Level.WARNING, null, ex);
            JOptionPane.showMessageDialog(this.parentFrame, ex.getMessage(), this.parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this.parentFrame, ex.getMessage(), this.parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
        } finally {
            this.finished = true;
            parentFrame.SetsForProcessing(false, parentFrame.jButton_StartOutput);
        }
    }

    private void Extract_Type1(String outFile_1type) throws SQLException, IOException, Exception {

        List<TagFileObject> newtags = null;
        List<String> sequences = null;
        ResultSet rs = null;
        String sql = "";
        String sql_report = "";
        BufferedWriter mBufferedWriter = null;

        try {

            mBufferedWriter = new BufferedWriter(new FileWriter(outFile_1type));
            tags = new ArrayList<TagFileObject>();

            //unique columns
            sql = "SELECT " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField
                    + " FROM " + DBSettings.extr_tableName
                    + " group by " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField
                    + " order by " + DBSettings.extr_fileField + " asc";
            rs = mDBSettings.getMSQLite().ExecuteQuery(sql);

            try {
                while (rs.next()) {
                    tags.add(new TagFileObject(rs.getString(1), rs.getString(2)));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            //add info about individuals:
            try {
                sql = "SELECT " + DBSettings.tags_tag + ", " + DBSettings.tags_file + ", " + DBSettings.tags_species
                        + " FROM " + DBSettings.tags_tableName;
                rs = mDBSettings.getMSQLite().ExecuteQuery(sql);

                while (rs.next()) {

                    String t = rs.getString(DBSettings.tags_tag);
                    String fi = rs.getString(DBSettings.tags_file);
                    String ind = rs.getString(DBSettings.tags_species);

                    //individual set:
                    for (TagFileObject mTagFileObject : tags) {
                        if (mTagFileObject.getFile().toUpperCase().equals(fi.toUpperCase()) && mTagFileObject.getTag().toUpperCase().equals(t.toUpperCase())) {
                            mTagFileObject.setIndividual(ind);
                            break;
                        }
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            /*for those tags where there is no informaltion about individual, set "artificial individ." for sorting purposes*/
            REPORT_NO_TAG_counter = 0; //for report file
            newtags = new ArrayList<TagFileObject>();
            for (int i = 0; i < tags.size(); i++) {
                TagFileObject mTagFileObject = tags.get(i);
                if (mTagFileObject.getIndividual().isEmpty() && isGenerateNo_TAGS()) {
                    mTagFileObject.setIndividual(ArtificialIndiviudal);
                    newtags.add(mTagFileObject);
                } else if (!mTagFileObject.getIndividual().isEmpty()) {
                    newtags.add(mTagFileObject);
                } else {
                    REPORT_NO_TAG_counter++;
                }
            }

            //re-change tag list:
            tags = newtags;

            //unique sequences
            sql = "select distinct " + DBSettings.extr_sequenceField + " from " + DBSettings.extr_tableName;

            //filtering for at least seq abundance
            if (variantsNumber > 0) {
                sql = "select " + DBSettings.extr_sequenceField + ", COUNT(" + DBSettings.extr_sequenceField + ")"
                        + " from " + DBSettings.extr_tableName
                        + " group by " + DBSettings.extr_sequenceField
                        + " having COUNT(" + DBSettings.extr_sequenceField + ") > " + variantsNumber;

                sql_report = "select " + DBSettings.extr_sequenceField + ", COUNT(" + DBSettings.extr_sequenceField + ")"
                        + " from " + DBSettings.extr_tableName
                        + " group by " + DBSettings.extr_sequenceField
                        + " having COUNT(" + DBSettings.extr_sequenceField + ") <= " + variantsNumber;
            }

            sequences = new ArrayList<String>();
            try {
                rs = mDBSettings.getMSQLite().ExecuteQuery(sql);
                while (rs.next()) {
                    sequences.add(rs.getString(1));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            //for report
            REPORT_variant_counter = 0;
            if (sql_report != null && !sql_report.equals("")) {
                try {
                    rs = mDBSettings.getMSQLite().ExecuteQuery(sql_report);
                    while (rs.next()) {
                        REPORT_variant_counter++;
                    }
                } catch (Exception e) {
                } finally {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                }
            }

            //Sorting TagFileObject by individual names:
            Collections.sort(tags, new TagFileObjectSortByIndividual());

            //tags names in outputfile: - line 1
            String tagsLine = "";
            tagsLine = "\t" + "tag";

            for (TagFileObject mTagFileObject : tags) {
                tagsLine += "\t" + mTagFileObject.getTag();
            }
            mBufferedWriter.write(tagsLine + "\n");

            //file names in outputfile: - line 2
            String tagsLine2 = "";
            tagsLine2 = "\t" + "file";

            for (TagFileObject mTagFileObject : tags) {
                tagsLine2 += "\t" + mTagFileObject.getFile();
            }
            mBufferedWriter.write(tagsLine2 + "\n");

            //individual names in outputfile: - line 3
            String tagsLine3 = "";
            tagsLine3 = "\t" + "ind";

            for (TagFileObject mTagFileObject : tags) {
                //clean artificial individual info from TagFileObject
                if (mTagFileObject.getIndividual().equals(ArtificialIndiviudal)) {
                    mTagFileObject.setIndividual("");
                }
                tagsLine3 += "\t" + mTagFileObject.getIndividual();
            }
            mBufferedWriter.write(tagsLine3 + "\n");

            //wyciaganie listy tagow dla danej sekwencji:
            for (String seq : sequences) {

                //cancel checkpoint:
                this.CancelCheckpoint();

                if (seq == null || seq.isEmpty()) {
                    continue;
                }

                //cleaning tags trac:
                for (TagFileObject mTagFileObject : tags) {
                    mTagFileObject.setCount(0);
                }

                sql = "select " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField + ", count(" + DBSettings.extr_tagField + ") as " + DBSettings.seqNumber + ", " + DBSettings.extr_allelName
                        + " from " + DBSettings.extr_tableName
                        + " where " + DBSettings.extr_sequenceField + " = '" + seq + "'"
                        + " group by " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField;
                rs = mDBSettings.getMSQLite().ExecuteQuery(sql);

                String allel = "";
                try {
                    while (rs.next()) {
                        TagFileObject localTagFileObject = null;
                        try {
                            String t = rs.getString(DBSettings.extr_tagField);
                            String f = rs.getString(DBSettings.extr_fileField);
                            int ile = rs.getInt(DBSettings.seqNumber);
                            allel = rs.getString(DBSettings.extr_allelName);

                            localTagFileObject = new TagFileObject(t, f);

                            for (TagFileObject mTagFileObject : tags) {
                                if (mTagFileObject.equals(localTagFileObject)) {
                                    mTagFileObject.setCount(ile);
                                    break;
                                }
                            }
                        } finally {
                            localTagFileObject = null;
                        }
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                }

                String line = allel + "\t" + seq;
                for (TagFileObject mTagFileObject : tags) {
                    line += "\t" + mTagFileObject.getCount();
                }

                //write to file:
                mBufferedWriter.write(line + "\n");
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (mBufferedWriter != null) {
                mBufferedWriter.flush();
                mBufferedWriter.close();
                mBufferedWriter = null;
            }
        }
    }

    private void Extract_Type2(String outFile_2type) throws Exception {
        List<String> sequences = null;
        ResultSet rs = null;
        String sql = "";
        BufferedWriter mBufferedWriter = null;
        try {

            //unique sequences
            sql = "select distinct " + DBSettings.extr_sequenceField + " from " + DBSettings.extr_tableName;
            //filtering for at least seq abundance
            if (variantsNumber > 0) {
                sql = "select " + DBSettings.extr_sequenceField + ", COUNT(" + DBSettings.extr_sequenceField + ")"
                        + " from " + DBSettings.extr_tableName
                        + " group by " + DBSettings.extr_sequenceField
                        + " having COUNT(" + DBSettings.extr_sequenceField + ") > " + variantsNumber;
            }

            sequences = new ArrayList<String>();

            try {
                rs = mDBSettings.getMSQLite().ExecuteQuery(sql);
                while (rs.next()) {
                    sequences.add(rs.getString(1));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            mBufferedWriter = new BufferedWriter(new FileWriter(outFile_2type));

            for (String seq : sequences) {

                //cancel checkpoint:
                this.CancelCheckpoint();

                if (seq == null || seq.isEmpty()) {
                    continue;
                }

                sql = "select " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField + ", count(" + DBSettings.extr_tagField + ") as " + DBSettings.seqNumber + ", " + DBSettings.extr_allelName
                        + " from " + DBSettings.extr_tableName
                        + " where " + DBSettings.extr_sequenceField + " = '" + seq + "'"
                        + " group by " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField;
                rs = mDBSettings.getMSQLite().ExecuteQuery(sql);

                try {

                    StringBuilder sb = new StringBuilder();
                    sb.append(seq);
                    while (rs.next()) {
                        try {
                            String t = rs.getString(DBSettings.extr_tagField);
                            String f = rs.getString(DBSettings.extr_fileField);
                            int ile = rs.getInt(DBSettings.seqNumber);
                            //allel = rs.getString(DBSettings.extr_allelName);

                            sb.append("\t").append(t).append("_").append(f).append("\t").append(ile);
                        } finally {
                        }
                    }
                    //write to file:
                    mBufferedWriter.write(sb.toString() + "\n");
                } finally {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (mBufferedWriter != null) {
                mBufferedWriter.flush();
                mBufferedWriter.close();
                mBufferedWriter = null;
            }
        }
    }

    private void Extract_Report(String outFile_report) throws Exception {

        BufferedWriter mBufferedWriter_report = null;
        ResultSet rs = null;
        String sql = "";
        try {

            mBufferedWriter_report = new BufferedWriter(new FileWriter(outFile_report));

            //REPORT:
            //import details info
            sql = "select " + DBSettings.info_activity + ", " + DBSettings.info_importinfo
                    + " from " + DBSettings.info_tableName
                    + " where " + DBSettings.info_activity + " = '" + DBSettings.mess_ImportInfo + "'"
                    + " order by " + DBSettings.info_dateTime + " desc";
            rs = mDBSettings.getMSQLite().ExecuteQuery(sql);
            Dictionary import_info = new Hashtable();

            try {
                while (rs.next()) {
                    String[] infos = rs.getString(DBSettings.info_importinfo).split(";");
                    for (int i = 0; i < infos.length; i++) {
                        String infosik = infos[i];

                        String key = "";
                        String value = "";

                        String[] kv = infosik.split("=");
                        try {
                            key = kv[0];
                            value = kv[1];
                        } catch (Exception ex) {
                        }
                        import_info.put(key, value);
                    }
                    break;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            try {
                mBufferedWriter_report.write("Import info" + "\n");
                mBufferedWriter_report.write("Primer F: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.PrimerF))) + "\n");
                mBufferedWriter_report.write("Primer R: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.PrimerR))) + "\n");
                mBufferedWriter_report.write("Fixed length: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.FixedLength))) + "\n");
                mBufferedWriter_report.write("Cut off: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.Cuttoff))) + "\n");
                mBufferedWriter_report.write("Tag length: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.TagLength))) + "\n");
                mBufferedWriter_report.write("2-sided Tags: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.TwoSideTags))) + "\n");
                mBufferedWriter_report.write("Forward: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.Forward))) + "\n");
                mBufferedWriter_report.write("Reverse: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.Reverse))) + "\n");
                mBufferedWriter_report.write("Extract only starting with seq: " + String.valueOf(import_info.get(String.valueOf(ProgramOptionEnum.ExtractOnlyStarting))) + "\n");
            } catch (Exception e) {
            }

            mBufferedWriter_report.write("\n");

            //export to file info:
            mBufferedWriter_report.write("Excluded from outputs: " + "\n");
            mBufferedWriter_report.write("NO_TAG: " + REPORT_NO_TAG_counter + "\n");
            mBufferedWriter_report.write("Variants: " + REPORT_variant_counter + "\n");


            //reads counter for diffrent tags
            /*
            SELECT tag, seq, count(seq) as ile from extract group by tag
             */
            sql = "select " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField + ", COUNT(" + DBSettings.extr_sequenceField + ") as " + DBSettings.seqNumber
                    + " from " + DBSettings.extr_tableName
                    + " group by " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField;
            rs = mDBSettings.getMSQLite().ExecuteQuery(sql);

            try {
                while (rs.next()) {
                    //individual set:
                    for (TagFileObject mTagFileObject : tags) {
                        if (mTagFileObject.getFile().toUpperCase().equals((rs.getString(DBSettings.extr_fileField)).toUpperCase()) && mTagFileObject.getTag().toUpperCase().equals((rs.getString(DBSettings.extr_tagField)).toUpperCase())) {
                            mTagFileObject.setCount((int) rs.getInt(DBSettings.seqNumber));
                            break;
                        }
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            mBufferedWriter_report.write("\n");
            mBufferedWriter_report.write("Amplicon's reads # (TAG): " + "\n");
            for (TagFileObject mTagFileObject : tags) {
                if (!mTagFileObject.getIndividual().equals("")) {
                    mBufferedWriter_report.write(mTagFileObject.getTag() + " " + mTagFileObject.getFile() + ": " + String.valueOf(mTagFileObject.getCount()) + "\n");
                }
            }

            mBufferedWriter_report.write("\n");
            mBufferedWriter_report.write("Amplicon's reads # (NO_TAG): " + "\n");
            for (TagFileObject mTagFileObject : tags) {
                if (mTagFileObject.getIndividual().equals("")) {
                    mBufferedWriter_report.write(mTagFileObject.getTag() + " " + mTagFileObject.getFile() + ": " + String.valueOf(mTagFileObject.getCount()) + "\n");
                }
            }

            //Unique sequence # for tags:
            //tags count cleaning:
            for (TagFileObject mTagFileObject : tags) {
                mTagFileObject.setCount(0);
            }

            //reads counter for diffrent tags
            for (TagFileObject mTagFileObject : tags) {
                int uniqCount = 0;
                sql = "SELECT " + DBSettings.extr_tagField + ", " + DBSettings.extr_fileField + ", " + DBSettings.extr_sequenceField
                        + " FROM " + DBSettings.extr_tableName
                        + " WHERE " + DBSettings.extr_tagField + "='" + mTagFileObject.getTag() + "' and file = '" + mTagFileObject.getFile()
                        + "' GROUP BY " + DBSettings.extr_sequenceField;

                rs = mDBSettings.getMSQLite().ExecuteQuery(sql);
                try {
                    while (rs.next()) {
                        uniqCount++;
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                }
                mTagFileObject.setCount(uniqCount);
            }

            mBufferedWriter_report.write("\n");
            mBufferedWriter_report.write("The number of unique variants (TAG): " + "\n");
            for (TagFileObject mTagFileObject : tags) {
                if (!mTagFileObject.getIndividual().equals("")) {
                    mBufferedWriter_report.write(mTagFileObject.getTag() + " " + mTagFileObject.getFile() + ": " + String.valueOf(mTagFileObject.getCount()) + "\n");
                }
            }

            mBufferedWriter_report.write("\n");
            mBufferedWriter_report.write("The number of unique variants (NO_TAG): " + "\n");
            for (TagFileObject mTagFileObject : tags) {
                if (mTagFileObject.getIndividual().equals("")) {
                    mBufferedWriter_report.write(mTagFileObject.getTag() + " " + mTagFileObject.getFile() + ": " + String.valueOf(mTagFileObject.getCount()) + "\n");
                }
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (mBufferedWriter_report != null) {
                try {
                    mBufferedWriter_report.flush();
                    mBufferedWriter_report.close();
                    mBufferedWriter_report = null;
                } catch (IOException ex) {
                    Logger.getLogger(DBExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private String GetFileName(String file, String dateTime, String type) {
        if (file.endsWith(DBSettings.outputExtesion)) {
            file = file.substring(0, file.lastIndexOf(DBSettings.outputExtesion));
        }

        return file + "_" + dateTime + "_" + type + DBSettings.outputExtesion;
    }

    private void CancelCheckpoint() throws InterruptedException {
        if (DBExtractor.interrupted()) {
            throw new InterruptedException("Operation canceled");
        }
    }
}
