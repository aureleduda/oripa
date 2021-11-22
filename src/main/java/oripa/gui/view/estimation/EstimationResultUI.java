/**
 * ORIPA - Origami Pattern Editor Copyright (C) 2005-2009 Jun Mitani
 * http://mitani.cs.tsukuba.ac.jp/
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package oripa.gui.view.estimation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oripa.application.estimation.EstimationResultFileAccess;
import oripa.domain.fold.FoldedModel;
import oripa.domain.fold.OverlapRelationList;
import oripa.gui.view.util.Dialogs;
import oripa.gui.view.util.GridBagConstraintsBuilder;
import oripa.gui.view.util.TitledBorderFactory;
import oripa.persistence.entity.FoldedModelDAO;
import oripa.persistence.entity.FoldedModelFilterSelector;
import oripa.resource.ResourceHolder;
import oripa.resource.ResourceKey;
import oripa.resource.StringID;

public class EstimationResultUI extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(EstimationResultUI.class);

	private static final long serialVersionUID = 1L;
	private JButton jButtonNextAnswer = null;
	private JButton jButtonPrevAnswer = null;
	private JCheckBox jCheckBoxOrder = null;
	private JCheckBox jCheckBoxShadow = null;
	private JLabel indexLabel = null;
	private FoldedModelScreen screen;
	private JCheckBox jCheckBoxUseColor = null;
	private JCheckBox jCheckBoxEdge = null;
	private JCheckBox jCheckBoxFillFace = null;
	private JButton jButtonExport = null;

	private final ColorRGBPanel frontColorRGBPanel = new ColorRGBPanel(DefaultColors.FRONT, "Front");
	private final ColorRGBPanel backColorRGBPanel = new ColorRGBPanel(DefaultColors.BACK, "Back");

	private final TitledBorderFactory titledBorderFactory = new TitledBorderFactory();

	// TODO: create label resource and apply it.
	private final ResourceHolder resources = ResourceHolder.getInstance();

	private String lastFilePath = null;

	private FoldedModel foldedModel;
	private OverlapRelationList overlapRelationList = null;

	/**
	 * This is the default constructor
	 */
	public EstimationResultUI() {
		super();
		try {
			initialize();
		} catch (Exception e) {
			Dialogs.showErrorDialog(this, "initializatiton error (folded model UI)", e);
		}
	}

	public void setScreen(final FoldedModelScreen s) {
		screen = s;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {

		setLayout(new GridBagLayout());

		var gbBuilder = new GridBagConstraintsBuilder(1).setAnchor(GridBagConstraints.FIRST_LINE_START)
				.setFill(GridBagConstraints.HORIZONTAL).setWeight(1, 0.0);

		add(createAnswerShiftPanel(), gbBuilder.getLineField());

		add(createConfigPanel(), gbBuilder.getLineField());

		add(createColorPanel(), gbBuilder.getLineField());

		gbBuilder.setWeight(1, 1).setFill(GridBagConstraints.BOTH);
		add(new JPanel(), gbBuilder.getLineField());

		gbBuilder.setWeight(1, 0.0).setFill(GridBagConstraints.HORIZONTAL)
				.setAnchor(GridBagConstraints.LAST_LINE_START);
		add(getJButtonExport(), gbBuilder.getLineField());

		this.setPreferredSize(new Dimension(216, 200));

		updateIndexLabel();

	}

	/**
	 * @param overlapRelationList
	 */
	public void setModel(final FoldedModel foldedModel) {
		this.foldedModel = foldedModel;
		this.overlapRelationList = foldedModel.getOverlapRelationList();
	}

	private JPanel createAnswerShiftPanel() {
		var answerShiftPanel = new JPanel();

		answerShiftPanel.setLayout(new GridBagLayout());
		answerShiftPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this, "Answers"));

		var gbBuilder = new GridBagConstraintsBuilder(2).setAnchor(GridBagConstraints.CENTER)
				.setWeight(0.5, 1.0);
		answerShiftPanel.add(getJButtonPrevAnswer(), gbBuilder.getNextField());
		answerShiftPanel.add(getJButtonNextAnswer(), gbBuilder.getNextField());

		indexLabel = new JLabel();

		answerShiftPanel.add(indexLabel, gbBuilder.getLineField());

		return answerShiftPanel;
	}

	private JPanel createConfigPanel() {
		var configPanel = new JPanel();

		configPanel.setLayout(new GridBagLayout());
		configPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this, "Drawing config"));

		var gbBuilder = new GridBagConstraintsBuilder(2).setAnchor(GridBagConstraints.CENTER)
				.setWeight(0.5, 0.5);

		configPanel.add(getJCheckBoxOrder(), gbBuilder.getNextField());
		configPanel.add(getJCheckBoxShadow(), gbBuilder.getNextField());
		configPanel.add(getJCheckBoxUseColor(), gbBuilder.getNextField());
		configPanel.add(getJCheckBoxEdge(), gbBuilder.getNextField());
		configPanel.add(getJCheckBoxFillFace(), gbBuilder.getNextField());

		return configPanel;
	}

	private JPanel createColorPanel() {
		var colorPanel = new JPanel();

		colorPanel.setLayout(new GridBagLayout());
		colorPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this, "Color config"));

		var gbBuilder = new GridBagConstraintsBuilder(1).setAnchor(GridBagConstraints.CENTER)
				.setWeight(1.0, 0.0);

		ChangeListener colorChangeListener = (e) -> {
			screen.setColors(
					frontColorRGBPanel.getColor(),
					backColorRGBPanel.getColor());
			screen.redrawOrigami();
		};

		colorPanel.add(frontColorRGBPanel, gbBuilder.getNextField());
		frontColorRGBPanel.addChangeListener(colorChangeListener);

		colorPanel.add(backColorRGBPanel, gbBuilder.getNextField());
		backColorRGBPanel.addChangeListener(colorChangeListener);

		return colorPanel;
	}

	public void updateIndexLabel() {

		if (overlapRelationList == null) {
			return;
		}

		indexLabel.setText("Folded model ["
				+ (overlapRelationList.getCurrentIndex() + 1) + "/"
				+ overlapRelationList.getCount() + "]");

	}

	/**
	 * This method initializes jButtonNextAnswer
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonNextAnswer() {

		if (jButtonNextAnswer != null) {
			return jButtonNextAnswer;
		}

		jButtonNextAnswer = new JButton();
		jButtonNextAnswer.setText("Next");

		jButtonNextAnswer.addActionListener(e -> {
			overlapRelationList.setNextIndex();
			screen.redrawOrigami();
			updateIndexLabel();
		});

		return jButtonNextAnswer;
	}

	/**
	 * This method initializes jButtonPrevAnswer
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonPrevAnswer() {
		if (jButtonPrevAnswer != null) {
			return jButtonPrevAnswer;
		}

		jButtonPrevAnswer = new JButton();
		jButtonPrevAnswer.setText("Prev");

		jButtonPrevAnswer.addActionListener(e -> {
			overlapRelationList.setPrevIndex();
			screen.redrawOrigami();
			updateIndexLabel();
		});

		return jButtonPrevAnswer;
	}

	/**
	 * This method initializes jCheckBoxOrder
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxOrder() {
		if (jCheckBoxOrder != null) {
			return jCheckBoxOrder;
		}

		jCheckBoxOrder = new JCheckBox();
		jCheckBoxOrder.setText("Flip");
		jCheckBoxOrder.addItemListener(e -> {
			screen.flipFaces(e.getStateChange() == ItemEvent.SELECTED);
		});

		return jCheckBoxOrder;

	}

	/**
	 * This method initializes jCheckBoxShadow
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxShadow() {
		if (jCheckBoxShadow != null) {
			return jCheckBoxShadow;
		}

		jCheckBoxShadow = new JCheckBox();
		jCheckBoxShadow.setText("Shade");

		jCheckBoxShadow.addItemListener(e -> {
			screen.shadeFaces(e.getStateChange() == ItemEvent.SELECTED);
		});

		return jCheckBoxShadow;
	}

	/**
	 * This method initializes jCheckBoxUseColor
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxUseColor() {
		if (jCheckBoxUseColor != null) {
			return jCheckBoxUseColor;
		}

		jCheckBoxUseColor = new JCheckBox();
		jCheckBoxUseColor.setSelected(true);
		jCheckBoxUseColor.setText("Use Color");

		jCheckBoxUseColor.addItemListener(e -> {
			screen.setUseColor(e.getStateChange() == ItemEvent.SELECTED);
		});

		return jCheckBoxUseColor;
	}

	/**
	 * This method initializes jCheckBoxEdge
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxEdge() {
		if (jCheckBoxEdge != null) {
			return jCheckBoxEdge;
		}

		jCheckBoxEdge = new JCheckBox();
		jCheckBoxEdge.setSelected(true);
		jCheckBoxEdge.setText("Draw Edge");

		jCheckBoxEdge.addItemListener(e -> {
			screen.drawEdge(e.getStateChange() == ItemEvent.SELECTED);
		});

		return jCheckBoxEdge;

	}

	/**
	 * This method initializes jCheckBoxFillFace
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxFillFace() {
		if (jCheckBoxFillFace != null) {
			return jCheckBoxFillFace;
		}

		jCheckBoxFillFace = new JCheckBox();
		jCheckBoxFillFace.setSelected(true);
		jCheckBoxFillFace.setText("FillFace");

		jCheckBoxFillFace.addItemListener(e -> {
			screen.setFillFace(e.getStateChange() == ItemEvent.SELECTED);
		});

		return jCheckBoxFillFace;
	}

	/**
	 * This method initializes jButtonExport
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonExport() {
		if (jButtonExport != null) {
			return jButtonExport;
		}

		jButtonExport = new JButton();
//		jButtonExport.setPreferredSize(new Dimension(90, 25));
		jButtonExport.setText("Export");
		jButtonExport.addActionListener(e -> export());

		return jButtonExport;
	}

	private void export() {
		try {
			var filterSelector = new FoldedModelFilterSelector(screen.isFaceOrderFlipped());
			final FoldedModelDAO dao = new FoldedModelDAO(filterSelector);
			EstimationResultFileAccess fileAccess = new EstimationResultFileAccess(dao);
			lastFilePath = fileAccess.saveFile(foldedModel, lastFilePath, this,
					filterSelector.getSavables());
		} catch (Exception ex) {
			logger.error("error: ", ex);
			Dialogs.showErrorDialog(this, resources.getString(
					ResourceKey.ERROR, StringID.Error.SAVE_FAILED_ID), ex);
		}
	}
}