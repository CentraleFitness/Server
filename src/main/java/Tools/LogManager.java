package Tools;

import java.io.Console;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private PrintWriter mLog;

    private static  LogManager INSTANCE = new LogManager();
    public static LogManager getINSTANCE() {return INSTANCE;}
    private LogManager() {
        try {
            mLog = new PrintWriter("server_" + new Date().getTime() + ".log", "UTF-8");
        } catch (Exception e) {
            System.err.println("LogManager: Could not open file");
        }
    }

    public void write(Object writer, String message) {
        mLog.println("WHEN: " + new Date().toString() + " :: WHERE: " + writer.getClass().getCanonicalName() + " :: WHAT: " + message);
        mLog.flush();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mLog.close();
    }
}
