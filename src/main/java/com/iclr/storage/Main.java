package com.iclr.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.iclr.storage.command.*;
import com.iclr.storage.gui.controller.MainGUIController;
import com.iclr.storage.linkage.FourBarLinkage;
import com.iclr.storage.linkage.ServoValveDefinition;
import com.iclr.storage.linkage.ServoValveLinkage;
import com.iclr.storage.logging.FileLogger;
import com.iclr.storage.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Edward on 21/03/2019.
 */
public class Main extends Application {
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
            fileOut.deleteOnExit();
        }
    }

    public static void doInUIThread(Runnable run){
        Platform.runLater(run);
    }

    public static void main(String[] args) {
        Logger.getInstance().attachListener(new Logger.LogListener() {
            @Override
            public void onLogClear() {

            }

            @Override
            public void onLogEntryAdd(Logger.LogEntry line) {
                System.out.println(line.toString());
            }

            @Override
            public void onAttach(List<Logger.LogEntry> previouslyLogged) {

            }
        });
        try {
            final FileLogger fl = new FileLogger(new File("log.txt"));
            Logger.getInstance().attachListener(fl);
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    fl.close();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadRXTXLibrary();
        CommandInterpreter.init();
        launch();
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
