package com.iclr.storage.gui.controller;

import com.iclr.storage.Main;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

import java.io.IOException;
import java.time.Duration;

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

    @FXML
    public void initialize(){
        //Make split pane occupy full width of parent
        globalVerticalContainer.prefWidthProperty().bind(rootPane.widthProperty());
        mainLayoutBorderPane.prefWidthProperty().bind(globalVerticalContainer.widthProperty());
        splitPane.prefWidthProperty().bind(mainLayoutBorderPane.widthProperty());
        splitPane.setDividerPosition(0,0.5);
        splitPane.prefHeightProperty().bind(mainLayoutBorderPane.heightProperty());
        mainLayoutBorderPane.prefHeightProperty().bind(rootPane.heightProperty());
        editorToolbar.prefWidthProperty().bind(leftPane.widthProperty());

        CodeArea area = new CodeArea();
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.prefHeightProperty().bind(leftPane.heightProperty().subtract(editorToolbar.heightProperty()));
        area.setWrapText(true);
        //area.appendText("Pause the mouse over the text for 1 second.");

        /*Popup popup = new Popup();
        Label popupMsg = new Label();
        popupMsg.setStyle(
                "-fx-background-color: black;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5;");
        popup.getContent().add(popupMsg);

        area.setMouseOverTextDelay(Duration.ofSeconds(1));
        area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
            int chIdx = e.getCharacterIndex();
            Point2D pos = e.getScreenPosition();
            popupMsg.setText("Character '" + area.getText(chIdx, chIdx+1) + "' at " + pos);
            popup.show(area, pos.getX(), pos.getY() + 10);
        });
        area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            popup.hide();
        });*/

        VirtualizedScrollPane codeScrollPane = new VirtualizedScrollPane<>(area);
        codeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        editorVbox.getChildren().add(codeScrollPane);

        this.commandsEditorController = new CommandsEditorController(this, area);
    }
}
