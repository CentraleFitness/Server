package Tools;

import java.io.Console;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private static PrintWriter mLog;
    private static boolean mEnabled = false;
    private static  LogManager INSTANCE = new LogManager();

    private LogManager() {
    }

    private static void logFile() {
        if (mEnabled == true) {
            try {

                File theDir = new File("Log");
                if (!theDir.exists()) theDir.mkdir();
                mLog = new PrintWriter("Log/" + new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date().getTime()) + ".log", "UTF-8");
            } catch (Exception e) {
                System.err.println("LogManager: Could not open file");
            }
        } else {
            mLog.close();
            mLog = null;
        }
    }

    public static void enable() {mEnabled = true; logFile();}
    public static void disable() {mEnabled = false; logFile();}

    public static void write(String message) {
        if (mEnabled == false) return;
        mLog.println("WHEN: " + new Date().toString() + " :: WHERE: " + getCallerCallerClassName() + " :: WHAT: " + message);
        mLog.flush();
    }

    public static void write(Exception e) {
        if (mEnabled == false) return;
        System.out.println("WHEN: " + new Date().toString() + " :: WHERE: " + getCallerCallerClassName() + " :: WHAT: " + "Exception (line " + e.getStackTrace()[0].getLineNumber() + "): " + e.toString());
        mLog.println("WHEN: " + new Date().toString() + " :: WHERE: " + getCallerCallerClassName() + " :: WHAT: " + "Exception (line " + e.getStackTrace()[0].getLineNumber() + "): " + e.toString());
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
                    return ste.getClassName() + ":" + ste.getMethodName() + "()";
                }
            }
        }
        return null;
    }
}
