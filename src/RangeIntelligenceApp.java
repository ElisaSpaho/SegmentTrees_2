import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * RangeIntelligenceApp
 * ─────────────────────────────────────────────────────────────────────────────
 * JavaFX application entry point for the Range Intelligence Engine GUI.
 *
 * This class is intentionally minimal — all scene construction is delegated
 * to DashboardController.buildScene() so the controller remains testable
 * independently of the JavaFX lifecycle.
 *
 * HOW TO COMPILE AND RUN (JavaFX required):
 *
 *   # Compile (replace <path-to-javafx-lib> with your actual JavaFX SDK lib path)
 *   javac --module-path <path-to-javafx-lib> \
 *         --add-modules javafx.controls \
 *         *.java
 *
 *   # Run
 *   java  --module-path <path-to-javafx-lib> \
 *         --add-modules javafx.controls \
 *         RangeIntelligenceApp
 *
 * For the console-only demo (no JavaFX needed):
 *   javac *.java
 *   java  Main
 */
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
