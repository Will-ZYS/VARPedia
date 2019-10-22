package Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import Application.Controllers.Controller;
import Application.Controllers.Welcome_ScreenController;
import com.aquafx_project.AquaFx;
import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        String config = System.getProperty("user.dir") + System.getProperty("file.separator") + ".config";
        String stylesheet = "";

        File file = new File(config);
        BufferedReader br = new BufferedReader(new FileReader(file));


        String line;
        while ((line = br.readLine()) != null) {
            if ((line.trim().startsWith("Stylesheet"))) {
                br.close();
                stylesheet = line.substring(line.indexOf("=")+1).trim();
                break;
            }
        }
        br.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Welcome_Screen.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setCurrentController(controller);
        controller.setStyleSheet(stylesheet);
        root.setId("background");
        primaryStage.setTitle("Welcome to VARpedia");
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().addAll(this.getClass().getResource("css/Welcome_Screen.css").toExternalForm(), this.getClass().getResource(stylesheet).toExternalForm());
//        AquaFx.style();
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) {

        //make the directories
        File creationDir = new File("Creation_Directory");
        File audioDir = new File(".Audio_Directory");
        File videoDir = new File(".Video_Directory");
        File wikitDir = new File(".Wikit_Directory");
        File imageDir = new File(".Image_Directory");
        File outputDir = new File(".Output_Directory");

        if (!creationDir.isDirectory()) {
            creationDir.mkdir();
        }

        if (!audioDir.isDirectory()) {
            audioDir.mkdir();
        }

        if (!videoDir.isDirectory()) {
            videoDir.mkdir();
        }

        if (!wikitDir.isDirectory()) {
            wikitDir.mkdir();
        }

        if (!imageDir.isDirectory()) {
            imageDir.mkdir();
        }
        
        if (!outputDir.isDirectory()) {
        	outputDir.mkdir();
        }

        launch(args);
    }
}
