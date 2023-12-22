package social.nickrest.npm;

public class DefualtNPMLogger implements NPMLogger {

    @Override
    public void info(String message) {
        System.out.println("[INFO] " + message);
    }

    @Override
    public void warn(String message) {
        System.out.println("[WARNING] " + message);
    }

    @Override
    public void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }

    @Override
    public void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

}
