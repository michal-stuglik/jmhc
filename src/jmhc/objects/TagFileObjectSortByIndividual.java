/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmhc.objects;

import java.util.Comparator;

/**
 *
 * @author Michal Stuglik
 */
public class TagFileObjectSortByIndividual implements Comparator<TagFileObject>{

    @Override
    public int compare(TagFileObject o1, TagFileObject o2) {
       return o1.getIndividual().compareTo(o2.getIndividual());
    }

}
