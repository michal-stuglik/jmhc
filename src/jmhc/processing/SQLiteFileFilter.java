/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmhc.processing;

import java.io.File;
import jmhc.objects.DBSettings;

/**
 *
 * @author Michal Stuglik
 */
public class SQLiteFileFilter   extends javax.swing.filechooser.FileFilter
  implements java.io.FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String name = f.getName().toLowerCase();
        return name.endsWith("sqlite3") || name.endsWith(DBSettings.dataBase_extension);
    }//end accept

    @Override
    public String getDescription() {
           return "SQLite File (*.sqlite3, *.sqlite)";
    }
}