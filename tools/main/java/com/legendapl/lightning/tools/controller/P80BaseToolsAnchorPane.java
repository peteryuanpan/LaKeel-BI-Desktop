package com.legendapl.lightning.tools.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.ws.rs.ProcessingException;

import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AccessDeniedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.BadRequestException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.InternalServerErrorException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ModificationNotAllowedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.NoResultException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceInUseException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.model.BaseModel;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.ProcessFlag;
import com.legendapl.lightning.tools.model.StringBase;
import com.legendapl.lightning.tools.service.BackRunService;
import com.legendapl.lightning.tools.service.CsvService;
import com.legendapl.lightning.tools.service.CsvService.CsvFormatException;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author LAC_徐
 * @since 2017/9/11
 *
 */
public abstract class P80BaseToolsAnchorPane extends C80ToolsMenuPane {

	@FXML
	protected JFXButton applyButton;
	@FXML
	protected JFXButton getButton;
	@FXML
	protected JFXButton importButton;
	@FXML
	protected JFXButton exportButton;
	@FXML
	protected JFXButton helpButton;
	@FXML
	protected StackPane spinnerPane;
	@FXML
	protected AnchorPane anchorPane;


	//Flags
	protected boolean imported = false;
	protected boolean geted = true;
	protected boolean backRun = false;
	protected boolean pathSet = false;

	private final FileChooser fileChooser = new FileChooser(); {
	fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
	}

	/**
	 * 現在のリソース名
	 */
	protected String resourceName = null;

	/**
	 * <br>バックで実行する(実行中のメッセージ表示)</br>
	 * </br>
	 * 例:
	 * <pre>
	 *     backW.run(() -> {
	 *         doSomething();
	 *     });
	 * </pre>
	 */
	final protected BackRunService backW = new BackRunService(
			() -> setBackRunFlag(true),
			() -> setBackRunFlag(false)
			);
	/**
	 * <br>バックで実行する</br>
	 * </br>
	 * 例:
	 * <pre>
	 *     back.run(() -> {
	 *         doSomething();
	 *     });
	 * </pre>
	 */
	final protected BackRunService back = new BackRunService();


	public P80BaseToolsAnchorPane() {
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.debug("initializing");

		setImportFlag(false);
		setGetFlag(false);
		setPathFlag(true);
		setBackRunFlag(false);

		init(location, resources);
		logger.debug("initialize done");
	}

	public void init(URL location, ResourceBundle resources){
	}

	/**
	 * CSVファイルのパス取得(Save)
	 *
	 * @return データ
	 */
	protected File getOpenCsvPath() {
		fileChooser.setTitle(Constants.DLG_OPEN_CSV_TITLE);

		File saveFile = fileChooser.showOpenDialog(currentStage);
		if(saveFile == null) {
			return null;
		} else {
			//check format
			fileChooser.setInitialDirectory(saveFile.getParentFile());
			return saveFile;
		}
	}

	/**
	 * CSVファイルのパス取得(Load)
	 *
	 * @return データ
	 */
	protected File getSaveCsvPath() {
		fileChooser.setTitle(Constants.DLG_SAVE_CSV_TITLE);
		fileChooser.setInitialFileName(serverInfo.getName() + "_" + currentToolName
				+ (resourceName == null ? "" : "_" + resourceName) + ".csv");

		File saveFile = fileChooser.showSaveDialog(currentStage);
		if(saveFile == null) {
			return null;
		} else {
			//check format
			fileChooser.setInitialDirectory(saveFile.getParentFile());
			return saveFile;
		}
	}

	/**
	 * データをCSVファイルのデータ取得
	 *
	 * @return データ。非nullの場合処理続く。
	 */
	protected List<CsvRow> getCsvRows(File csvFile) {
		try {
			List<CsvRow> csvRows = CsvService.getDataFromCsv(csvFile);
			return csvRows;
		} catch(IOException e) {
			showError(Constants.CSV_IMPORT_ERROR, Arrays.asList(e.getMessage()));
			return null;
		} catch(CsvFormatException e) {
			showError(Constants.CSV_FORMAT_ERROR, Arrays.asList(e.getMessage()));
			return null;
		}
	}

