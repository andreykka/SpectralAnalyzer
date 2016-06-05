package medicine.com.spectralanalyzer.pojo;

import java.io.File;

public class ItemData {

    private int tag;
    private File file;
    private String filename;

    public ItemData(int tag, File file, String filename) {
        this.tag = tag;
        this.file = file;
        this.filename = filename;
    }

    public ItemData(int tag, File file) {
        this(tag, file, file.getName());
    }

    public int getTag() {
        return tag;
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemData itemData = (ItemData) o;

        return !(file != null ? !file.equals(itemData.file) : itemData.file != null);

    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }
}
