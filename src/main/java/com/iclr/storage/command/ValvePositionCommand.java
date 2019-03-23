package com.iclr.storage.command;

/**
 * Created by Edward on 22/03/2019.
 */
public class ValvePositionCommand extends ServoCommand<Double> implements Cloneable {

    /**
     * 0 is open, 90 is closed
     * @param servoNum num of servo to control
     * @param angle desired angle, 0 is open and 90 is closed
     */
    public ValvePositionCommand(int servoNum, double angle){
        super(false, new ServoCommandInterpreter<Double>() {
            @Override
            public ServoCommand<Double> interpretCommand(int servoNum, String commandArgRaw, Object... otherParams) throws ServoCommandSyntaxException {
                try {
                    double angle = Double.parseDouble(commandArgRaw);
                    if (angle > 90){
                        throw new ServoCommandSyntaxException("Specified valve angle '"+commandArgRaw+"' is greater than max allowable (90)!");
                    }
                    if (angle < 0){
                        throw new ServoCommandSyntaxException("Specified valve angle '"+commandArgRaw+"' is not allowed to be negative!");
                    }
                    return new ValvePositionCommand(servoNum,angle);
                } catch (NumberFormatException e) {
                    throw new ServoCommandSyntaxException("Specified valve angle '"+commandArgRaw+"' is not a valid double (number)!");
                }
            }
        });
        this.servonum = servoNum;
        this.commandArg = angle;
    }

    @Override
    protected String getCommandLabel() {
        return "valveAngle";
    }

    @Override
    protected byte getCommandID() {
        return 0x0;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        int angleEnc = (int) ((getCommandArg()/90) * 65535); //Convert so between 0 and 65535
        if (angleEnc > 65535){ //Enforce angle is always max 16 bits
            angleEnc = 65535;
        }
        if (angleEnc < 0){
            angleEnc = 0;
        }
        return angleEnc;
    }
}
