package com.iclr.storage.linkage;

import com.iclr.storage.util.ClampAngle;

/**
 * Created by Edward on 22/03/2019.
 * Assumes the first root from the four bar linkage is the configuration used
 */
public class ServoValveLinkage {
    private FourBarLinkage linkage; //Right pivot = valve pivot for this "convention"
    private double linkageServoAngleForClosedValveDegrees = 217; //Following the convention of the linkage
    private double linkageValveAngleForClosedDegrees; //In the linkage convention
    private double linkageValveAngleForOpenDegrees; //In the linkage convention
    private double servoClosedAngleDegrees = 0; //Angle to tell servo to be at to have the valve closed
    private ServoAngleSignConvention servoAngleSignConvention = ServoAngleSignConvention.POSITIVE_TOWARDS_THE_VALVE;
    private ValveCloseHandleRotationDirection closeHandleRotationDirection = ValveCloseHandleRotationDirection.TOWARDS_SERVO;

    /**
     * Create servo valve linkage between a given servo and valve
     * @param distanceBetweenPivots Distance between pivots
     * @param lengthOfValveLinkageBar Length of valve linkage bar ("Right linkage")
     * @param lengthOfServoLinkageBar Length of servo linkage bar ("Left linkage")
     * @param lengthOfConnectingBar Length of bar connecting
     * @param linkageServoAngleForClosedValveDegrees Angle of the servo ("Left Linkage") in the convention setup by FourBarLinkage for which the valve is closed
     * @param closeHandleRotationDirection Direction the handle of the valve is turned to close it, eg. towards or away from the servo
     */
    public ServoValveLinkage(double distanceBetweenPivots, double lengthOfValveLinkageBar, double lengthOfServoLinkageBar, double lengthOfConnectingBar, double linkageServoAngleForClosedValveDegrees, ValveCloseHandleRotationDirection closeHandleRotationDirection, ServoAngleSignConvention servoAngleSignConvention, double servoClosedAngleDegrees){
        this.linkage = new FourBarLinkage(distanceBetweenPivots,lengthOfValveLinkageBar,lengthOfServoLinkageBar,lengthOfConnectingBar);
        this.linkageServoAngleForClosedValveDegrees = linkageServoAngleForClosedValveDegrees;
        this.closeHandleRotationDirection = closeHandleRotationDirection;
        this.linkageValveAngleForClosedDegrees = this.linkage.getPossibleAnglesOfRightLinkageDegrees(linkageServoAngleForClosedValveDegrees)[0];
        switch (this.closeHandleRotationDirection){
            case TOWARDS_SERVO:
                this.linkageValveAngleForOpenDegrees = this.linkageValveAngleForClosedDegrees - 90;
                break;
            case AWAY_FROM_SERVO:
                this.linkageValveAngleForOpenDegrees = this.linkageValveAngleForClosedDegrees + 90;
                break;
        }
        this.servoClosedAngleDegrees = servoClosedAngleDegrees;
        this.servoAngleSignConvention = servoAngleSignConvention;
    }

    //This valve angle parameter is defined as 0 is open and 90 is closed
    public double getLinkageValveAngleFromValveAngleDegrees(double valveAngleDeg){
        switch (this.closeHandleRotationDirection){
            case TOWARDS_SERVO:
                return ClampAngle.clampDeg(valveAngleDeg+this.linkageValveAngleForOpenDegrees);
            case AWAY_FROM_SERVO:
                return ClampAngle.clampDeg(-valveAngleDeg+this.linkageValveAngleForOpenDegrees);
        }
        return -1;
    }

    //This valve angle returned is defined as 0 is open and 90 is closed
    public double getValveAngleFromLinkageValveAngleDegrees(double linkageValveAngleDegrees){
        switch (this.closeHandleRotationDirection){
            case TOWARDS_SERVO:
                return ClampAngle.clampDeg(linkageValveAngleDegrees-this.linkageValveAngleForOpenDegrees);
            case AWAY_FROM_SERVO:
                return ClampAngle.clampDeg(-linkageValveAngleDegrees-this.linkageValveAngleForOpenDegrees);
        }
        return -1;
    }

