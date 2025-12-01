package av.code.wicked;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import av.code.wicked.hull.HullAction;
import av.code.wicked.hull.HullAnimationController;
import av.code.wicked.hull.HullStep;
import av.code.wicked.hull.MonotoneChainHull;
import av.code.wicked.view.AxisOverlay;
import av.code.wicked.view.CoordinateMapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Primary JavaFX controller: bootstraps the stage, captures user input, invokes
 * {@link MonotoneChainHull} to produce {@link HullStep}s, and delegates playback to
 * {@link HullAnimationController} so the canvas can visualize the convex hull evolution.
 */
public class UIController {
    private static final int RANDOM_POINT_COUNT = 25;
    private static final double POINT_RADIUS = 4.0;
    private static final Duration ANIMATION_INTERVAL = Duration.millis(600);
    private static final Color COLOR_POINT = Color.DODGERBLUE;
    private static final Color COLOR_HIGHLIGHT = Color.ORANGE;

    private final Stage stage;
    private final ObservableList<Point2D> points = FXCollections.observableArrayList();
    private final RandomPointGenerator pointGenerator = new RandomPointGenerator();
    private final Map<Point2D, Circle> pointNodes = new HashMap<>();
    private final MonotoneChainHull hullSolver = new MonotoneChainHull();
    private final CoordinateMapper coordinateMapper = new CoordinateMapper();
    private final ChangeListener<Number> canvasResizeListener = (obs, oldVal, newVal) -> refreshViewProjection();

    private HullAnimationController animationController;
    private Polyline upperHullLine;
    private Polyline lowerHullLine;
    private Polyline finalHullLine;
    private Circle highlightedPoint;
    private Color highlightedPointBaseColor;
    private HullStep lastRenderedStep;

    @FXML private Pane pointCanvas;
    @FXML private StackPane canvasStack;
    @FXML private AxisOverlay axisOverlay;
    @FXML private Button clearButton;
    @FXML private Button randomPointsButton;
    @FXML private Button computeButton;
    @FXML private Button playPauseButton;
    @FXML private Button stepButton;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;

    // Stage bootstrap ------------------------------------------------------

    public UIController(Stage stage) {
        this.stage = stage;
    }

    public void launchUI() {
        try {
            Parent root = loadView();
            Scene scene = buildScene(root);
            showStage(scene);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize UI", ex);
        }
    }

