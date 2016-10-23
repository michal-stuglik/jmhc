package jmhc.objects;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.regex.Matcher;
import org.biojava.utils.regex.Pattern;
import org.biojava.utils.regex.PatternFactory;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class SequencePattern {

    private boolean matched = false;

    private void setMatched(boolean matched) {
        this.matched = matched;
    }

    public boolean isMatched() {
        return matched;
    }
    private int start = 0;

    private void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }
    private int end = 0;

    private void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }
    private boolean ambiguity = false;

    private void setAmbiguity(boolean ambiguity) {
        this.ambiguity = ambiguity;
    }

    public boolean isAmbiguity() {
        return ambiguity;
    }
    private Sequence Sequence;

    public Sequence getSequence() {
        return Sequence;
    }
    private SymbolList patternSource;

    private void setSequence(Sequence Sequence) {
        this.Sequence = Sequence;
        this.setSequenceAsString(Sequence.seqString());
    }

    public void setPatternSource(SymbolList patternSource) {
        this.patternSource = patternSource;
    }

    public SymbolList getPatternSource() {
        return patternSource;
    }
    private SymbolList discoveredSequence;

    public SymbolList getDiscoveredSequence() {
        return discoveredSequence;
    }

    private void setDiscoveredSequence(SymbolList discoveredSequence) {
        this.discoveredSequence = discoveredSequence;
    }
    private int howManyMatches = 0;

    public int getHowManyMatches() {
        return howManyMatches;
    }

    private FiniteAlphabet getIUPAC() {
        return DNATools.getDNA();
    }
    private String sequenceAsString;

    public String getSequenceAsString() {
        return sequenceAsString;
    }

    private void setSequenceAsString(String sequenceAsString) {
        this.sequenceAsString = sequenceAsString;
    }
    private Boolean findFirstOne;

    public SequencePattern(Sequence mSequence, SymbolList patternSource, Boolean findFirstOne) throws Exception {

        this.findFirstOne = findFirstOne;
        this.patternSource = patternSource;
        this.setSequence(mSequence);

        this.run();
    }

    public final void run() throws Exception {

        Matcher matcher = null;
        Pattern pattern = null;
        SymbolList searchInSequenceSL = null;
        PatternFactory FACTORY = null;

        try {

            //FiniteAlphabet IUPAC = DNATools.getDNA();
            searchInSequenceSL = DNATools.createDNA(getSequenceAsString());

            // Create pattern using pattern factory.
            FACTORY = PatternFactory.makeFactory(getIUPAC());

            try {
                pattern = FACTORY.compile(patternSource.seqString());
            } catch (Exception e) {
                throw e;

            } // Obtain iterator of matches.
            try {
                matcher = pattern.matcher(searchInSequenceSL);
            } catch (Exception e) {
                throw e;


            } // Foreach match
            while (matcher.find()) {
                this.howManyMatches++;

                //set matched as true:
                this.setMatched(true);

                //start
                this.setStart(matcher.start());

                //end
                this.setEnd(matcher.end() - 1);

                //output sequence
                this.setDiscoveredSequence(Sequence.subList(this.start, this.end));
                System.out.print("SequencePattern > pattern_length:sequence_length:start:end " + patternSource.seqString().length() + ":" + Sequence.seqString().length() + ":" + this.start + ":" + this.end + " pattern_seq : discovered_seq > " + patternSource.seqString() + " : " + this.getDiscoveredSequence().seqString() + "\n");

                //ambiguity check
                this.setAmbiguity(SeqUtils.hasAmbiquity(this.discoveredSequence));

                if (findFirstOne && howManyMatches == 1) {
                    break;
                }
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            matcher = null;
            pattern = null;
            searchInSequenceSL = null;
            FACTORY = null;
        }
    }

    public void Destroy() {
        this.discoveredSequence = null;
        this.Sequence = null;
        this.patternSource = null;
    }
}
