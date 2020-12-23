package oripa.domain.cptool;

import java.util.Collection;

import javax.vecmath.Vector2d;

import oripa.domain.creasepattern.CreasePatternInterface;
import oripa.geom.GeomUtil;
import oripa.geom.RectangleDomain;
import oripa.value.OriLine;

/**
 * A tool to modify crease pattern (or line collection)
 *
 * @author Koji
 *
 */
public class Painter {
	// FIXME all methods should return success/failure

	private final CreasePatternInterface creasePattern;

	@SuppressWarnings("unused")
	private Painter() {
		creasePattern = null;
	}

	public Painter(final CreasePatternInterface aCreasePattern) {
		creasePattern = aCreasePattern;
	}

	/**
	 * @return creasePattern
	 */
	public CreasePatternInterface getCreasePattern() {
		return creasePattern;
	}

	/**
	 * reset selection mark of all lines.
	 *
	 */
	public void resetSelectedOriLines() {
		LineSelectionModifier modifier = new LineSelectionModifier();
		modifier.resetSelectedOriLines(creasePattern);
	}

	/**
	 * set {@code true} to selection mark of all lines except the lines of paper
	 * boundary.
	 *
	 */
	public void selectAllOriLines() {
		LineSelectionModifier modifier = new LineSelectionModifier();
		modifier.selectAllOriLines(creasePattern);
	}

	/**
	 * count how many lines are selected.
	 *
	 * @return
	 */
	public int countSelectedLines() {
		LineSelectionModifier modifier = new LineSelectionModifier();
		return modifier.countSelectedLines(creasePattern);
	}

	/**
	 *
	 */
	public void removeSelectedLines() {

		ElementRemover remover = new ElementRemover();
		remover.removeSelectedLines(creasePattern);
	}

	/**
	 * add given line to crease pattern. All lines which cross with given line
	 * are divided at the cross points (given line is also divided).
	 *
	 * @param inputLine
	 *            a line to be added
	 */
	public void addLine(
			final OriLine inputLine) {

		LineAdder lineAdder = new LineAdder();
		lineAdder.addLine(inputLine, creasePattern);
	}

	public void pasteLines(
			final Collection<OriLine> lines) {

		LinePaster paster = new LinePaster();
		paster.paste(lines, creasePattern);
	}

	/**
	 * add mirrored lines
	 *
	 * @param baseLine
	 *            a line to be the axis of symmetry
	 * @param lines
	 *            lines to be mirrored
	 */
	public void mirrorCopyBy(final OriLine baseLine,
			final Collection<OriLine> lines) {

		LineMirror mirror = new LineMirror();
		Collection<OriLine> copiedLines = mirror.createMirroredLines(baseLine, lines);

		LineAdder adder = new LineAdder();
		adder.addAll(copiedLines, creasePattern);
	}

	/**
	 * remove given line from the crease pattern.
	 *
	 * @param l
	 *            the line to be removed
	 */
	public void removeLine(
			final OriLine l) {

		ElementRemover remover = new ElementRemover();
		remover.removeLine(l, creasePattern);
	}

	/**
	 * remove all given lines from the crease pattern.
	 *
	 * @param lines
	 *            to be removed
	 */
	public void removeLines(
			final Collection<OriLine> lines) {

		ElementRemover remover = new ElementRemover();
		remover.removeLines(lines, creasePattern);
	}

	/**
	 * remove given vertex from the crease pattern.
	 *
	 * @param v
	 *            the vertex to be removed
	 */
	public void removeVertex(
			final Vector2d v) {

		ElementRemover remover = new ElementRemover();
		remover.removeVertex(v, creasePattern);
	}

