package av.code.wicked.view;

import java.util.Objects;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Renders ruler ticks and labels around the drawing canvas using a Cartesian coordinate frame.
 */
public final class AxisOverlay extends Pane {

    private static final double TICK_LENGTH = 8;
    private static final double MIN_PIXEL_STEP = 70;

    private final Group graphics = new Group();
    private CoordinateMapper mapper;
    private Pane verticalStrip;
    private Pane horizontalStrip;

    public AxisOverlay() {
        setPickOnBounds(false);
        setMouseTransparent(true);
        getChildren().add(graphics);
        widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        heightProperty().addListener((obs, oldVal, newVal) -> redraw());
    }

    public void configure(CoordinateMapper mapper, Pane verticalStrip, Pane horizontalStrip) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.verticalStrip = Objects.requireNonNull(verticalStrip, "verticalStrip");
        this.horizontalStrip = Objects.requireNonNull(horizontalStrip, "horizontalStrip");
        this.mapper.widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        this.mapper.heightProperty().addListener((obs, oldVal, newVal) -> redraw());
        redraw();
    }

    private void redraw() {
        graphics.getChildren().clear();
        if (verticalStrip != null) {
            verticalStrip.getChildren().clear();
        }
        if (horizontalStrip != null) {
            horizontalStrip.getChildren().clear();
        }
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0 || mapper == null) {
            return;
        }
        drawBorder(width, height);
        drawHorizontalRuler(width, height);
        drawVerticalRuler(width, height);
    }

    private void drawBorder(double width, double height) {
        Line top = new Line(0, 0, width, 0);
        Line bottom = new Line(0, height, width, height);
        Line left = new Line(0, 0, 0, height);
        Line right = new Line(width, 0, width, height);
        top.setStroke(Color.BLACK);
        bottom.setStroke(Color.BLACK);
        left.setStroke(Color.BLACK);
        right.setStroke(Color.BLACK);
        graphics.getChildren().addAll(top, bottom, left, right);
    }

    private void drawHorizontalRuler(double width, double height) {
        if (horizontalStrip == null) {
            return;
        }
        double step = computeStep(width);
        double offset = verticalStrip != null ? verticalStrip.getWidth() : 0;
        for (double value = 0; value <= width + 0.5; value += step) {
            double x = Math.min(value, width);
            Line tick = new Line(x, height, x, height - TICK_LENGTH);
            tick.setStroke(Color.BLACK);
            graphics.getChildren().add(tick);
            if (Math.abs(value) > 1e-6) {
                addLabel(horizontalStrip, formatValue(value), offset + x, horizontalStrip.getHeight() * 0.25);
            }
        }
    }

    private void drawVerticalRuler(double width, double height) {
        if (verticalStrip == null) {
            return;
        }
        double step = computeStep(height);
        for (double offset = 0; offset <= height + 0.5; offset += step) {
            double y = Math.min(height - offset, height);
            Line tick = new Line(0, y, TICK_LENGTH, y);
            tick.setStroke(Color.BLACK);
            graphics.getChildren().add(tick);
            double value = mapper.toModelY(y);
            if (Math.abs(value) > 1e-6) {
                addLabel(verticalStrip, formatValue(value), verticalStrip.getWidth() * 0.2, y);
            }
        }
    }

    private double computeStep(double span) {
        double rawStep = Math.max(MIN_PIXEL_STEP, span / 8d);
        double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
        double normalized = rawStep / magnitude;
        double nice;
        if (normalized < 1.5) {
            nice = 1;
        } else if (normalized < 3) {
            nice = 2;
        } else if (normalized < 7) {
            nice = 5;
        } else {
            nice = 10;
        }
        return nice * magnitude;
    }

    private void addLabel(Pane host, String text, double absoluteX, double absoluteY) {
        Label label = new Label(text);
        label.setPadding(Insets.EMPTY);
        label.setTextFill(Color.BLACK);
        host.getChildren().add(label);
        Runnable align = () -> {
            double width = label.prefWidth(-1);
            double height = label.prefHeight(-1);
            double x = Math.max(2, Math.min(host.getWidth() - width - 2, absoluteX - width / 2 - (host == horizontalStrip && verticalStrip != null ? verticalStrip.getWidth() : 0)));
            double y = Math.max(2, Math.min(host.getHeight() - height - 2, absoluteY - height / 2));
            label.setLayoutX(x);
            label.setLayoutY(y);
        };
        label.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> align.run());
        host.widthProperty().addListener((obs, oldVal, newVal) -> align.run());
        host.heightProperty().addListener((obs, oldVal, newVal) -> align.run());
        align.run();
    }

    private String formatValue(double value) {
        double rounded = Math.rint(value * 10) / 10;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-4) {
            return String.format("%.0f", rounded);
        }
        return String.format("%.1f", rounded);
    }
}
