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
package oripa.domain.cptool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.vecmath.Vector2d;

import oripa.geom.GeomUtil;
import oripa.value.CalculationResource;
import oripa.value.OriLine;

/**
 * @author OUCHI Koji
 *
 */
public class OverlappingLineDivider {

	private final SequentialLineFactory sequentialLineFactory = new SequentialLineFactory();

	private final PointSorter pointSorter = new PointSorter();

	public void divideIfOverlap(final Collection<OriLine> dividerLines, final Collection<OriLine> lines) {
		var extractor = new OverlappingLineExtractor();

		var allLines = new HashSet<OriLine>(dividerLines);
		allLines.addAll(lines);

		var overlapGroups = extractor.extractOverlapsGroupedBySupport(allLines);

		Set<OriLine> dividerLineSet = new HashSet<>(dividerLines);
		Set<OriLine> lineSet = ConcurrentHashMap.newKeySet();

		lineSet.addAll(lines);

		overlapGroups.parallelStream().forEach(overlaps -> {
			var dividerOverlaps = overlaps.stream()
					.filter(ov -> dividerLineSet.contains(ov))
					.collect(Collectors.toSet());

			var lineOverlaps = overlaps.stream()
					.filter(ov -> !dividerOverlaps.contains(ov))
					.collect(Collectors.toSet());

			lineSet.removeAll(lineOverlaps);

			dividerOverlaps.forEach(divider -> divideLinesIfOverlap(divider, lineOverlaps));

			lineSet.addAll(lineOverlaps);
		});

		lines.clear();
		lines.addAll(lineSet);
	}

	private void divideLinesIfOverlap(final OriLine dividerLine, final Collection<OriLine> lines) {

		Set<OriLine> targettedLines = ConcurrentHashMap.newKeySet();
		Set<OriLine> splitLines = ConcurrentHashMap.newKeySet();

		lines.parallelStream()
				.forEach(line -> {
					var splitPoints = new ArrayList<Vector2d>();

					int overlapCount = GeomUtil.distinguishLineSegmentsOverlap(dividerLine.getP0(), dividerLine.getP1(),
							line.getP0(), line.getP1());

					switch (overlapCount) {
					case 2:
					case 3:
						splitPoints.addAll(createSplitPoints(line, dividerLine.getP0()));
						splitPoints.addAll(createSplitPoints(line, dividerLine.getP1()));
						break;
					default:
						return;
					}

					pointSorter.sortPointsOnLine(splitPoints, line);

					targettedLines.add(line);
					splitLines.addAll(
							sequentialLineFactory.createSequentialLines(splitPoints, line.getType()));
				});

		lines.removeAll(targettedLines);
		lines.addAll(splitLines);
	}

	private List<Vector2d> createSplitPoints(final OriLine line, final Vector2d p) {
		var points = new ArrayList<Vector2d>(List.of(line.getP0(), line.getP1()));

		// is close to segment?
		if (GeomUtil.distancePointToSegment(p, line.getP0(),
				line.getP1()) > CalculationResource.POINT_EPS) {
			return points;
		}

		if (GeomUtil.distance(p, line.getP0()) >= CalculationResource.POINT_EPS) {
			points.add(p);
		} else if (GeomUtil.distance(p, line.getP1()) >= CalculationResource.POINT_EPS) {
			points.add(p);
		}

		return points;
	}

}