	/**
	 * add vertex on a line
	 *
	 * @param line
	 *            the line on which the new vertex will be added
	 * @param v
	 *            the vertex to be added
	 * @return true if the vertex is added.
	 */
	public boolean addVertexOnLine(
			final OriLine line, final Vector2d v) {

		LineDivider divider = new LineDivider();
		Collection<OriLine> dividedLines = divider.divideLineInCollection(line, v, creasePattern,
				creasePattern.getPaperSize());

		if (dividedLines == null) {
			return false;
		}

		ElementRemover remover = new ElementRemover();
		remover.removeLine(line, creasePattern);

		LineAdder adder = new LineAdder();
		adder.addAll(dividedLines, creasePattern);

		return true;
	}

	/**
	 * add three inner lines of rabbit-ear molecule for given triangle
	 *
	 * @param v0
	 *            the vertex of the triangle
	 * @param v1
	 *            the vertex of the triangle
	 * @param v2
	 *            the vertex of the triangle
	 * @param lineType
	 *            the type of the new lines
	 */
	public void addTriangleDivideLines(
			final Vector2d v0, final Vector2d v1, final Vector2d v2, final OriLine.Type lineType) {

		Vector2d c = GeomUtil.getIncenter(v0, v1, v2);
		LineAdder adder = new LineAdder();
		adder.addLine(new OriLine(c, v0, lineType), creasePattern);
		adder.addLine(new OriLine(c, v1, lineType), creasePattern);
		adder.addLine(new OriLine(c, v2, lineType), creasePattern);
	}

	/**
	 * add perpendicular bisector line between v0 and v1
	 *
	 * @param v0
	 *            an end point of split line
	 * @param v1
	 *            an end point of split line
	 * @param domain
	 *            including the paper
	 * @param lineType
	 *            the type of the bisector line
	 */
	public void addPBisector(
			final Vector2d v0, final Vector2d v1, final RectangleDomain domain,
			final OriLine.Type lineType) {

		BisectorFactory factory = new BisectorFactory();
		OriLine bisector = factory.createPerpendicularBisector(v0, v1,
				domain, lineType);

		LineAdder adder = new LineAdder();
		adder.addLine(bisector, creasePattern);
	}

	/**
	 * add a bisector line from v1 to given line.
	 *
	 * @param v0
	 *            the end point of a line incident to the angle point
	 * @param v1
	 *            the vertex of the angle
	 * @param v2
	 *            the end point of a line incident to the angle point
	 * @param l
	 *            a line which will cross the bisector line. the cross point
	 *            will be the end point of the bisector line.
	 * @param lineType
	 *            the type of the bisector line
	 */
	public void addBisectorLine(
			final Vector2d v0, final Vector2d v1, final Vector2d v2,
			final OriLine l, final OriLine.Type lineType) {

		BisectorFactory factory = new BisectorFactory();
		OriLine bisector = factory.createAngleBisectorLine(v0, v1, v2, l, lineType);

		LineAdder adder = new LineAdder();
		adder.addLine(bisector, creasePattern);

	}

	/**
	 * change type of given line.
	 *
	 * @param l
	 *            the line to be changed
	 * @param from
	 *            the type before change.
	 * @param to
	 *            the type after change.
	 */
	public void alterLineType(
			final OriLine l, final TypeForChange from, final TypeForChange to) {

		LineTypeChanger changer = new LineTypeChanger();
		changer.alterLineType(l, creasePattern, from, to);
	}

	/**
	 * change type of given lines.
	 *
	 * @param lines
	 *            the lines to be changed
	 * @param from
	 *            the type before change.
	 * @param to
	 *            the type after change.
	 */
	public void alterLineTypes(
			final Collection<OriLine> lines, final TypeForChange from, final TypeForChange to) {
		LineTypeChanger changer = new LineTypeChanger();
		changer.alterLineTypes(lines, creasePattern, from, to);
	}

