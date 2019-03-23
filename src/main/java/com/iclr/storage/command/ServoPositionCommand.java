package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ServoPositionCommand extends ServoCommand<Double> {
    private double maxAngle;

    public ServoPositionCommand(int servoNum,double angle,double maxAngle){
        super(new ServoCommandInterpreter<Double>() {
            @Override
            public ServoCommand<Double> interpretCommand(int servoNum, String commandArgRaw, Object... otherParams) throws ServoCommandSyntaxException {
                try {
                    double maxAngle = (double) otherParams[0];
                    double angle = Double.parseDouble(commandArgRaw);
                    if (angle > maxAngle){
                        throw new ServoCommandSyntaxException("Specified servo angle '"+commandArgRaw+"' is greater than max allowable ("+maxAngle+")!");
                    }
                    if (angle < 0){
                        throw new ServoCommandSyntaxException("Specified servo angle '"+commandArgRaw+"' is not allowed to be negative!");
                    }
                    return new ServoPositionCommand(servoNum,angle, maxAngle);
                } catch (NumberFormatException e) {
                    throw new ServoCommandSyntaxException("Specified servo angle '"+commandArgRaw+"' is not a valid double (number)!");
                }
            }
        });
        this.servonum = servoNum;
        this.commandArg = angle;
        this.maxAngle = maxAngle;
    }

    @Override
    protected String getCommandLabel() {
        return "servoAngle";
    }

    @Override
    protected byte getCommandID() {
        return 0x0;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        int angleEnc = (int) ((getCommandArg()/maxAngle) * 65535); //Convert so between 0 and 65535
        if (angleEnc > 65535){ //Enforce angle is always max 16 bits
            angleEnc = 65535;
        }
        if (angleEnc < 0){
            angleEnc = 0;
        }
        return angleEnc;
    }
}
