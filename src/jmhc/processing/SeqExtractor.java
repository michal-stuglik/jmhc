package jmhc.processing;

/**
 *
 * @author Michal Stuglik
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmhc.forms.ExtractorFrame;
import jmhc.forms.ProgramControler;
import jmhc.objects.DBSettings;
import jmhc.objects.ProgramOptionEnum;
import jmhc.objects.SequencePattern;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.regex.Matcher;
import org.biojava.utils.regex.Pattern;
import org.biojava.utils.regex.PatternFactory;
import jmhc.tools.SeqUtils;
import jmhc.objects.TAG;
import jmhc.objects.TAGvariants;
import org.biojava.bio.seq.io.SeqIOTools;

public class SeqExtractor extends Thread implements Runnable {

    private SymbolList PrimerF;
    private SymbolList PrimerR;
    private SymbolList PrimerRcF;
    private SymbolList PrimerRcR;
    protected int TAGlength = 0;
    private int primerRcutoff = 0;
    private JLabel jLabel_statusLabel;
    private int totalCounter;
    private ProgramControler mProgramControler;
    private ExtractorFrame parentFrame;
    private SymbolList MustStartSequence;
    private Boolean allowOneMismatchInTag;
    private HashMap<String, String> mismatch_tags = null;
    private HashMap<String, String> mismatch_tags_right = null;

    public Boolean getAllowOneMismatchInTag() {
        return allowOneMismatchInTag;
    }

    public void setAllowOneMismatchInTag(Boolean allowOneMismatchInTag) {
        this.allowOneMismatchInTag = allowOneMismatchInTag;
    }

    public SeqExtractor(ProgramControler mProgramControler, JLabel jLabel_statusLabel, ExtractorFrame parentFrame) {
        this.mProgramControler = mProgramControler;
        this.jLabel_statusLabel = jLabel_statusLabel;
        this.parentFrame = parentFrame;
    }
    private boolean oneSideTags = true;

    public boolean isOneSideTags() {
        return oneSideTags;
    }

    public void setOneSideTags(boolean oneSideTags) {
        this.oneSideTags = oneSideTags;
    }
    private String tagFile;

    public String getTagFile() {
        return tagFile;
    }

    public void setTagFile(String tagFile) {
        this.tagFile = tagFile;
    }

    public void setSeqBeginerMustBe(String seqBeginerMustBe) throws Exception {
        try {
            this.MustStartSequence = DNATools.createDNA(seqBeginerMustBe);
        } catch (IllegalSymbolException ex) {
            throw new Exception("Illegal char in primer");
        }
    }
    private boolean mustStartWith;

    public boolean isMustStartWith() {
        return mustStartWith;
    }

    public void setMustStartWith(boolean mustStartWith) {
        this.mustStartWith = mustStartWith;
    }

    public void setPrimerRcutoff(int primerRcutoff) {
        this.primerRcutoff = primerRcutoff;
    }

    public void setTAGlength(int TAGlength) {
        this.TAGlength = TAGlength;
    }

    public void setPrimerF(String PrimerF) throws Exception {
        try {
            this.PrimerF = DNATools.createDNA(PrimerF);
        } catch (IllegalSymbolException ex) {
            throw new Exception("Illegal char in primer");
        }
    }

    public void setPrimerR(String PrimerR) throws Exception {
        try {
            this.PrimerR = DNATools.createDNA(PrimerR);

            if (this.isCutoff()) {
                if (this.primerRcutoff > 0) {
                    int remove = this.PrimerR.length() - primerRcutoff;
                    if (remove > 0) {
                        this.PrimerR = this.PrimerR.subList(remove + 1, this.PrimerR.length());
                        System.out.println("Cutoff-ed PrimerRcR " + this.PrimerR.seqString());
                    }
                }
            }
        } catch (IllegalSymbolException ex) {
            throw new Exception("Illegal char in primer");
        }
    }

    private void setRcPrimerF(String PrimerF) throws Exception {
        try {
            SymbolList forward = DNATools.createDNA(PrimerF);
            this.PrimerRcF = DNATools.reverseComplement(forward);
        } catch (BioException ex) {
            throw ex;
        }
    }

    private void setRcPrimerR(String PrimerR) throws Exception {
        try {
            SymbolList forward = DNATools.createDNA(PrimerR);
            System.out.println("PrimerR " + forward.seqString());

            if (this.isCutoff()) {
                if (this.primerRcutoff > 0) {
                    int remove = forward.length() - primerRcutoff;
                    if (remove > 0) {
                        forward = forward.subList(remove + 1, forward.length());
                        System.out.println("Cutoff-ed PrimerRcR " + forward.seqString());
                    }
                }
            }

            this.PrimerRcR = DNATools.reverseComplement(forward);
            System.out.println("Cutoff-ed PrimerRcR " + PrimerRcR.seqString());

        } catch (BioException ex) {
            throw ex;
        }
    }
    private int fileCounter = 0;

    public int getFileCounter() {
        return fileCounter;
    }
    private int sequenceCounter = 0;

    public int getSequenceCounter() {
        return sequenceCounter;
    }
    private boolean finished = false;

    public boolean isFinished() {
        return finished;
    }
    private List<String> fileList;

    public void setFileList(List<String> fileList) {
        this.fileList = fileList;
    }
    private boolean strain_F = true;

    public boolean isStrain_F() {
        return strain_F;
    }

    public void setStrain_F(boolean strain_F) {
        this.strain_F = strain_F;
    }
    private boolean strain_R;

    public boolean isStrain_R() {
        return strain_R;
    }

    public void setStrain_R(boolean strain_R) {
        this.strain_R = strain_R;
    }
    protected String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
    protected int importedSequences;

    public int getImportedSequences() {
        return importedSequences;
    }

    public void setImportedSequences(int importedSequences) {
        this.importedSequences = importedSequences;
    }
    protected int counterR;

    public int getCounterR() {
        return counterR;
    }

    public void setCounterR(int counterR) {
        this.counterR = counterR;
    }
    protected int counterF;

    public int getCounterF() {
        return counterF;
    }

    public void setCounterF(int counterF) {
        this.counterF = counterF;
    }
    private boolean cutoff;

    public boolean isCutoff() {
        return cutoff;
    }

    public void setCutoff(boolean cutoff) {
        this.cutoff = cutoff;
    }
    private int insertCounter = 0;

    public int getInsertCounter() {
        return insertCounter;
    }
    private int fixedSequenceLength = 0;

    public int getFixedSequenceLength() {
        return fixedSequenceLength;
    }

    public void setFixedSequenceLength(int fixedSequenceLength) {
        this.fixedSequenceLength = fixedSequenceLength;
    }

    private void pre_runSettings() throws Exception {
        this.setRcPrimerF(this.PrimerF.seqString());
        this.setRcPrimerR(this.PrimerR.seqString());
    }

    public void StartProcessing() {
        start();
    }

    @Override
    @SuppressWarnings("static-access")
    public void run() /* throws InterruptedException*/ {
        totalCounter = 0;
        try {

            //pre settings
            this.pre_runSettings();

            //fasta files processing:
            for (int i = 0; i < fileList.size(); i++) {
                String fileNamePath = fileList.get(i);
                this.fastaFilesProcess(fileNamePath);
            }

            //log into table:
            String importLogString = String.valueOf(ProgramOptionEnum.PrimerF) + "=" + this.PrimerF.seqString() + ";"
                    + String.valueOf(ProgramOptionEnum.PrimerR) + "=" + this.PrimerR.seqString() + ";"
                    + String.valueOf(ProgramOptionEnum.FixedLength) + "=" + String.valueOf(this.fixedSequenceLength) + ";"
                    + String.valueOf(ProgramOptionEnum.Cuttoff) + "=" + String.valueOf(this.primerRcutoff) + ";"
                    + String.valueOf(ProgramOptionEnum.TagLength) + "=" + String.valueOf(this.TAGlength) + ";"
                    + String.valueOf(ProgramOptionEnum.TwoSideTags) + "=" + String.valueOf(!this.isOneSideTags()) + ";"
                    + String.valueOf(ProgramOptionEnum.Forward) + "=" + String.valueOf(this.strain_F) + ";"
                    + String.valueOf(ProgramOptionEnum.Reverse) + "=" + String.valueOf(this.strain_R) + ";"
                    + String.valueOf(ProgramOptionEnum.ExtractOnlyStarting) + "=" + String.valueOf(this.MustStartSequence.seqString()) + ";"
                    + String.valueOf(ProgramOptionEnum.AllowOneMismatchInTag) + "=" + String.valueOf(this.allowOneMismatchInTag);

            mProgramControler.getDBSettings().log_InfoTable(DBSettings.mess_ImportInfo, "count: " + insertCounter, importLogString);

            //log into table:
            mProgramControler.getDBSettings().log_InfoTable("extract", "extraction to file", "");

            this.setInfo("processed: " + totalCounter + ", imported: " + insertCounter);
            SeqUtils.SetStatus(jLabel_statusLabel, "success!" + " " + this.getInfo());
            JOptionPane.showMessageDialog(this.parentFrame, "success!" + " " + this.getInfo(), this.parentFrame.getName(), JOptionPane.INFORMATION_MESSAGE);

        } catch (InterruptedException ex) {
            SeqUtils.SetStatus(jLabel_statusLabel, ex.getMessage());
            Logger.getLogger(DBSettings.loggerProgram).log(Level.WARNING, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this.parentFrame, ex.getMessage(), this.parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this.parentFrame, ex.getMessage(), this.parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
        } finally {
            this.finished = true;
            parentFrame.SetsForProcessing(false, parentFrame.jButton_Start);
        }
    }

    private void CheckIfAllowOneMismatchInTag(String fileName) {


        //get known tags from databse:
        ResultSet rs = null;

        //SELECT header , seq from allels group by seqss
        String sql = "Select " + DBSettings.tags_tag + ","
                + DBSettings.tags_file
                + " from " + DBSettings.tags_tableName
                + " where " + DBSettings.tags_file + "=" + "'" + fileName + "'";

        try {
            rs = mProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
            while (rs.next()) {
                String t = rs.getString(1);
                if (this.oneSideTags) {

                    TAGvariants mTAGvariants = new TAGvariants(t, mProgramControler);
                    mismatch_tags = mTAGvariants.fillHashMap(mismatch_tags);

                } else {//2 side tag

                    int tag_l = t.length();

                    if (tag_l % 2 != 0) {//just checking
                        mProgramControler.getDBSettings().log_InfoTable("import", "allow one mismatch in tag", "this option is skipped, non-even tag length in database");
                        mismatch_tags = new HashMap<String, String>();
                        return;
                    }

                    if (this.TAGlength != (tag_l / 2)) {
                        mProgramControler.getDBSettings().log_InfoTable("import", "allow one mismatch in tag", "this option is skipped, tag length is differs from " + Integer.toString(this.TAGlength));
                        mismatch_tags = new HashMap<String, String>();
                        return;
                    }

                    String left_tag = t.substring(0, t.length() / 2);
                    String right_tag = t.substring(t.length() / 2);//TODO: check this value

                    TAGvariants mTAGvariants_l = new TAGvariants(left_tag, mProgramControler);
                    mismatch_tags = mTAGvariants_l.fillHashMap(mismatch_tags);

                    TAGvariants mTAGvariants_r = new TAGvariants(right_tag, mProgramControler);
                    mismatch_tags_right = mTAGvariants_r.fillHashMap(mismatch_tags_right);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SeqExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SeqExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SeqExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void fastaFilesProcess(String fileNamePath) throws Exception {

        BufferedReader br = null;
        File f = null;

        try {

            f = new File(fileNamePath);
            String fileName = f.getName();

            fileCounter++;
            sequenceCounter = 0;

            try {

                mismatch_tags = new HashMap<String, String>();
                mismatch_tags_right = new HashMap<String, String>();

                //TAGvariants
                if (this.allowOneMismatchInTag) {
                    CheckIfAllowOneMismatchInTag(fileName);
                }

                //prepare a BufferedReader for file io
                br = new BufferedReader(new FileReader(fileNamePath));
                                
                SequenceIterator iter = (SequenceIterator) SeqIOTools.fileToBiojava("fasta", "DNA", br);


                long time_iter_end = 0;
                long time_iter_start = 0;

                long time_seqproc_end = 0;
                long time_seqproc_start = 0;


                while (iter.hasNext()) {

                    time_iter_end = System.currentTimeMillis();
                    //System.out.println("Seq iter: " + String.valueOf(time_iter_end - time_iter_start));

                    totalCounter++;
                    sequenceCounter++;

                    if (SeqExtractor.interrupted()) {
                        throw new InterruptedException("Import canceled");
                    }

                    //thread update:
                    if (totalCounter % 100 == 0) {
                        SeqUtils.SetStatus(jLabel_statusLabel, "file: " + fileName + ", sequences: " + sequenceCounter + " total: " + totalCounter);
                    }

                    Sequence sequence = null;
                    try {

//                        try {
                        sequence = iter.nextSequence();

                        //time_seqproc_start = System.currentTimeMillis();

                        //sequence processing:
                        this.sequenceSearchProcess(sequence, fileName);

//                        time_seqproc_end = System.currentTimeMillis();
//                        System.out.println("Seq proc: " + String.valueOf(time_seqproc_end - time_seqproc_start));

                        //sequence = null;

                    } catch (BioException ex) {
                        Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, ex.getMessage(), ex);

                        Throwable mThrowable = ex.getCause();
                        if (mThrowable instanceof IOException) {
                            Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, "Problems with reading seqs from file " + fileNamePath, ex);
                            return;
                        }

                        //TODO:
//                        Object[] options = {"Please continue", "Exit program", "Skip all wrong sequences"};
//                        int n = JOptionPane.showOptionDialog(null, ex.getMessage(), "Ooo troubles...",
//                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//                        if (n == JOptionPane.OK_OPTION) {
//                            continue;
//                        } else {
//                            throw new InterruptedException("Import canceled");
//                        }
                    }
                    time_iter_start = System.currentTimeMillis();
                }

            } catch (FileNotFoundException ex) {
                SeqUtils.SetStatus(jLabel_statusLabel, ex.getMessage());
                Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(parentFrame, ex.getMessage(), parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
            } catch (BioException ex) {
                SeqUtils.SetStatus(jLabel_statusLabel, ex.getMessage());
                Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(parentFrame, ex.getMessage(), parentFrame.getName(), JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            f = null;
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    private void sequenceSearchProcess(Sequence sequence, String fileName) throws Exception {

        SequencePattern sp_primerF = null;
        SequencePattern sp_primerRcR = null;
        SequencePattern sp_primerR = null;
        SequencePattern sp_primerRcF = null;
        SequencePattern sp_startsWith = null;

        Boolean isPrimerF = false;
        Boolean isPrimerRcR = false;
        Boolean isPrimerR = false;
        Boolean isPrimerRcF = false;

        TAG tagF = null;
        TAG tagR = null;

        String TAG_F_value = "";
        String TAG_R_value = "";

        SymbolList mhcSequence = null;

        try {

            System.out.print(sequence.getName() + "\n");

            if (this.isStrain_F()) {
                sp_primerF = new SequencePattern(sequence, PrimerF, Boolean.TRUE);
                isPrimerF = sp_primerF.isMatched();

                if (getFixedSequenceLength() <= 0) { //fixlength case: only F
                    sp_primerRcR = new SequencePattern(sequence, PrimerRcR, Boolean.FALSE);
                    isPrimerRcR = sp_primerRcR.isMatched();
                }
            }

            if (this.isStrain_R()) {
                sp_primerR = new SequencePattern(sequence, PrimerR, Boolean.TRUE);
                isPrimerR = sp_primerR.isMatched();

                sp_primerRcF = new SequencePattern(sequence, PrimerRcF, Boolean.FALSE);
                isPrimerRcF = sp_primerRcF.isMatched();
            }

            /**
             *for extracting only fixed length sequence
             * this option is only avaiable for F, non-2side tag, non-R
             * GUI form take care about these assumptions, searching on F - R direction
             */
            if (this.isStrain_F() && isPrimerF && !sp_primerF.isAmbiguity() && getFixedSequenceLength() > 0) {

                //Tag F
                tagF = new TAG(sequence, TAGlength, true, sp_primerF, mismatch_tags);
                if (!TagTest(tagF)) {
                    return;
                }

                try { //sequence extraction
                    mhcSequence = sequence.subList(sp_primerF.getEnd() + 1, sequence.length());

                    try {  //get fixed length:
                        mhcSequence = mhcSequence.subList(1, getFixedSequenceLength());
                        System.out.print(mhcSequence.seqString() + "\n");
                    } catch (Exception e) {
                        return;
                    }

                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, ex.getMessage(), ex);
                    return;
                } catch (Exception ex) {
                    throw ex;
                }

                counterF++;
            } //searching on F - R direction
            else if (this.isStrain_F() && isPrimerF && isPrimerRcR && !sp_primerF.isAmbiguity() && !sp_primerRcR.isAmbiguity()) {

                //Tag F
                tagF = new TAG(sequence, TAGlength, true, sp_primerF, mismatch_tags);
                if (!TagTest(tagF)) {
                    return;
                }

                //Tag R
                if (!isOneSideTags()) {
                    tagR = new TAG(sequence, TAGlength, false, sp_primerRcR, mismatch_tags_right);
                    if (!TagTest(tagR)) {
                        return;
                    }
                }

                try {
                    if (sp_primerF.getEnd() > sp_primerRcR.getStart()) {
                        Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, null, "Warning! strange primer overlap. length: " + sequence.length());
                        return;
                    }

                    //test
                    if (sp_primerRcR.getStart() - sp_primerF.getEnd() < 1) {
                        return;
                    }

                    //sequence extraction
                    mhcSequence = sequence.subList(sp_primerF.getEnd() + 1, sp_primerRcR.getStart() - 1);

                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, ex.getMessage(), ex);
                    return;
                } catch (Exception ex) {
                    throw ex;
                }

                counterF++;
            } //searching on R - F direction
            else if (this.isStrain_R() && isPrimerR && isPrimerRcF && !sp_primerR.isAmbiguity() && !sp_primerRcF.isAmbiguity()) {
                try {

                    //Tag R
                    if (!isOneSideTags()) {
                        tagR = new TAG(sequence, TAGlength, true, sp_primerR, mismatch_tags_right);
                        if (!TagTest(tagR)) {
                            return;
                        }
                    }

                    //Tag F
                    tagF = new TAG(sequence, TAGlength, false, sp_primerRcF, mismatch_tags);
                    if (!TagTest(tagF)) {
                        return;
                    }

                    //test
                    if (sp_primerRcF.getStart() - sp_primerR.getEnd() < 1) {
                        return;
                    }

                    //sequence extraction
                    mhcSequence = sequence.subList(sp_primerR.getEnd() + 1, sp_primerRcF.getStart() - 1);

                    //reverseComplement  to have F-R direction
                    mhcSequence = DNATools.reverseComplement(mhcSequence);

                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, ex.getMessage(), ex);
                    return;
                } catch (Exception ex) {
                    throw ex;
                }

                counterR++;
            }

            try {
                //sequence ambiguity check:
                if (mhcSequence == null || SeqUtils.hasAmbiquity(mhcSequence)) {
                    return;
                }

                //tag R value
                if (tagR != null && !tagR.isEmpty()) {
                    TAG_R_value = tagR.getTagSequence().seqString().toUpperCase();
                }

                //tag F value
                if (tagF != null && !tagF.isEmpty()) {
                    TAG_F_value = tagF.getTagSequence().seqString().toUpperCase();
                }
            } catch (Exception ex) {
                throw ex;
            }

            //if seq must start with desire string seq:
            if (mustStartWith) {
                sp_startsWith = new SequencePattern(DNATools.createDNASequence(mhcSequence.seqString(), "dna"), MustStartSequence, Boolean.TRUE);
                if (!sp_startsWith.isMatched()) {
                    return;
                } else {
                    mhcSequence = mhcSequence.subList(sp_startsWith.getStart(), mhcSequence.length());
                }
            }

            try {


                long time_seq_insert_start = System.currentTimeMillis();

                String sql_R = "Insert into " + DBSettings.extr_tableName
                        + " ("
                        + DBSettings.extr_tagFField + ", "
                        + DBSettings.extr_tagRField + ", "
                        + DBSettings.extr_sequenceName + ", "
                        + DBSettings.extr_sequenceField + ", "
                        + DBSettings.extr_fileField
                        + ") values ('"
                        + TAG_F_value + "','"
                        + TAG_R_value + "','"
                        + sequence.getName() + "','"
                        + mhcSequence.seqString().toUpperCase() + "','"
                        + fileName
                        + "')";

                this.mProgramControler.getDBSettings().getMSQLite().Execute(sql_R);
                insertCounter++;

                long time_seq_insert_end = System.currentTimeMillis();
                System.out.println("Seq insert: " + String.valueOf(time_seq_insert_end - time_seq_insert_start));




            } catch (Exception ex) {
                throw ex;
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (tagF != null) {
                tagF.Destroy();
                tagF = null;
            }
            if (tagR != null) {
                tagR.Destroy();
                tagR = null;
            }
            if (sp_primerF != null) {
                sp_primerF.Destroy();
                sp_primerF = null;
            }
            if (sp_primerRcR != null) {
                sp_primerRcR.Destroy();
                sp_primerRcR = null;
            }
            if (sp_primerR != null) {
                sp_primerR.Destroy();
                sp_primerR = null;
            }
            if (sp_primerRcF != null) {
                sp_primerRcF.Destroy();
                sp_primerRcF = null;
            }
        }
    }

    public Matcher PaternSearch(SymbolList Primer, Sequence searchInSequence) throws Exception {

        Matcher matcher = null;
        Pattern pattern;

        try {

            FiniteAlphabet IUPAC = DNATools.getDNA();
            SymbolList searchInSequenceSL = DNATools.createDNA(searchInSequence.seqString());

            // Create pattern using pattern factory.
            PatternFactory FACTORY = PatternFactory.makeFactory(IUPAC);
            pattern = FACTORY.compile(Primer.seqString());
            matcher = pattern.matcher(searchInSequenceSL);

        } catch (Exception ex) {
            throw ex;
        }
        return matcher;
    }
    private List<String> genericTAGs;

    public List<String> getGenericTAGs() throws Exception {
        if (genericTAGs == null) {
            try {
                TagsGenerator tg = new TagsGenerator();
                return tg.getGenericTags(4, "CGAT");
            } catch (Exception ex) {
                throw ex;
            }
        }
        return genericTAGs;
    }
    protected String speciesTAGsFile;

    public void setSpeciesTAGsFile(String speciesTAGsFile) {
        this.speciesTAGsFile = speciesTAGsFile;
    }
    private List<String> experimentalTAGs;

    public List<String> getExperimentalTAGs() throws IOException {
        if (!this.speciesTAGsFile.isEmpty()) {

            experimentalTAGs = new ArrayList<String>();
            try {
                BufferedReader input = new BufferedReader(new FileReader(this.speciesTAGsFile));
                try {
                    String line = null; //not declared within while loop
                    while ((line = input.readLine()) != null) {
                        String[] reg = line.split("\n");
                        if (reg.length == 2) {
                            experimentalTAGs.add(reg[1]);
                        }
                    }
                } finally {
                    input.close();
                }
            } catch (IOException ex) {
                throw ex;
            }
        }
        return experimentalTAGs;
    }

    private Boolean TagTest(TAG tag) {
        if (tag == null || !tag.isCorrect() || tag.isAmbiguity()) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }
}
