package social.nickrest.npm;

public interface NPMLogger {

    void info(String message);
    void warn(String message);
    void error(String message);
    void debug(String message);

    default void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }

}