	/**
	 * データをCSVファイルに保存
	 *
	 * @param file CSVファイル
	 * @param rows データ
	 * @return 成功かどうか
	 */
	protected boolean saveCsv(File file, List<List<String>> rows) {
		try{
			CsvService.saveDataToCsv(file, rows);
			return true;
		} catch (Exception e) {
			logger.error("Failed to save csv file.");
			logger.error(e.getMessage(), e);
			showError(Constants.CSV_EXPORT_ERROR, Arrays.asList(e.getMessage()));
			return false;
		}
	}

	/**
	 * サーバーデータのバックアップをCSVファイルに保存
	 *
	 * @param rows データ
	 * @return 成功かどうか
	 */
	protected boolean saveBackup(List<List<String>> rows) {
		try{
			File dir = new File(Constants.CSV_BACKUP_FOLDER);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			CsvService.saveDataToCsv(Constants.CSV_BACKUP_FOLDER + getBackupFileName(), rows);
			return true;
		} catch (IOException e) {
			logger.error("Failed to save backup.");
			logger.error(e.getMessage(), e);
			showError(Constants.CSV_EXPORT_ERROR, Arrays.asList(e.getMessage()));
			return false;
		}
	}

	private String getBackupFileName() {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(now) + "_"
				+ serverInfo.getName() + "_" + currentToolName
				+ (resourceName == null ? "" : "_" + resourceName) + ".csv";
	}

