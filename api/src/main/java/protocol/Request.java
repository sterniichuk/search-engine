package protocol;

public enum Request {
    BUILD, SEARCH, KILL;

    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int CREATED = 201;
}
