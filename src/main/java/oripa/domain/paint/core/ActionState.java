package oripa.domain.paint.core;

import javax.vecmath.Vector2d;

import oripa.domain.paint.PaintContextInterface;

/**
 * An interface for (modified) State pattern with undo. Implementation of this
 * interface should determine the state to be used after every action/undo.
 *
 * @author OUCHI Koji
 */
public interface ActionState {
	/**
	 * Performs the action of this state and returns the next state.
	 *
	 * @return next state.
	 */
	public ActionState doAction(PaintContextInterface context,
			Vector2d currentPoint, boolean differentAction);

	/**
	 * Performs undo of this state and returns the previous state.
	 *
	 * @return previous state.
	 */
	public ActionState undo(PaintContextInterface context);

	public ActionState getNextState();

	public ActionState getPreviousState();

}
