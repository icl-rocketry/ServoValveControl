package com.iclr.storage.gui.controller;

import com.iclr.storage.command.CommandInterpreter;
import com.iclr.storage.command.ServoCommand;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.Nodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

public class CommandsEditorController {
    private CodeArea codeArea;
    private MainGUIController parentGUI;
    private File currentFile = null;

    public CommandsEditorController(MainGUIController parentGUI, CodeArea codeArea){
        this.parentGUI = parentGUI;
        this.codeArea = codeArea;
        initMenuBarHandling();
        Nodes.addInputMap(codeArea, consume(keyPressed(S, CONTROL_DOWN), event -> save())); //ctrl s shortcut to save
        codeArea.textProperty().addListener((observable, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });
        loadDemoCode();
        initCodeProcessing();
    }

    public void initCodeProcessing(){
        this.parentGUI.getCodeValidateButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    CommandInterpreter.extractCommands(getLines().toArray(new String[]{}));
                } catch (ServoCommand.ServoCommandSyntaxException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                    alert.setHeaderText("Error parsing commands file!");
                    alert.showAndWait();
                    return;
                }
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Successfully validated file! No syntax errors found!", ButtonType.OK);
                alert.setHeaderText("Success!");
                alert.showAndWait();
            }
        });
    }

    public void loadDemoCode(){
        this.currentFile = null;
        this.codeArea.clear();
        InputStream is = CommandsEditorController.class.getClassLoader().getResourceAsStream("commandsDemo.txt");
        try {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            setLines(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openFileDialog(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Open Commands File");
        File file = fileChooser.showOpenDialog(MainGUIController.stage);
        if (file != null){
            //OPEN FILE
            openFile(file);
        }
    }

    public void openFile(File f){
        this.codeArea.clear(); //Clear current text being displayed
        try { //Load text from file and write into code area
            List<String> lines = Files.readAllLines(f.toPath());
            this.setLines(lines);
            this.currentFile = f;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error opening file: "+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    public void setLines(List<String> lines){
        StringBuilder sb = new StringBuilder();
        for (String s:lines){
            if(sb.length() > 0){
                sb.append("\r\n");
            }
            sb.append(s);
        }
        this.codeArea.clear();
        this.codeArea.appendText(sb.toString());
    }

    public List<String> getLines(){
        List<String> lines = new ArrayList<>();
        lines.addAll(Arrays.asList(this.codeArea.getText().replaceAll(Pattern.quote("\r"),"").split(Pattern.quote("\n"))));
        return lines;
    }

    public void save(){
        if(this.currentFile == null){
            saveAs();
            return;
        }

        try {
            if(!currentFile.getParentFile().exists()){
                currentFile.mkdirs();
            }
            if(!currentFile.exists()){
                currentFile.createNewFile();
            }
            Files.write(this.currentFile.toPath(), getLines(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving to file: "+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    public void saveAs(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Save Commands File As");
        File file = fileChooser.showSaveDialog(MainGUIController.stage);
        if (file != null){
            this.currentFile = file;
            save();
        }
    }

    protected void initMenuBarHandling(){
        this.parentGUI.getEditorMenubar();
        MenuItem openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { //Menu item clicked
                openFileDialog();
            }
        });
        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                save();
            }
        });
        MenuItem saveAsMenuItem = new MenuItem("Save As");
        saveAsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveAs();
            }
        });
        //Populate "File" menu
        this.parentGUI.getEditorMenubar().getMenus().get(0).getItems().addAll(openMenuItem, saveMenuItem, saveAsMenuItem);

        MenuItem loadDemoMenuItem = new MenuItem("Load Demo Commands");
        loadDemoMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You will lose unsaved changes to the currently open file. Continue?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() != ButtonType.YES) {
                    return;
                }
                loadDemoCode();
            }
        });
        //Populate "Edit" menu
        this.parentGUI.getEditorMenubar().getMenus().get(1).getItems().add(loadDemoMenuItem);

        MenuItem helpMeMenuItem = new MenuItem("Help Me!");
        helpMeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Look at the demo file by going to Edit->Load Demo Commands to see information on how to control the servo valves", ButtonType.OK);
                alert.setHeaderText("Help Information");
                alert.showAndWait();
            }
        });
        //Populate help menu
        this.parentGUI.getEditorMenubar().getMenus().get(2).getItems().add(helpMeMenuItem);
    }

    private static final Pattern SYNTAX_PATTERN = Pattern.compile("((?<cmd>[^\\s%]+)[\\s]+(?<arg>[^\\s%]+)[\\s]*([^\\s%]*?)[^%]*?(\\r\\n|\\r|\\n)?)|(?<comment>%.+(\\r\\n|\\r|\\n)?)");

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {

        Matcher matcher = SYNTAX_PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            if(matcher.group("comment") != null) {
                spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
            }
            if(matcher.group("cmd") != null) {
                spansBuilder.add(Collections.singleton("cmd"), matcher.start("arg")-matcher.start());
                spansBuilder.add(Collections.singleton("arg"), matcher.end() - matcher.start("arg"));
            }
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
