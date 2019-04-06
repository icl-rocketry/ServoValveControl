package com.iclr.storage.gui.controller;

import com.iclr.storage.ConnectionStatusChangeListener;
import com.iclr.storage.Main;
import com.iclr.storage.ValveServoController;
import com.iclr.storage.command.CommandInterpreter;
import com.iclr.storage.command.ServoCommand;
import com.iclr.storage.linkage.ServoValveDefinition;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;

public class MainGUIController {
    static double OPENING_FRACTION_OF_SCREEN_HEIGHT = 0.75;
    static double ASPECT_RATIO = 16.0/9.0; //Width/Height - Starting aspect ratio before user resizes

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
    protected ToolBar editorToolbar;
    @FXML
    protected MenuBar editorMenubar;
    @FXML
    protected VBox editorVbox;
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
            }
        });
    }

    private ValveServoController valveServoController = null;

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

        CodeArea area = new CodeArea();
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.prefHeightProperty().bind(leftPane.heightProperty().subtract(editorToolbar.heightProperty()));
        area.setWrapText(true);

        VirtualizedScrollPane codeScrollPane = new VirtualizedScrollPane<>(area);
        codeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        editorVbox.getChildren().add(codeScrollPane);

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
                System.out.println("Commands sent");
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
