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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oripa.application.estimation.EstimationResultFileAccess;
import oripa.domain.fold.FoldedModel;
import oripa.domain.fold.origeom.OverlapRelation;
import oripa.gui.view.util.ColorRGBPanel;
import oripa.gui.view.util.Dialogs;
import oripa.gui.view.util.GridBagConstraintsBuilder;
import oripa.gui.view.util.ListItemSelectionPanel;
import oripa.gui.view.util.TitledBorderFactory;
import oripa.persistence.entity.FoldedModelDAO;
import oripa.persistence.entity.FoldedModelFilterSelector;
import oripa.persistence.entity.exporter.FoldedModelEntity;
import oripa.resource.ResourceHolder;
import oripa.resource.ResourceKey;
import oripa.resource.StringID;

public class EstimationResultUI extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(EstimationResultUI.class);

	private static final long serialVersionUID = 1L;

	private final ResourceHolder resources = ResourceHolder.getInstance();

	// JPanel drawing the model estimation
	private FoldedModelScreen screen;

	// setup components used
	private final ListItemSelectionPanel<OverlapRelation> answerSelectionPanel = new ListItemSelectionPanel<>("");

	private final JCheckBox orderCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.ORDER_FLIP_ID));
	private final JCheckBox shadowCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SHADOW_ID));
	private final JCheckBox useColorCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.USE_COLOR_ID));
	private final JCheckBox edgeCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.EDGE_ID));
	private final JCheckBox fillFaceCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.FILL_FACE_ID));

	private final ColorRGBPanel frontColorRGBPanel = new ColorRGBPanel(this, DefaultColors.FRONT,
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.FACE_COLOR_FRONT_ID));
	private final ColorRGBPanel backColorRGBPanel = new ColorRGBPanel(this, DefaultColors.BACK,
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.FACE_COLOR_BACK_ID));

	private final JButton saveColorsButton = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SAVE_COLORS_ID));

	private final JButton exportButton = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.EXPORT_ID));

	private final TitledBorderFactory titledBorderFactory = new TitledBorderFactory();

	private String lastFilePath = null;

	private FoldedModel foldedModel;
	private OverlapRelation overlapRelation;

	private BiConsumer<Color, Color> saveColorsListener;

	/**
	 * This is the default constructor
	 */
	public EstimationResultUI() {
		super();
		try {
			initialize();
		} catch (Exception e) {
			Dialogs.showErrorDialog(this,
					resources.getString(ResourceKey.ERROR, StringID.Error.ER_UI_INIT_FAILED_ID), e);
		}
	}

	/**
	 * set Screen displaying the folded Model Estimation
	 *
	 * @param s
	 *            {@code FoldedModelScreen} to be used
	 */
	public void setScreen(final FoldedModelScreen s) {
		screen = s;
	}

	/**
	 * Set Model to be displayed and update index label
	 *
	 * @param foldedModel
	 *            {@code FoldedModel} to be displayed
	 */
	public void setModel(final FoldedModel foldedModel) {
		this.foldedModel = foldedModel;
		answerSelectionPanel.setItems(foldedModel.getOverlapRelations());
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {

		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(216, 200));

		var gbBuilder = new GridBagConstraintsBuilder(1).setAnchor(GridBagConstraints.FIRST_LINE_START)
				.setFill(GridBagConstraints.HORIZONTAL).setWeight(1, 0.0);

		add(createAnswerShiftPanel(), gbBuilder.getLineField());
		add(createConfigPanel(), gbBuilder.getLineField());
		add(createColorPanel(), gbBuilder.getLineField());

		gbBuilder.setWeight(1, 1).setFill(GridBagConstraints.BOTH);
		add(new JPanel(), gbBuilder.getLineField());

		gbBuilder.setWeight(1, 0.0).setFill(GridBagConstraints.HORIZONTAL)
				.setAnchor(GridBagConstraints.LAST_LINE_START);
		add(exportButton, gbBuilder.getLineField());

		initialCheckBoxSetting();
		addActionListenersToComponents();
	}

	/**
	 * register listeners with all used components
	 */
	private void addActionListenersToComponents() {
		answerSelectionPanel.addPropertyChangeListener(ListItemSelectionPanel.ITEM,
				e -> {
					var newValue = (OverlapRelation) e.getNewValue();
					overlapRelation = newValue;
					screen.setOverlapRelation(newValue);
				});

		orderCheckBox.addItemListener(e -> {
			screen.flipFaces(e.getStateChange() == ItemEvent.SELECTED);
		});
		shadowCheckBox.addItemListener(e -> {
			screen.shadeFaces(e.getStateChange() == ItemEvent.SELECTED);
		});
		useColorCheckBox.addItemListener(e -> {
			screen.setUseColor(e.getStateChange() == ItemEvent.SELECTED);
		});
		edgeCheckBox.addItemListener(e -> {
			screen.drawEdge(e.getStateChange() == ItemEvent.SELECTED);
		});
		fillFaceCheckBox.addItemListener(e -> {
			screen.setFillFace(e.getStateChange() == ItemEvent.SELECTED);
		});

		ChangeListener colorRGBChangeListener = (e) -> {
			var frontColor = frontColorRGBPanel.getColor();
			var backColor = backColorRGBPanel.getColor();
			screen.setColors(frontColor, backColor);
			screen.redrawOrigami();
		};
		frontColorRGBPanel.addChangeListener(colorRGBChangeListener);
		backColorRGBPanel.addChangeListener(colorRGBChangeListener);

		saveColorsButton.addActionListener(
				e -> saveColorsListener.accept(frontColorRGBPanel.getColor(), backColorRGBPanel.getColor()));

		exportButton.addActionListener(e -> export());
	}

	private void initialCheckBoxSetting() {
		useColorCheckBox.setSelected(true);
		edgeCheckBox.setSelected(true);
		fillFaceCheckBox.setSelected(true);
	}

	private JPanel createAnswerShiftPanel() {
		var answerShiftPanel = new JPanel();

		answerShiftPanel.setLayout(new GridBagLayout());
		answerShiftPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this,
				resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.ANSWERS_PANEL_ID)));

		answerShiftPanel.add(answerSelectionPanel);

		return answerShiftPanel;
	}

	private JPanel createConfigPanel() {
		var configPanel = new JPanel();

		configPanel.setLayout(new GridBagLayout());
		configPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this,
				resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.DRAWING_CONFIG_PANEL_ID)));

		var gbBuilder = new GridBagConstraintsBuilder(2).setAnchor(GridBagConstraints.CENTER)
				.setWeight(0.5, 0.5);

		configPanel.add(orderCheckBox, gbBuilder.getNextField());
		configPanel.add(shadowCheckBox, gbBuilder.getNextField());
		configPanel.add(useColorCheckBox, gbBuilder.getNextField());
		configPanel.add(edgeCheckBox, gbBuilder.getNextField());
		configPanel.add(fillFaceCheckBox, gbBuilder.getNextField());

		return configPanel;
	}

	private JPanel createColorPanel() {
		var colorPanel = new JPanel();

		colorPanel.setLayout(new GridBagLayout());
		colorPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this,
				resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.FACE_COLOR_PANEL_ID)));

		var gbBuilder = new GridBagConstraintsBuilder(1).setAnchor(GridBagConstraints.CENTER)
				.setWeight(1.0, 0.0);

		colorPanel.add(frontColorRGBPanel, gbBuilder.getNextField());

		colorPanel.add(backColorRGBPanel, gbBuilder.getNextField());

		colorPanel.add(saveColorsButton, gbBuilder.getNextField());
		return colorPanel;
	}

	public void setColors(final Color front, final Color back) {
		logger.debug("Front color = {}", front);
		logger.debug("Back color = {}", back);
		frontColorRGBPanel.setColor(front == null ? DefaultColors.FRONT : front);
		backColorRGBPanel.setColor(back == null ? DefaultColors.BACK : back);
	}

	public void setSaveColorsListener(final BiConsumer<Color, Color> listener) {
		saveColorsListener = listener;
	}

	/**
	 * open export dialog for current folded estimation
	 */
	private void export() {
		try {
			var filterSelector = new FoldedModelFilterSelector(screen.isFaceOrderFlipped());
			final FoldedModelDAO dao = new FoldedModelDAO(filterSelector);
			EstimationResultFileAccess fileAccess = new EstimationResultFileAccess(dao);

			var entity = new FoldedModelEntity(foldedModel.getOrigamiModel(), overlapRelation);

			lastFilePath = fileAccess.saveFile(entity, lastFilePath, this,
					filterSelector.getSavables());
		} catch (Exception ex) {
			logger.error("error: ", ex);
			Dialogs.showErrorDialog(this, resources.getString(
					ResourceKey.ERROR, StringID.Error.SAVE_FAILED_ID), ex);
		}
	}
}