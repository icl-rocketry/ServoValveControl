package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ServoWaitMillisCommand extends ServoCommand<Long> {
    public ServoWaitMillisCommand(int servoNum, long time){
        super(new ServoCommandInterpreter<Long>() {
            @Override
            public ServoCommand<Long> interpretCommand(int servoNum, String commandArgRaw, Object... otherParams) throws ServoCommandSyntaxException {
                try {
                    long l = Long.parseLong(commandArgRaw);
                    if (l < 0){
                        throw new NumberFormatException();
                    }
                    if (l > 65535){
                        throw new ServoCommandSyntaxException("Specified wait time '"+commandArgRaw+"' is too large to express with a 16 bit int! Use wait sec instead of wait millis");
                    }
                    return new ServoWaitMillisCommand(servoNum,l);
                } catch (NumberFormatException e) {
                    throw new ServoCommandSyntaxException("Specified wait time '"+commandArgRaw+"' is not a valid wait time (large integer)!");
                }
            }
        });
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
