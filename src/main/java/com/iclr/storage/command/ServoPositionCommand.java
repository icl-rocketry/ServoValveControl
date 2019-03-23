package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ServoPositionCommand extends ServoCommand<Double> {
    private double maxAngle;

    public ServoPositionCommand(int servoNum,double angle,double maxAngle){
        super();
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
