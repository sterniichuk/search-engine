package protocol;

import static java.lang.StringTemplate.STR;

public enum RequestBuilder {
    THREADS, VARIANT, FOLDERS, FOLDER, START,
    SIZE;

    private static final char DELIMITER = ':';

    public String putValue(String value) {
        return STR. "\{ this }\{ DELIMITER }\{ value }" ;
    }

    public String putValue(int value) {
        return STR. "\{ this }\{ DELIMITER }\{ value }" ;
    }

    public int getInt(String request) {
        return Integer.parseInt(getString(request));
    }

    public String getString(String request) {
        return request.replace(STR. "\{ this }\{ DELIMITER }" , "");
    }

    public boolean equalString(String request) {
        return this.toString().equals(request);
    }
}
