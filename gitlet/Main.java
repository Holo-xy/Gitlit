package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Holo-xy
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                gitlet.Repository.init();
                break;
            case "add":
                gitlet.Repository.add(args[1]);
                break;
            case "commit":
                gitlet.Repository.commit(args[1]);
                break;
            case "rm":
                gitlet.Repository.rm(args[1]);
                break;
        }
    }
}
