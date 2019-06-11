package com.iclr.storage;

import com.iclr.storage.command.ServoCommand;
import com.iclr.storage.command.ServoPositionCommand;
import com.iclr.storage.command.ValvePositionCommand;
import com.iclr.storage.linkage.ServoValveDefinition;
import com.iclr.storage.linkage.ServoValveLinkage;
import com.iclr.storage.logging.Logger;

import java.util.Arrays;

/**
 * Created by Edward on 22/03/2019.
 */
public class ValveServoController extends ServoController {
    private ServoValveDefinition[] servoValves;
    private double[] currentServoAngles; //Current servo angles of every valve
    private double[] currentServoAngleOffsets; //Current offsets of servo angles for every valve
    public ValveServoController(String comPort, int baudRate, ServoValveDefinition[] servoValves, ConnectionStatusChangeListener<? extends ValveServoController> connectionStatusChangeListener){
        super(comPort, baudRate, connectionStatusChangeListener);
        this.servoValves = servoValves;
        this.currentServoAngles = new double[this.servoValves.length];
        this.currentServoAngleOffsets = new double[this.servoValves.length];
        for(int i=0;i<this.servoValves.length;i++){
            this.currentServoAngles[i] = this.servoValves[i].getServoValveLinkage().getServoClosedAngleDegrees(); //Assume valves start closed
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
                ServoValveLinkage svl = null; //Use the linkage to relate servo and valve angles
                try {
                    svl = servoValves[cmd.getServonum()].getServoValveLinkage();
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.error("Error, servo valve "+cmd.getServonum()+" not defined");
                }
                double prevServoAngle = prevServoAngles[cmd.getServonum()]; //servo angle that the valve will have been set to before this command executes
                double prevValveAngleDesired = svl.getValveAngleForGivenServoAngleDeg(prevServoAngle)-prevServoOffsets[cmd.getServonum()]; //Convert to valve angle and remove offset
                if(prevServoOffsets[cmd.getServonum()] > 0){ //Valve has been increasing angle
                    if (valveAngleDesired < prevValveAngleDesired){
                        //Valve wants to switch directions to decreasing angle
                        prevServoOffsets[cmd.getServonum()] = -0.5*servoValves[cmd.getServonum()].getServoHandleDeadAngleDegrees();
                    }
                } else { //Valve has been decreasing angle
                    if (valveAngleDesired > prevValveAngleDesired){
                        //Valve wants to switch directions to increasing angle
                        prevServoOffsets[cmd.getServonum()] = +0.5*servoValves[cmd.getServonum()].getServoHandleDeadAngleDegrees();
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

    @Override
    public void receiveAngleUpdate(int servoNum,double newAngle){
        Logger.println(Logger.LogEntry.Severity.HIGH,"Servo "+servoNum+" now at "+newAngle+" degrees");
        if(servoNum < currentServoAngles.length) {
            currentServoAngles[servoNum] = newAngle;
        }
    }

    public double getLastKnownServoAngle(int servoNum){
        return currentServoAngles[servoNum];
    }

    public ServoValveDefinition[] getServoValves() {
        return this.servoValves;
    }
}
