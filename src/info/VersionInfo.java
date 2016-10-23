
package info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michal Stuglik
 */
public class VersionInfo {

    private static String major = "1";
    private static String minor = "5";
    private String version = "";
    private String date = "";

    @SuppressWarnings("static-access")
    public String getVersion() {
        return this.major + "." + this.minor + "." + this.version;
    }

    public String getDate() {
        return date;
    }

    public VersionInfo() {
        this.ReadVersion();
    }

    private void ReadVersion() {

        String r = "RevisionInfo.txt";
        InputStream mInputStream = getClass().getResourceAsStream(r);

        if (mInputStream != null) {
            String line;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(mInputStream, "UTF-8"));

                while ((line = reader.readLine()) != null) {
                    String[] kv = line.split(" = ");
                    if (kv.length == 2) {
                        String key = kv[0];
                        String value = kv[1];
                        if (key.equals("SvnRevision")) {
                            this.version = value;
                        } else if (key.equals("SvnDate")) {
                            this.date = value;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(VersionInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(VersionInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    mInputStream.close();
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(VersionInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
