package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.PrefixSelectionComboBox;
import org.controlsfx.control.textfield.CustomTextField;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.LayoutData;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.custom.FilterVBox;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.factory.AdhocBaseFactory;
import com.legendapl.lightning.adhoc.factory.AdhocBuildTreeFactory;
import com.legendapl.lightning.adhoc.factory.AdhocSaveFactory;
import com.legendapl.lightning.adhoc.factory.ComboBoxFactory;
import com.legendapl.lightning.adhoc.factory.CrossTableViewFactory;
import com.legendapl.lightning.adhoc.factory.FilterPaneFactory;
import com.legendapl.lightning.adhoc.factory.LayoutStoreFactory;
import com.legendapl.lightning.adhoc.factory.LayoutSwitchFactory;
import com.legendapl.lightning.adhoc.factory.LayoutTransferFactory;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.factory.TableViewFactory;
import com.legendapl.lightning.adhoc.factory.export.ExportFactory;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableFieldTreeFactory;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableValueTreeFactory;
import com.legendapl.lightning.adhoc.factory.itemTree.TableItemTreeFactory;
import com.legendapl.lightning.adhoc.filter.editor.GeneralEditor;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.BackRunService;
import com.legendapl.lightning.adhoc.service.DatabaseService;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.tools.data.AdhocData;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpanTableView;

/**
 * アドホックビュー編集のコントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/3/21
 */
public class P121AdhocAnchorPane extends C100AdhocBaseAnchorPane {

	/*-------------------------All-----------------------------*/
	@FXML
	private SplitPane mainSplitPane;
	@FXML
	private StackPane spinnerPane;

	/*-------------------------Left-----------------------------*/

	@FXML
	private AnchorPane leftPane;

	// components in left pane
	@FXML
	private SplitPane spilitPane;
	@FXML
	private AnchorPane fieldPane;

	// components in field pane
	@FXML
	private HBox domainHBox;
	@FXML
	private Button domainReduceButton;
	@FXML
	private Label topicNameLabel;
	@FXML
	private Button domainExpandButton;
	@FXML
	private HBox fieldHBox;
	@FXML
	private Label fieldNameLabel;
	@FXML
	private Button fieldExpandButton;
	@FXML
	private CustomTextField fieldSearchField;
	@FXML
	private TreeView<BaseNode> fieldTreeView;

	@FXML
	private AnchorPane valuePane;

	// components in value pane
	@FXML
	private HBox valueHBox;
	@FXML
	private Label valueNameLabel;
	@FXML
	private Button valueExpandButton;
	@FXML
	private CustomTextField valueSearchField;
	@FXML
	private TreeView<BaseNode> valueTreeView;

	/*-------------------------Middle-----------------------------*/

	@FXML
	private AnchorPane mainPane;

	// components in main pane
	@FXML
	private VBox mainVBox;
	@FXML
	private HBox mainFirstHBox;
	@FXML
	private Label adhocNameLabel;
	@FXML
	private HBox mainSecondHBox;
	@FXML
	private ToolBar toolBar;
	@FXML
	private MenuButton saveButton;
	@FXML
	private MenuButton exportButton;
	@FXML
	private Button lastStepButton;
	@FXML
	private Button nextStepButton;
	@FXML
	private Button firstStepButton;
	@FXML
	private Button switchButton;
	@FXML
	private Button sortButton;
	@FXML
	private Button sqlButton;
	@FXML
	private PrefixSelectionComboBox<AdhocModelType> viewComboBox;
	@FXML
	private PrefixSelectionComboBox<AdhocModelType> dataComboBox;
	@FXML
	private HBox mainThirdHBox;
	@FXML
	private Label columnLabel;
	@FXML @Deprecated
	private FlowPane columnFlow;
	private LayoutFlowPane layoutColumnFlow;
	@FXML
	private HBox mainFourthHBox;
	@FXML
	private Label rowLabel;
	@FXML @Deprecated
	private FlowPane rowFlow;
	private LayoutFlowPane layoutRowFlow;
	@FXML
	private HBox mainFifthHBox;
	@FXML
	private Button submitButton;

