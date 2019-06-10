package com.iclr.storage.logging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Edward on 10/06/2019.
 */
public class Logger {
    private static Logger instance;
    public static Logger getInstance(){
        if(instance == null){
            instance = new Logger();
        }
        return instance;
    }

    public static class LogEntry {
        private String line;
        private long time;
        private Severity severity;

        public static enum Severity {
            SEVERE(4,"log_severe"),
            HIGH(3,"log_high"),
            NORMAL(2,"log_normal"),
            LOW(1,"log_low"),
            DEBUG(0,"log_debug");

            private int i = -1;
            private String styleClass;
            private Severity(int i, String styleClass){
                this.i = i;
                this.styleClass = styleClass;
            }

            public String getStyleClass(){
                return this.styleClass;
            }

            public boolean isAtLeastAsSevereAs(Severity s){
                return this.i >= s.i;
            }
        }

        public LogEntry(String line){
            this(Severity.NORMAL, line);
        }

        public LogEntry(Severity severity, String line){
            this(severity, line, System.currentTimeMillis());
        }

        public LogEntry(Severity severity, String line, long time){
            this.severity = severity;
            this.line = line;
            this.time = time;
        }

        public String getLine() {
            return line;
        }

        public long getTime() {
            return time;
        }

        public Severity getSeverity() {
            return severity;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss.SSS");
            sb.append("["+sdf.format(new Date(this.getTime()))+"|"+getSeverity().name().toUpperCase()+"]: ");
            sb.append(this.getLine());
            return sb.toString();
        }
    }

    public static interface LogListener {
        public void onLogClear();
        public void onLogEntryAdd(LogEntry line);
        public void onAttach(List<LogEntry> previouslyLogged);
    }

    private List<LogEntry> lines = new ArrayList<>();
    private List<LogListener> logListeners = new ArrayList<>();

    private Logger(){

    }

    public void attachListener(LogListener listener){
        this.logListeners.add(listener);
        listener.onAttach(this.getLines());
    }

    public void clearLines(){
        this.lines.clear();
        for (LogListener ll:logListeners){
            ll.onLogClear();
        }
    }

    public List<LogEntry> getLines(){
        return new ArrayList<>(this.lines);
    }

    public List<LogEntry> getLines(LogEntry.Severity severity){
        List<LogEntry> toReturn = new ArrayList<>();
        for(LogEntry line:getLines()) {
            if(line.getSeverity().isAtLeastAsSevereAs(severity)){
                toReturn.add(line);
            }
        }
        return toReturn;
    }

    public static void error(String str){
        println(LogEntry.Severity.SEVERE, "Error: "+str);
    }

    public static void debug(String str){
        Logger.getInstance().addEntry(new LogEntry(LogEntry.Severity.DEBUG, str, System.currentTimeMillis()));
    }

    public static void println(String str){
        Logger.println(LogEntry.Severity.NORMAL, str);
    }

    public static void println(LogEntry.Severity severity, String str){
        Logger.getInstance().addEntry(new LogEntry(severity, str, System.currentTimeMillis()));
    }

    public void addEntry(LogEntry entry){
        this.lines.add(entry);
        for (LogListener ll:logListeners){
            ll.onLogEntryAdd(entry);
        }
    }
}
