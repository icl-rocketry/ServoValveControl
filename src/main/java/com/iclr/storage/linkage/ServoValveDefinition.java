package com.iclr.storage.linkage;

public class ServoValveDefinition {
    private int servonum;
    private double servoHandleDeadAngleDegrees = 1.364; //Angle which handle can turn without any motion of the actual ball inside the valve
    private ServoValveLinkage servoValveLinkage;

    public ServoValveDefinition(ServoValveLinkage servoValveLinkage, int servonum, double servoHandleDeadAngleDegrees) {
        this.servoValveLinkage = servoValveLinkage;
        this.servonum = servonum;
        this.servoHandleDeadAngleDegrees = servoHandleDeadAngleDegrees;
    }

    public ServoValveLinkage getServoValveLinkage() {
        return servoValveLinkage;
    }

    public void setServoValveLinkage(ServoValveLinkage servoValveLinkage) {
        this.servoValveLinkage = servoValveLinkage;
    }

    public int getServonum() {
        return servonum;
    }

    public void setServonum(int servonum) {
        this.servonum = servonum;
    }

    public double getServoHandleDeadAngleDegrees() {
        return servoHandleDeadAngleDegrees;
    }

    public void setServoHandleDeadAngleDegrees(double servoHandleDeadAngleDegrees) {
        this.servoHandleDeadAngleDegrees = servoHandleDeadAngleDegrees;
    }
}