	@FXML
	private AnchorPane tablePane;
	@FXML
	private VBox tableVBox;
	@FXML
	private HBox tableHBox;
	@FXML
	private Region tableRegion;
	@FXML
	private CellSpanTableView tableView;

	/*-------------------------Right-----------------------------*/

	@FXML
	private StackPane stackPane;
	@FXML
	private Button filterReduceButton;
	@FXML
	private AnchorPane rightPane;
	@FXML
	private AnchorPane rightFilterAnchor;
	@FXML
	private ListView<FilterVBox> filterView;
	@FXML
	private TitledPane filterTitled;
	@FXML
	private TextArea filterText;
	@FXML
	private Label filterError;
	@FXML
	private CheckBox filterCheck;

	/*-------------------------Data-----------------------------*/

	private Adhoc adhoc;

	private List<TableField> tableColumns = new ArrayList<TableField>();
	private List<TableField> tableRows = new ArrayList<TableField>();
	private List<CrossTableField> crossTableColumns = new ArrayList<CrossTableField>();
	private List<CrossTableField> crossTableRows = new ArrayList<CrossTableField>();
	private List<CrossTableField> crossTableValues = new ArrayList<CrossTableField>();

	public static AdhocModelType viewModelType;
	public static AdhocModelType dataModelType;

	private TableViewFactory tableViewFactory;
	private CrossTableViewFactory crossTableViewFactory;

	private TableItemTreeFactory tableItemTreeFactory;
	private CrossTableFieldTreeFactory crossTableFieldTreeFactory;
	private CrossTableValueTreeFactory crossTableValueTreeFactory;
	private FilterPaneFactory filterPaneFactory;

	private TreeItem<BaseNode> alSuperRoot = new TreeItem<>(new DBNode());
	private TreeItem<BaseNode> fieldSuperRoot = new TreeItem<>(new DBNode());
	private TreeItem<BaseNode> valueSuperRoot = new TreeItem<>(new DBNode());

	private final BackRunService backW = new BackRunService(
			() -> setBackRunFlag(true),
			() -> setBackRunFlag(false)
	);
	private DatabaseService databaseService;
	private Alert alertSqlButtonWindow;

	/*-------------------------initialize-----------------------------*/

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		adhoc = ShareDataService.loadAdhoc();
		initAttribute();
		initFactory();

		Platform.runLater(() -> {
			initLayout();
			initTable();
			initTree();
			initAdhocStage();
			initOthers();
			initComboBox();
			generateDataView(null);
		});

