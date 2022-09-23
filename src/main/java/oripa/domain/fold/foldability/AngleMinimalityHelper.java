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
package oripa.domain.fold.foldability;

/**
 * @author OUCHI Koji
 *
 */
public class AngleMinimalityHelper {
	public static final double EPS = 1e-6;

	public boolean isMinimal(final RingArrayList<LineGap> ring, final int index) {
		var previous = ring.getPrevious(index).getValue();
		var target = ring.getElement(index).getValue();
		var next = ring.getNext(index).getValue();

		if (target.getLineType() == next.getLineType()) {
			return false;
		}

		return previous.getAngle() - target.getAngle() > -EPS &&
				next.getAngle() - target.getAngle() > -EPS;
	}

	public int foldPartially(final RingArrayList<LineGap> ring, final int index) {
		var previousElement = ring.getPrevious(index);
		var targetElement = ring.getElement(index);
		var nextElement = ring.getNext(index);

		var previous = previousElement.getValue();
		var target = targetElement.getValue();
		var next = nextElement.getValue();

		previous.setAngle(previous.getAngle() + next.getAngle() - target.getAngle());

		ring.dropConnection(nextElement.getRingIndex());
		ring.dropConnection(targetElement.getRingIndex());

		return previousElement.getRingIndex();
	}
}
