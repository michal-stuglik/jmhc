package jmhc.objects;

import info.VersionInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import jmhc.tools.SQLite;
import jmhc.tools.SeqUtils;

/**
 *
 * @author Michal Stuglik
 */
public class DBSettings {

    private SQLite mSQLite;
    //extract table
    public static String extr_alterSequenceField = "altSeq";
    public static String extr_allelName = "allelName";
    public static String extr_sequenceName = "name";
    public static String extr_sequenceField = "seq";
    public static String extr_fileField = "file";
    public static String extr_tableName = "extract";
    public static String extr_tagFField = "tagF";
    public static String extr_tagRField = "tagR";
    public static String extr_numField = "num";
    public static String extr_idField = "objectid";
    public static String extr_tagField = "tag";
    //allel table
    public static String allel_tableName = "allels";
    public static String allel_idField = "objectid";
    public static String allel_header = "header";
    public static String allel_sequence = "seq";
    public static String allel_length = "length";
    public static String dataBase_extension = ".sqlite";
    //info table
    public static String info_tableName = "info";
    public static String info_activity = "activity";
    public static String info_dateTime = "dateTime";
    public static String info_description = "description";
    public static String info_sysinfo = "sysinfo";
    public static String info_importinfo = "importinfo";
    //tags table:
    public static String tags_tableName = "tags";
    public static String tags_idField = "id";
    public static String tags_tag = "tag";
    public static String tags_file = "file";
    public static String tags_species = "species";
    //libero:
    public static String seqNumber = "seqNumber";
    public static String fastaExtesion = ".fas";
    public static String outputExtesion = ".txt";
    //exceptions
    public static String exc_connectToDataBase = "No connection, try to connect to database";
    public static String exc_pleaseSet = "Please set: ";
    //logger:
    public static String loggerProgram = "programLogger";
    public static String loggerDataBase = "baseLogger";
    //message:
    public static String mess_NotEmptyTable = "Table is not empty, Do you want to continue";
    public static String mess_ValueMustBeInteger = "Value must be integer";
    public static String mess_ImportInfo = "Import";

    public SQLite getMSQLite() {
        return mSQLite;
    }

    public void setMSQLite(SQLite mSQLite) {
        this.mSQLite = mSQLite;
    }

    public DBSettings(String dbPath) throws SQLException {
        mSQLite = new SQLite(dbPath);
    }

    public DBSettings() throws SQLException {
    }

    public void PRAGMA_settings_ON_Start() throws SQLException, ClassNotFoundException {
        
        String create_sql = "PRAGMA synchronous = OFF";
        getMSQLite().Execute(create_sql);
       
        create_sql = "PRAGMA main.page_size = 4096";
        getMSQLite().Execute(create_sql);
        
        create_sql = "PRAGMA main.cache_size=10000";
        getMSQLite().Execute(create_sql);
        /**/
        
        create_sql = "PRAGMA main.journal_mode=WAL";
        //create_sql = "PRAGMA main.journal_mode=OFF";
        getMSQLite().Execute(create_sql);
        
        create_sql = "PRAGMA main.locking_mode=EXCLUSIVE";
        //create_sql = "PRAGMA main.locking_mode=NORMAL";
        getMSQLite().Execute(create_sql);
        
        //??
        create_sql = "PRAGMA main.temp_store = MEMORY";
        getMSQLite().Execute(create_sql);
        
    }

    public void recreate_ExtractTable() throws SQLException, ClassNotFoundException {

        String drop_sql = "DROP TABLE  IF EXISTS " + DBSettings.extr_tableName;
        Boolean Execute = getMSQLite().Execute(drop_sql);

        String create_sql = "CREATE TABLE " + DBSettings.extr_tableName
                + " ("
                + DBSettings.extr_idField + " INTEGER PRIMARY KEY, "
                + DBSettings.extr_tagFField + " TEXT, "
                + DBSettings.extr_tagRField + " TEXT, "
                + DBSettings.extr_tagField + " TEXT, "
                + DBSettings.extr_sequenceName + " TEXT, "
                + DBSettings.extr_allelName + " TEXT, "
                + DBSettings.extr_sequenceField + " TEXT, "
                + DBSettings.extr_numField + " INTEGER, "
                + DBSettings.extr_fileField + " TEXT, "
                + DBSettings.extr_alterSequenceField + " TEXT"
                + ")";

        getMSQLite().Execute(create_sql);
    }