    private Parent loadView() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("MainView.fxml"));
        loader.setController(this);
        return loader.load();
    }

    private Scene buildScene(Parent root) {
        return new Scene(root, 1280, 720);
    }

    private void showStage(Scene scene) {
        stage.setTitle("Convex Hull");
        stage.setScene(scene);
        stage.show();
    }

    // FXML lifecycle -------------------------------------------------------

    @FXML
    private void initialize() {
        configureCanvasInfrastructure();
        wireCanvasClicks();
        wireControlButtons();
        initializeHullLayers();
        initializeAnimationController();
        disableTransportControls();
        updateStatus("Ready.");
    }

    private void configureCanvasInfrastructure() {
        if (pointCanvas == null) {
            return;
        }
        coordinateMapper.bindTo(pointCanvas.widthProperty(), pointCanvas.heightProperty());
        pointCanvas.widthProperty().addListener(canvasResizeListener);
        pointCanvas.heightProperty().addListener(canvasResizeListener);
        if (axisOverlay != null) {
            if (canvasStack != null) {
                axisOverlay.prefWidthProperty().bind(canvasStack.widthProperty());
                axisOverlay.prefHeightProperty().bind(canvasStack.heightProperty());
            } else {
                axisOverlay.prefWidthProperty().bind(pointCanvas.widthProperty());
                axisOverlay.prefHeightProperty().bind(pointCanvas.heightProperty());
            }
            axisOverlay.setCoordinateMapper(coordinateMapper);
        }
        refreshViewProjection();
    }

    private void wireCanvasClicks() {
        if (pointCanvas == null) {
            return;
        }
        pointCanvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                addPoint(event.getX(), event.getY());
                invalidateHullAnimation("Point added. Prepare hull again.");
            } else if (event.getButton() == MouseButton.SECONDARY) {
                removePointAt(event.getX(), event.getY());
            }
        });
    }

    private void wireControlButtons() {
        if (clearButton != null) {
            clearButton.setOnAction(event -> clearAllPoints());
        }
        if (randomPointsButton != null) {
            randomPointsButton.setOnAction(event -> {
                populateWithRandomPoints();
                invalidateHullAnimation("Random points added. Prepare hull again.");
            });
        }
        if (computeButton != null) {
            computeButton.setOnAction(event -> prepareHullAnimation());
        }
        if (playPauseButton != null) {
            playPauseButton.setOnAction(event -> togglePlayPause());
        }
        if (stepButton != null) {
            stepButton.setOnAction(event -> stepAnimation());
        }
        if (resetButton != null) {
            resetButton.setOnAction(event -> resetAnimation());
        }
    }

    private void initializeHullLayers() {
        if (pointCanvas == null) {
            return;
        }
        removeExistingHullLines();
        upperHullLine = createHullPolyline(Color.CRIMSON);
        lowerHullLine = createHullPolyline(Color.LIMEGREEN);
        finalHullLine = createHullPolyline(Color.BLUE);
    }

    private Polyline createHullPolyline(Color stroke) {
        Polyline line = new Polyline();
        line.setStroke(stroke);
        line.setStrokeWidth(2);
        pointCanvas.getChildren().add(line);
        return line;
    }

    private void removeExistingHullLines() {
        pointCanvas.getChildren().removeAll(upperHullLine, lowerHullLine, finalHullLine);
    }

    // Animation orchestration ----------------------------------------------

    private void initializeAnimationController() {
        animationController = new HullAnimationController(ANIMATION_INTERVAL);
        animationController.setStepConsumer(this::applyHullStep);
        animationController.setResetListener(this::resetHullVisualization);
        animationController.setCompletionListener(() -> {
            playPauseButton.setText("Replay");
            stepButton.setDisable(true);
            updateStatus("Hull complete. Press Replay or Reset.");
        });
    }

    private void prepareHullAnimation() {
        if (points.size() < 3) {
            updateStatus("Need at least 3 points to compute a convex hull.");
            return;
        }
        animationController.loadSteps(calculateHullSteps());
        enableTransportControls();
        playPauseButton.setText("Play");
        stepButton.setDisable(false);
        updateStatus("Hull prepared. Press Play or Step.");
    }

    private List<HullStep> calculateHullSteps() {
        return hullSolver.compute(points);
    }

    private void togglePlayPause() {
        if (playPauseButton.isDisable() || !animationController.hasSteps()) {
            return;
        }
        if (animationController.isPlaying()) {
            animationController.pause();
            playPauseButton.setText("Play");
            stepButton.setDisable(false);
            updateStatus("Animation paused.");
        } else {
            animationController.play();
            playPauseButton.setText("Pause");
            stepButton.setDisable(true);
            updateStatus("Animation playing...");
        }
    }

    private void stepAnimation() {
        if (stepButton.isDisable() || !animationController.hasSteps()) {
            return;
        }
        animationController.stepForward();
        playPauseButton.setText("Play");
    }

    private void resetAnimation() {
        if (resetButton.isDisable()) {
            return;
        }
        animationController.reset();
        playPauseButton.setText("Play");
        stepButton.setDisable(false);
        resetHullVisualization();
        resetPointColors();
        updateStatus("Animation reset.");
    }

    private void applyHullStep(HullStep step) {
        lastRenderedStep = step;
        renderHull(step);
        highlightFocusPoint(step.focusPoint());
        updateStatus("Step " + step.stepNumber() + ": " + step.description());
    }

    private void renderHull(HullStep step) {
        if (upperHullLine == null || lowerHullLine == null || finalHullLine == null || step == null) {
            return;
        }

        populatePolyline(upperHullLine, step.upperHull());
        populatePolyline(lowerHullLine, step.lowerHull());

        finalHullLine.getPoints().clear();
        if (step.action() == HullAction.FINALIZED) {
            List<Point2D> finalPath = new ArrayList<>(step.upperHull());
            if (!step.lowerHull().isEmpty()) {
                finalPath.addAll(step.lowerHull());
            }
            if (!finalPath.isEmpty()) {
                populatePolyline(finalHullLine, finalPath);
                if (finalPath.size() > 1) {
                    Point2D firstView = coordinateMapper.toView(finalPath.get(0));
                    finalHullLine.getPoints().addAll(firstView.getX(), firstView.getY());
                }
            }
        }

        pointCanvas.getChildren().removeAll(upperHullLine, lowerHullLine, finalHullLine);
        pointCanvas.getChildren().addAll(upperHullLine, lowerHullLine, finalHullLine);
    }

    private void populatePolyline(Polyline polyline, List<Point2D> path) {
        List<Double> coordinates = polyline.getPoints();
        coordinates.clear();
        path.forEach(point -> {
            Point2D viewPoint = coordinateMapper.toView(point);
            coordinates.add(viewPoint.getX());
            coordinates.add(viewPoint.getY());
        });
    }

    private void highlightFocusPoint(Point2D modelPoint) {
        if (highlightedPoint != null) {
            highlightedPoint.setFill(highlightedPointBaseColor != null ? highlightedPointBaseColor : COLOR_POINT);
            highlightedPoint = null;
            highlightedPointBaseColor = null;
        }
        if (modelPoint == null) {
            return;
        }
        Circle circle = pointNodes.get(modelPoint);
        if (circle != null) {
            highlightedPoint = circle;
            highlightedPointBaseColor = (Color) circle.getFill();
            circle.setFill(COLOR_HIGHLIGHT);
        }
    }

    private void resetHullVisualization() {
        if (upperHullLine != null) {
            upperHullLine.getPoints().clear();
        }
        if (lowerHullLine != null) {
            lowerHullLine.getPoints().clear();
        }
        if (finalHullLine != null) {
            finalHullLine.getPoints().clear();
        }
        lastRenderedStep = null;
        highlightFocusPoint(null);
    }

    private void resetPointColors() {
        pointNodes.values().forEach(circle -> circle.setFill(COLOR_POINT));
        highlightedPoint = null;
        highlightedPointBaseColor = null;
    }

    // Point management -----------------------------------------------------

    private void addPoint(double viewX, double viewY) {
        addModelPoint(coordinateMapper.toModel(viewX, viewY));
    }

    private void addPoints(List<Point2D> modelPoints) {
        modelPoints.forEach(this::addModelPoint);
    }

    private void addModelPoint(Point2D modelPoint) {
        points.add(modelPoint);
        Circle circle = createPointCircle(modelPoint);
        pointNodes.put(modelPoint, circle);
    }

    private void populateWithRandomPoints() {
        double width = resolveCanvasDimension(pointCanvas.getWidth(), pointCanvas.getScene() != null ? pointCanvas.getScene().getWidth() : 0);
        double height = resolveCanvasDimension(pointCanvas.getHeight(), pointCanvas.getScene() != null ? pointCanvas.getScene().getHeight() : 0);
        List<Point2D> generated = pointGenerator.generatePoints(RANDOM_POINT_COUNT, width, height, POINT_RADIUS);
        List<Point2D> modelPoints = new ArrayList<>(generated.size());
        generated.forEach(viewPoint -> modelPoints.add(coordinateMapper.toModel(viewPoint)));
        addPoints(modelPoints);
    }

    private void clearAllPoints() {
        points.clear();
        pointNodes.clear();
        if (pointCanvas != null) {
            pointCanvas.getChildren().clear();
            initializeHullLayers();
        }
        lastRenderedStep = null;
        invalidateHullAnimation("Canvas cleared.");
    }

    private Circle createPointCircle(Point2D point) {
        Point2D viewPoint = coordinateMapper.toView(point);
        Circle circle = new Circle(viewPoint.getX(), viewPoint.getY(), POINT_RADIUS, COLOR_POINT);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1.5);
        attachTooltip(circle, point);
        enableRemoval(circle, point);
        pointCanvas.getChildren().add(circle);
        return circle;
    }

    private void attachTooltip(Circle circle, Point2D point) {
        Tooltip tooltip = new Tooltip(formatPoint(point));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(circle, tooltip);
    }

    private void enableRemoval(Circle circle, Point2D point) {
        circle.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                points.remove(point);
                pointNodes.remove(point);
                pointCanvas.getChildren().remove(circle);
                invalidateHullAnimation("Point removed. Prepare hull again.");
                event.consume();
            }
        });
    }

    // Utility helpers ------------------------------------------------------

    private void invalidateHullAnimation(String reason) {
        if (animationController != null) {
            animationController.reset();
        }
        disableTransportControls();
        playPauseButton.setText("Play");
        updateStatus(reason);
    }

    private void enableTransportControls() {
        playPauseButton.setDisable(false);
        stepButton.setDisable(false);
        resetButton.setDisable(false);
    }

    private void disableTransportControls() {
        playPauseButton.setDisable(true);
        stepButton.setDisable(true);
        resetButton.setDisable(true);
        playPauseButton.setText("Play");
    }

    private double resolveCanvasDimension(double paneExtent, double fallbackExtent) {
        return paneExtent > 0 ? paneExtent : fallbackExtent;
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private String formatPoint(Point2D modelPoint) {
        return String.format("(%.1f, %.1f)", modelPoint.getX(), modelPoint.getY());
    }

    private void refreshViewProjection() {
        if (pointCanvas == null || coordinateMapper.getWidth() <= 0 || coordinateMapper.getHeight() <= 0) {
            return;
        }
        points.forEach(point -> {
            Circle circle = pointNodes.get(point);
            if (circle != null) {
                Point2D viewPoint = coordinateMapper.toView(point);
                circle.setCenterX(viewPoint.getX());
                circle.setCenterY(viewPoint.getY());
            }
        });
        if (lastRenderedStep != null) {
            renderHull(lastRenderedStep);
        }
    }

    private void removePointAt(double x, double y) {
        Point2D modelPoint = coordinateMapper.toModel(x, y);
        points.remove(modelPoint);
        Circle circle = pointNodes.remove(modelPoint);
        if (circle != null) {
            pointCanvas.getChildren().remove(circle);
            invalidateHullAnimation("Point removed. Prepare hull again.");
        } else {
            // If no exact point is found, attempt to find and remove the nearest point within a certain radius
            double radius = POINT_RADIUS * 2; // Search within twice the point radius
            List<Point2D> nearbyPoints = new ArrayList<>();
            for (Point2D point : points) {
                if (point.distance(modelPoint) <= radius) {
                    nearbyPoints.add(point);
                }
            }
            if (!nearbyPoints.isEmpty()) {
                Point2D nearestPoint = nearbyPoints.get(0);
                points.remove(nearestPoint);
                Circle nearestCircle = pointNodes.remove(nearestPoint);
                if (nearestCircle != null) {
                    pointCanvas.getChildren().remove(nearestCircle);
                    invalidateHullAnimation("Point removed. Prepare hull again.");
                }
            }
        }
    }
}
