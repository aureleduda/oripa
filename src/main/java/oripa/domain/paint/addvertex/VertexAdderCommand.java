/**
 * ORIPA - Origami Pattern Editor
 * Copyright (C) 2013-     ORIPA OSS Project  https://github.com/oripa/oripa
 * Copyright (C) 2005-2009 Jun Mitani         http://mitani.cs.tsukuba.ac.jp/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package oripa.domain.paint.addvertex;

import oripa.domain.cptool.Painter;
import oripa.domain.paint.PaintContext;
import oripa.util.Command;

/**
 * @author OUCHI Koji
 *
 */
public class VertexAdderCommand implements Command {
	private final PaintContext context;

	public VertexAdderCommand(final PaintContext context) {
		this.context = context;
	}

	@Override
	public void execute() {
		if (context.getVertexCount() == 0 || context.getLineCount() != 1) {
			throw new IllegalArgumentException(
					String.format("wrong argument. there should be %d pickedVertices and %d pickedLines", 1, 1));
		}

		context.creasePatternUndo().pushUndoInfo();

		Painter painter = context.getPainter();

		if (!painter.addVertexOnLine(
				context.popLine(), context.popVertex())) {
			context.creasePatternUndo().undo();
		}

		context.clear(false);
	}
}
