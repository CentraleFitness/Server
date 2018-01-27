package Tools;

import sun.reflect.Reflection;

import java.io.Console;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private static PrintWriter mLog;

    private static  LogManager INSTANCE = new LogManager();
    private LogManager() {
        try {
            mLog = new PrintWriter("server_" + new Date().getTime() + ".log", "UTF-8");
        } catch (Exception e) {
            System.err.println("LogManager: Could not open file");
        }
    }

    public static void write(String message) {
        mLog.println("WHEN: " + new Date().toString() + " :: WHERE: " + getCallerCallerClassName() + " :: WHAT: " + message);
        mLog.flush();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mLog.close();
    }

    private static class KDebug {
        public static String getCallerClassName() {
            StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
            for (int i=1; i<stElements.length; i++) {
                StackTraceElement ste = stElements[i];
                if (!ste.getClassName().equals(KDebug.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                    return ste.getClassName();
                }
            }
            return null;
        }
    }

    private static String getCallerCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(KDebug.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
                if (callerClassName==null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    return ste.getClassName();
                }
            }
        }
        return null;
    }
}
