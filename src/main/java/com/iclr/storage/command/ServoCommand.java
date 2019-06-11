package com.iclr.storage.command;

import com.iclr.storage.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Edward on 22/03/2019.
 */
public abstract class ServoCommand<T> {
    private static Map<String,Byte> commandMap = new HashMap<String,Byte>();
    private static Map<Byte,String> inverseCommandMap = new HashMap<Byte,String>();
    private static Map<String,ServoCommandInterpreter> commandInterpreterMap = new HashMap<>();

    protected int servonum;
    protected T commandArg;

    public static String getCommandLabel(byte commandID){
        return inverseCommandMap.get((Byte)commandID);
    }

    public static byte getCommandID(String commandLabel){
        return commandMap.get(commandLabel);
    }

    public static boolean commandExists(byte commandID){
        return inverseCommandMap.containsKey(commandID);
    }

    public static boolean commandExists(String commandLabel){
        return commandMap.containsKey(commandLabel);
    }

    public static ServoCommandInterpreter getCommandInterpreter(String commandLabel){
        return commandInterpreterMap.get(commandLabel);
    }

    public ServoCommand(ServoCommandInterpreter<T> commandInterpreter){
        this(true,commandInterpreter);
    }

    public static interface ServoCommandInterpreter<S> {
        public ServoCommand<S> interpretCommand(int servoNum,String commandArgRaw, Object... otherParams) throws ServoCommandSyntaxException;
    }

    public static class ServoCommandSyntaxException extends Exception {
        public ServoCommandSyntaxException(String msg){
            super(msg);
        }
    }

    public ServoCommand(boolean addToInverseMap,ServoCommandInterpreter<T> commandInterpreter){
        if (getCommandID() > 15){
            throw new RuntimeException("Servo command has invalid ID! Max is 15");
        }
        if(addToInverseMap && inverseCommandMap.containsKey(getCommandID()) && inverseCommandMap.get(getCommandID()) != getCommandLabel()) {
            throw new RuntimeException("Conflicting servo command defined for ID "+getCommandID()+"!");
        }
        if(!commandMap.containsKey(getCommandLabel())) {
            commandMap.put(getCommandLabel(), getCommandID());
            commandInterpreterMap.put(getCommandLabel(),commandInterpreter);
            if (addToInverseMap) {
                inverseCommandMap.put(getCommandID(), getCommandLabel());
            }
        }
    }

    //Encode a command into format that takes up a tiny space (3 bytes)
    public byte[] encodeCommand() {
        //First 4 bits servo number
        //Second 4 bits are command
        //16 bit "unsigned int" for command arg
        if (servonum > 15){
            throw new RuntimeException("Encoding only allows for servo numbers 0->15, requested value ("+servonum+") outside this range");
        }
        byte sNumEnc = (byte) servonum; //Convert to byte (Data only in second half of byte)
        int cmdArg = encodeCommandArgTo16BitInt();
        if(cmdArg > 65535 || cmdArg < 0){
            throw new RuntimeException("Encoding only allows for command arg to be a 16 bit unsigned int (max val 65535)");
        }
        byte firstByte = (byte) (servonum << 4 | (getCommandID())); //Servo num MSB is byte MSB, command enc LSB is byte LSB
        byte secondByte = (byte) (((cmdArg & 0xFF00) >> 8)); //First 8 bits of the encoded angle put into an 8 bit byte
        byte thirdByte = (byte) (cmdArg & 0xFF); //Last 8 bits of the encoded angle
        return new byte[]{firstByte,secondByte,thirdByte};
    }

    protected abstract String getCommandLabel();
    protected abstract byte getCommandID();
    protected abstract int encodeCommandArgTo16BitInt();

    public int getServonum() {
        return servonum;
    }

    public void setServonum(int servonum) {
        this.servonum = servonum;
    }

    public T getCommandArg() {
        return commandArg;
    }

    public void setCommandArg(T commandArg) {
        this.commandArg = commandArg;
    }
}
