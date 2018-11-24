package laskin;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application{

    public void start(Stage primaryStage) {
        StackPane pane = new StackPane();
            
        GraafinenLaskin nakyma = new GraafinenLaskin();
        pane.getChildren().add(nakyma);

        Scene scene = new Scene(pane, 200, 120);
        
        primaryStage.setTitle("graafinen laskin");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("foo");
        launch(args);
    }
}
