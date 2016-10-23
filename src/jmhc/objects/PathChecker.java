package jmhc.objects;

/**
 *
 * @author Michal Stuglik
 */
public class PathChecker {

    private String path;

    public String getPath() {
        return path;
    }

    public PathChecker(String path) {
        this.path = path;
    }
    private String message = "";

    public String getMessage() {
        return message;
    }

    public boolean Check() {

        boolean result = true;
        this.message = "OK";

        if (path == null || path.isEmpty()) {
            this.message = "Empty path";
            return false;
        }

        if (path.length() > 259) {
            this.message = "Path cannot contain more than 259 chars";
            return false;
        }

        if (path.trim().contains(" ")) {
            this.message = "Path cannot contain space character";
            return false;
        }

        return result;
    }
}