    public double getLinkageServoAngleFromServoAngleDegrees(double servoAngleDeg){
        switch (this.servoAngleSignConvention){
            case POSITIVE_AWAY_FROM_VALVE:
                switch (this.closeHandleRotationDirection){
                    case TOWARDS_SERVO:
                        return ClampAngle.clampDeg(servoAngleDeg-this.linkageServoAngleForClosedValveDegrees-this.servoClosedAngleDegrees);
                    case AWAY_FROM_SERVO:
                        return ClampAngle.clampDeg(-(servoAngleDeg-this.linkageServoAngleForClosedValveDegrees)-this.servoClosedAngleDegrees);
                }
            case POSITIVE_TOWARDS_THE_VALVE:
                switch (this.closeHandleRotationDirection){
                    case TOWARDS_SERVO:
                        return ClampAngle.clampDeg(-servoAngleDeg + this.servoClosedAngleDegrees + this.linkageServoAngleForClosedValveDegrees);
                    case AWAY_FROM_SERVO:
                        return ClampAngle.clampDeg(servoAngleDeg+this.linkageServoAngleForClosedValveDegrees-this.servoClosedAngleDegrees);
                }
        }
        return -1;
    }

    //Convert between sign conventions (Linkage vs reality)
    public double getServoAngleFromLinkageServoAngleDegrees(double linkageServoAngleDeg){
        switch (this.servoAngleSignConvention){
            case POSITIVE_AWAY_FROM_VALVE:
                switch (this.closeHandleRotationDirection){
                    case TOWARDS_SERVO:
                        return ClampAngle.clampDeg(linkageServoAngleDeg-this.linkageServoAngleForClosedValveDegrees+this.servoClosedAngleDegrees);
                    case AWAY_FROM_SERVO:
                        return ClampAngle.clampDeg(-(linkageServoAngleDeg-this.linkageServoAngleForClosedValveDegrees)+this.servoClosedAngleDegrees);
                }
            case POSITIVE_TOWARDS_THE_VALVE:
                switch (this.closeHandleRotationDirection){
                    case TOWARDS_SERVO:
                        return ClampAngle.clampDeg(-(linkageServoAngleDeg-this.linkageServoAngleForClosedValveDegrees)+this.servoClosedAngleDegrees);
                    case AWAY_FROM_SERVO:
                        return ClampAngle.clampDeg((linkageServoAngleDeg-this.linkageServoAngleForClosedValveDegrees)+this.servoClosedAngleDegrees);
                }
        }
        return -1;
    }

    public double getLinkageServoAngleForClosedValveDegrees() {
        return linkageServoAngleForClosedValveDegrees;
    }

    public double getLinkageValveAngleForClosedDegrees() {
        return linkageValveAngleForClosedDegrees;
    }

    public double getLinkageValveAngleForOpenDegrees() {
        return linkageValveAngleForOpenDegrees;
    }

    public double getLinkageServoAngleForOpenValveDegrees() {
        return this.linkage.getPossibleAnglesOfLeftLinkageDegrees(this.linkageValveAngleForOpenDegrees)[0];
    }

    public double getServoClosedAngleDegrees(){
        return this.servoClosedAngleDegrees;
    }

    /**
     * The direction the servo turns when given a positive or negative change in angle
     */
    public static enum ServoAngleSignConvention {
        POSITIVE_AWAY_FROM_VALVE, //Servo turns away from the valve when you increase it's angle (that is sent to it)
        POSITIVE_TOWARDS_THE_VALVE; //Servo turns towards from the valve when you increase it's angle (that is sent to it)
    }

    /**
     * The direction the valve handle turns when closing the valve
     */
    public static enum ValveCloseHandleRotationDirection {
        TOWARDS_SERVO,AWAY_FROM_SERVO;
    }

    public ValveCloseHandleRotationDirection getValveCloseHandleRotationDirection(){
        return this.closeHandleRotationDirection;
    }

    public FourBarLinkage getLinkage(){
        return this.linkage;
    }

    /**
     * Valve angle parameter defined here as 0 is open and 90 is closed
     */
    public double getAngleReqByServoForGivenValveAngleDeg(double valveAngle){
        double linkageVAngle= this.getLinkageValveAngleFromValveAngleDegrees(valveAngle);
        double linkageSAngle = this.linkage.getPossibleAnglesOfLeftLinkageDegrees(linkageVAngle)[0];
        return this.getServoAngleFromLinkageServoAngleDegrees(linkageSAngle);
    }

    /**
     * Valve angle returned here defined here as 0 is open and 90 is closed
     */
    public double getValveAngleForGivenServoAngleDeg(double servoAngle){
        double linkageSAngle = this.getLinkageServoAngleFromServoAngleDegrees(servoAngle);
        double linkageVAngle = this.linkage.getPossibleAnglesOfRightLinkageDegrees(linkageSAngle)[0];
        return this.getValveAngleFromLinkageValveAngleDegrees(linkageVAngle);
    }
}
