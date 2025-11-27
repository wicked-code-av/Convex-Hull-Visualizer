# Convex Hull Visualizer

A JavaFX desktop application that demonstrates ***Andrew's monotone-chain algorithm***.

## Highlights

### *Minimalistic and intuitive UI that can...*

- add/remove points directly on the canvas
- generate random point sets
- calculate all steps of convex hull construction in one go
- animate the step-by-step process with play/pause/step/reset controls
- status label shows the current algorithm step

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

### Recommended: IntelliJ IDEA

- Clone project sources from GitHub.
- Open in IntelliJ IDEA.
- IntelliJ will auto-detect the Maven project and download dependencies.
- Execute main method.
- IntelliJ handles compilation and running.

> Note: JDK 23 version 23+ recommended.
> Maven is required to resolve JavaFX dependencies, especially when not using IntelliJ.

## Usage Tips

1. Click anywhere on the canvas to drop a point; right-click to remove it.
2. Use **Add Random Points** for a quick dataset, then **Prepare Hull** to build the step list.
3. Hit **Play** to animate, **Pause** to inspect, **Step** to advance manually, and **Reset** to clear colors & outlines without losing points.
4. Watch the status label for the active algorithm step (mirrors the source comments).