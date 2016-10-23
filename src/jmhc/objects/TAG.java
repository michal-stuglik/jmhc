package jmhc.objects;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.SymbolList;
import jmhc.tools.SeqUtils;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.SimpleSymbolList;

/**
 *
 * @author Michal Stuglik
 */
public class TAG {

    private SymbolList tagSequence;
    private HashMap<String, String> mismatch_hm = null;

    public SymbolList getTagSequence() {
        return tagSequence;
    }

    public SymbolList getRCTagSequence() throws IllegalAlphabetException {
        return DNATools.reverseComplement(tagSequence);
    }

    public void setTagSequence(SymbolList tagSequence) {
        this.tagSequence = tagSequence;
    }
    private Sequence sequence;

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
    private SequencePattern primerF;
    private boolean ambiguity;

    public boolean isAmbiguity() {
        return ambiguity;
    }

    public void setAmbiguity(boolean ambiguity) {
        this.ambiguity = ambiguity;
    }
    private int length;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    private boolean correct;

    public boolean isCorrect() {
        return correct;
    }
    private boolean empty;

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
    private boolean leftSideTag;

    public boolean isLeftSideTag() {
        return leftSideTag;
    }
    private SequencePattern primer;

    public SequencePattern getPrimer() {
        return primer;
    }

    public void setPrimer(SequencePattern primer) {
        this.primer = primer;
    }

    public TAG(Sequence sequence, int length, boolean tagLeft, SequencePattern primer, HashMap<String, String> mismatch_hm) {

        this.sequence = sequence;
        this.length = length;
        this.leftSideTag = tagLeft;
        this.primer = primer;
        this.mismatch_hm = mismatch_hm;

        try {
            this.run();
        } catch (Exception ex) {
            Logger.getLogger(DBSettings.loggerProgram).log(Level.SEVERE, null, ex);
            return;
        }
    }

    private void run() throws Exception {
        this.correct = false;
        SymbolList tag = null;

        try {

            if (this.getLength() == 0) {

                this.setEmpty(true);
                this.correct = true;

                return;
            }

            if (isLeftSideTag()) {

                int tag_start = this.getPrimer().getStart() - this.getLength();
                int tag_end = this.getPrimer().getStart() - 1;

                if (tag_start <= 0 || tag_end - tag_start < 0) {

                    System.out.print("[TAG start position:TAG length] " + this.getPrimer().getStart() + ":" + this.getLength() + "\n");
                    return;
                }

                tag = getSequence().subList(tag_start, tag_end);

                //mismatch substitution
                if (this.mismatch_hm != null) {
                    tag = ChangeTagIfNessesery(tag);
                }

                this.setTagSequence(tag);

            } else {

                int tag_start = this.getPrimer().getEnd() + 1;
                int tag_end = this.getPrimer().getEnd() + this.getLength();

                try {
                    tag = getSequence().subList(tag_start, tag_end);
                } catch (Exception e) {
                    return;
                }

                //mismatch substitution
                if (this.mismatch_hm != null) {
                    tag = ChangeTagIfNessesery(tag);
                }

                this.setTagSequence(DNATools.reverseComplement(tag));
            }

            this.setAmbiguity(SeqUtils.hasAmbiquity(tag));
            this.correct = true;

        } catch (Exception e) {
            throw e;
        }


    }

    private SymbolList ChangeTagIfNessesery(SymbolList tag) {

        if (this.mismatch_hm.containsKey(tag.seqString().toUpperCase())) {
            try {
                String s_tag = this.mismatch_hm.get(tag.seqString().toUpperCase());

                Alphabet dna = DNATools.getDNA();
                SymbolTokenization dnaToke = dna.getTokenization("token");
                tag = new SimpleSymbolList(dnaToke, s_tag);
                //mProgramControler.getDBSettings().log_InfoTable("import", "allow one mismatch in tag", "this option is skipped, non-unique (after one mismathc) tags");

            } catch (BioException ex) {
                Logger.getLogger(TAG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return tag;

    }

    public void Destroy() {
        this.primerF = null;
        this.sequence = null;
        this.tagSequence = null;
    }
}
