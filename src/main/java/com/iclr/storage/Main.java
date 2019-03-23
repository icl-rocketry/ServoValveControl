package com.iclr.storage;

import com.iclr.storage.command.*;
import com.iclr.storage.linkage.FourBarLinkage;
import com.iclr.storage.linkage.ServoValveLinkage;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by Edward on 21/03/2019.
 */
public class Main {
    static {
        if(1==0) {
            try {
                //runntime Path
                String runPath = new File(".").getCanonicalPath();

                //create folder
                File dir = new File(runPath + File.separator + "libs");
                dir.mkdir();

                //get environment variables and add the path of the 'lib' folder
                String currentLibPath = System.getProperty("java.library.path");
                System.setProperty("java.library.path",
                        currentLibPath + ";" + dir.getAbsolutePath());

                Field fieldSysPath = ClassLoader.class
                        .getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);

                loadLib(runPath, "/rxtxParallel.dll", "rxtxParallel");
                loadLib(runPath, "/rxtxSerial.dll", "rxtxSerial");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        /*try {
            NativeUtils.loadLibraryFromJar("/rxtxSerial.dll");
            NativeUtils.loadLibraryFromJar("/rxtxParallel.dll");
        } catch (IOException e) {
            e.printStackTrace(); // This is probably not the best way to handle exception :-)
        }*/
    }

    private static void loadLib(String path, String resPath, String name) throws IOException {
        name = name + ".dll";
        File fileOut = new File(path + File.separator + "libs" + File.separator+name);
        try (InputStream is = Main.class.getResourceAsStream(resPath)) {
            Files.copy(is, fileOut.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fileOut.delete();
            throw e;
        } catch (NullPointerException e) {
            fileOut.delete();
            throw new FileNotFoundException("File " + resPath + " was not found inside JAR.");
        }

        try {
            System.load(fileOut.getAbsolutePath());
        } finally {
            //fileOut.deleteOnExit();
        }
    }

    public static void main(String[] args) {
        FourBarLinkage fbl = new FourBarLinkage(0.96,0.95,0.5,1.4);
        //ServoValveLinkage svl = new ServoValveLinkage(0.96,0.95,0.5,1.4,217, ServoValveLinkage.ValveCloseHandleRotationDirection.TOWARDS_SERVO, ServoValveLinkage.ServoAngleSignConvention.POSITIVE_TOWARDS_THE_VALVE,0);
        ServoValveLinkage svl = new ServoValveLinkage(57.6,57.5,29,83,217, ServoValveLinkage.ValveCloseHandleRotationDirection.TOWARDS_SERVO, ServoValveLinkage.ServoAngleSignConvention.POSITIVE_TOWARDS_THE_VALVE,0);
        double servoAngle = svl.getAngleReqByServoForGivenValveAngleDeg(0);
        System.out.println("Servo angle req: "+servoAngle);
        //double valveAngle = svl.getValveAngleForGivenServoAngleDeg(servoAngle);

        ServoController servoManualControl = new ValveServoController(new ServoValveLinkage[]{svl},new double[]{1.364});
        //ServoCommand cmd = new ServoPositionCommand(0,180,180);

        ServoCommand cmd0 = new ServoWaitMillisCommand(0,0);
        ServoCommand cmd1 = new ValvePositionCommand(0,90);
        ServoCommand cmd2 = new ServoWaitMillisCommand(0,1500);
        ServoCommand cmd3 = new ValvePositionCommand(0,0);
        ServoCommand cmd4 = new ServoWaitSecCommand(0,2);
        ServoCommand cmd5 = new ValvePositionCommand(0,90);
        ServoCommand cmd6 = new ServoWaitSecCommand(0,2);
        ServoCommand cmd7 = new ValvePositionCommand(0,70);
        ServoCommand cmd8 = new ServoWaitSecCommand(0,2);
        ServoCommand cmd9 = new ValvePositionCommand(0,60);
        ServoCommand cmd10 = new ServoWaitSecCommand(0,2);
        ServoCommand cmd11 = new ValvePositionCommand(0,58);
        ServoCommand cmd12 = new ServoWaitSecCommand(0,2);
        ServoCommand cmd13 = new ValvePositionCommand(0,60);
        ServoCommand cmd14 = new ServoWaitSecCommand(0,2);
        ServoCommand cmdLast = new ValvePositionCommand(0,90);

        //ServoCommand cmd3 = new ServoPositionCommand(0,180,180);
        try {
            System.out.println("Opening port to servo...");
            servoManualControl.openPort();
            //servoManualControl.sendCommands(cmd);
            long l = System.currentTimeMillis();
            while(System.currentTimeMillis() - l < 120000) {
                servoManualControl.sendCommands(cmd0, cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8, cmd9, cmd10, cmd11, cmd12, cmd13, cmd14, cmdLast);
                Thread.sleep(20000);
            }
            try {
                //Wait to see output
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            servoManualControl.closePort();
        }
    }
}
