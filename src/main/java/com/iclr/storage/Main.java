package com.iclr.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.iclr.storage.command.*;
import com.iclr.storage.gui.controller.MainGUIController;
import com.iclr.storage.linkage.FourBarLinkage;
import com.iclr.storage.linkage.ServoValveDefinition;
import com.iclr.storage.linkage.ServoValveLinkage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Edward on 21/03/2019.
 */
public class Main extends Application {
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

    private ExecutorService executorService;
    private ValveServoDefinitionsManager valveServoDefinitionsManager;
    public static Main instance;

    public static void loadRXTXLibrary(){
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

    public Main() {
        instance = this;
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

    public static void doInUIThread(Runnable run){
        Platform.runLater(run);
    }

    public static void main(String[] args) {
        loadRXTXLibrary();
        CommandInterpreter.init();
        ServoValveLinkage svl = new ServoValveLinkage(55.02,56.42,28.8,82.62,224.5, ServoValveLinkage.ValveCloseHandleRotationDirection.TOWARDS_SERVO, ServoValveLinkage.ServoAngleSignConvention.POSITIVE_TOWARDS_THE_VALVE,0);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray ja = new JsonArray();
        ja.add(gson.toJsonTree(new ServoValveDefinition(svl, 0, 1.364)));
        String jsonList = gson.toJson(ja);
        File f = new File("servoValveDefinitions.json");
        try {
            if(!f.exists()){
                f.createNewFile();
            }
            Files.write(f.toPath(), Arrays.asList(new String[]{jsonList}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        launch();
        /*FourBarLinkage fbl = new FourBarLinkage(0.96,0.95,0.5,1.4);
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
        }*/
    }

    public void init(){
        executorService = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                executorService.shutdown();
            }
        });
    }

    public ValveServoDefinitionsManager getValveServoDefinitionsManager() {
        return valveServoDefinitionsManager;
    }

    public ExecutorService getExecutorService(){
        return this.executorService;
    }

    public void terminate(){
        this.executorService.shutdown();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.valveServoDefinitionsManager = new ValveServoDefinitionsManager(new File("servoValveDefinitions.json"));
        this.valveServoDefinitionsManager.loadDefinitions();
        primaryStage.getIcons().add(new Image(Main.class.getClassLoader().getResourceAsStream("img/icon.png")));
        MainGUIController.start(primaryStage);
    }
}
