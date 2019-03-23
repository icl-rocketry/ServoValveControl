package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ServoWaitMillisCommand extends ServoCommand<Long> {
    public ServoWaitMillisCommand(int servoNum, long time){
        super();
        this.servonum = servoNum;
        this.commandArg = time;
    }

    @Override
    protected String getCommandLabel() {
        return "waitMillis";
    }

    @Override
    protected byte getCommandID() {
        return 0x01;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        int i = ((Long)getCommandArg()).intValue();
        return i;
    }
}