		// TODO: setDisable(false), delete
		Platform.runLater(() -> {
			domainReduceButton.setDisable(true);
			domainExpandButton.setDisable(true);
			fieldSearchField.setDisable(true);
			valueSearchField.setDisable(true);
			fieldExpandButton.setDisable(true);
			valueExpandButton.setDisable(true);
		});
	}

	private void initAttribute() {
		// modelType
		viewModelType = (null == adhoc.getViewModelType()) ? AdhocConstants.AdhocProperty.InitialViewModelType : adhoc.getViewModelType();
		dataModelType = (null == adhoc.getDataModelType()) ? AdhocConstants.AdhocProperty.InitialDataModelType : adhoc.getDataModelType();
		// layoutFlowPane
		layoutColumnFlow = new LayoutFlowPane(columnFlow);
		layoutRowFlow = new LayoutFlowPane(rowFlow);
		LayoutStoreFactory.init(viewModelType, layoutColumnFlow.getChildren(), layoutRowFlow.getChildren());
		// alertSqlButtonWindow
		alertSqlButtonWindow = AlertWindowService.getAlert(AlertType.NONE, AdhocUtils.getString("P121.window.querySQL.title"), null, "");
	}

	private void initFactory() {
		
		// AdhocBaseFactory
		AdhocBaseFactory.setAdhoc(adhoc);
		AdhocBaseFactory.setFieldTreeView(fieldTreeView);
		AdhocBaseFactory.setValueTreeView(valueTreeView);
		AdhocBaseFactory.setAlSuperRoot(alSuperRoot);
		AdhocBaseFactory.setFieldSuperRoot(fieldSuperRoot);
		AdhocBaseFactory.setValueSuperRoot(valueSuperRoot);
		AdhocBaseFactory.setColumnFlow(layoutColumnFlow);
		AdhocBaseFactory.setRowFlow(layoutRowFlow);
		AdhocBaseFactory.setTableColumns(tableColumns);
		AdhocBaseFactory.setTableRows(tableRows);
		AdhocBaseFactory.setCrossTableColumns(crossTableColumns);
		AdhocBaseFactory.setCrossTableRows(crossTableRows);
		AdhocBaseFactory.setCrossTableValues(crossTableValues);
		AdhocBaseFactory.setBackW(backW);
		AdhocBaseFactory.setSpinnerPane(spinnerPane);
		
		// DatabaseService
		databaseService = new DatabaseService(adhoc);
		AdhocBaseFactory.setDatabaseService(databaseService);
		
		// ItemTreeFactory
		tableItemTreeFactory = new TableItemTreeFactory();
		crossTableFieldTreeFactory = new CrossTableFieldTreeFactory();
		crossTableValueTreeFactory = new CrossTableValueTreeFactory();
		AdhocBaseFactory.setTableItemTreeFactory(tableItemTreeFactory);
		AdhocBaseFactory.setCrossTableFieldTreeFactory(crossTableFieldTreeFactory);
		AdhocBaseFactory.setCrossTableValueTreeFactory(crossTableValueTreeFactory);
		
		// TableViewFactory
		tableViewFactory = new TableViewFactory(tableView);
		crossTableViewFactory = new CrossTableViewFactory(tableView);
		AdhocBaseFactory.setTableViewFactory(tableViewFactory);
		AdhocBaseFactory.setCrossTableViewFactory(crossTableViewFactory);
		
		// FilterFactory
		GeneralEditor.setDatabaseInfo(adhoc);
		FilterPaneFactory.setRightFilterAnchor(rightFilterAnchor);
		FilterPaneFactory.setFilterView(filterView);
		FilterPaneFactory.setFilterTitled(filterTitled);
		FilterPaneFactory.setFilterError(filterError);
		FilterPaneFactory.setFilterText(filterText);
		FilterPaneFactory.setFilterReduceButton(filterReduceButton);
		FilterPaneFactory.setFilterCheck(filterCheck);
		filterPaneFactory = new FilterPaneFactory();
		AdhocBaseFactory.setFilterFactory(filterPaneFactory);
		
		// StatementFactory
		StatementFactory.setLastStepButton(lastStepButton);
		StatementFactory.setNextStepButton(nextStepButton);
		StatementFactory.setFirstStepButton(firstStepButton);
		StatementFactory.init();
		
		// AdhocSaveFactory
		AdhocSaveFactory.setSaveButton(saveButton);
		AdhocSaveFactory.setAdhocNameLabel(adhocNameLabel);
		AdhocSaveFactory.init();
		
		// ExportFactory
		ExportFactory.setExportButton(exportButton);
		ExportFactory.setCellSpanTableView(tableView);
		ExportFactory.init();
	}

	private void initLayout() {
		// layoutColumnFlow
		layoutColumnFlow.widthProperty().addListener((v) -> doLayout(layoutColumnFlow));
		layoutColumnFlow.heightProperty().addListener((v) -> doLayout(layoutColumnFlow));
		layoutColumnFlow.setPrefHeight(37);
		layoutColumnFlow.prefWidthProperty().bind(mainThirdHBox.widthProperty().subtract(AdhocUtils.getLabelWidth(columnLabel)));
		AdhocUtils.replaceListElement(mainThirdHBox.getChildren(), columnFlow, layoutColumnFlow);
		// layoutRowFlow
		layoutRowFlow.widthProperty().addListener((v) -> doLayout(layoutRowFlow));
		layoutRowFlow.heightProperty().addListener((v) -> doLayout(layoutRowFlow));
		layoutRowFlow.setPrefHeight(37);
		layoutRowFlow.prefWidthProperty().bind(mainThirdHBox.widthProperty().subtract(AdhocUtils.getLabelWidth(rowLabel)));
		AdhocUtils.replaceListElement(mainFourthHBox.getChildren(), rowFlow, layoutRowFlow);
		// LayoutDataService
		LayoutTransferFactory.transferToLayout(viewModelType);
	}

	private void doLayout(LayoutFlowPane flow) {
		Platform.runLater(() -> {
			flow.requestLayout();
			flow.layout();
			flow.layoutChildren();
		});
	}

	private void initTable() {
		// treeView
		fieldTreeView.prefHeightProperty().bind(
				fieldPane.heightProperty().subtract(domainHBox.heightProperty()).subtract(fieldHBox.heightProperty()));
		valueTreeView.prefHeightProperty().bind(valuePane.heightProperty().subtract(valueHBox.heightProperty()));
		// tableView
		tableView.setPlaceholder(new Label());
		tableView.getStylesheets().add("/view/tableView.css");
		tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		// tableScrollPane
		tablePane.prefHeightProperty()
				.bind(mainVBox.heightProperty().subtract(mainFirstHBox.heightProperty())
				.subtract(mainSecondHBox.heightProperty()).subtract(mainThirdHBox.heightProperty())
				.subtract(mainFourthHBox.heightProperty()).subtract(mainFifthHBox.heightProperty()));
		tablePane.prefWidthProperty().bind(mainPane.widthProperty());
		tableView.prefHeightProperty().bind(tablePane.heightProperty());
		tableView.prefWidthProperty().bind(tablePane.widthProperty());
		tableView.getSelectionModel().clearSelection();
	}

	private void initTree() {
		// focusedProperty addListener
		fieldTreeView.focusedProperty().addListener((record, oldValue, newValue)
				-> handleActionAddListenerTreeViewFocusedProperty(fieldTreeView, record, oldValue, newValue));
		valueTreeView.focusedProperty().addListener((record, oldValue, newValue)
				-> handleActionAddListenerTreeViewFocusedProperty(valueTreeView, record, oldValue, newValue));
		// initTreeElement
		fieldTreeView.setRoot(fieldSuperRoot);
		fieldTreeView.setShowRoot(false);
		fieldTreeView.setEditable(true);
		fieldTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		valueTreeView.setRoot(valueSuperRoot);
		valueTreeView.setShowRoot(false);
		valueTreeView.setEditable(true);
		valueTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// buildTree
		AdhocBuildTreeFactory.buildTree();
		AdhocBuildTreeFactory.expandTree();
	}
	
	private void initOthers() {
		// Fix Left Pane
		mainSplitPane.setDividerPositions(leftPane.getMinWidth()/mainSplitPane.getPrefWidth());
		SplitPane.setResizableWithParent(leftPane, Boolean.FALSE);
		// nameLabel
		topicNameLabel.setText(AdhocUtils.getString("P100.fileType.TOPIC")+":"+adhoc.getTopicName());
		adhocNameLabel.setText(AdhocSaveFactory.fileName);
		columnLabel.prefWidthProperty().bind(rowLabel.prefWidthProperty());
		// toolTip
		saveButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.saveButton")));
		exportButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.exportButton")));
		lastStepButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.lastStepButton")));
		nextStepButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.nextStepButton")));
		firstStepButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.reDisplayButton")));
		switchButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.switchButton")));
		sortButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.sortButton")));
		sqlButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.sqlButton")));
		submitButton.setTooltip(new Tooltip(AdhocUtils.getString("P121.tooltip.submitButton")));
		// filter
		filterCheck.setSelected(adhoc.getFilterCheckFlg());
		filterText.setText(adhoc.getFilterConnect());
		// AlertWindow
		AlertWindowService.SingleAlert.init();
	}

	private void initComboBox() {
		// comboBoxFactory
		ComboBoxFactory.setViewComboBox(viewComboBox);
		ComboBoxFactory.setDataComboBox(dataComboBox);
		ComboBoxFactory.setComboBoxSelector(new ComboBoxFactory.ComboBoxSelector() {
			@Override public void handleActionViewComboBoxSelect(AdhocModelType newValue) {
				handleActionViewComboBoxSelectImpl(newValue);
			}
			@Override public void handleActionDataComboBoxSelect(AdhocModelType newValue) {
				handleActionDataComboBoxSelectImpl(newValue);
			}
		});
		ComboBoxFactory.init();
		// comboBox
		handleActionViewComboBoxSelectImpl(viewModelType);
		handleActionDataComboBoxSelectImpl(dataModelType);
		viewComboBox.getSelectionModel().select(viewModelType);
		dataComboBox.getSelectionModel().select(dataModelType);
	}

	/*-------------------------handleAction-----------------------------*/

	private void setBackRunFlag(boolean flag) {
		logger.debug("Set flag backRun : " + flag);
		Platform.runLater(() -> {
			spinnerPane.setVisible(flag);
		});
	}

	private void handleActionViewComboBoxSelectImpl(AdhocModelType newValue) {
		if (null != newValue) {
			viewModelType = newValue;
			switch (viewModelType) {
			case TABLE:
				// label
				columnLabel.setText(AdhocUtils.getString("P121.layout.column.label"));
				rowLabel.setText(AdhocUtils.getString("P121.layout.group.label"));
				rowLabel.setPrefWidth(AdhocUtils.isJapanese.get() ? 111 : 111);
				// switchButton
				switchButton.disableProperty().unbind();
				switchButton.setDisable(true);
				// sortButton
				sortButton.setDisable(false);
				// exportButton
				ExportFactory.setMenuItemVisible(newValue);
				// other
				LayoutStoreFactory.convertToTableModel(layoutColumnFlow.getChildren(), layoutRowFlow.getChildren());
				tableItemTreeFactory.initModel();
				updateAlertSqlButtonWindowTextArea();
				break;
			case CROSSTABLE:
				// label
				columnLabel.setText(AdhocUtils.getString("P121.layout.column.label"));
				rowLabel.setText(AdhocUtils.getString("P121.layout.row.label"));
				rowLabel.setPrefWidth(AdhocUtils.isJapanese.get() ? 20 : 111);
				// switchButton
				switchButton.disableProperty().bind(switchButtonDisable());
				// sortButton
				sortButton.setDisable(true);
				// exportButton
				ExportFactory.setMenuItemVisible(newValue);
				// other
				LayoutStoreFactory.convertToCrossTableModel(layoutColumnFlow.getChildren(), layoutRowFlow.getChildren());
				crossTableFieldTreeFactory.initModel();
				crossTableValueTreeFactory.initModel();
				updateAlertSqlButtonWindowTextArea();
				break;
			default:
				break;
			}
		}
	}

	private void handleActionDataComboBoxSelectImpl(AdhocModelType newValue) {
		if (null != newValue) {
			dataModelType = newValue;
		}
	}

	private void handleActionAddListenerTreeViewFocusedProperty(
			TreeView<BaseNode> treeView,
			ObservableValue<? extends Boolean> record, Boolean oldValue, Boolean newValue) {

		if (adhocStage.isFocused()) {
			if (oldValue && !newValue) {
				Platform.runLater(() -> {
					treeView.getSelectionModel().clearSelection();
				});
			}
		}
	}

	/**
	 * 実行前チェックを行う
	 * @param event
	 */
	public void generateDataView(ActionEvent event) {
		// フィルタチェックかどうかを判断
		Boolean checkFlg = true;
		if (filterCheck.isSelected()) {
			// フィルタアイテムをチェック
			if (!filterPaneFactory.doFilterItemCheck()) {
				checkFlg = false;
				filterError.setText("");
				filterText.setStyle("-fx-background-color: #FFFFFF");
			} else {
				// テキストをチェック
				if (!filterPaneFactory.doFilterCheck()) {
					checkFlg = false;
				}
			}
		}
		if (checkFlg) {
			// データを保存
			filterPaneFactory.doSave();
			// データを表示
			generateDataViewImpl(event);
		}
	}
	
	public static Boolean success;
	public static AdhocModelType lastViewModelType;
	public static AdhocModelType lastDataModelType;

	public void generateDataViewImpl(ActionEvent event) {
		
		backW.run(() -> {
			Thread.sleep(1000); // TODO: QA#17のため、より良い方法がある?
			
			try {
				switch (viewModelType) {
				case CROSSTABLE:
					LayoutData layoutData = LayoutTransferFactory.getLayoutData(viewModelType);
					crossTableViewFactory.setValueIndex(layoutData.getValueIndex());
					crossTableViewFactory.setRow(layoutData.isRow());
					tableViewFactory.setInTable(false);
					setViewFactoryFullData();
					debugGenerateDataView();
					success = crossTableViewFactory.generateDataView(event);
					break;
				case TABLE:
					crossTableViewFactory.setInCrossTable(false);
					setViewFactoryFullData();
					debugGenerateDataView();
					success = tableViewFactory.generateDataView(event);
					break;
				default:
					break;
				}
				
			} catch (Exception e) {
				success = false;
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_ADHOC_SHOW_TABLE"), e.getMessage());
				
			} finally {
				lastViewModelType = viewModelType;
				lastDataModelType = dataModelType;
				Platform.runLater(() -> {
					ExportFactory.exportButton.setDisable(!success);
					updateAlertSqlButtonWindowTextArea();
				});
			}
		});
	}
	
	private void setViewFactoryFullData() {
		switch (dataModelType) {
		case FULLDATA:
			crossTableViewFactory.setFullData(true);
			tableViewFactory.setFullData(true);
			break;
		case SIMPLEDATA:
			crossTableViewFactory.setFullData(false);
			tableViewFactory.setFullData(false);
			break;
		default:
			break;
		}
	}

	public void handleActionClickedFieldExpandButton(ActionEvent event) {
		try {
			String paneFXML = "/view/P122CalculatedFieldAnchorPane.fxml";
			ShareDataService.setCalculatedField(true);
			Stage stage = showPane(event, paneFXML,
								  getTitle("P122.field", AdhocUtils.bundleMessage),
								  Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
			stage.show();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
			return;
		}
	}

	public void handleActionClickedValueExpandButton(ActionEvent event) {
		try {
			String paneFXML = "/view/P122CalculatedFieldAnchorPane.fxml";
			ShareDataService.setCalculatedField(false);
			Stage stage = showPane(event, paneFXML,
								  getTitle("P122.measure", AdhocUtils.bundleMessage),
								  Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
			stage.show();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
			return;
		}
	}
	
	public void handleActionClickedSortButton(ActionEvent event) {
		try {
			String paneFXML = "/view/P123SortFieldAnchorPane.fxml";
			Stage stage = showPane(event, paneFXML,
								  getTitle("P123.sort", AdhocUtils.bundleMessage),
								  Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
			stage.show();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
			return;
		}
	}

	public void handleActionFieldClickSortButton(ActionEvent event) {
		try {
			String paneFXML = "/view/P123SortFieldAnchorPane.fxml";
			Stage stage = showPane(event, paneFXML,
								  getTitle("P123.sort", AdhocUtils.bundleMessage),
								  Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
			stage.show();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
			return;
		}
	}

	public void handleActionClickedSwitchButton(ActionEvent event) {
		LayoutSwitchFactory.switchCrossTableLayout();
	}

	private ObservableValue<? extends Boolean> switchButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(layoutColumnFlow.getChildren(), layoutRowFlow.getChildren());
            }
            @Override protected boolean computeValue() {
            	return layoutColumnFlow.getChildren().isEmpty() && layoutRowFlow.getChildren().isEmpty();
			}
		};
	}

	public void handleActionClickedSqlButton(ActionEvent event) {
		Platform.runLater(() -> {
			updateAlertSqlButtonWindowTextArea();
			alertSqlButtonWindow.show();
			Stage alertStage = (Stage) alertSqlButtonWindow.getDialogPane().getScene().getWindow();
			alertStage.setIconified(false);
		});
	}

	private void updateAlertSqlButtonWindowTextArea() {
		TextArea textArea = (TextArea) alertSqlButtonWindow.getDialogPane().getContent();
		switch(viewModelType) {
		case CROSSTABLE:
			textArea.setText(crossTableViewFactory.getSqlStatement());
			break;
		case TABLE:
			textArea.setText(tableViewFactory.getSqlStatement());
			break;
		default:
			break;
		}
	}

	private void initAdhocStage() {
		// focusedProperty
		Stage alertStage = (Stage) alertSqlButtonWindow.getDialogPane().getScene().getWindow();
		alertStage.initModality(Modality.WINDOW_MODAL);
		adhocStage.focusedProperty().addListener((record, oldVal, newVal) -> {
			alertStage.setAlwaysOnTop(newVal);
		});
		// OnCloseRequest
		adhocStage.setOnCloseRequest(event -> {
			Alert alert = null;
			// true:変更なし; false :変更あり
			Boolean noChangeFlg = lastStepButton.isDisabled();
			if (noChangeFlg) {
				// 保存しない 直接に閉じる
				handleActionCloseAdhocStage(null);
			} else {
				alert = AlertWindowService.getAlertConfirm(AdhocUtils.getString("ALERT_CONFIRM_SAVE_ADHOC_STAGE"));
				Optional<ButtonType> result = alert.showAndWait();
				if (ButtonType.OK == result.get()) {
					if (!AdhocSaveFactory.menuItemSaveDisable()) {
						// 保存
						AdhocSaveFactory.handleActionSave();
					} else {
						// 名前を付けて保存
						AdhocSaveFactory.handleActionSaveWithName();
					}
					event.consume();
				} else if (ButtonType.CANCEL == result.get()) {
					 handleActionCloseAdhocStage(null);
				}else {
					event.consume();
				}
			}
		});
	}

	private void handleActionCloseAdhocStage(ActionEvent event) {
		ShareDataService.clear();
		AdhocData.roots.remove("/view/P121AdhocAnchorPane.fxml");
		setObject(null);
		Platform.runLater(() -> {
			if (alertSqlButtonWindow.isShowing()) {
				logger.info("Close the SQL Query stage.");
				alertSqlButtonWindow.hide();
				alertSqlButtonWindow.close();
			}
		});
		Platform.runLater(() -> {
			logger.info("Close the adhoc stage.");
			adhocStage.hide();
			adhocStage.close();
		});
	}
	
	/*-------------------------Debug-----------------------------*/

	private void debugGenerateDataView() {
		if (AdhocConstants.Debug.GenerateDataView) {
			String log = "[columns]: ";
			for (Field field : AdhocModelType.TABLE == viewModelType ? tableColumns : crossTableColumns) {
				log += field.getLabel() + " ";
			}
			logger.info(log);
			log = AdhocModelType.TABLE == viewModelType ? "[groups]: " : "[rows]: ";
			for (Field field : AdhocModelType.TABLE == viewModelType ? tableRows : crossTableRows) {
				log += field.getLabel() + " ";
			}
			logger.info(log);
			if (AdhocModelType.CROSSTABLE == viewModelType) {
				log = "[values]: ";
				for (Field field : crossTableValues) {
					log += field.getLabel() + " ";
				}
				logger.info(log);
				logger.info("valueIndex: " + crossTableViewFactory.getValueIndex() + ", isRow:" + crossTableViewFactory.isRow());
			}
		}
	}

}

