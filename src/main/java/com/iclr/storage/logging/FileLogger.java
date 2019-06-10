package com.iclr.storage.logging;

import java.io.*;
import java.util.List;

/**
 * Created by Edward on 10/06/2019.
 */
public class FileLogger implements Logger.LogListener {
    private PrintWriter out;
    public FileLogger(File f) throws IOException {
        if(!f.exists()){
            f.createNewFile();
        }
        FileWriter fw = new FileWriter(f, true);
        BufferedWriter bw = new BufferedWriter(fw);
        out = new PrintWriter(bw);
    }

    public void close(){
        this.out.flush();
        this.out.close();
        this.out = null;
    }

    @Override
    public void onLogClear() {
    }

    @Override
    public void onLogEntryAdd(Logger.LogEntry line) {
        if(out != null){
            out.write(line.toString()+"\r\n");
            out.flush();
        }
    }

    @Override
    public void onAttach(List<Logger.LogEntry> previouslyLogged) {
        if(out != null){
            for(Logger.LogEntry le:previouslyLogged) {
                out.write(le.toString()+"\r\n");
                out.flush();
            }
        }
    }
}
