package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ServoNullCommand extends ServoCommand<Double> {

    public ServoNullCommand(int servoNum){
        super(new ServoCommandInterpreter<Double>() {
            @Override
            public ServoCommand<Double> interpretCommand(int servoNumber,String commandArgRaw,Object... otherParams) throws ServoCommandSyntaxException {
                return new ServoNullCommand(servoNumber);
            }
        });
        this.servonum = servoNum;
    }

    @Override
    protected String getCommandLabel() {
        return "servoNull";
    }

    @Override
    protected byte getCommandID() {
        return 0x3;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        return 0;
    }
}
