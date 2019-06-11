package com.iclr.storage.gui.controller;

import com.iclr.storage.ConnectionStatusChangeListener;
import com.iclr.storage.Main;
import com.iclr.storage.ValveServoController;
import com.iclr.storage.command.CommandInterpreter;
import com.iclr.storage.command.ServoCommand;
import com.iclr.storage.linkage.ServoValveDefinition;
import com.iclr.storage.logging.Logger;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainGUIController {
    static double OPENING_FRACTION_OF_SCREEN_HEIGHT = 0.75;
    static double ASPECT_RATIO = 16.0/9.0; //Width/Height - Starting aspect ratio before user resizes
    static MainGUIController instance;

    @FXML
    protected SplitPane splitPane;
    @FXML
    protected AnchorPane rootPane;
    @FXML
    protected VBox globalVerticalContainer;
    @FXML
    protected BorderPane mainLayoutBorderPane;
    @FXML
    protected AnchorPane leftPane;
    @FXML
    protected AnchorPane rightPane;
    @FXML
    protected ToolBar editorToolbar;
    @FXML
    protected MenuBar editorMenubar;
    @FXML
    protected VBox editorVbox;
    @FXML
    protected VBox rightSideVbox;
    @FXML
    protected Button codeValidateButton;
    @FXML
    protected ImageView logoImage;
    @FXML
    protected TextField connectionPortBox;
    @FXML
    protected TextField connectionBaudRateBox;
    @FXML
    protected Button connectionConnectButton;
    @FXML
    protected Label connectionStatusLabel;
    @FXML
    protected Circle connectionStatusIndicator;
    @FXML
    protected Button commandExecuteButton;
    private StyleClassedTextArea loggingTextArea;

    public MainGUIController(){
        instance = this;
    }

    public static Stage stage;

    private CommandsEditorController commandsEditorController;

    public MenuBar getEditorMenubar(){
        return this.editorMenubar;
    }

    public Button getCodeValidateButton(){
        return this.codeValidateButton;
    }

    public ToolBar getEditorToolbar(){
        return this.editorToolbar;
    }

    public void appendTextToLoggingPane(final String text){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(loggingTextArea != null) {
                    loggingTextArea.appendText(text);
                }
            }
        });
    }

    public void clearTextLoggingPane(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(loggingTextArea != null) {
                    loggingTextArea.clear();
                }
            }
        });
    }

    public static void start(Stage primaryStage) throws IOException {
        MainGUIController.stage = primaryStage;
        Parent root = FXMLLoader.load(MainGUIController.class.getClassLoader().getResource("layout/GUIMain.fxml"));
        Scene scene = new Scene(root, 711, 400);
        primaryStage.setTitle("ICLR Servo Valve Controller");
        primaryStage.setScene(scene);

        scene.getStylesheets().add(CommandsEditorController.class.getClassLoader().getResource("style/syntaxHighlighting.css").toExternalForm());
        scene.getStylesheets().add(MainGUIController.class.getClassLoader().getResource("style/mainStyle.css").toExternalForm());


        //Size stage so that it displays nicely
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        double heightPixels = primaryScreenBounds.getHeight()*OPENING_FRACTION_OF_SCREEN_HEIGHT;
        double widthPixels = ASPECT_RATIO * primaryScreenBounds.getHeight()*OPENING_FRACTION_OF_SCREEN_HEIGHT;
        if (widthPixels > primaryScreenBounds.getWidth()){ //Can't display this width on this screen, scale down so fits
            double scaleFactor = primaryScreenBounds.getWidth() / widthPixels;
            heightPixels *= scaleFactor;
            widthPixels *= scaleFactor;
        }

        //set Stage boundaries to visible bounds of the main screen
        primaryStage.setX(primaryScreenBounds.getMinX()+0.5*(primaryScreenBounds.getWidth()-widthPixels));
        primaryStage.setY(primaryScreenBounds.getMinY()+0.5*(primaryScreenBounds.getHeight()-heightPixels));
        primaryStage.setWidth(widthPixels);
        primaryStage.setHeight(heightPixels);

        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Main.instance.terminate();
                if (MainGUIController.instance != null && MainGUIController.instance.valveServoController != null){
                    MainGUIController.instance.valveServoController.closePort();
                }
            }
        });
    }

    private ValveServoController valveServoController = null;
    private String lastSentCommand;

    public void sendManualCommandAction(TextField commandField){
        String cmd = commandField.getText();
        this.lastSentCommand = cmd;
        commandField.clear();
        String[] cmdLines = cmd.split(Pattern.quote(";"));
        try {
            ServoCommand[] commands = CommandInterpreter.extractCommands(cmdLines);
            if(commands.length < 1){
                Logger.println(Logger.LogEntry.Severity.LOW,"Hint: If you didn't mean to send 0 commands, servoNum alone does not count as a command. You must specify it in the same line as your other commands, eg. 'servoNum 0; valveAngle 90'");
            }
            Logger.println(Logger.LogEntry.Severity.LOW,"Sending "+commands.length+" commands");
            if(valveServoController == null || (!valveServoController.isReady() || !valveServoController.isConnected())){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error: You are not connected! Connect first!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            valveServoController.sendCommands(commands);
        } catch (ServoCommand.ServoCommandSyntaxException e) {
            Logger.println(Logger.LogEntry.Severity.SEVERE,"Parsing error: "+e.getMessage());
            return;
        }
    }

    final Pattern LOG_PATTERN = Pattern.compile("\\[\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d\\|\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\|(?<severity>.+)\\]:.+(\\r\\n|\\r|\\n)?");

    @FXML
    public void initialize(){
        stage.setMinWidth(logoImage.getFitWidth()*1.2);
        //Make split pane occupy full width of parent
        globalVerticalContainer.prefWidthProperty().bind(rootPane.widthProperty());
        mainLayoutBorderPane.prefWidthProperty().bind(globalVerticalContainer.widthProperty());
        splitPane.prefWidthProperty().bind(globalVerticalContainer.widthProperty());
        splitPane.setDividerPosition(0,0.5);
        splitPane.prefHeightProperty().bind(mainLayoutBorderPane.heightProperty());
        mainLayoutBorderPane.prefHeightProperty().bind(rootPane.heightProperty());
        editorToolbar.prefWidthProperty().bind(leftPane.widthProperty());
        rightSideVbox.prefWidthProperty().bind(rightPane.widthProperty());

        CodeArea area = new CodeArea();
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.prefHeightProperty().bind(leftPane.heightProperty().subtract(editorToolbar.heightProperty()));
        area.setWrapText(true);

        StyleClassedTextArea logTextArea = new StyleClassedTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.valueOf("#d8e7ff").toString()), null, null)));
        logTextArea.setWrapText(true);
        logTextArea.setMouseTransparent(false);
        MainGUIController.this.loggingTextArea = logTextArea;

        VirtualizedScrollPane codeScrollPane = new VirtualizedScrollPane<>(area);
        codeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        editorVbox.getChildren().add(codeScrollPane);

        VirtualizedScrollPane logScrollPane = new VirtualizedScrollPane<>(logTextArea);
        logScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightSideVbox.getChildren().add(logScrollPane);

        final TextField commandInput = new TextField();
        commandInput.setPromptText("Enter commands here to override current commands and execute (Separated by ';')");
        Button commandBtn = new Button("Go");
        commandBtn.setMinWidth(40);
        HBox commandBox = new HBox();
        commandBox.prefWidthProperty().bind(leftPane.widthProperty());
        commandBox.getChildren().add(commandInput);
        commandBox.getChildren().add(commandBtn);
        rightSideVbox.getChildren().add(commandBox);
        commandInput.prefWidthProperty().bind(commandBox.widthProperty().subtract(commandBtn.widthProperty()));
        commandInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    sendManualCommandAction(commandInput);
                }
                else if(event.getCode() == KeyCode.UP){
                    commandInput.setText(lastSentCommand);
                }
            }
        });
        commandBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendManualCommandAction(commandInput);
            }
        });

        logTextArea.prefHeightProperty().bind(rightPane.heightProperty().subtract(commandBox.heightProperty()));

        logTextArea.textProperty().addListener((observable, oldText, newText) -> {
            Matcher matcher = LOG_PATTERN.matcher(newText);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while(matcher.find()) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                if(matcher.group("severity") != null) {
                    Logger.LogEntry.Severity sv = Logger.LogEntry.Severity.valueOf(matcher.group("severity"));
                    if(sv != null) {
                        spansBuilder.add(Collections.singleton(sv.getStyleClass()), matcher.end() - matcher.start());
                    }
                }
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), newText.length() - lastKwEnd);
            logTextArea.setStyleSpans(0, spansBuilder.create());
        });

        Logger.getInstance().attachListener(new Logger.LogListener() {
            @Override
            public void onLogClear() {
                clearTextLoggingPane();
            }

            @Override
            public void onLogEntryAdd(Logger.LogEntry line) {
                appendTextToLoggingPane(line.toString()+"\r\n");
            }

            @Override
            public void onAttach(List<Logger.LogEntry> previouslyLogged) {
                clearTextLoggingPane();
                for (Logger.LogEntry le: previouslyLogged){
                    appendTextToLoggingPane(le.toString()+"\r\n");
                }
            }
        });

        this.commandsEditorController = new CommandsEditorController(this, area);

        connectionConnectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(valveServoController != null){
                    valveServoController.closePort();
                    valveServoController = null;
                    connectionPortBox.setEditable(true);
                    connectionBaudRateBox.setEditable(true);
                    connectionConnectButton.setText("Connect");
                    connectionStatusLabel.setText("Status: Disconnected");
                    connectionStatusIndicator.setFill(Paint.valueOf("#ff1f1f"));
                }
                else {
                    connectionPortBox.setEditable(true);
                    connectionBaudRateBox.setEditable(true);
                    connectionConnectButton.setText("Disconnect");
                    String COMPort = connectionPortBox.getText();
                    String baudRaw = connectionBaudRateBox.getText();
                    int baudRate = 0;
                    try {
                        baudRate = Integer.parseInt(baudRaw);
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Error: Specified baud rate is not an integer", ButtonType.OK);
                        alert.showAndWait();
                        return;
                    }

                    valveServoController = new ValveServoController(COMPort, baudRate, Main.instance.getValveServoDefinitionsManager().getValveServoDefinitionList().toArray(new ServoValveDefinition[]{}), new ConnectionStatusChangeListener<ValveServoController>() {
                        @Override
                        public void onStatusChange(ValveServoController controller) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    connectionStatusLabel.setText("Status: "+(controller.isConnected()?(controller.isReady()?"Connected":"Connecting..."):"Disconnected"));
                                    if (!controller.isConnected()){
                                        connectionStatusIndicator.setFill(Paint.valueOf("#FF1F1F"));
                                    }
                                    else if(!controller.isReady()){
                                        connectionStatusIndicator.setFill(Paint.valueOf("#FFFF00"));
                                    }
                                    else if(controller.isReady()){
                                        connectionStatusIndicator.setFill(Paint.valueOf("#00FF00"));
                                    }
                                }
                            });
                        }
                    });
                    try {
                        connectionStatusLabel.setText("Status: Connecting...");
                        connectionStatusIndicator.setFill(Paint.valueOf("#ffff00"));
                        connectionBaudRateBox.setEditable(false);
                        connectionPortBox.setEditable(false);
                        valveServoController.openPort();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Error connecting to port: "+e.getMessage(), ButtonType.OK);
                        alert.showAndWait();
                        connectionStatusIndicator.setFill(Paint.valueOf("#ff1f1f"));
                        connectionStatusLabel.setText("Status: Disconnected...");
                    }
                }
            }
        });

        commandExecuteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ServoCommand[] cmds;
                try {
                    cmds = commandsEditorController.getCommandsWithUserFriendlyValidate();
                } catch (ServoCommand.ServoCommandSyntaxException e) {
                    return;
                }
                if(valveServoController == null || (!valveServoController.isReady() || !valveServoController.isConnected())){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error: You are not connected! Connect first!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
                valveServoController.sendCommands(cmds);
                Logger.println(Logger.LogEntry.Severity.LOW,"Commands sent");
            }
        });
        //commandExecuteButton.setStyle("-fx-background-color: #00FF00; ");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if(valveServoController != null && valveServoController.isConnected()){
                    valveServoController.closePort();
                }
            }
        }));
    }
}
