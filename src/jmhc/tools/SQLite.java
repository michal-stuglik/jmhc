package jmhc.tools;

/**
 *
 * @author Michal Stuglik
 */
import java.sql.*;

public class SQLite {

    protected String dbPath;

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public SQLite(String dbPath) {
        this.dbPath = dbPath;
    }
    public Connection connection;

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        if (this.connection == null || this.connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                try {
                    this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                } catch (SQLException ex) {
                    throw ex;
                }
            } catch (ClassNotFoundException ex) {
                throw ex;
            }
            return this.connection;
        } else {
            return this.connection;
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Boolean Execute(String sql) throws SQLException, ClassNotFoundException {
        //return  this.getConnection().createStatement().execute(sql);

        Statement st = this.getConnection().createStatement();
        boolean bool_exe  = st.execute(sql);
        st.close();
        return bool_exe;
    }

    public ResultSet ExecuteQuery(String sql) throws SQLException, ClassNotFoundException {
        return this.getConnection().createStatement().executeQuery(sql);
    }

    public void Close() throws SQLException {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            throw ex;
        }
    }
}
