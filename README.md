# Convex Hull Visualizer

A JavaFX desktop application that demonstrates Andrew's monotone-chain algorithm step by step. Click to drop points, generate random datasets, then watch the upper and lower hulls form in distinct colors while detailed status updates describe each iteration.

## Highlights

- **Point-first workflow** – add/remove points directly on the canvas or let the random generator populate a playground.
- **Transport-style controls** – prepare, play, pause, single-step, or reset the animation at any time.
- **Upper vs. lower hull clarity** – separate polylines (crimson/green) emphasize how the chain grows before it finally closes (blue).
- **Contextual status feed** – every iteration echoes the matching algorithm step (1–14) so the math lines up with the visuals.

## Programming Style & Architecture

| Layer | Responsibility | Key Classes |
| --- | --- | --- |
| **UI orchestration** | Bootstraps the stage, wires mouse/toolbar events, and renders hull polylines without duplicating algorithm logic. | `UIController` |
| **Algorithm core** | Runs Andrew's monotone chain once, emitting immutable snapshots that describe the state after each numbered step. | `MonotoneChainHull`, `HullStep`, `HullAction` |
| **Animation driver** | Plays those snapshots back on a timeline or one-by-one, keeping the UI responsive and the solver stateless. | `HullAnimationController` |

This separation means:
- View code stays focused on JavaFX concerns (scene graph layers, tooltips, highlighting) while data flow remains immutable.
- The solver never touches JavaFX types beyond `Point2D`, simplifying testing and enabling alternate renderers if desired.
- Animation state (play/pause/step) is encapsulated, so transport controls simply toggle the controller rather than micromanage timers.

## Getting Started

```powershell
Clone Project from GitHub.
Open in IntelliJ IDEA.
Execute main method.
IntelliJ handles everything.
```

> Note: Ensure proper Maven configuration, especially when using another IDE as IntelliJ.
> Maven is required to resolve JavaFX dependencies.

## Usage Tips

1. Click anywhere on the canvas to drop a point; right-click to remove it.
2. Use **Add Random Points** for a quick dataset, then **Prepare Hull** to build the step list.
3. Hit **Play** to animate, **Pause** to inspect, **Step** to advance manually, and **Reset** to clear colors & outlines without losing points.
4. Watch the status label for the active algorithm step (mirrors the numbered pseudocode in the README and source comments).

## License

MIT (see repository).
