package com.iclr.storage;

import com.google.gson.*;
import com.iclr.storage.linkage.ServoValveDefinition;
import com.iclr.storage.linkage.ServoValveLinkage;
import com.iclr.storage.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ValveServoDefinitionsManager {
    private File definitionsFile;
    private List<ServoValveDefinition> valveServoDefinitionList = new ArrayList<>();

    public ValveServoDefinitionsManager(File definitionsFile){
        this.definitionsFile = definitionsFile;
    }

    public List<ServoValveDefinition> getValveServoDefinitionList(){
        return this.valveServoDefinitionList;
    }

    public void saveDefinitions(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray ja = new JsonArray();
        for (ServoValveDefinition definition:getValveServoDefinitionList()){
            ja.add(gson.toJsonTree(definition));
        }
        String jsonList = gson.toJson(ja);
        try {
            if(!this.definitionsFile.exists()){
                this.definitionsFile.getParentFile().mkdirs();
                this.definitionsFile.createNewFile();
            }
            Files.write(definitionsFile.toPath(), Arrays.asList(new String[]{jsonList}), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDefinitions() {
        if (!this.definitionsFile.exists()) {
            this.definitionsFile.getParentFile().mkdirs();
            //Write default
            ServoValveLinkage svl = new ServoValveLinkage(55.02, 56.42, 28.8, 82.62, 224.5, ServoValveLinkage.ValveCloseHandleRotationDirection.TOWARDS_SERVO, ServoValveLinkage.ServoAngleSignConvention.POSITIVE_TOWARDS_THE_VALVE, 0);
            this.valveServoDefinitionList.add(new ServoValveDefinition(svl, 0, 1.364));
            saveDefinitions();
        }
        //Load
        this.valveServoDefinitionList = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        JsonParser parser = new JsonParser();
        try {
            JsonElement elem = parser.parse(new String(Files.readAllBytes(this.definitionsFile.toPath()), Charset.forName("UTF-8")));
            JsonArray ja = (JsonArray) elem;
            Iterator<JsonElement> it = ja.iterator();
            while (it.hasNext()){
                JsonElement el = it.next();
                ServoValveDefinition definition = gson.fromJson(el,ServoValveDefinition.class);
                this.valveServoDefinitionList.add(definition);
            }
            Logger.println("Loaded "+this.valveServoDefinitionList.size()+" definitions of servo valves");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