	/**
	 * ユーザに処理を続くかの確認
	 *
	 * @return 確認結果。trueの場合は処理続く
	 */
	protected boolean canSync() {
		if(imported) {
			logger.debug("Already imported, call comfirm dialogue.");

			Optional<ButtonType> bType = showInfoW(AlertType.CONFIRMATION,
					Constants.DLG_ALREADY_IMPORTED_TITLE,
					Constants.DLG_ALREADY_IMPORTED_CONTENT,
					null);
			if(bType.get() == ButtonType.CANCEL)
				return false;
			else if(bType.get() == ButtonType.OK){
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * フラグimportedを設定
	 *
	 */
	protected void setImportFlag(boolean flag) {
		logger.debug("Set flag imported : " + flag);
		imported = flag;
		Platform.runLater(() -> {
			applyButton.setDisable(!(pathSet && geted && imported));
		});
	}

	/**
	 * フラグgetedを設定
	 *
	 */
	protected void setGetFlag(boolean flag) {
		logger.debug("Set flag geted : " + flag);

		geted = flag;
		Platform.runLater(() -> {
			applyButton.setDisable(!(pathSet && geted && imported));
			importButton.setDisable(!(pathSet && geted));
			exportButton.setDisable(!(pathSet && geted));
		});
	}

	/**
	 * フラグbackRunを設定
	 *
	 */
	protected void setBackRunFlag(boolean flag) {
		logger.debug("Set flag backRun : " + flag);

		backRun = flag;
		Platform.runLater(() -> {
			spinnerPane.setVisible(backRun);
		});
	}

	/**
	 * フラグpathSetを設定
	 *
	 */
	protected void setPathFlag(boolean flag) {
		logger.debug("Set flag pathSet : " + flag);

		pathSet = flag;
		Platform.runLater(() -> {
			getButton.setDisable(!pathSet);
			applyButton.setDisable(!(pathSet && geted && imported));
			importButton.setDisable(!(pathSet && geted));
			exportButton.setDisable(!(pathSet && geted));
		});
	}

	/**
	 * 「CSVインポート」Work
	 * @param csvRows インポートされたcsvデータ
	 * @return 成功かどうか
	 */
	abstract protected boolean csvImportWork(List<CsvRow> csvRows);

	/**
	 * 「CSVエクスポート」Work
	 * @param csvFileExport エクスポートする予定のCSVファイル
	 * @return 成功かどうか
	 */
	abstract protected boolean csvExportWork(File csvFileExport);

	/**
	 * 「取得」Work
	 * @return 成功かどうか
	 */
	abstract protected boolean getWork();

	/**
	 * 「適用」Work
	 * @return 成功かどうか
	 */
	abstract protected boolean applyWork();

	/**
	 * 「CSVインポート」押下
	 */
	public void csvImportFired(ActionEvent event) throws IOException {
		logger.debug("Button CsvImport pressed.");

		if(!canSync()) return;
		File csvFileImport = getOpenCsvPath();
		if(csvFileImport == null) return;

		backW.run(() -> {
			List<CsvRow> csvRows = getCsvRows(csvFileImport);
			if(csvRows == null) return false;
			return csvImportWork(csvRows);
		});
	}

	/**
	 * 「CSVエクスポート」押下
	 */
	public void csvExportFired(ActionEvent event) throws IOException {
		logger.debug("Button CsvExport pressed.");

		if(!canSync()) return;
		File csvFileExport = getSaveCsvPath();
		if(csvFileExport == null) return;
		backW.run(() -> csvExportWork(csvFileExport));
	}

	/**
	 * 「取得」押下
	 */
	public void getFired(ActionEvent event) throws IOException {
		logger.debug("Button Get pressed.");

		if(!canSync()) return;
		backW.run(() -> getWork());
	}

	/**
	 * 「適用」押下
	 */
	public void applyFired(ActionEvent event) throws IOException {
		logger.debug("Button Apply pressed.");

		backW.run(() -> applyWork());
	}

	/**
	 * [ヘルプ]押下
	 */
	public void csvHelpFired(ActionEvent event) throws IOException {
		logger.debug("Button Help pressed.");

		showInfo(AlertType.NONE, null, null, getHelpContent());
	}

	/**
	 * [ヘルプ]のコンテンツ
	 * @return String
	 */
	protected List<String> getHelpContent() {
		return Arrays.asList(
				Constants.P80_HELP_CONTENT_CSVFLAG,
				new String(""),
				Constants.P80_HELP_CONTENT_CSVFLAG_DELETE,
				Constants.P80_HELP_CONTENT_CSVFLAG_UPDATE,
				Constants.P80_HELP_CONTENT_CSVFLAG_ADD
		);
	}

	/**
	 * 「取得」
	 */
	public void getWithoutNotify() {
		backW.run(() -> getWork());
	}

	/**
	 * メッセージを表示
	 *
	 * @param mainInfos メイン　メッセージ
	 */
	protected void showInfo(String mainInfo) {
		showInfo(mainInfo, null);
	}

	/**
	 * メッセージを表示
	 *
	 * @param mainInfos メイン　メッセージ
	 * @param detailInfos 詳しい　メッセージ
	 */
	protected void showInfo(String mainInfo, List<String> detailInfo) {
		showInfo(AlertType.INFORMATION, null, mainInfo, detailInfo);
	}

	/**
	 * エラーメッセージを表示
	 *
	 * @param mainErrors メイン　エラーメッセージ
	 */
	protected void showError(String mainError) {
		showError(mainError, null);
	}

	/**
	 * エラーメッセージを表示
	 *
	 * @param mainErrors メイン　エラーメッセージ
	 * @param detailErrors 詳しい　エラーメッセージ
	 */
	protected void showError(String mainError, List<String> detailError) {
		showInfo(AlertType.ERROR, null, mainError, detailError);
	}

	/**
	 * セージを表示
	 *
	 * @param type メッセージ　タイプ
	 * @param title タイトル
	 * @param mainInfos メイン　メッセージ
	 * @param detailInfos 詳しい　メッセージ
	 */
	public void showInfo(AlertType type, String titleInfo, String mainInfo, List<String> detailInfo) {
		List<String> detailInfoTmp = new ArrayList<String>();
		if (detailInfo != null) {
			detailInfoTmp.addAll(detailInfo);
		}
		Platform.runLater(() -> {
			showInfoW(type, titleInfo, mainInfo, detailInfoTmp);
		});
	}

	/**
	 * セージを表示
	 *
	 * @param type メッセージ　タイプ
	 * @param title タイトル
	 * @param mainInfos メイン　メッセージ
	 * @param detailInfos 詳しい　メッセージ
	 * @param return バタン　イプ
	 */
	protected Optional<ButtonType> showInfoW(AlertType type, String titleInfo, String mainInfo, List<String> detailInfo) {
		logger.debug( MessageFormat.format (
				"Information dialogue, title : {0}, type : {1}, mainMessage : {2}, detailMessage : {3}",
				titleInfo, type, mainInfo, detailInfo
				)
		);

		Alert alert;
		if (type != AlertType.NONE) alert = new Alert(type);
		else alert = new Alert(type, "", ButtonType.OK);

		String titleText = titleInfo == null ? getTitleForShowInfo(type) : titleInfo;
		alert.setTitle(titleText);

		String headerText = mainInfo == null ? " " : mainInfo;
		alert.setHeaderText(headerText);
		if (type != AlertType.NONE) {
			GridPane gridPane = (GridPane) alert.getDialogPane().getChildren().get(0);
			GridPane headerTextPanel = new GridPane();
	        headerTextPanel.getChildren().addAll(gridPane.getChildren());
	        Label headerLabel = (Label) headerTextPanel.getChildren().get(0);
	        headerLabel.getStylesheets().add("view/error.css");
	        headerLabel.getStyleClass().add("tools-alert-text-font");
	        StackPane graphicContainer = new StackPane(getImageViewForShowInfo(type));
	        headerTextPanel.add(graphicContainer, 1, 0);
	        headerTextPanel.getColumnConstraints().setAll(gridPane.getColumnConstraints());
	        headerTextPanel.getStyleClass().addAll(gridPane.getStyleClass());
	        headerTextPanel.setMaxWidth(gridPane.getMaxWidth());
	        headerTextPanel.setVisible(true);
	        headerTextPanel.setManaged(true);
			alert.getDialogPane().setHeader(headerTextPanel);
		}

		if (detailInfo != null) {
			String detailText = getDetailTextForShowInfo(type, detailInfo);
			if (detailText != null && !detailText.isEmpty()) {
				TextArea text = new TextArea(detailText);
				text.setEditable(false);
				text.setPrefHeight(getTextHeightForShowInfo(type, detailText));
				if (type == AlertType.NONE) text.setPrefWidth(text.getPrefHeight() * 1.9);
				text.getStylesheets().add("view/error.css");
				text.getStyleClass().add("tools-alert-text-font");
		        alert.getDialogPane().setContent(text);
			}
		}

		if (type == AlertType.CONFIRMATION) {
			ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().getChildren().get(2);
			ObservableList<Node> buttonList = buttonBar.getButtons();
			for (Node buttonTmp : buttonList) {
				Button button = (Button) buttonTmp;
				if (button.isDefaultButton()) {
					button.setText(Constants.P80_BUTTON_TEXT_YES);
				}
				else if (button.isCancelButton()) {
					button.setText(Constants.P80_BUTTON_TEXT_NO);
				}
			}
		}

		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("/images/LightningIcon.png"));
		alert.setResizable(true);

		return alert.showAndWait();
	}

	private String getDetailTextForShowInfo(AlertType type, List<String> detailInfo) {
		String detailText = new String("");
		if (detailInfo != null && !detailInfo.isEmpty()) {
			detailText = new String("");
			for (String error : detailInfo) {
				detailText += error;
				detailText += "\n";
			}
		}
		return detailText;
	}

	private int getLineNumberForShowInfo(String str) {
		int lineNum = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\n') {
				lineNum = lineNum + 1;
			}
		}
		if (!str.isEmpty() && str.charAt(str.length()-1) != '\n') {
			lineNum = lineNum + 1;
		}
		return lineNum;
	}

