package com.iclr.storage;

import com.iclr.storage.command.ServoCommand;
import com.iclr.storage.command.ServoPositionCommand;
import com.iclr.storage.command.ValvePositionCommand;
import com.iclr.storage.linkage.ServoValveLinkage;

import java.util.Arrays;

/**
 * Created by Edward on 22/03/2019.
 */
public class ValveServoController extends ServoController {
    private ServoValveLinkage[] linkages;
    private double[] valveHandleDeadAnglesDeg; //Angle which handle can turn without any motion of the actual ball inside the valve
    private double[] currentServoAngles; //Current servo angles of every valve, TODO: Populate with data from servo controller
    private double[] currentServoAngleOffsets; //Current offsets of servo angles for every valve
    public ValveServoController(ServoValveLinkage[] linkages, double[] valveHandleDeadAnglesDeg){
        super();
        this.linkages = linkages;
        this.valveHandleDeadAnglesDeg = valveHandleDeadAnglesDeg;
        this.currentServoAngles = new double[this.linkages.length];
        this.currentServoAngleOffsets = new double[this.linkages.length];
        for(int i=0;i<this.linkages.length;i++){
            this.currentServoAngles[i] = this.linkages[i].getServoClosedAngleDegrees(); //Assume valves start closed
        }
    }

    @Override
    public void sendCommands(ServoCommand... commands){
        //Alter angles
        double[] prevServoAngles = Arrays.copyOf(this.currentServoAngles, this.currentServoAngles.length);
        double[] prevServoOffsets = Arrays.copyOf(this.currentServoAngleOffsets, this.currentServoAngleOffsets.length);
        for(int i=0;i<commands.length;i++){
            ServoCommand cmd = commands[i];
            if(cmd instanceof ValvePositionCommand){ //Swap valve position commands for servo position commands
                double valveAngleDesired = ((ValvePositionCommand) cmd).getCommandArg(); //Desired valve angle
                ServoValveLinkage svl = linkages[cmd.getServonum()]; //Use the linkage to relate servo and valve angles
                double prevServoAngle = prevServoAngles[cmd.getServonum()]; //servo angle that the valve will have been set to before this command executes
                double prevValveAngleDesired = svl.getValveAngleForGivenServoAngleDeg(prevServoAngle)-prevServoOffsets[cmd.getServonum()]; //Convert to valve angle and remove offset
                if(prevServoOffsets[cmd.getServonum()] > 0){ //Valve has been increasing angle
                    if (valveAngleDesired < prevValveAngleDesired){
                        //Valve wants to switch directions to decreasing angle
                        prevServoOffsets[cmd.getServonum()] = -0.5*valveHandleDeadAnglesDeg[cmd.getServonum()];
                    }
                } else { //Valve has been decreasing angle
                    if (valveAngleDesired > prevValveAngleDesired){
                        //Valve wants to switch directions to increasing angle
                        prevServoOffsets[cmd.getServonum()] = +0.5*valveHandleDeadAnglesDeg[cmd.getServonum()];
                    }
                }

                double servoAngle = svl.getAngleReqByServoForGivenValveAngleDeg(valveAngleDesired+prevServoOffsets[cmd.getServonum()]);
                //Hackily keep in correct range
                if (servoAngle > 180 && servoAngle < 190){
                    servoAngle = 180;
                }
                if (servoAngle > 180){
                    servoAngle -= 360;
                    if (servoAngle < 0 && servoAngle > -10){
                        servoAngle = 0;
                    }
                }
                prevServoAngles[cmd.getServonum()] = servoAngle; //Update the last servo angle so that subsequent commands are correct for this valve
                //Convert to ServoPositionCommand
                commands[i] = new ServoPositionCommand(cmd.getServonum(),servoAngle,180);
            }
        }
        currentServoAngleOffsets = prevServoOffsets; //Update offsets for when next command list sent
        super.sendCommands(commands);
    }

    public double getLastKnownServoAngle(int servoNum){
        return currentServoAngles[servoNum];
    }

    public ServoValveLinkage[] getLinkages() {
        return linkages;
    }

    public double[] getValveHandleDeadAnglesDeg() {
        return valveHandleDeadAnglesDeg;
    }
}