    public void recreate_AllelTable() throws SQLException, ClassNotFoundException {

        String drop_sql = "DROP TABLE  IF EXISTS " + DBSettings.allel_tableName;
        getMSQLite().Execute(drop_sql);

        String create_sql = "CREATE TABLE " + DBSettings.allel_tableName
                + " ("
                + DBSettings.allel_idField + " INTEGER PRIMARY KEY, "
                + DBSettings.allel_header + " TEXT, "
                + DBSettings.allel_sequence + " TEXT, "
                + DBSettings.allel_length + " INTEGER "
                + ")";

        getMSQLite().Execute(create_sql);
    }

    public void recreate_InfoTable() throws SQLException, ClassNotFoundException {

        String drop_sql = "DROP TABLE  IF EXISTS " + DBSettings.info_tableName;
        getMSQLite().Execute(drop_sql);

        String create_sql = "CREATE TABLE " + DBSettings.info_tableName
                + " ("
                + DBSettings.info_activity + " TEXT, "
                + DBSettings.info_dateTime + " TEXT, "
                + DBSettings.info_description + " TEXT, "
                + DBSettings.info_sysinfo + " TEXT, "
                + DBSettings.info_importinfo + " TEXT "
                + ")";

        getMSQLite().Execute(create_sql);
    }

    public void recreate_TagsTable() throws SQLException, ClassNotFoundException {

        String drop_sql = "DROP TABLE  IF EXISTS " + DBSettings.tags_tableName;
        getMSQLite().Execute(drop_sql);

        String create_sql = "CREATE TABLE " + DBSettings.tags_tableName
                + " ("
                + DBSettings.tags_idField + " INTEGER PRIMARY KEY, "
                + DBSettings.tags_tag + " TEXT, "
                + DBSettings.tags_file + " TEXT, "
                + DBSettings.tags_species + " TEXT "
                + ")";

        getMSQLite().Execute(create_sql);
    }

    public void check_ExistingData(String tableName, JInternalFrame frame) throws Exception {

        ResultSet rs = null;
        Boolean existsData = false;
        try {

            String sql = "SELECT * FROM " + tableName;
            rs = this.getMSQLite().ExecuteQuery(sql);

            while (rs.next()) {
                existsData = true;
                break;
            }

            if (rs != null) {
                rs.close();
            }

            if (existsData) {
                Object[] options = {"Add data",
                    "Overwrite data",
                    "Cancel"};
                int n = JOptionPane.showOptionDialog(frame,
                        "Data exists in table. What to do?",
                        "Warning",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]);

                switch (n) {
                    case 0:
                        break;
                    case 1:
                        this.delete_Records(tableName);
                        break;
                    case 2:
                        throw new Exception("Process canceled");
                    default:
                        break;
                }
            }

        } catch (SQLException ex) {
            //Logger.getLogger(DBSettings.loggerDataBase).log(Level.SEVERE, null, ex);
            //JOptionPane.showMessageDialog(frame, ex.getMessage(), frame.getName(), JOptionPane.ERROR_MESSAGE);
            throw ex;
        } catch (Exception ex) {
            throw ex;
        } finally {
            rs = null;
        }
    }

    public boolean check_ExistingData(String tableName) throws Exception {

        ResultSet rs = null;
        Boolean existsData = false;

        try {

            String sql = "SELECT * FROM " + tableName;
            rs = this.getMSQLite().ExecuteQuery(sql);

            while (rs.next()) {
                existsData = true;
                break;
            }

            if (rs != null) {
                rs.close();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" /*+ e.getMessage()*/);
        } catch (Exception ex) {
            throw ex;
        } finally {
            rs = null;
        }
        return existsData;
    }

    public void delete_Records(String tableName) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM " + tableName;
        Boolean del = this.getMSQLite().Execute(sql);
    }

    public void log_InfoTable(String activity, String description, String importInfo) throws SQLException, ClassNotFoundException {

        VersionInfo version = new VersionInfo();

        //log into table:
        String sql = "Insert into " + DBSettings.info_tableName
                + " ("
                + DBSettings.info_activity + ", "
                + DBSettings.info_dateTime + ", "
                + DBSettings.info_description + ", "
                + DBSettings.info_importinfo + ", "
                + DBSettings.info_sysinfo
                + ") values ('"
                + activity + "','"
                + SeqUtils.getDateTime() + "','"
                + description + "','"
                + importInfo + "','"
                + "v." + version.getVersion() + " " + version.getDate()
                + "')";

        this.getMSQLite().Execute(sql);
    }
}
