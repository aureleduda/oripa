package oripa.domain.paint.triangle;

import oripa.domain.paint.ObjectGraphicDrawer;
import oripa.domain.paint.PaintContextInterface;
import oripa.domain.paint.core.GraphicMouseAction;

public class TriangleSplitAction extends GraphicMouseAction {

	public TriangleSplitAction() {
		setActionState(new SelectingVertexForTriangleSplit());
	}

	@Override
	public void onDrag(final PaintContextInterface context, final boolean differentAction) {

	}

	@Override
	public void onRelease(final PaintContextInterface context, final boolean differentAction) {

	}

	@Override
	public void onDraw(final ObjectGraphicDrawer drawer, final PaintContextInterface context) {

		super.onDraw(drawer, context);

		drawPickCandidateVertex(drawer, context);
	}

	@Override
	public void onPress(final PaintContextInterface context, final boolean differentAction) {

	}

}
