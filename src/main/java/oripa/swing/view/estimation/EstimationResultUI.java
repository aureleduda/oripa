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
package oripa.swing.view.estimation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oripa.domain.fold.FoldedModel;
import oripa.domain.fold.origeom.OverlapRelation;
import oripa.domain.fold.subface.SubFace;
import oripa.gui.view.View;
import oripa.gui.view.estimation.DefaultColors;
import oripa.gui.view.estimation.EstimationResultUIView;
import oripa.resource.ResourceHolder;
import oripa.resource.ResourceKey;
import oripa.resource.StringID;
import oripa.swing.view.util.ColorRGBPanel;
import oripa.swing.view.util.Dialogs;
import oripa.swing.view.util.GridBagConstraintsBuilder;
import oripa.swing.view.util.ListItemSelectionPanel;
import oripa.swing.view.util.SimpleModalWorker;
import oripa.swing.view.util.TitledBorderFactory;

public class EstimationResultUI extends JPanel implements EstimationResultUIView {
	private static final Logger logger = LoggerFactory.getLogger(EstimationResultUI.class);

	private static final long serialVersionUID = 1L;

	private final ResourceHolder resources = ResourceHolder.getInstance();

	// JPanel drawing the model estimation
	private FoldedModelScreen screen;

	// setup components used
	private final ListItemSelectionPanel answerSelectionPanel = new ListItemSelectionPanel("");

