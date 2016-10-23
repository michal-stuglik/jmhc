/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmhc.objects;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmhc.forms.ProgramControler;

/**
 *
 * @author Michal Stuglik
 */
public class TAGvariants {

    private List nucleotides = new ArrayList();
    public List<String> TagList = null;
    public HashMap TagVariants = null;
    private ProgramControler mProgramControler = null;

    private void setNuceotideList() {
        nucleotides.add('A');
        nucleotides.add('C');
        nucleotides.add('T');
        nucleotides.add('G');
    }

    public TAGvariants(String tag, ProgramControler mProgramControler) {
        this.setNuceotideList();
        this.mProgramControler = mProgramControler;

        this.TagList = new ArrayList<String>(1);
        this.TagList.add(tag);

        this.TagVariants = getTagVariants();
    }

    public TAGvariants(List<String> tags) {
        this.setNuceotideList();

        this.TagList = tags;
        this.TagVariants = getTagVariants();
    }

    private HashMap getTagVariants() {

        HashMap ht = new HashMap();

        for (int k = 0; k < TagList.size(); k++) {

            String Tag = TagList.get(k);

            if (Tag != null && !Tag.isEmpty()) {

                char[] tag_array = Tag.toCharArray();

                for (int i = 0; i < tag_array.length; i++) {
                    char original_nulceotide = tag_array[i];

                    for (int j = 0; j < nucleotides.size(); j++) {
                        char n = nucleotides.get(j).toString().charAt(0);

                        if (n == original_nulceotide) {
                            continue;
                        }

                        char[] mutated_array = new char[tag_array.length];
                        System.arraycopy(tag_array, 0, mutated_array, 0, tag_array.length);

                        mutated_array[i] = n;
                        String mutated_tag = new String(mutated_array);

                        if (!ht.containsKey(mutated_tag)) {
                            ht.put(mutated_tag, Tag);
                        } else {
                            System.out.print(mutated_tag);
                        }
                    }
                }
            }
        }

        return ht;
    }

    public HashMap fillHashMap(HashMap<String, String> hm) {

        HashMap<String, String> m1 = this.TagVariants;

        for (String key : m1.keySet()) {

            if (!hm.containsKey(key)) {
                hm.put(key, m1.get(key));

            } else { //this key is already in db!

                System.out.println("Old Key: " + key + ", Value: " + hm.get(key));
                System.out.println("Old Key: " + key + ", Value: " + m1.get(key));

                try {
                    //TODO: warning in logs
                    mProgramControler.getDBSettings().log_InfoTable("import", "option: allow one mismatch in tag", "skipped, non-unique tags");
                } catch (SQLException ex) {
                    Logger.getLogger(TAGvariants.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TAGvariants.class.getName()).log(Level.SEVERE, null, ex);
                }
                hm = new HashMap<String, String>();
            }
        }
        return hm;
    }
}
