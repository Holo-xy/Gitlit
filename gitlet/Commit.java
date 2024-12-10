package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.readObject;

/** Represents a gitlet commit object.
 *
 *  @author Holo-xy
 */
public class Commit implements Serializable {

    /** The message of this Commit. */
    private String message;
    private Date date;
    private Commit parent;

    public Map<String, String> children = new HashMap<String, String>();

    public Commit() {
        this.message = "initial commit";
        this.date = new Date(0);
        this.parent = null;
    }


    public Commit(Commit parent, String message) {
        this.message = message;
        this.date = new Date();
        this.parent = parent;
    }

    public void cloneChildren(Commit parent) {
        this.parent = parent;
        this.children = new HashMap<>(parent.children);
    }

    public void updateChildren(){
        Map<String, String> map = readObject(Repository.STAGING_AREA_FILE, HashMap.class);
        for(var entry: map.entrySet()) {
            children.put(entry.getKey(), entry.getValue());
        }
    }

    public static Commit readCommit(String hash) {
        File file = Utils.join(Repository.OBJECTS_DIR, hash);
        if (!file.exists()) {
            throw new IllegalArgumentException("Commit with hash " + hash + " does not exist.");
        }
        return Utils.readObject(file, Commit.class);
    }

    public void saveCommit(String name) {
        File file = Utils.join(Repository.OBJECTS_DIR, name);
        Utils.writeObject(file, this);
    }

}
