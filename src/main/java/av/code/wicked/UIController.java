package av.code.wicked;

import java.security.SecureRandom;
import java.util.Random;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UIController {
    private static final int RANDOM_POINT_COUNT = 25;
    private static final double POINT_RADIUS = 4.0;

    private final Stage stage;
    private final ObservableList<Point2D> points = FXCollections.observableArrayList();
    private final Random random = new SecureRandom();

    @FXML private Pane pointCanvas;
    @FXML private Button clearButton;
    @FXML private Button randomPointsButton;
    @FXML private Button computeButton;

    public UIController(Stage stage) {
        this.stage = stage;
    }

    public void init() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("MainView.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("Convex Hull");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize UI", ex);
        }
    }



    @FXML
    private void initialize() {
        if (pointCanvas != null) {
            pointCanvas.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    addPoint(event.getX(), event.getY());
                }
            });
        }
        if (clearButton != null) {
            clearButton.setOnAction(event -> handleClearPoints());
        }
        if (randomPointsButton != null) {
            randomPointsButton.setOnAction(event -> handleAddRandomPoints());
        }
    }

    private void addPoint(double x, double y) {
        Point2D point = new Point2D(x, y);
        points.add(point);
        drawPoint(point);
    }

    @FXML
    private void handleClearPoints() {
        points.clear();
        pointCanvas.getChildren().clear();
    }

    @FXML
    private void handleAddRandomPoints() {
        double width = pointCanvas.getWidth();
        double height = pointCanvas.getHeight();
        if (width <= 0 || height <= 0) {
            width = pointCanvas.getScene().getWidth();
            height = pointCanvas.getScene().getHeight();
        }
        for (int i = 0; i < RANDOM_POINT_COUNT; i++) {
            double x = POINT_RADIUS + random.nextDouble() * Math.max(width - 2 * POINT_RADIUS, 0);
            double y = POINT_RADIUS + random.nextDouble() * Math.max(height - 2 * POINT_RADIUS, 0);
            addPoint(x, y);
        }
    }

    private void drawPoint(Point2D point) {
        Circle circle = new Circle(point.getX(), point.getY(), POINT_RADIUS, Color.DODGERBLUE);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1.5);
        Tooltip tooltip = new Tooltip(formatPoint(point));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(circle, tooltip);
        circle.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                points.remove(point);
                pointCanvas.getChildren().remove(circle);
                event.consume();
            }
        });
        pointCanvas.getChildren().add(circle);
    }

    private String formatPoint(Point2D point) {
        return String.format("(%.1f, %.1f)", point.getX(), point.getY());
    }
}
