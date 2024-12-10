package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static gitlet.Utils.*;
import static gitlet.Utils.writeObject;

/** Represents a gitlet repository.
 *
 *  @author Holo-xy
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The Gitlit directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The current selected branch. */
    private static final File BRANCH_HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** The directory used for storing files staged for the next commit. */
    public static final File STAGING_AREA_FILE = join(GITLET_DIR, "staging");

    /** The directory containing all committed objects. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /** The directory for storing references to branches. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    /** The folder containing files that represent branch heads. */
    public static final File BRANCH_HEADS_DIR = join(REFS_DIR, "heads");

    /** The file representing the HEAD pointer for the current branch. */
    public static File HEAD_FILE = join(BRANCH_HEADS_DIR, "master");




    public static void init(){
        createDirectoryIfNotExists(GITLET_DIR);
        createDirectoryIfNotExists(OBJECTS_DIR);
        createDirectoryIfNotExists(REFS_DIR);
        createDirectoryIfNotExists(BRANCH_HEADS_DIR);

        Commit initialCommit = new Commit();
        String commitSha1 = sha1(serialize(initialCommit));
        writeObject(join(OBJECTS_DIR, commitSha1), initialCommit);

        createFileWithContents(BRANCH_HEAD_FILE, "refs/heads/master");
        createFileWithContents(HEAD_FILE, commitSha1);
    }


    public static void add(String fileName) {
        try {
            if (!STAGING_AREA_FILE.exists()) {
                STAGING_AREA_FILE.createNewFile();
                writeObject(STAGING_AREA_FILE, new HashMap<String, String>());
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the staging file: " + e.getMessage());
        }

        // Read file contents and compute its SHA-1 hash
        byte[] fileContent = readContents(join(CWD, fileName));
        String fileSha1 = sha1(fileContent);

        // Check if the file exists in a commit
        Commit commit = Commit.readCommit(readContentsAsString(HEAD_FILE));
        if (Objects.equals(commit.children.get(fileName), fileSha1)) {
            System.out.println(fileName + " already exists in the current commit.");
            return;
        };

        // Update the staging area
        Map<String, String> map = readObject(STAGING_AREA_FILE, HashMap.class);
        map.put(fileName, fileSha1);
        writeObject(STAGING_AREA_FILE, (Serializable) map);

        // Save the file
        File stagedFile = join(OBJECTS_DIR, fileSha1);
        createFileWithObject(stagedFile, fileContent);
    }

    public static void commit(String message){
        if(!STAGING_AREA_FILE.exists() || isStagingAreaEmpty()) {
            System.out.println("nothing to commit (create/copy files and use \"git add\" to track)");
            return;
        }

        if(Objects.equals(message, "")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        // Read parent commit from HEAD
        String parentHash = readContentsAsString(HEAD_FILE);
        Commit parent = Commit.readCommit(parentHash);

        // Create a new commit with the current staging area
        Commit commit = new Commit(parent, message);
        commit.cloneChildren(parent);
        commit.updateChildren();

        // Serialize and store the new commit
        String commitSha1 = sha1(serialize(commit));
        createFileWithObject(join(OBJECTS_DIR, commitSha1), commit);

        // Update HEAD
        writeContents(HEAD_FILE, commitSha1);

        // Clear the staging area
        writeObject(STAGING_AREA_FILE, new HashMap<String, String>());

        System.out.println("Committed as " + commitSha1);
    }

    private static void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private static void createFileWithContents(File file, Object... contents) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                writeContents(file, contents);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating file " + file.getPath() + ": " + e.getMessage());
        }
    }

    private static void createFileWithObject(File file, Object object) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                writeObject(file, (Serializable) object);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating file " + file.getPath() + ": " + e.getMessage());
        }
    }

    private static boolean isStagingAreaEmpty() {
        Map<String, String> stagingArea = readObject(STAGING_AREA_FILE, HashMap.class);
        return stagingArea.isEmpty();
    }

}
