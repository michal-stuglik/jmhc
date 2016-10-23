package jmhc.processing;

import java.io.File;

/**
 *
 * @author Michal Stuglik
 */
public class TxtFileFilter extends javax.swing.filechooser.FileFilter
  implements java.io.FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String name = f.getName().toLowerCase();
        return name.endsWith("txt");// || name.endsWith("csv");
    }

    @Override
    public String getDescription() {
        //return "TXT and CSV text Files (*.txt, *.csv)";
         return "TXT Files (*.txt)";
    }
}
