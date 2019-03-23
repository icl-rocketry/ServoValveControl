package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ServoWaitSecCommand extends ServoCommand<Long> {
    public ServoWaitSecCommand(int servoNum, long time){
        super();
        this.servonum = servoNum;
        this.commandArg = time;
    }

    @Override
    protected String getCommandLabel() {
        return "waitSec";
    }

    @Override
    protected byte getCommandID() {
        return 0x02;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        int i = ((Long)getCommandArg()).intValue();
        return i;
    }
}
