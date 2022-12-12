package utils;

public class Logger {
    
    private static Logger instance;
    
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        
        return instance;
    }
    
    public enum LogLevel {
        DEBUG, TRACE, ERROR
    }
    
    private final LogLevel logLevel = LogLevel.DEBUG;
    
    public void error(String string) {
        System.err.println(string);
    }
    
    public void trace(String string) {
        if (logLevel == LogLevel.TRACE || logLevel == LogLevel.DEBUG) {
            System.out.printf("[TRACE] %s\n", string);
        }
    }
    
    public void debug(String string) {
        if (logLevel == LogLevel.DEBUG) {
            System.out.println(string);
        }
    }
}
