package poormanshttpserver;

public class PathEntry {
    private final String method;
    private final String path;

    public PathEntry(String method, String path) {
        this.method = method;
        this.path = path;
    }


    public boolean equals(Object otherObject) {
        if (otherObject == this) {
            return true;
        }

        if (!(otherObject instanceof PathEntry)) {
            return false;
        }

        PathEntry otherPathEntry = (PathEntry) otherObject;
        return this.method.equals(otherPathEntry.method) && this.path.equals(otherPathEntry.path);
    }

    public int hashCode() {
        return this.method.hashCode() * this.path.hashCode();
    }
}