	private int getTextHeightForShowInfo(AlertType type, String detailInfo) {
		// TODO : to use more effective way
		int height = 0;
		int lineNum = getLineNumberForShowInfo(detailInfo);
		if (type == AlertType.NONE) {
			height = Math.min(300, Math.max(200, 20 * lineNum));
		} else {
			height = Math.min(300, Math.max(200, 20 * lineNum));
		}
		return height;
	}

	private String getTitleForShowInfo(AlertType type) {
		String title;
		switch (type) {
		case ERROR:
			title = Constants.DLG_ERROR_WINDOWS_TITLE;
			break;
		case INFORMATION:
			title = Constants.DLG_INFO_WINDOWS_TITLE;
			break;
		case CONFIRMATION:
			// TODO to set title
		case WARNING:
			// TODO to set title
		case NONE:
			title = Constants.P80_HELP_TITLE;
			break;
		default:
			title = new String("");
			break;
		}
		return title;
	}

	private ImageView getImageViewForShowInfo(AlertType type) {
		ImageView imageView;
		switch (type) {
		case ERROR:
			imageView = new ImageView("/images/tools.dialog/dialog-error.png");
			break;
		case INFORMATION:
			imageView = new ImageView("/images/tools.dialog/dialog-information.png");
			break;
		case CONFIRMATION:
			imageView = new ImageView("/images/tools.dialog/dialog-confirm.png");
			break;
		case WARNING:
			imageView = new ImageView("/images/tools.dialog/dialog-warning.png");
			break;
		case NONE:
		default:
			imageView = null;
			break;
		}
		return imageView;
	}

