package com.iclr.storage.command;

import java.nio.charset.Charset;

/**
 * Created by Edward on 22/03/2019.
 */
public class CommandEncoder {
    public static byte[] encodeCommandListToByteArray(ServoCommand... commands){
        byte[] res = new byte[commands.length * 3]; //Each command takes up 3 bytes
        for(int i=0;i<commands.length;i++){
            byte[] enc = commands[i].encodeCommand();
            int j = i*3;
            res[j] = enc[0];
            res[j+1] = enc[1];
            res[j+2] = enc[2];
        }
        return res;
    }
}
