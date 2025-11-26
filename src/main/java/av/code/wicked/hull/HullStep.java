package av.code.wicked.hull;

import java.util.List;

import javafx.geometry.Point2D;

public record HullStep(
        int stepNumber,
        HullAction action,
        List<Point2D> upperHull,
        List<Point2D> lowerHull,
        String description,
        Point2D focusPoint
) {}
