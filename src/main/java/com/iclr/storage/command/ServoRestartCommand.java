package com.iclr.storage.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edward on 01/06/2021
 */
public class ServoRestartCommand extends ServoCommand<Void> {

    public ServoRestartCommand(int servoNum){
        super(new ServoCommandInterpreter<Void>() {
            @Override
            public ServoCommand<Void> interpretCommand(int servoNum, String commandArgRaw, Object... otherParams) throws ServoCommandSyntaxException {
                return new ServoRestartCommand(servoNum);
            }
        });
        this.servonum = servoNum;
        this.commandArg = null;
    }

    @Override
    protected String getCommandLabel() {
        return "restart";
    }

    @Override
    protected byte getCommandID() {
        return 0x05;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        int i = 0;
        return i;
    }
}
