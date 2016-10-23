package jmhc.processing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import jmhc.forms.ProgramControler;
import jmhc.objects.DBSettings;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class AllelsTableInfo {

    private ProgramControler mProgramControler;
    private JLabel jLabel_statusLabel;

    public AllelsTableInfo(ProgramControler mProgramControler, JLabel jLabel_statusLabel) {
        this.mProgramControler = mProgramControler;
        this.jLabel_statusLabel = jLabel_statusLabel;
    }

    private HashMap<String, String> AllelsExtract() throws SQLException, Exception {

        HashMap<String, String> allels = null;
        ResultSet rs = null;

        try {
            allels = new HashMap<String, String>();

            //first indexing
            boolean idx_bool = SeqUtils.dbIndexService(mProgramControler, "idx_allel_seq", DBSettings.allel_tableName, DBSettings.allel_sequence);

            //SELECT header , seq from allels group by seq
            String sql = "Select " + DBSettings.allel_header + ","
                    + DBSettings.allel_sequence
                    + " from " + DBSettings.allel_tableName
                    + " group by " + DBSettings.allel_sequence;
            try {
                rs = mProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
                while (rs.next()) {
                    if (!allels.containsKey(rs.getString(2))) {
                        allels.put(rs.getString(2), rs.getString(1));
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        } finally {
            rs = null;
        }
        return allels;
    }

    public void UpdateAllelInfoInExtractTable() throws SQLException, Exception {
        UpdateAllelInfoInExtractTable(0);
    }

    public void UpdateAllelInfoInExtractTable(int min_presence) throws SQLException, Exception {

        ResultSet rs = null;
        HashMap AllelsInDataBase = null;
        List<String> sequences = null;

        try {

            AllelsInDataBase = this.AllelsExtract();

            //clearing allele names
            String sql = "UPDATE " + DBSettings.extr_tableName
                    + " SET " + DBSettings.extr_allelName + " = NULL";
            
            this.mProgramControler.getDBSettings().getMSQLite().Execute(sql);

            /*
            String sql = "SELECT " + DBSettings.extr_sequenceField
            + " FROM " + DBSettings.extr_tableName
            + " GROUP BY " + DBSettings.extr_sequenceField;
             */

            //SELECT * FROM COMPANY GROUP BY name HAVING count(name) > 2;

            sql = "SELECT " + DBSettings.extr_sequenceField
                    + " FROM " + DBSettings.extr_tableName
                    + " GROUP BY " + DBSettings.extr_sequenceField
                    + " HAVING count( " + DBSettings.extr_sequenceField + ") > " + String.valueOf(min_presence);

            try {
                sequences = new ArrayList<String>();
                rs = this.mProgramControler.getDBSettings().getMSQLite().ExecuteQuery(sql);
                while (rs.next()) {
                    sequences.add(rs.getString(1));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            }

            int counter = 0;
            int label_counter = 0;
            int size = sequences.size();
            for (int i = 0; i < sequences.size(); i++) {
                String seq = sequences.get(i);
                label_counter++;

                String name = "";
                if (AllelsInDataBase != null && !AllelsInDataBase.isEmpty() && AllelsInDataBase.containsKey(seq)) {
                    name = String.valueOf(AllelsInDataBase.get(seq));
                } else {
                    counter++;
                    name = "seq" + SeqUtils.GetNextNumberAsString(counter, 5);
                }

                sql = "UPDATE " + DBSettings.extr_tableName
                        + " SET " + DBSettings.extr_allelName + " = '" + name + "'"
                        + " WHERE " + DBSettings.extr_sequenceField + " = '" + seq + "'";

                if (label_counter % 100 == 0) {
                    SeqUtils.SetStatus(jLabel_statusLabel, "Updating alleles name : " + label_counter + " of " + size);
                }
                this.mProgramControler.getDBSettings().getMSQLite().Execute(sql);
            }

        } catch (Exception e) {
            throw e;
        }
    }
}