	private final JCheckBox filterEnabledCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.USE_FILTER_ID));
	private final JCheckBox subfaceVisibleCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SHOW_SUBFACE_ID));
	private final JComboBox<Integer> subfaceIndexCombo = new JComboBox<>();
	private final JComboBox<String> suborderIndexCombo = new JComboBox<>();

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

	private final JButton saveSVGConfigButton = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SAVE_SVG_CONFIG_ID));

	private final JButton exportButton = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.EXPORT_ID));

	private final JLabel svgFaceStrokeWidthLabel = new JLabel(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SVG_FACE_STROKEWIDTH_ID));
	private final JTextField svgFaceStrokeWidthField = new JTextField();
	private final JLabel svgPrecreaseStrokeWidthLabel = new JLabel(
			resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SVG_CREASE_STROKEWIDTH_ID));
	private final JTextField svgPrecreaseStrokeWidthField = new JTextField();

	private final TitledBorderFactory titledBorderFactory = new TitledBorderFactory();

	private FoldedModel foldedModel;
	private OverlapRelation overlapRelation;

	private List<OverlapRelation> filteredOverlapRelations;

	/**
	 * < index of selected subface, index of selected suborder >
	 */
	private final Map<Integer, Integer> filterSelectionMap = new HashMap<>();

	/**
	 * < index of subface, list< overlap relation indices > >
	 */
	private Map<Integer, List<Set<Integer>>> subfaceToOverlapRelationIndices;

	private BiConsumer<Color, Color> saveColorsListener;

	private Function<FoldedModel, Map<Integer, List<Set<Integer>>>> filterInitializationListener;

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

	/* (non Javadoc)
	 * @see oripa.gui.view.estimation.EstimationResultUIView#setModel(oripa.domain.fold.FoldedModel)
	 */
	@Override
	public void setModel(final FoldedModel foldedModel) {
		this.foldedModel = foldedModel;

		setOverlapRelations(foldedModel.getOverlapRelations());
	}

	private void initializeFilterComponents() {
		var frame = (JFrame) SwingUtilities.getWindowAncestor(this);

		var dialog = new DialogWhileComputing(frame, resources);

		var worker = new SimpleModalWorker(dialog, () -> {
			subfaceToOverlapRelationIndices = filterInitializationListener.apply(foldedModel);
			subfaceToOverlapRelationIndices.forEach((s, orders) -> filterSelectionMap.put(s, 0));
		});

		worker.executeModal();

		prepareSubfaceIndexCombo();
		prepareSuborderIndexCombo(0);

		subfaceIndexCombo.setSelectedIndex(0);
		suborderIndexCombo.setSelectedIndex(0);

	}

	private void prepareSubfaceIndexCombo() {
		subfaceIndexCombo.removeAllItems();
		subfaceToOverlapRelationIndices.forEach((s, orders) -> {
			if (orders.size() > 2) {
				subfaceIndexCombo.addItem(s);
			}
		});

		if (subfaceIndexCombo.getItemCount() == 0) {
			subfaceIndexCombo.addItem(0);
		}
	}

	private void prepareSuborderIndexCombo(final int subfaceIndex) {
		suborderIndexCombo.removeAllItems();

		var orders = subfaceToOverlapRelationIndices.get(subfaceIndex);
		for (int order = 0; order < orders.size(); order++) {
			suborderIndexCombo.addItem(order == 0 ? "none" : Integer.toString(order));
		}
	}

	private void setSubfaceToScreen(final SubFace subface) {
		if (subface == null) {
			screen.setSelectedSubface(null);
			return;
		}

		screen.setSelectedSubface(subfaceVisibleCheckBox.isSelected() ? subface.getOutline() : null);
	}

	private void setOverlapRelations(final List<OverlapRelation> overlapRelations) {
		filteredOverlapRelations = overlapRelations;
		answerSelectionPanel.setItemCount(overlapRelations.size());
	}

	private void selectOverlapRelation(final int index) {
		if (filteredOverlapRelations.size() > 0) {
			overlapRelation = filteredOverlapRelations.get(index);
		}
		screen.setOverlapRelation(overlapRelation);
	}

	@Override
	public FoldedModel getModel() {
		return foldedModel;
	}

	private List<OverlapRelation> filter() {
		// use a set for fast computation
		var filteredIndices = new HashSet<Integer>();
		for (int k = 0; k < foldedModel.getOverlapRelations().size(); k++) {
			filteredIndices.add(k);
		}

		// take AND of all selected filters
		filterSelectionMap.forEach((subfaceIndex, suborderIndex) -> {
			var selectedIndices = subfaceToOverlapRelationIndices.get(subfaceIndex).get(suborderIndex);
			filteredIndices.retainAll(selectedIndices);
		});

		return filteredIndices.stream().map(k -> foldedModel.getOverlapRelations().get(k)).collect(Collectors.toList());
	}

	private void setFilterEnabled(final boolean enabled) {
		subfaceIndexCombo.setEnabled(enabled);
		suborderIndexCombo.setEnabled(enabled);
		subfaceVisibleCheckBox.setEnabled(enabled);

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

		add(createSVGConfigPanel(), gbBuilder.getLineField());

		gbBuilder.setWeight(1, 1).setFill(GridBagConstraints.BOTH);
		add(new JPanel(), gbBuilder.getLineField());

		gbBuilder.setWeight(1, 0.0).setFill(GridBagConstraints.HORIZONTAL)
				.setAnchor(GridBagConstraints.LAST_LINE_START);
		add(exportButton, gbBuilder.getLineField());

		initializeComponentSetting();
		addActionListenersToComponents();
	}

	/**
	 * register listeners with all used components
	 */
	private void addActionListenersToComponents() {
		answerSelectionPanel.addPropertyChangeListener(ListItemSelectionPanel.INDEX,
				e -> {
					selectOverlapRelation((int) e.getNewValue());
				});

		filterEnabledCheckBox.addActionListener(e -> {
			if (filterEnabledCheckBox.isSelected()) {
				initializeFilterComponents();

				setFilterEnabled(true);

				setOverlapRelations(filter());
				selectOverlapRelation(0);

				var subface = foldedModel.getSubfaces().get(0);
				setSubfaceToScreen(subface);
			} else {
				setFilterEnabled(false);

				setOverlapRelations(foldedModel.getOverlapRelations());
				selectOverlapRelation(0);

				setSubfaceToScreen(null);
			}
		});

		subfaceIndexCombo.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				var subfaceIndex = (Integer) e.getItem();
				var suborderIndex = filterSelectionMap.get(subfaceIndex);

				setOverlapRelations(filter());
				selectOverlapRelation(0);

				var subface = foldedModel.getSubfaces().get(subfaceIndex);
				setSubfaceToScreen(subface);

				prepareSuborderIndexCombo(subfaceIndex);
				suborderIndexCombo.setSelectedIndex(suborderIndex);
			}
		});

		suborderIndexCombo.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				var subfaceIndex = (Integer) subfaceIndexCombo.getSelectedItem();
				var suborderIndex = suborderIndexCombo.getSelectedIndex();
				filterSelectionMap.put(subfaceIndex, suborderIndex);

				setOverlapRelations(filter());
				selectOverlapRelation(0);
			}
		});

		subfaceVisibleCheckBox.addActionListener(e -> {
			var subfaceIndex = (Integer) subfaceIndexCombo.getSelectedItem();
			var subface = foldedModel.getSubfaces().get(subfaceIndex);

			setSubfaceToScreen(subface);
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
	}

	private void initializeComponentSetting() {
		filterEnabledCheckBox.setSelected(false);
		setFilterEnabled(false);

		subfaceVisibleCheckBox.setSelected(true);

		useColorCheckBox.setSelected(true);
		edgeCheckBox.setSelected(true);
		fillFaceCheckBox.setSelected(true);
	}

	private JPanel createAnswerShiftPanel() {
		var answerShiftPanel = new JPanel();

		var gbBuilder = new GridBagConstraintsBuilder(1);

		answerShiftPanel.setLayout(new GridBagLayout());
		answerShiftPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this,
				resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.ANSWERS_PANEL_ID)));

		answerShiftPanel.add(answerSelectionPanel, gbBuilder.getNextField());

		var filterPanel = new JPanel();
		filterPanel.add(subfaceIndexCombo);
		filterPanel.add(suborderIndexCombo);

		answerShiftPanel.add(filterEnabledCheckBox, gbBuilder.getNextField());
		answerShiftPanel.add(subfaceVisibleCheckBox, gbBuilder.getNextField());
		answerShiftPanel.add(filterPanel, gbBuilder.getNextField());

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

	private JPanel createSVGConfigPanel() {
		var svgPanel = new JPanel();

		svgPanel.setLayout(new GridBagLayout());
		svgPanel.setBorder(titledBorderFactory.createTitledBorderFrame(this,
				resources.getString(ResourceKey.LABEL, StringID.EstimationResultUI.SVG_CONFIG_PANEL_ID)));

		var gbBuilder = new GridBagConstraintsBuilder(2).setAnchor(GridBagConstraints.CENTER)
				.setWeight(1.0, 0.0);

		svgPanel.add(svgFaceStrokeWidthLabel, gbBuilder.getNextField());
		svgPanel.add(svgFaceStrokeWidthField, gbBuilder.getNextField());

		svgPanel.add(svgPrecreaseStrokeWidthLabel, gbBuilder.getNextField());
		svgPanel.add(svgPrecreaseStrokeWidthField, gbBuilder.getNextField());

		svgPanel.add(saveSVGConfigButton, gbBuilder.getLineField());

		return svgPanel;
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

	@Override
	public void addSaveSVGConfigButtonListener(final Runnable listener) {
		addButtonListener(saveSVGConfigButton, listener);
	}

	@Override
	public void addExportButtonListener(final Runnable listener) {
		addButtonListener(exportButton, listener);
	}

	private void addButtonListener(final AbstractButton button, final Runnable listener) {
		button.addActionListener(e -> listener.run());
	}

	@Override
	public OverlapRelation getOverlapRelation() {
		return overlapRelation;
	}

	@Override
	public int getOverlapRelationIndex() {
		return foldedModel.getOverlapRelations().indexOf(overlapRelation);
	}

	@Override
	public boolean isFaceOrderFlipped() {
		return screen.isFaceOrderFlipped();
	}

	@Override
	public double getSVGFaceStrokeWidth() {
		return Double.parseDouble(svgFaceStrokeWidthField.getText());
	}

	@Override
	public void setSVGFaceStrokeWidth(final double strokeWidth) {
		svgFaceStrokeWidthField.setText(Double.toString(strokeWidth));
	}

	@Override
	public double getSVGPrecreaseStrokeWidth() {
		return Double.parseDouble(svgPrecreaseStrokeWidthField.getText());
	}

	@Override
	public void setSVGPrecreaseStrokeWidth(final double strokeWidth) {
		svgPrecreaseStrokeWidthField.setText(Double.toString(strokeWidth));
	}

	@Override
	public void setFilterInitializationListener(
			final Function<FoldedModel, Map<Integer, List<Set<Integer>>>> listener) {
		filterInitializationListener = listener;
	}

	@Override
	public Color getFrontColor() {
		return frontColorRGBPanel.getColor();
	}

	@Override
	public Color getBackColor() {
		return backColorRGBPanel.getColor();
	}

	@Override
	public void showExportErrorMessage(final Exception e) {
		Dialogs.showErrorDialog(this, resources.getString(
				ResourceKey.ERROR, StringID.Error.SAVE_FAILED_ID), e);
	}

	@Override
	public void showErrorMessage(final Exception e) {
		Dialogs.showErrorDialog(this, resources.getString(
				ResourceKey.ERROR, StringID.Error.DEFAULT_TITLE_ID), e);
	}

	@Override
	public View getTopLevelView() {
		return (View) SwingUtilities.getWindowAncestor(this);
	}
}