	/**
	 * v1-v2 is the symmetry line, v0-v1 is the subject to be copied.
	 *
	 * @param v0
	 *            the end point of subject line not connected to the symmetry
	 *            line
	 * @param v1
	 *            the connecting point of the subject line and the symmetry line
	 * @param v2
	 *            the end point of symmetry line not connected to the subject
	 *            line
	 * @param lineType
	 *            the type of the symmetric line ({@link OriLine.Type#VALLEY}
	 *            etc.)
	 *
	 * @return true if line is added
	 */
	public boolean addSymmetricLine(
			final Vector2d v0, final Vector2d v1, final Vector2d v2, final OriLine.Type lineType) {

		SymmetricLineFactory factory = new SymmetricLineFactory();
		OriLine symmetricLine;
		try {
			symmetricLine = factory.createSymmetricLine(v0, v1, v2, creasePattern, lineType);
		} catch (PainterCommandFailedException comEx) {
			return false;
		}

		LineAdder adder = new LineAdder();
		adder.addLine(symmetricLine, creasePattern);

		return true;
	}

	/**
	 * add possible rebouncing of the fold.
	 *
	 * @param v0
	 *            the end point of subject line not connected to the symmetry
	 *            line
	 * @param v1
	 *            the connecting point of the subject line and the symmetry line
	 * @param v2
	 *            the end point of symmetry line not connected to the subject
	 *            line
	 * @param startV
	 * @param lineType
	 *            the type of the symmetric lines
	 * @return true if lines are added
	 */
	public boolean addSymmetricLineAutoWalk(
			final Vector2d v0, final Vector2d v1, final Vector2d v2,
			final OriLine.Type lineType) {

		SymmetricLineFactory factory = new SymmetricLineFactory();

		Collection<OriLine> autoWalkLines;
		try {
			autoWalkLines = factory.createSymmetricLineAutoWalk(
					v0, v1, v2, v0, creasePattern, lineType);

		} catch (PainterCommandFailedException comEx) {
			return false;
		}
		LineAdder adder = new LineAdder();
		adder.addAll(autoWalkLines, creasePattern);

		return true;
	}

	/**
	 * add copy of selected lines with a rotation around specified center point.
	 * For a line l, this method creates rotatedLine(l, angleDeg * i) for i = 1
	 * ... repetitionCount.
	 *
	 * @param cx
	 *            x of center
	 * @param cy
	 *            y of center
	 * @param angleDeg
	 *            amount of rotation in degrees
	 * @param repetitionCount
	 * @param selectedLines
	 *            lines to be copied
	 *
	 */
	public void copyWithRotation(
			final double cx, final double cy, final double angleDeg, final int repetitionCount,
			final Collection<OriLine> selectedLines) {

		RotatedLineFactory factory = new RotatedLineFactory();

		Collection<OriLine> copiedLines = factory.createRotatedLines(
				cx, cy, angleDeg, repetitionCount,
				selectedLines, creasePattern);

		LineAdder adder = new LineAdder();
		adder.addAll(copiedLines, creasePattern);
	}

	/**
	 * add copy of selected lines with tiling.
	 *
	 * @param row
	 *            the count of tiles on x coordinate
	 * @param col
	 *            the count of tiles on y coordinate
	 * @param interX
	 *            interval length of x coordinate
	 * @param interY
	 *            interval length of y coordinate
	 * @param selectedLines
	 *            lines to be copied
	 */
	public void copyWithTiling(
			final int row, final int col, final double interX, final double interY,
			final Collection<OriLine> selectedLines) {

		TiledLineFactory factory = new TiledLineFactory();

		Collection<OriLine> copiedLines = factory.createTiledLines(
				row, col, interX, interY,
				selectedLines, creasePattern);

		LineAdder adder = new LineAdder();
		adder.addAll(copiedLines, creasePattern);
	}

	/**
	 * add copy of selected lines as the paper is filled out.
	 *
	 * @param selectedLines
	 */
	public void fillOut(
			final Collection<OriLine> selectedLines) {

		TiledLineFactory factory = new TiledLineFactory();

		Collection<OriLine> copiedLines = factory.createFullyTiledLines(
				selectedLines, creasePattern,
				creasePattern.getPaperSize());

		LineAdder adder = new LineAdder();
		adder.addAll(copiedLines, creasePattern);

	}
}