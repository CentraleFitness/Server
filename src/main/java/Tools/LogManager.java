package Tools;

import java.io.Console;
import java.io.PrintWriter;
import java.util.Date;

public class LogManager {

    private PrintWriter mLog;

    private static  LogManager INSTANCE = new LogManager();
    public static LogManager getINSTANCE() {return INSTANCE;}
    private LogManager() {
        try {
            mLog = new PrintWriter(new Date().toString(), "UTF-8");
        } catch (Exception e) {
            System.err.println("LogManager: Could not open file");
        }
    }

    public void write(Object writer, String message) {
        mLog.write("\nWHEN:" + new Date().toString() + "\nWHERE " + writer.getClass().getCanonicalName() + "\nWHAT" + message);
    }
}
