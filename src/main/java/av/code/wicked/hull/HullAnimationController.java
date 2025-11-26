package av.code.wicked.hull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class HullAnimationController {

    private final Timeline timeline;
    private List<HullStep> steps = Collections.emptyList();
    private Iterator<HullStep> iterator = Collections.emptyIterator();
    private Consumer<HullStep> stepConsumer = step -> {};
    private Runnable resetListener = () -> {};
    private Runnable completionListener = () -> {};

    public HullAnimationController(Duration interval) {
        timeline = new Timeline(new KeyFrame(interval, event -> playNextStep()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void loadSteps(List<HullStep> steps) {
        this.steps = steps == null ? Collections.emptyList() : List.copyOf(steps);
        this.iterator = this.steps.iterator();
        resetListener.run();
    }

    public void setStepConsumer(Consumer<HullStep> stepConsumer) {
        this.stepConsumer = stepConsumer != null ? stepConsumer : step -> {};
    }

    public void setResetListener(Runnable resetListener) {
        this.resetListener = resetListener != null ? resetListener : () -> {};
    }

    public void setCompletionListener(Runnable completionListener) {
        this.completionListener = completionListener != null ? completionListener : () -> {};
    }

    public boolean hasSteps() {
        return !steps.isEmpty();
    }

    public boolean isPlaying() {
        return timeline.getStatus() == Timeline.Status.RUNNING;
    }

    public void play() {
        if (steps.isEmpty()) {
            return;
        }
        timeline.play();
    }

    public void pause() {
        timeline.pause();
    }

    public void stepForward() {
        playNextStep();
    }

    public void reset() {
        pause();
        iterator = steps.iterator();
        resetListener.run();
    }

    private void playNextStep() {
        if (iterator == null || !iterator.hasNext()) {
            pause();
            completionListener.run();
            return;
        }
        HullStep step = iterator.next();
        stepConsumer.accept(step);
    }
}

