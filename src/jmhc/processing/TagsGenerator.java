package jmhc.processing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Stuglik
 */
public class TagsGenerator {

    //private String word = "";
    private List<String> wordList;

    public TagsGenerator() {
        wordList = new ArrayList<String>();
    }

    private void addWord(String word, int length) {
        if (word.length() == length) {
            if (!wordList.contains(word)) {
                wordList.add(word);
            }
        }
    }

    public List<String> getGenericTags(int length, String letters) throws Exception {
        try {

            char[] charSet = letters.toCharArray();
            this.generate(length, charSet, "");

        } catch (Exception e) {
            throw e;
        }
        return wordList;
    }

    private void generate(int length, char[] charSet, String word) {

        String w = word;

        if (w.length() == length - 1) {

            for (int i = 0; i < charSet.length; i++) {
                char c = charSet[i];
                w += c;
                this.addWord(w, length);
                w = w.substring(0, w.length() - 1);
            }
        } else {
            for (int i = 0; i < charSet.length; i++) {
                char c = charSet[i];
                w += c;
                this.addWord(w, length);

                this.generate(length, charSet, w);
                w = w.substring(0, w.length() - 1);
            }
        }
    }
}
