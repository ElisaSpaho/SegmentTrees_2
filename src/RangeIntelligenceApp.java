import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RangeIntelligenceApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DashboardController controller = new DashboardController();
        Scene scene = controller.buildScene();

        primaryStage.setTitle("Range Intelligence Engine — Cinema Ticket Tracker");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