	@SuppressWarnings("serial")
	final private Map<Class<? extends Exception>, String> errMsgMap = Collections.unmodifiableMap(
			new HashMap<Class<? extends Exception>, String>() {{
				put(ProcessingException.class, Constants.SERVER_ERROR_Processing);
				put(AccessDeniedException.class, Constants.SERVER_ERROR_AccessDenied);
				put(ResourceNotFoundException.class, Constants.SERVER_ERROR_ResourceNotFound);
				put(ResourceInUseException.class, Constants.SERVER_ERROR_ResourceInUse);
				put(AuthenticationFailedException.class, Constants.SERVER_ERROR_AuthenticationFailed);
				put(InternalServerErrorException.class, Constants.SERVER_ERROR_InternalServerError);
				put(ModificationNotAllowedException.class, Constants.SERVER_ERROR_ModificationNotAllowed);
				put(NoResultException.class, Constants.SERVER_ERROR_NoResult);
				put(BadRequestException.class, Constants.SERVER_ERROR_BadRequest);
				put(P84PermissionAnchorPaneReport.ReportFormatException.class, Constants.SERVER_ERROR_ReportFormatWrong);
				put(P84PermissionAnchorPaneReport.WithoutSuperuserException.class, Constants.P84_ERROR_WITHOUT_SUPERUSER);
	}});
	final private String dftErrMsg = Constants.SERVER_ERROR_Unknown;

	/**
	 * APIを実行する時のエラー
	 *
	 * @param mainErrorsメイン　エラーメッセージ
	 * @param e 実行する時の例外
	 */
	protected void showAPIException(String mainErrors, Exception e) {
		logger.debug(MessageFormat.format("Api error, Exception type :", e.getClass()));

		Class<? extends Exception> ec = e.getClass();
		String detail = errMsgMap.get(ec);

		if (e instanceof P81UserAnchorPane.ApplyMeetSuperuserException) {
			detail = e.getMessage();
		}

		if(detail != null) {
			showError(mainErrors, Arrays.asList(detail));
		} else {
			showError(mainErrors, Arrays.asList(dftErrMsg));
		}
	}

