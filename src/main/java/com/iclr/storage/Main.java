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
        ServoValveLinkage svl = new ServoValveLinkage(55.02,56.42,28.8,82.62,224.5, ServoValveLinkage.ValveCloseHandleRotationDirection.TOWARDS_SERVO, ServoValveLinkage.ServoAngleSignConvention.POSITIVE_TOWARDS_THE_VALVE,0);
        //double valveAngle = svl.getValveAngleForGivenServoAngleDeg(servoAngle);

        ServoController servoManualControl = new ValveServoController("COM7", 57600,new ServoValveLinkage[]{svl},new double[]{1.364});
        //ServoCommand cmd = new ServoPositionCommand(0,180,180);

        ServoCommand[] cmds = null;
        try {
            CommandInterpreter.init();
            cmds = CommandInterpreter.extractCommands(new File("commands.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServoCommand.ServoCommandSyntaxException e) {
            e.printStackTrace();
        }
        if(cmds == null){
            return;
        }

        try {
            System.out.println("Opening port to servo...");
            servoManualControl.openPort();
            servoManualControl.sendCommands(cmds);
            Thread.sleep(50000); //Wait to see the output
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            servoManualControl.closePort();
        }
    }
}
