package jmhc.forms;

import jmhc.objects.DBSettings;
import jmhc.objects.OSenum;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Michal Stuglik
 */
public class ProgramControler {

    protected DBSettings DBSettings;

    public DBSettings getDBSettings() {
        return DBSettings;
    }

    public void setDBSettings(DBSettings DBSettings) {
        this.DBSettings = DBSettings;
    }
    private OSenum operatingSystem = OSenum.Windows;

    public OSenum getOperatingSystem() {
        if (SystemUtils.IS_OS_WINDOWS && SystemUtils.OS_ARCH.equals("x86")) {
            operatingSystem = operatingSystem.Win_x86;
        } else if (SystemUtils.IS_OS_WINDOWS && SystemUtils.OS_ARCH.equals("amd64")) {//need to be tested!
            operatingSystem = operatingSystem.Win_x86_64;
        } else if (SystemUtils.IS_OS_LINUX && SystemUtils.OS_ARCH.equals("x86")) {
            operatingSystem = operatingSystem.Linux_x86;
        } else if (SystemUtils.IS_OS_LINUX && SystemUtils.OS_ARCH.equals("amd64")) {
            operatingSystem = operatingSystem.Linux_x86_64;
        } else {
            operatingSystem = operatingSystem.NotSupportedOS;
        }
        return operatingSystem;
    }

    public OSenum getOperatingSystem2() {
        if (SystemUtils.IS_OS_WINDOWS) {
            operatingSystem = operatingSystem.Windows;
        } else if (SystemUtils.IS_OS_LINUX) {
            operatingSystem = operatingSystem.Linux;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            operatingSystem = operatingSystem.MacOS;
        } else {
            operatingSystem = operatingSystem.NotSupportedOS;
        }
        return operatingSystem;
    }
}