	/**
	 * セルの色を設置します。
	 *
	 * @param tableColumn JFXTreeTableColumn
	 * @param functionName 値を取得するメソッド名
	 */
	protected <T extends BaseModel<T>> void addColor(JFXTreeTableColumn<T, String> tableColumn, String functionName) {
		tableColumn.setCellFactory(column -> {
			return new TreeTableCell<T, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty); // This is mandatory
					try {
						TreeItem<T> treeItem = getTreeTableView().getTreeItem(getIndex());
						if (treeItem != null) {
							T auxPerson = treeItem.getValue();
							Method method = auxPerson.getClass().getMethod(functionName, new Class[0]);
							StringBase contendCell = (StringBase) method.invoke(auxPerson, new Object[0]);
							setText(contendCell.get());
							ProcessFlag mainFlag = auxPerson.getFlag();

							//getStyleClass().removeAll(allStyleClasses);
							getStyleClass().clear();

							switch (mainFlag) {
							case ADD:
								getStyleClass().add(Constants.P80_ROW_CELL_CLASS_ADD);
								break;
							case DELETE:
								getStyleClass().add(Constants.P80_ROW_CELL_CLASS_DELETE);
								break;
							case UPDATE:
								getStyleClass().add(Constants.P80_ROW_CELL_CLASS_UPDATE);
								break;
							case NONE:
								switch (contendCell.getFlag()) {
								case ADD:
									getStyleClass().add(Constants.P80_CELL_CLASS_ADD);
									break;
								case DELETE:
									getStyleClass().add(Constants.P80_CELL_CLASS_DELETE);
									break;
								case UPDATE:
									getStyleClass().add(Constants.P80_CELL_CLASS_UPDATE);
									break;
								default:
									getStyleClass().add(Constants.P80_CELL_CLASS_NONE);
									break;
								}
								break;
							default:
								getStyleClass().add(Constants.P80_ROW_CELL_CLASS_NONE);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		});
	}

	/**
	 * テーブルにデータを設置します。
	 *
	 * @param dataList データリスト
	 * @param columns　JFXTreeTableColumnリスト
	 * @param functions 値を取得するメソッド名リスト
	 * @param table JFXTreeTableView
	 */
	protected <T extends BaseModel<T>> void showData(ObservableList<T> dataList,
			List<JFXTreeTableColumn<T, String>> columns,
			List<String> functions,
			JFXTreeTableView<T> table) {
		logger.debug(MessageFormat.format("Showing data, columns num : {0}, rows num : {1}",
				columns == null ? 0 : columns.size(), dataList == null ? 0 : dataList.size()));

		TreeItem<T> root = new RecursiveTreeItem<>(dataList, RecursiveTreeObject::getChildren);
		for(int i=0; i<columns.size(); i++) {
			JFXTreeTableColumn<T, String> column = columns.get(i);
			String function = functions.get(i);
			/*
			column.setCellValueFactory((TreeTableColumn.CellDataFeatures<T, String> param) -> {
				if (column.validateValue(param)) {
					T cell = param.getValue().getValue();
					try {
						Method method = cell.getClass().getMethod(function, new Class[0]);
						StringBase cellData = (StringBase)method.invoke(cell, new Object[0]);
						return cellData;
					} catch(Exception e) {
						e.printStackTrace();
						return null;
					}
				} else {
					return column.getComputedValue(param);
				}
			});
			*/
			addColor(column, function);
		}

		if(!table.getStylesheets().contains("view/tools.css")) {
			table.getStylesheets().add("view/tools.css");
		}

		Platform.runLater(() -> {
			if(!columns.equals(table.getColumns()))
				table.getColumns().setAll(columns);
			table.setRoot(root);
			table.setShowRoot(false);
			table.setEditable(true);
		});
		columns.forEach(column -> {
			column.setContextMenu(null);
		});
	}
}
