package jmhc.objects;

/**
 *
 * @author Michal Stuglik
 */
public class TagFileObject {

    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
    private String individual = "";

    public String getIndividual() {
        if (individual == null) {
            individual = "";
        }
        return individual;
    }

    public void setIndividual(String individual) {
        this.individual = individual;
    }

    public TagFileObject(String tag, String file) {
        this.tag = tag;
        this.file = file;
    }
    private String tagNamefileName;

    public String getTagNamefileName() {
        if (tagNamefileName == null || (tagNamefileName == null ? "" == null : tagNamefileName.equals(""))) {
            tagNamefileName = this.getTag() + "___" + this.getFile();
        }
        return tagNamefileName;
    }
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TagFileObject other = (TagFileObject) obj;
        if ((this.getTagNamefileName() == null) ? (other.getTagNamefileName() != null) : !this.getTagNamefileName().equals(other.getTagNamefileName())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.tagNamefileName != null ? this.tagNamefileName.hashCode() : 0);
        return hash;
    }

    public void Destroy() {
        this.file = null;
        this.individual = null;
        this.tag = null;
        this.tagNamefileName = null;
    }
}
