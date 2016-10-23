/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmhc.processing;

import java.io.File;

/**
 *
 * @author Michal Stuglik
 */
public class FastFileFilter  extends javax.swing.filechooser.FileFilter
  implements java.io.FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String name = f.getName().toLowerCase();
        return name.endsWith("fasta") || name.endsWith("fas")|| name.endsWith("fna");
    }//end accept

    @Override
    public String getDescription() {
           return "FASTA Files (*.fasta, *.fas, *.fna)";
    }
}

