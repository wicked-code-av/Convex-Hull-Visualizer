package av.code.wicked.hull;

import java.util.List;

import javafx.geometry.Point2D;

/**
 * Snapshot emitted by {@link MonotoneChainHull} describing the current upper/lower hull
 * and metadata for {@link HullAnimationController} to replay on the JavaFX canvas.
 */
public record HullStep(
        int stepNumber,
        HullAction action,
        List<Point2D> upperHull,
        List<Point2D> lowerHull,
        String description,
        Point2D focusPoint
) {}
