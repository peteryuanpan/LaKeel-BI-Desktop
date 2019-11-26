package com.legendapl.lightning.controller;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey;
import com.legendapl.lightning.model.ExcelDefinition;
import com.legendapl.lightning.model.ExcelDefinitionTableRecord;
import com.legendapl.lightning.model.ExcelJob;
import com.legendapl.lightning.model.ParameterTableRecord;
import com.legendapl.lightning.service.CellConversionService;
import com.legendapl.lightning.service.CreateExcelViewService;
import com.legendapl.lightning.service.ExcelCooperationService;
import com.legendapl.lightning.service.ExecuteAPIService;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Excel貼り付けのジョブを作成する画面のコントローラクラス<br>
 * ウィザード形式のため、StackPaneで複数画面を表現する<br>
 * 
 * 一部時間を要する可能性のある処理はバックグラウンドで実行し、画面に待機中スピナーを表示する。
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class W07ExcelPasteWizardController extends C01ToolbarController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	/** 大元のStackPane */
	private StackPane stackPane;

	@FXML
	/** 待機中のスピナーを表示するStackPane */
	private StackPane spinnerPane;

	/** Excel貼り付け定義のモデルクラス */
	private ExcelDefinition excelDefinition;

	/** Excel貼り付け定義のリスト */
	private List<ExcelDefinition> excelDefinitionList = new ArrayList<ExcelDefinition>();

	/** Excelジョブのモデルクラス(複数) */
	private ExcelJob excelJob;

	private final String JOBPATH = System.getProperty("user.dir") + "\\" + Constant.Application.WORK_FILE_PATH
			+ "\\excelJob\\excelDefinition\\" + Constant.ServerInfo.serverName + "\\";
	private final String PARAMPATH = System.getProperty("user.dir") + "\\" + Constant.Application.WORK_FILE_PATH
			+ "\\excelJob\\params\\";

	/*******************************************
	 * 
	 * 進捗表示パネル
	 * 
	 *******************************************/

	@FXML
	/** 進捗を表示する画面左部のStackPane */
	private StackPane flowStackPane;

	@FXML
	/** 進捗を表示する画面左部のVBox */
	private VBox stageVBox;

	@FXML
	/** キャンセルボタン */
	private JFXButton cancelAndMoveButton;

	/*******************************************
	 * 
	 * ホーム画面
	 * 
	 *******************************************/

	@FXML
	/** Excelパスとジョブ一覧を表示するStackPane */
	private StackPane homePane;

	@FXML
	/** Excelファイル選択画面のテキストフィールド */
	private JFXTextField fileSelectTextField;

	@FXML
	/** ジョブの一覧を表示するテーブルビュー */
	private JFXTreeTableView<ExcelDefinitionTableRecord> excelJobTable;

	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> reportLabel;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> targetColumn;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> sheet;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> cell;

	@FXML
	/** 編集ボタン */
	private JFXButton editButton;
	@FXML
	/** 削除ボタン */
	private JFXButton deleteButton;

	/** TableViewのレコード */
	ObservableList<ExcelDefinitionTableRecord> tableRecord = FXCollections.observableArrayList();

	/** Excelファイルのフルパス */
	private String filePath;

	/** 変更が無いか確認するために保持するファイルパス */
	private String prevFilePath;

	/** ウィザード画面が新規作成状態か編集状態かを判定する */
	private boolean editFlag = false;

	/*******************************************
	 * 
	 * レポート選択画面
	 * 
	 *******************************************/

	@FXML
	/** レポート選択画面 */
	private StackPane reportSelectStackPane;

	@FXML
	/** レポ―ト選択画面のテキストフィールド */
	private JFXTextField reportSelectTextField;

	@FXML
	/** レポート名が表示されるテキストフィールド */
	private JFXTextField reportNameTextField;

	/** レポートのURI */
	private String reportUri;

	/** 変更が無いか確認するために保持するレポートのURI */
	private String prevReportUri;

	/*******************************************
	 * 
	 * セル選択画面
	 * 
	 *******************************************/

	@FXML
	/** Excelのプレビュー画面 */
	private StackPane excelViewStackPane;

	@FXML
	/** Excelプレビュー画面のスプレッドシート */
	private SpreadsheetView spreadsheetView;

	@FXML
	/** Excelプレビュー画面のズームバー */
	private Slider slider;

	@FXML
	/** チェックボックス: セル位置を入力する */
	private JFXCheckBox inputCheckBox;

	@FXML
	/** チェックボックス: プレビュー画面から位置を選択する */
	private JFXCheckBox selectCheckBox;

	@FXML
	/** 入力時の列 */
	private JFXTextField columnTextField;

	@FXML
	/** 入力時の行 */
	private JFXTextField rowTextField;

	@FXML
	/** シート選択のコンボボックス */
	private JFXComboBox<String> sheetSelectComboBox;

	@FXML
	/** 入力時のStackPane */
	private StackPane inputStackPane;

	@FXML
	/** プレビュー画面のStackPane */
	private StackPane previewStackPane;

	/** スプレッドシートを作成するサービスクラス */
	private CreateExcelViewService createExcelViewService;

	/** セルの列番号 */
	private Integer columnIndex;

	/** セルの行番号 */
	private Integer rowIndex;

	private boolean afterSecondTime = false;

	/** 変更が無いか確認するために保持するシート名 */
	private String prevSheetName;

	/*******************************************
	 * 
	 * オプション指定画面
	 * 
	 *******************************************/

	@FXML
	/** チェックボックス: 列ヘッダを含む */
	private JFXCheckBox headerCheckBox;

	@FXML
	/** チェックボックス: 通常実行 */
	private JFXCheckBox normalExecuteCheckBox;

	@FXML
	/** チェックボックス: 低負荷実行 */
	private JFXCheckBox lowMemoryExecuteCheckBox;

	@FXML
	/** 列選択画面 */
	private StackPane columnStackPane;

	@FXML
	/** 列選択のリストビュー */
	private ListSelectionView<String> listSelectionView;

	/** 貼り付ける列のリスト(カンマ区切り) */
	private String targetColumns;

	/** 帳票に含まれる列のリスト */
	private List<String> columnList;

	/** 帳票に含まれる詳細バンドのリスト */
	private List<String> detailList;

	/*******************************************
	 * 
	 * パラメータの保存画面
	 * 
	 *******************************************/

	@FXML
	/** パラメータ―の保存画面 */
	private StackPane parameterPane;

	@FXML
	/** パラメータ―の保存チェックボックス */
	private JFXCheckBox parameterCheckBox;

	@FXML
	/** パラメータ―を表示するテーブル */
	private JFXTreeTableView<ParameterTableRecord> parameterTable;

	@FXML
	private JFXTreeTableColumn<ParameterTableRecord, String> name;
	@FXML
	private JFXTreeTableColumn<ParameterTableRecord, String> value;

	@FXML
	private JFXButton openReportExexuteButton;

	/** パラメータのファイル名 */
	private String paramFileName;

	/** パラメータのファイル名とパラメータのHashMap */
	private HashMap<String, HashMap<String, Object>> paramFileNameMap = new HashMap<String, HashMap<String, Object>>();

	/** 削除予定のパラメータリスト */
	private List<String> deleteParamList = new ArrayList<String>();

	/** TableViewのレコードを定義 */
	ObservableList<ParameterTableRecord> paramTableRecordList = FXCollections.observableArrayList();

	/*******************************************
	 * 
	 * 保存先の設定画面
	 * 
	 *******************************************/

	@FXML
	private JFXCheckBox rewriteCheckBox;

	@FXML
	/** 出力先のExcelパス */
	private JFXTextField excelOutputTextField;

	@FXML
	/** タイトルの設定画面 */
	private StackPane saveStackPane;

	@FXML
	/** タイトルの設定画面のテキストフィールド */
	private JFXTextField jobOutputTextField;

	/**
	 * 初期化
	 * 
	 * @param arg0
	 * @param arg1
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("URL=" + arg0 + ", ResourceBulder=" + arg1);

		if (object == null) {
			// 待機中スピナーを非表示にする
			spinnerPane.setVisible(false);
		} else if (object instanceof ExcelJob) {
			new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					excelJob = (ExcelJob) object;

					// Excelの定義リストにジョブに含まれる貼り付けの定義を追加していく
					for (ExcelDefinition excelDefinition : excelJob.getExcelDefinitionList()) {
						excelDefinitionList.add(excelDefinition);
						Platform.runLater(() -> setExcelJobTable(excelDefinition));
					}
					object = null;

					/*************** ホーム画面 ****************/
					// Excelファイルのパス
					Platform.runLater(() -> fileSelectTextField.setText(excelJob.getExcelPath()));
					filePath = excelJob.getExcelPath();

					/*************** 保存先の指定 ****************/
					Platform.runLater(() -> {
						// 上書きフラグ
						if (excelJob.isRewriteFlag())
							rewriteCheckBox.setSelected(true);
						// 保存先のExcelパス
						excelOutputTextField.setText(excelJob.getSaveExcelPath());
						// ジョブタイトル
						jobOutputTextField.setText(JOBPATH + excelJob.getJobPath());
						// 待機中スピナーを非表示にする
						spinnerPane.setVisible(false);
					});
					return null;
				}
			}).start();
		}

		// ホーム画面をセット
		stackPane.getChildren().clear();
		stackPane.setVisible(false);
		saveStackPane.setVisible(false);

		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Platform.runLater(() -> {
					// テーブルビューの列幅を設定
					reportLabel.setPrefWidth(excelJobTable.getWidth() * 0.30);
					targetColumn.setPrefWidth(excelJobTable.getWidth() * 0.30);
					sheet.setPrefWidth(excelJobTable.getWidth() * 0.30);
					cell.setPrefWidth(excelJobTable.getWidth() * 0.1);

					// アイテムが無い時のメッセージ
					excelJobTable.setPlaceholder(new Label(myResource.getString("W07.select_excel.table.empty_msg")));

					// 終了時の確認ダイアログ
					anchorPane.getScene().getWindow().setOnCloseRequest((WindowEvent t) -> {
						// Alertダイアログの利用
						Alert alert = new Alert(AlertType.WARNING, "", ButtonType.YES, ButtonType.CANCEL);
						alert.setTitle(myResource.getString("common.confirmation.dialog.title"));
						alert.getDialogPane().setHeaderText(myResource.getString("W07.common.dialog.header.end"));
						alert.getDialogPane().setContentText(myResource.getString("W07.common.dialog.messeage.end"));

						// アイコンを設定
						Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
						alertStage.getIcons().add(new Image("/images/LightningIcon.png"));

						Optional<ButtonType> result = alert.showAndWait();

						if (result.get() != ButtonType.YES) {
							t.consume();
						}
					});
				});
				return null;
			}
		}).start();

		// アイテム選択時に編集、削除のボタンを有効にする。
		excelJobTable.getSelectionModel().selectedItemProperty().addListener((record, oldVal, newVal) -> {
			editButton.setDisable(false);
			deleteButton.setDisable(false);
		});

		// セル選択画面でシートを変更した際のイベントハンドラ
		sheetSelectComboBox.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldVal, String newVal) -> {
					// シートが変更された際に[プレビュー画面から位置を選択する]にチェックが入っていればExcelビューを更新する
					if (selectCheckBox.isSelected()) {
						// 待機中スピナーを表示
						spinnerPane.setVisible(true);
						prevSheetName = sheetSelectComboBox.getSelectionModel().getSelectedItem();
						new Thread(new Task<Void>() {
							@Override
							protected Void call() throws Exception {
								createExcelView();
								return null;
							}
						}).start();
					}
				});

	}

	/**
	 * 編集時にジョブの各値をノードにバインドする
	 * 
	 * @param excelDefinition
	 */
	public void editExcelDefinition(ExcelDefinition excelDefinition, String excelPath) {

		this.excelDefinition = excelDefinition;

		/*************** Excelファイルの選択 ****************/
		// Excelファイルのパス
		filePath = excelPath;
		prevFilePath = filePath;

		/*************** レポート選択 ****************/
		// レポートのリソースID
		Platform.runLater(() -> {
			reportSelectTextField.setText(excelDefinition.getReportUri());
			reportNameTextField.setText(excelDefinition.getReportName());
		});
		reportUri = excelDefinition.getReportUri();

		/*************** セル位置の指定 ****************/
		// シート一覧を取得してシートのリストにバインドする
		createExcelViewService = new CreateExcelViewService(filePath, 0, false);

		try {
			ObservableList<String> sheetList = createExcelViewService.getSheets();
			Platform.runLater(() -> {
				sheetSelectComboBox.setItems(sheetList);
				sheetSelectComboBox.getSelectionModel().select(excelDefinition.getSheet());
			});
			prevSheetName = excelDefinition.getSheet();
			// セルの指定形式とセルの位置(ビューからの選択の場合はフェーズ移行時にセル位置を設定する)
			CellConversionService cellConversion = new CellConversionService();
			Integer rowIndex = cellConversion.getRowIndex(excelDefinition.getCell());
			String columnAlphabet = cellConversion.getColumnAlphabet(excelDefinition.getCell());
			Platform.runLater(() -> {
				if (excelDefinition.isInputCellFlag()) {
					inputCheckBox.setSelected(true);
					columnTextField.setText(columnAlphabet);
					rowTextField.setText(String.valueOf(rowIndex + 1));
					previewStackPane.setVisible(false);
					inputStackPane.setVisible(true);
				} else if (excelDefinition.isViewSelectCellFlag()) {
					inputCheckBox.setSelected(false);
					selectCheckBox.setSelected(true);
					previewStackPane.setVisible(true);
					inputStackPane.setVisible(false);
				}
			});

			/*************** オプションの選択 ****************/

			// 実行モードの判定(デフォルトで通常実行にチェックが入っているので、通常実行時は何もしない)
			if (excelDefinition.isLowMemoryFlag()) {
				normalExecuteCheckBox.setSelected(false);
				lowMemoryExecuteCheckBox.setSelected(true);
			}

			// 列ヘッダフラグ
			if (excelDefinition.isColumnHeaderFlag())
				Platform.runLater(() -> headerCheckBox.setSelected(true));

			/**
			 * 列ヘッダの可用性を判定する
			 * 
			 * null: 帳票利用不可<br>
			 * false: 列ヘッダ取得不可<br>
			 * true: 列ヘッダ取得可能
			 */
			Boolean enableFlag = ExcelCooperationService.isEnableHeader(excelDefinition.getReportUri());

			// nullの場合は帳票が利用不能
			// if (null == enableFlag) {
			// Platform.runLater(() -> {
			// showDialog(AlertType.ERROR,
			// myResource.getString("common.error.dialog.title"),
			// messageRes.getString(LoggerMessageKey.Error.ERROR_W07_03));
			// excelJobStage.hide();
			// excelJobStage.close();
			// });
			// return;
			// }
			// falseの場合は列ヘッダが無効(=列ヘッダのチェックボックスを無効にする)
			if (null != enableFlag) {
				if (!enableFlag)
					Platform.runLater(() -> headerCheckBox.setDisable(true));

				if (!excelDefinition.getTargetColumns().equals("ALL")) {
					List<String> targetColumnList = Arrays.asList(excelDefinition.getTargetColumns().split(", "));
					Platform.runLater(() -> {
						listSelectionView.getSourceItems().clear();
						// 選択された列リストを追加
						for (int i = 0; i < targetColumnList.size(); i++) {
							listSelectionView.getTargetItems().add(targetColumnList.get(i));
						}
					});
					if (enableFlag) {
						columnList = ExcelCooperationService.getColumnList(reportUri);
						Platform.runLater(() -> {
							// 未選択の列リストを追加(選択済みに含まれるアイテムは追加しない)
							for (int i = 0; i < columnList.size(); i++) {
								if (!targetColumnList.contains(columnList.get(i)))
									listSelectionView.getSourceItems().add(columnList.get(i));
							}
						});
					} else {
						detailList = ExcelCooperationService.getDetailList(reportUri);
						Platform.runLater(() -> {
							for (int i = 0; i < detailList.size(); i++) {
								if (!targetColumnList.contains(detailList.get(i)))
									listSelectionView.getSourceItems().add(detailList.get(i));
							}
						});
					}
				} else {
					if (enableFlag) {
						columnList = ExcelCooperationService.getColumnList(reportUri);
						Platform.runLater(() -> {
							// 列ヘッダが有効かつ"ALL"の場合はcolumnListをすべて選択済みへバインドする。
							for (int i = 0; i < columnList.size(); i++) {
								listSelectionView.getTargetItems().add(columnList.get(i));
							}
						});
					} else {
						detailList = ExcelCooperationService.getDetailList(reportUri);
						Platform.runLater(() -> {
							// 列ヘッダが無効かつ"ALL"の場合はdetailListListをすべて選択済みへバインドする。
							for (int i = 0; i < detailList.size(); i++) {
								listSelectionView.getTargetItems().add(detailList.get(i));
							}
						});
					}
				}
			}
			prevReportUri = reportUri;

			/*************** パラメータの保存 ****************/
			paramFileName = excelDefinition.getParameterFileName();

			if (StringUtils.isEmpty(excelDefinition.getParameterFileName()))
				parameterCheckBox.setSelected(false);
			else {
				parameterCheckBox.setSelected(true);

				if (null != paramFileNameMap && paramFileNameMap.containsKey(excelDefinition.getParameterFileName())) {
					setParameterTable(paramFileNameMap.get(excelDefinition.getParameterFileName()));
				} else {
					// バイトデータを読み取り、dataMapに設定
					byte[] tempbytes = ExcelCooperationService
							.readFileToByte(PARAMPATH + excelDefinition.getParameterFileName() + ".dat");

					ByteArrayInputStream bais = new ByteArrayInputStream(tempbytes);
					ObjectInputStream ois = new ObjectInputStream(bais);
					@SuppressWarnings("unchecked")
					HashMap<String, Object> parameter = (HashMap<String, Object>) ois.readObject();

					setParameterTable(parameter);
				}
			}

			// 編集時のフラグを有効にする。
			editFlag = true;

			// 待機中スピナーを非表示にする
			Platform.runLater(() -> spinnerPane.setVisible(false));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Platform.runLater(() -> {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_99_HEADER));
				excelJobStage.hide();
				excelJobStage.close();
			});
		}
	}

	/**
	 * [キャンセル]ボタン<br>
	 * 定義の作成をキャンセルしてホーム画面に戻る
	 * 
	 * @param event
	 */
	public void cancelAndMoveToHome(ActionEvent event) {
		Alert alert = new Alert(AlertType.WARNING, "", ButtonType.YES, ButtonType.NO);
		alert.setTitle(myResource.getString("common.warn.dialog.title"));
		alert.getDialogPane().setHeaderText(myResource.getString("W07.common.dialog.header.go_home"));
		alert.getDialogPane().setContentText(myResource.getString("W07.common.dialog.messeage.go_home"));

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() != ButtonType.YES) {
			return;
		}

		// ウィザードのStackPaneをクリアする。
		stackPane.getChildren().clear();
		for (int i = 0; i < stageVBox.getChildren().size(); i++) {
			((HBox) stageVBox.getChildren().get(i)).getChildren().get(1).setStyle("");
		}
		// 全てのノードの値をクリアする
		clearAll();

		homePane.setVisible(true);
	}

	/*******************************************
	 * 
	 * ホーム画面
	 * 
	 *******************************************/

	/**
	 * [参照]ボタン<br>
	 * Excelファイル選択のExplorerを開く
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void openFileExplorer(ActionEvent event) throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(StringUtils.isEmpty(Constant.ServerInfo.workspace)
				? System.getProperty("user.home") : Constant.ServerInfo.exportFolderPath));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XLSX Files", "*.xlsx"),
				new ExtensionFilter("XLSM Files", "*.xlsm"));
		fileChooser.setTitle(myResource.getString("W07.select_excel.file_chooser.title"));

		File saveFile = fileChooser.showOpenDialog(excelJobStage);

		if (null == saveFile) {
			return;
		}

		// ファイルが存在するかチェックする
		if (null != saveFile && !saveFile.exists()) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_05));
			return;
		}

		CreateExcelViewService createExcelViewService = new CreateExcelViewService(saveFile.getPath(), 0, false);
		ObservableList<String> sheetList = createExcelViewService.getSheets();

		// Excelファイルを変更した際のバリデーション
		for (ExcelDefinitionTableRecord record : tableRecord) {
			for (int i = 0; i < sheetList.size(); i++) {
				if (record.getSheet().get().equals(sheetList.get(i)))
					break;
				else if (i == sheetList.size() - 1) {
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
							MessageFormat.format(messageRes.getString(LoggerMessageKey.Error.ERROR_W07_19),
									saveFile.getPath().substring(saveFile.getPath().lastIndexOf("\\") + 1),
									record.getSheet().get()));
					return;
				}

			}
		}

		if (null != saveFile)
			fileSelectTextField.setText(saveFile.getPath());

		filePath = fileSelectTextField.getText();
	}

	/**
	 * [新規作成]ボタン<br>
	 * 
	 * @param event
	 */
	public void createJob(ActionEvent event) throws IOException {

		// Excelファイルのテキストフィールドの入力があるかチェックする。
		if (StringUtils.isEmpty(filePath)) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_04));
			return;
		}

		// Excelファイルが存在するかチェックする
		File file = new File(filePath);
		if (!file.exists()) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_05));
			return;
		}

		// 貼り付け定義の上限のチェック
		if (null != excelJobTable.getRoot() && excelJobTable.getRoot().getChildren().size() >= 12) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_16));
			return;
		}

		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(0)).getChildren().get(1).setStyle("-fx-font-weight: bold;");
		stackPane.getChildren().add(reportSelectStackPane);
		stackPane.setVisible(true);
		homePane.setVisible(false);
	}

	/**
	 * [編集]ボタン<br>
	 * 
	 * @param event
	 */
	public void editJob(ActionEvent event) throws IOException {

		// シートとセルを取得(シート+セルをキーに定義を特定する)
		String cell = excelJobTable.getSelectionModel().getSelectedItem().getValue().getCell().getValue();
		String sheet = excelJobTable.getSelectionModel().getSelectedItem().getValue().getSheet().getValue();

		// 定義のリストから編集対象の定義を元に編集画面を表示する。
		for (ExcelDefinition definition : excelDefinitionList) {
			if (definition.getCell().equals(cell) && definition.getSheet().equals(sheet)) {
				new Thread(new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						editExcelDefinition(definition, fileSelectTextField.getText());
						return null;
					}
				}).start();
				break;
			}
		}

		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(0)).getChildren().get(1).setStyle("-fx-font-weight: bold;");
		stackPane.getChildren().add(reportSelectStackPane);
		stackPane.setVisible(true);
		spinnerPane.setVisible(true);
		homePane.setVisible(false);

		// 編集と削除ボタンを無効化
		editButton.setDisable(true);
		deleteButton.setDisable(true);

	}

	/**
	 * [削除]ボタン<br>
	 * 
	 * @param event
	 */
	public void deleteJob(ActionEvent event) throws IOException {

		// シートとセルを取得(シート+セルをキーにジョブを特定する)
		String cell = excelJobTable.getSelectionModel().getSelectedItem().getValue().getCell().getValue();
		String sheet = excelJobTable.getSelectionModel().getSelectedItem().getValue().getSheet().getValue();

		// ジョブのリストから削除対象のジョブを消去する
		for (ExcelDefinition job : excelDefinitionList) {
			if (job.getCell().equals(cell) && job.getSheet().equals(sheet)) {
				excelDefinitionList.remove(job);
				deleteParamList.add(job.getParameterFileName());
				break;
			}
		}

		// テーブルビューからアイテムを消去
		tableRecord.remove(excelJobTable.getSelectionModel().getSelectedItem().getValue());

		// 選択をクリア
		excelJobTable.getSelectionModel().clearSelection();

		// 編集と削除ボタンを無効化
		editButton.setDisable(true);
		deleteButton.setDisable(true);

	}

	/**
	 * [次へ]ボタン<br>
	 * ホーム画面から保存先の指定へ
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void homeToSave(ActionEvent event) throws Exception {

		// 定義が一つもない場合はエラー
		if (null == excelJobTable.getRoot() || excelJobTable.getRoot().getChildren().size() == 0) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_17));
			return;
		}

		// Excelの出力先はデフォルトで参照元のExcelファイルのパスをバインドする。パスに変更があった際は変更時にテキストフィールドを空にしている
		if (StringUtils.isEmpty(excelOutputTextField.getText()))
			excelOutputTextField.setText(filePath);

		// 画面を保存先の指定に切り替える
		homePane.setVisible(false);
		saveStackPane.setVisible(true);

	}

	/*******************************************
	 * 
	 * レポート選択画面
	 * 
	 *******************************************/

	/**
	 * [参照]ボタン<br>
	 * レポート選択のダイアログを表示する
	 * 
	 * @param event
	 */
	public void openReportSelectDiarog(ActionEvent event) {
		try {
			Stage reportStage = showPane(event, "/view/W10ReportSelectAnchorPane.fxml",
					myResource.getString("W10.window.title"), Modality.APPLICATION_MODAL, primaryStage);
			reportStage.showAndWait();
			String[] uriAndLabel = (String[]) object;
			if (null != uriAndLabel) {
				reportSelectTextField.setText(uriAndLabel[0]);
				reportNameTextField.setText(uriAndLabel[1]);
			}
			setObject(null);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * [次へ]ボタン<br>
	 * レポート選択からセル選択へ
	 * 
	 * @param event
	 */
	public void reportSelectToCellSelect(ActionEvent event) throws IOException {

		reportUri = reportSelectTextField.getText();
		// 待機中スピナーを表示
		spinnerPane.setVisible(true);
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				if (StringUtils.isEmpty(reportUri)) {
					spinnerPane.setVisible(false);
					Platform.runLater(
							() -> showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									messageRes.getString(LoggerMessageKey.Error.ERROR_W07_01)));
					return null;
				}

				try {
					ExecuteAPIService.getClientReportUnit(reportUri);
				} catch (Exception e) {
					spinnerPane.setVisible(false);
					Platform.runLater(
							() -> showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									messageRes.getString(LoggerMessageKey.Error.ERROR_W07_02)));
					return null;
				}

				// 列ヘッダが有効かどうかチェックする
				Boolean enableFlag;
				try {
					enableFlag = ExcelCooperationService.isEnableHeader(reportUri);
				} catch (Exception e) {
					spinnerPane.setVisible(false);
					Platform.runLater(
							() -> showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									messageRes.getString(LoggerMessageKey.Error.ERROR_W07_03)));
					return null;
				}

				// nullの場合: 帳票自体がそもそも実行不可
				if (null == enableFlag) {
					spinnerPane.setVisible(false);
					Platform.runLater(
							() -> showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									messageRes.getString(LoggerMessageKey.Error.ERROR_W07_03)));
					return null;
				}

				// [編集時でプレビューからの表示になっている]且つ[1週目]且つ[excelPathに変更が無い]の場合にはExcelのプレビュー画面を生成する
				if (null != excelDefinition && excelDefinition.isViewSelectCellFlag() && !afterSecondTime
						&& filePath.equals(prevFilePath)) {
					createExcelView();
				}

				if (!filePath.equals(prevFilePath)) {
					Platform.runLater(() -> {
						// デフォルトで[セルの位置を入力する]にチェックを入れておく
						selectCheckBox.setSelected(false);
						inputCheckBox.setSelected(true);
					});

					// シート一覧を取得してシートのリストにバインドする
					createExcelViewService = new CreateExcelViewService(filePath, 0, false);
					ObservableList<String> sheetList = createExcelViewService.getSheets();

					// 初期化処理
					Platform.runLater(() -> {

						sheetSelectComboBox.setItems(sheetList);

						// 初期表示のみデフォルトで一番左のシートを選択
						if (sheetSelectComboBox.getSelectionModel().getSelectedItem() == null)
							sheetSelectComboBox.getSelectionModel().select(0);

						// デフォルトではプレビュー表示は無効にする
						previewStackPane.setVisible(false);
						// セルのテキスト入力を有効にする
						inputStackPane.setVisible(true);

						// 行と列のテキストフィールドを空にする
						columnTextField.clear();
						rowTextField.clear();

						// 保存先のExcelパスを空にする(空の場合、デフォルトで元のExcelパスがバインドされるため)
						excelOutputTextField.clear();

						// 異なるExcelでもシート名が同名だとビューが更新されないのでファイルが変更された段階でシート名の変数をNULLにする
						prevSheetName = null;
					});
				}

				Platform.runLater(() -> {
					// 待機中スピナーを非表示
					spinnerPane.setVisible(false);
					stackPane.getChildren().clear();
					stackPane.getChildren().add(flowStackPane);
					((HBox) stageVBox.getChildren().get(0)).getChildren().get(1).setStyle("");
					((HBox) stageVBox.getChildren().get(1)).getChildren().get(1).setStyle("-fx-font-weight: bold;");
					stackPane.getChildren().add(excelViewStackPane);
				});
				prevFilePath = filePath;
				return null;
			}
		}).start();

	}

	/*******************************************
	 * 
	 * セル選択画面
	 * 
	 *******************************************/

	/**
	 * 選択されたExcelを開く
	 * 
	 * @param event
	 */
	public void openExcel(ActionEvent event) throws IOException {
		/**
		 * エクスポートしたファイルを起動する
		 * 
		 * ファイルによっては時間がかかるのでバックグラウンドスレッドで開く
		 */
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				// ファイルを開く
				try {
					File file = new File(filePath);
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				return null;
			}
		}).start();

	}

	/**
	 * Excelのプレビュー画面(spreadsheetView)の設定を行うメソッド<br>
	 * バックグラウンドスレッドで実行される。
	 * 
	 */
	public void createExcelView() {
		try {
			createExcelViewService = new CreateExcelViewService(filePath,
					sheetSelectComboBox.getSelectionModel().getSelectedIndex(), false);

			// 2回目以降は先に既存のビューを消去する。
			if (afterSecondTime) {
				Platform.runLater(() -> {
					previewStackPane.getChildren().remove(3);
					previewStackPane.getChildren().remove(2);
				});
			}

			// spreadsheetView(Excelのプレビュー画面)の設定
			spreadsheetView = createExcelViewService.getView();
			StackPane.setMargin(spreadsheetView, new Insets(30, 0, 0, 0));

			// slider(ズームバー)の設定
			slider = new Slider(0.25D, 2D, 1.0D);
			slider.setMaxWidth(200);
			slider.setPrefWidth(200);
			StackPane.setAlignment(slider, Pos.TOP_RIGHT);
			StackPane.setMargin(slider, new Insets(10, 0, 0, 0));

			Platform.runLater(() -> {
				previewStackPane.getChildren().add(spreadsheetView);
				previewStackPane.getChildren().add(slider);
			});

			// sliderとspreadsheetViewの関連付け
			spreadsheetView.zoomFactorProperty().bindBidirectional(slider.valueProperty());

			// [1回目]且つ[編集時]且つ[ファイルパスに変更が無い]且つ[画面選択の設定になっている(編集前のジョブが)]場合に設定されたセルを選択しておく
			if (!afterSecondTime && excelDefinition != null && excelDefinition.isViewSelectCellFlag()
					&& filePath.equals(excelJob.getExcelPath())) {
				CellConversionService cellConversion = new CellConversionService();
				Integer columnIndex = cellConversion.getColumnIndex(excelDefinition.getCell());
				Integer rowIndex = cellConversion.getRowIndex(excelDefinition.getCell());
				Platform.runLater(() -> spreadsheetView.getSelectionModel().select(rowIndex,
						spreadsheetView.getColumns().get(columnIndex)));
			}

			// 2回目以降のフラグを立てる
			afterSecondTime = true;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			// 待機中スピナーを非表示にする
			Platform.runLater(() -> spinnerPane.setVisible(false));
		}
	}

	/**
	 * [セルの位置を入力する]にチェックを入れた時のイベントハンドラ<br>
	 * テキスト入力を有効にする
	 * 
	 * @param event
	 */
	public void checkInput(ActionEvent event) throws IOException {
		// 既にチェックされていた場合は何もしない
		if (!selectCheckBox.isSelected()) {
			inputCheckBox.setSelected(true);
			return;
		}

		selectCheckBox.setSelected(false);
		inputCheckBox.setSelected(true);

		previewStackPane.setVisible(false);
		inputStackPane.setVisible(true);

	}

	/**
	 * [プレビュー画面からセルの位置を選択する]にチェックを入れた時のイベントハンドラ<br>
	 * UIからのセル選択を有効にする
	 * 
	 * @param event
	 */
	public void checkPreview(ActionEvent event) throws IOException {
		// 既にチェックされていた場合は何もしない
		if (!inputCheckBox.isSelected()) {
			selectCheckBox.setSelected(true);
			return;
		}

		selectCheckBox.setSelected(true);
		inputCheckBox.setSelected(false);

		previewStackPane.setVisible(true);
		inputStackPane.setVisible(false);

		// [プレビュー画面から位置を選択する]にチェックを入れた段階で選択したシートに変更があればExcelビューを更新する
		if (!sheetSelectComboBox.getSelectionModel().getSelectedItem().equals(prevSheetName)
				|| null == spreadsheetView) {
			prevSheetName = sheetSelectComboBox.getSelectionModel().getSelectedItem();
			// 待機中スピナーを表示
			spinnerPane.setVisible(true);
			new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					createExcelView();
					return null;
				}
			}).start();
		}
	}

	/**
	 * [戻る]ボタン<br>
	 * セル選択からレポート選択へ
	 * 
	 * @param event
	 */
	public void reportSelectFromCellSelect(ActionEvent event) throws IOException {
		stackPane.getChildren().clear();
		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(1)).getChildren().get(1).setStyle("");
		((HBox) stageVBox.getChildren().get(0)).getChildren().get(1).setStyle("-fx-font-weight: bold;");
		stackPane.getChildren().add(reportSelectStackPane);
	}

	/**
	 * [次へ]ボタン<br>
	 * Excelプレビューから列選択へ
	 * 
	 * @param event
	 */
	public void cellSelectToColumnSelect(ActionEvent event) throws IOException {

		/** プレビュー画面から選択の場合 */
		if (selectCheckBox.isSelected()) {
			// 画面から選択する際にセルが選択されているかチェックする
			columnIndex = spreadsheetView.getSelectionModel().getFocusedCell().getColumn();
			rowIndex = spreadsheetView.getSelectionModel().getFocusedCell().getRow();
			if ((columnIndex == -1 || rowIndex == -1)) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_06));
				return;
			}
		}
		/** 入力でのセル指定の場合 */
		else if (inputCheckBox.isSelected()) {
			/**
			 * 入力、フォーマットのチェック
			 */
			// 入力で指定する際に入力がされているかチェックする
			if (StringUtils.isEmpty(columnTextField.getText()) || StringUtils.isEmpty(rowTextField.getText())) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_07));
				return;
			}
			// 列が半角英字もしくは半角数字であるかどうか(Excelの設定に応じて列は数字での指定に対応)※半角英数字は不可！！！！
			else if (!(columnTextField.getText().matches("^[0-9]*$")
					|| columnTextField.getText().matches("^[a-zA-Z]+$"))) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_08));
				return;
			}
			// 行が半角数値であるかどうか
			else if (!rowTextField.getText().matches("^[0-9]*$")) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_09));
				return;
			}

			/**
			 * 値の最大値チェック
			 */
			// 列を数値で入力されている場合
			if (columnTextField.getText().matches("^[0-9]*$")) {
				int value = Integer.parseInt(columnTextField.getText());
				if (value > 16384) {
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
							messageRes.getString(LoggerMessageKey.Error.ERROR_W07_14));
					return;
				}
			}
			// 列を英字で入力されている場合
			else if (columnTextField.getText().matches("^[a-zA-Z]+$")) {
				if (columnTextField.getText().length() > 3) {
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
							messageRes.getString(LoggerMessageKey.Error.ERROR_W07_14));
					return;
				}
				CellConversionService cellConversionService = new CellConversionService(columnTextField.getText(),
						Integer.parseInt(rowTextField.getText()));
				int value = cellConversionService.getColumnIndex(cellConversionService.getCellString());
				if (value > 16383) { // ここのvalueはインデックスであるため、1引く
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
							messageRes.getString(LoggerMessageKey.Error.ERROR_W07_14));
					return;
				}
			}

			// 行
			if (Integer.parseInt(rowTextField.getText()) > 1048576) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_15));
				return;
			}

		}

		// シートとセルの複合キーで重複チェックを行う
		CellConversionService cellConversion = null;
		if (selectCheckBox.isSelected()) {
			cellConversion = new CellConversionService(columnIndex, rowIndex + 1);
		} else if (inputCheckBox.isSelected()) {
			cellConversion = new CellConversionService(columnTextField.getText(),
					Integer.parseInt(rowTextField.getText()));
		}
		if (null != excelJobTable.getRoot()) {
			for (TreeItem<ExcelDefinitionTableRecord> record : excelJobTable.getRoot().getChildren()) {

				/** テーブルビューのセルとシート */
				String recordCell = record.getValue().getCell().get();
				String recordSheet = record.getValue().getSheet().get();
				/** 選択されたセルとシート */
				String selectCell = cellConversion.getCellString();
				String selectSheet = sheetSelectComboBox.getSelectionModel().getSelectedItem();

				// 選択したセルとシートの組み合わせが現在編集中の定義の場合は重複を許可する
				if (null != excelDefinition && excelDefinition.getCell().equals(selectCell)
						&& excelDefinition.getSheet().equals(selectSheet))
					break;

				if (recordCell.equals(selectCell) && recordSheet.equals(selectSheet)) {
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
							messageRes.getString(LoggerMessageKey.Error.ERROR_W07_18));
					return;
				}
			}
		}

		// レポートに変更が無いか確認し、変更があれば列選択を初期化する
		if (!reportUri.equals(prevReportUri)) {

			// 列ヘッダが有効かどうかチェックする
			Boolean enableFlag = ExcelCooperationService.isEnableHeader(reportUri);

			// nullの場合: 帳票自体がそもそも実行不可
			if (null == enableFlag) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_03));
				return;
			}

			// 待機中スピナーを表示
			spinnerPane.setVisible(true);

			new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					Platform.runLater(() -> {
						listSelectionView.getSourceItems().clear();
						listSelectionView.getTargetItems().clear();
					});

					// falseの場合: 列ヘッダに余計なコンポーネントがある、または詳細バンドのコンポーネント数と一致しない
					if (!enableFlag) {
						// 詳細バンドのリストを取得する
						detailList = ExcelCooperationService.getDetailList(reportUri);
						for (String detail : detailList) {
							Platform.runLater(() -> listSelectionView.getSourceItems().add(detail));
						}
						Platform.runLater(() -> headerCheckBox.setDisable(true));
					}
					// trueの場合: 列ヘッダに余計なコンポーネントが無い、且つ詳細バンドのコンポーネント数と一致する
					else if (enableFlag) {
						// 帳票に含まれる列リストを取得
						columnList = ExcelCooperationService.getColumnList(reportUri);
						// 詳細バンドのリストを取得する
						detailList = ExcelCooperationService.getDetailList(reportUri);
						for (String column : columnList) {
							Platform.runLater(() -> listSelectionView.getSourceItems().add(column));
						}
						Platform.runLater(() -> headerCheckBox.setDisable(false));
					}

					// 待機中スピナーを非表示にする
					Platform.runLater(() -> spinnerPane.setVisible(false));
					return null;
				}
			}).start();
			prevReportUri = reportUri;
		}

		stackPane.getChildren().clear();
		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(1)).getChildren().get(1).setStyle("");
		((HBox) stageVBox.getChildren().get(2)).getChildren().get(1).setStyle("-fx-font-weight: bold;");

		stackPane.getChildren().add(columnStackPane);
	}

	/*******************************************
	 * 
	 * オプション指定画面
	 * 
	 *******************************************/

	/**
	 * [戻る]ボタン<br>
	 * オプション指定からExcelプレビューへ
	 * 
	 * @param event
	 */
	public void cellSelectFromColumnSelect(ActionEvent event) throws IOException {
		stackPane.getChildren().clear();
		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(2)).getChildren().get(1).setStyle("");
		((HBox) stageVBox.getChildren().get(1)).getChildren().get(1).setStyle("-fx-font-weight: bold;");

		stackPane.getChildren().add(excelViewStackPane);
	}

	/**
	 * [次へ]ボタン<br>
	 * オプション指定からパラメータ保存の設定へ
	 * 
	 * @param event
	 */
	public void columnSelectToParameterSelect(ActionEvent event) throws IOException {

		if (listSelectionView.getTargetItems().size() == 0) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_10));
			return;
		}
		// 入力コントロールを含まない場合はチェックボックスを無効化する
		if (null == ExecuteAPIService.getClientReportUnit(reportUri).getInputControls()) {
			parameterCheckBox.setSelected(false);
			parameterCheckBox.setDisable(true);
			openReportExexuteButton.setDisable(true);
		} else {
			parameterCheckBox.setDisable(false);
			openReportExexuteButton.setDisable(false);
		}

		// 選択済みに追加した列一覧を取得する
		StringBuilder builder = new StringBuilder();
		for (String str : listSelectionView.getTargetItems()) {
			// テキストフィールドの場合、両端のダブルクォートを消去する
			if (str.startsWith("\""))
				str = str.substring(1, str.length() - 1);

			builder.append(str + ", ");
		}
		if (builder.toString().length() == 0)
			targetColumns = null;
		else
			targetColumns = builder.toString().substring(0, builder.toString().length() - 2);

		stackPane.getChildren().clear();
		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(2)).getChildren().get(1).setStyle("");
		((HBox) stageVBox.getChildren().get(3)).getChildren().get(1).setStyle("-fx-font-weight: bold;");

		stackPane.getChildren().add(parameterPane);

		// パラメータテーブルの列幅を設定
		double activeWidth = anchorPane.getScene().getWindow().getWidth() - 200;// 200は進捗パネルの幅
		value.setPrefWidth(activeWidth * 0.7);
		name.setPrefWidth(activeWidth * 0.3);

		// アイテムが無い時のメッセージを設定
		parameterTable.setPlaceholder(new Label(myResource.getString("W07.save_parameter.table.empty_msg")));
	}

	/**
	 * [通常実行]にチェックを入れた時のイベントハンドラ
	 * 
	 * @param event
	 */
	public void checkNormal(ActionEvent event) throws IOException {
		// 既にチェックされていた場合は何もしない
		if (!lowMemoryExecuteCheckBox.isSelected()) {
			normalExecuteCheckBox.setSelected(true);
			return;
		}

		normalExecuteCheckBox.setSelected(true);
		lowMemoryExecuteCheckBox.setSelected(false);
	}

	/**
	 * [低負荷実行]にチェックを入れた時のイベントハンドラ
	 * 
	 * @param event
	 */
	public void checkLowMemory(ActionEvent event) throws IOException {
		// 既にチェックされていた場合は何もしない
		if (!normalExecuteCheckBox.isSelected()) {
			lowMemoryExecuteCheckBox.setSelected(true);
			return;
		}

		normalExecuteCheckBox.setSelected(false);
		lowMemoryExecuteCheckBox.setSelected(true);
	}

	/*******************************************
	 * 
	 * パラメータ指定画面
	 * 
	 *******************************************/

	/**
	 * [完了]ボタン<br>
	 * Excel貼り付け定義を保存し、ホーム画面に戻る。
	 * 
	 * @param event
	 */
	public void saveSingleJob(ActionEvent event) {

		if (parameterCheckBox.isSelected()) {
			// パラメータが一つもない場合はエラー
			if (null == parameterTable.getRoot() || parameterTable.getRoot().getChildren().size() == 0) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						myResource.getString("W09.window.title"));
				return;
			}
		}

		// 編集時にパラメータを保存しないに変更した場合にパラメータファイルを削除予定リストに追加する。
		if (!parameterCheckBox.isSelected() && null != excelDefinition
				&& !StringUtils.isEmpty(excelDefinition.getParameterFileName())) {
			deleteParamList.add(excelDefinition.getParameterFileName());
		}

		// ウィザードのStackPaneをクリアする。
		stackPane.getChildren().clear();
		((HBox) stageVBox.getChildren().get(3)).getChildren().get(1).setStyle("");

		ExcelDefinition excelDefinition = new ExcelDefinition();
		excelDefinition.setReportUri(reportUri);// リソースID
		excelDefinition.setReportName(reportNameTextField.getText());
		excelDefinition.setInputCellFlag(inputCheckBox.isSelected());// 列ヘッダ
		excelDefinition.setViewSelectCellFlag(selectCheckBox.isSelected());// セルのビュー選択
		excelDefinition.setColumnHeaderFlag(headerCheckBox.isSelected());// セルの入力
		excelDefinition.setParameterFileName(paramFileName);// パラメータのファイル名

		if (lowMemoryExecuteCheckBox.isSelected())
			excelDefinition.setLowMemoryFlag(true);
		else
			excelDefinition.setLowMemoryFlag(false);

		CellConversionService cellConversion = null;
		if (selectCheckBox.isSelected()) {
			cellConversion = new CellConversionService(columnIndex, rowIndex + 1);
		} else if (inputCheckBox.isSelected()) {
			cellConversion = new CellConversionService(columnTextField.getText(),
					Integer.parseInt(rowTextField.getText()));
		}
		excelDefinition.setCell(cellConversion.getCellString()); // セル

		excelDefinition.setSheet(sheetSelectComboBox.getSelectionModel().getSelectedItem());// シート

		// 全ての列を選択しているかを確認する(全列を選択していても並び順に変更があれば"ALL"の扱いにはならない)
		boolean allColumnFlag = true;
		if (null != columnList) {
			if (columnList.size() == listSelectionView.getTargetItems().size()) {
				for (int i = 0; i < columnList.size(); i++) {
					if (!columnList.get(i).equals(listSelectionView.getTargetItems().get(i))) {
						allColumnFlag = false;
						break;
					}
				}
			} else
				allColumnFlag = false;
		} else {
			if (detailList.size() == listSelectionView.getTargetItems().size()) {
				for (int i = 0; i < detailList.size(); i++) {
					if (!detailList.get(i)
							.equals(listSelectionView.getTargetItems().get(i).replace((i + 1) + ": ", ""))) {
						allColumnFlag = false;
						break;
					}
				}
			} else
				allColumnFlag = false;
		}

		if (allColumnFlag)
			excelDefinition.setTargetColumns("ALL");
		else
			excelDefinition.setTargetColumns(targetColumns);

		// ジョブのリストに追加(編集時は上書きするためリストにaddしない)
		if (!editFlag)
			excelDefinitionList.add(excelDefinition);

		// ホーム画面のテーブルビューに値をセットする
		setExcelJobTable(excelDefinition);

		// 全てのノードの値をクリアする
		clearAll();

		homePane.setVisible(true);
	}

	/**
	 * 貼り付け定義のモデルクラス(ExcelDefinition)を引数に受け取り、テーブルビュー(JFXTreeTableView)に値をバインドする<br>
	 * 
	 * @param excelDefinition
	 */
	@SuppressWarnings("unchecked")
	public void setExcelJobTable(ExcelDefinition excelDefinition) {

		// ExcelDefinitionからテーブルビューのモデルクラス(ExcelDefinitionTableRecord)にデータをバインド
		ExcelDefinitionTableRecord excelDefinitionTableRecord = new ExcelDefinitionTableRecord();
		excelDefinitionTableRecord.setReportLabel(excelDefinition.getReportName());
		excelDefinitionTableRecord.setTargetColumns(excelDefinition.getTargetColumns());
		excelDefinitionTableRecord.setSheet(excelDefinition.getSheet());
		excelDefinitionTableRecord.setCell(excelDefinition.getCell());

		// テーブルにアイテム(ジョブ)の追加/編集を行う。
		if (editFlag) {
			// シートとセルを取得(シート+セルをキーにジョブを特定する)
			String cell = excelJobTable.getSelectionModel().getSelectedItem().getValue().getCell().getValue();
			String sheet = excelJobTable.getSelectionModel().getSelectedItem().getValue().getSheet().getValue();

			// ジョブのリストから編集対象のジョブを上書きする
			for (int i = 0; i < excelDefinitionList.size(); i++) {
				if (excelDefinitionList.get(i).getCell().equals(cell)
						&& excelDefinitionList.get(i).getSheet().equals(sheet)) {
					excelDefinitionList.set(i, excelDefinition);
					break;
				}
			}
			// テーブルビューを上書きする
			excelJobTable.getSelectionModel().getSelectedItem().setValue(excelDefinitionTableRecord);
			return;
		} else
			tableRecord.add(excelDefinitionTableRecord);

		TreeItem<ExcelDefinitionTableRecord> root = new RecursiveTreeItem<>(tableRecord,
				RecursiveTreeObject::getChildren);

		reportLabel
				.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
					if (reportLabel.validateValue(param)) {
						return param.getValue().getValue().getReportLabel();
					} else {
						return reportLabel.getComputedValue(param);
					}
				});
		targetColumn
				.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
					if (targetColumn.validateValue(param)) {
						return param.getValue().getValue().getTargetColumns();
					} else {
						return targetColumn.getComputedValue(param);
					}
				});
		sheet.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
			if (sheet.validateValue(param)) {
				return param.getValue().getValue().getSheet();
			} else {
				return sheet.getComputedValue(param);
			}
		});
		cell.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
			if (cell.validateValue(param)) {
				return param.getValue().getValue().getCell();
			} else {
				return cell.getComputedValue(param);
			}
		});

		// グルーピングを不可にする
		reportLabel.setContextMenu(null);
		targetColumn.setContextMenu(null);
		sheet.setContextMenu(null);
		cell.setContextMenu(null);

		excelJobTable.getColumns().setAll(reportLabel, targetColumn, sheet, cell);
		excelJobTable.setRoot(root);
		excelJobTable.setShowRoot(false);
		excelJobTable.setEditable(true);
	}

	/**
	 * [戻る]ボタン<br>
	 * 
	 * @param event
	 */
	public void columnSelectFromParameterSelect(ActionEvent event) {
		stackPane.getChildren().clear();
		stackPane.getChildren().add(flowStackPane);
		((HBox) stageVBox.getChildren().get(3)).getChildren().get(1).setStyle("");
		((HBox) stageVBox.getChildren().get(2)).getChildren().get(1).setStyle("-fx-font-weight: bold;");

		stackPane.getChildren().add(columnStackPane);
	}

	/**
	 * [パラメータ入力画面の表示]ボタン<br>
	 * レポート実行画面を表示する。
	 * 
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void openReportWindow(ActionEvent event) {
		// 実行するレポートを画面遷移先にセット
		setObject(reportSelectTextField.getText() + Constant.ExcelJob.SAVEPARAMETERKEY);

		Stage reportStage;
		try {
			reportStage = showPane(event, "/view/W04InputControlAnchorPane.fxml", reportNameTextField.getText(),
					Modality.WINDOW_MODAL, null);

			// 画面サイズの取得
			loadWindowSize();
			Double displayWidth = virtualBounds.getWidth();
			// ウィンドウ位置が記録されているか確認。
			if (preferences.getReportWindowY() != null && preferences.getReportWindowX() != null) {
				// 記録されたウィンドウ位置
				Double preferencesX = Double.parseDouble(preferences.getReportWindowX());
				Double preferencesY = Double.parseDouble(preferences.getReportWindowY());
				Double preferencesWidth = Double.parseDouble(preferences.getReportWindowWidth());
				Double preferencesHeight = Double.parseDouble(preferences.getReportWindowHeight());

				// preferences.xmlからウィンドウ幅を取得
				reportStage.setWidth(preferencesWidth);
				reportStage.setHeight(preferencesHeight);

				// 記録されている位置が現在のディスプレイの範囲以内か確認

				// サブディスプレイが左側の場合
				if (virtualBounds.x < 0) {
					if (preferencesX >= virtualBounds.x - preferencesHeight) {
						reportStage.setX(preferencesX);
						reportStage.setY(preferencesY);
					} else {
						reportStage.setX(0);
						reportStage.setY(0);
					}
				}
				// サブディスプレイが右側、もしくは無い場合
				if (virtualBounds.x >= 0) {
					if (0 <= preferencesX && preferencesX <= displayWidth - 20) {
						reportStage.setX(preferencesX);
						reportStage.setY(preferencesY);
					} else {
						reportStage.setX(0);
						reportStage.setY(0);
					}
				}
			}

			// エクスポート時にキャンセルボタンを押下するか、レポート画面生成時にエラーが発生したた場合は処理を中断
			if (null != object && object instanceof Boolean && false == (Boolean) object) {
				setObject(null);
				return;
			}
			controller.setStage(reportStage);

			reportStage.setOnCloseRequest((WindowEvent t) -> {
				try {
					logger.debug("close&cancel");
					((W04InputControlController) (this.getController())).close(t);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			});

			// ウィンドウ最小幅を設定
			reportStage.setMinWidth(Constant.Graphic.REPORT_STAGE_MIN_WIDTH);
			reportStage.setMinHeight(Constant.Graphic.REPORT_STAGE_MIN_HEIGHT);

			reportStageList.add(reportStage);
			reportStage.showAndWait();

			// HashMapを取得し、テーブルにバインド
			HashMap<String, Object> parameter;
			if (object instanceof HashMap) {
				parameter = (HashMap<String, Object>) object;
				setObject(null);

				// テーブルビューにパラメータをセット
				setParameterTable(parameter);

				// 編集時に新しいパラメータを設定された場合は既存のパラメータを消去する。
				if (!StringUtils.isEmpty(paramFileName)) {
					paramFileNameMap.remove(paramFileName);
					deleteParamList.add(paramFileName);
				}
				// パラメータのファイル名を乱数で生成
				String random = RandomStringUtils.randomAlphabetic(10);
				String time = new SimpleDateFormat(Constant.ExcelJob.PARAMDATEFORMAT).format(new Date());
				paramFileName = Constant.ExcelJob.PARAM + time + random;
				// ファイル名とパラメータ(HashMap)のHashMapに格納(ジョブの保存時にファイルとして書き出す。)
				paramFileNameMap.put(paramFileName, parameter);
			}

		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

	/**
	 * パラメータ(HashMap)を引数に受け取り、テーブルビュー(JFXTreeTableView)に値をバインドする<br>
	 * 
	 * @param parameter
	 */
	@SuppressWarnings("unchecked")
	public void setParameterTable(HashMap<String, Object> parameter) {
		// 初期化
		parameterTable.setRoot(null);
		paramTableRecordList.clear();

		ParameterTableRecord parameterTableRecord;
		for (Entry<String, Object> e : parameter.entrySet()) {

			parameterTableRecord = new ParameterTableRecord();
			parameterTableRecord.setParamName(e.getKey());
			parameterTableRecord.setParamValue(e.getValue());

			// テーブルにアイテム(ジョブ)を追加する。
			paramTableRecordList.add(parameterTableRecord);
		}

		TreeItem<ParameterTableRecord> root = new RecursiveTreeItem<>(paramTableRecordList,
				RecursiveTreeObject::getChildren);

		name.setCellValueFactory((TreeTableColumn.CellDataFeatures<ParameterTableRecord, String> param) -> {
			if (name.validateValue(param)) {
				return param.getValue().getValue().getParamName();
			} else {
				return name.getComputedValue(param);
			}
		});
		value.setCellValueFactory((TreeTableColumn.CellDataFeatures<ParameterTableRecord, String> param) -> {
			if (value.validateValue(param)) {
				return param.getValue().getValue().getParamValue();
			} else {
				return value.getComputedValue(param);
			}
		});

		// グルーピングを不可にする
		name.setContextMenu(null);
		value.setContextMenu(null);

		parameterTable.getColumns().setAll(name, value);
		parameterTable.setRoot(root);
		parameterTable.setShowRoot(false);
		parameterTable.setEditable(true);
	}

	/**
	 * [完了]ボタンを押下した際に各ノードに設定された値を全てクリアする。<br>
	 * 
	 */
	public void clearAll() {

		/** 1. 帳票の選択 */
		reportSelectTextField.clear();
		reportNameTextField.clear();
		prevReportUri = null;

		/** 2. 貼り付け位置の指定 */
		spreadsheetView = null;
		slider = null;
		inputCheckBox.setSelected(true);
		selectCheckBox.setSelected(false);
		columnTextField.clear();
		rowTextField.clear();
		inputStackPane.setVisible(true);
		previewStackPane.setVisible(false);
		sheetSelectComboBox.getSelectionModel().select(0);
		afterSecondTime = false;
		if (previewStackPane.getChildren().size() >= 4) {
			previewStackPane.getChildren().remove(3);
			previewStackPane.getChildren().remove(2);
		}

		/** 3. 貼り付け列の選択 */
		headerCheckBox.setSelected(false);
		listSelectionView.getTargetItems().clear();
		listSelectionView.getSourceItems().clear();

		/** 4. パラメータの保存 */
		parameterCheckBox.setSelected(false);
		if (null != parameterTable.getRoot())
			parameterTable.getRoot().getChildren().clear();
		paramTableRecordList.clear();
		paramFileName = null;

		/** Excel貼り付け定義の作成画面のテーブルビューの選択をクリアする。 */
		excelJobTable.getSelectionModel().clearSelection();
		// 編集と削除ボタンを無効化
		editButton.setDisable(true);
		deleteButton.setDisable(true);

		// 編集時のフラグを無効にする。
		editFlag = false;
	}

	/*******************************************
	 * 
	 * 保存先の設定画面
	 * 
	 *******************************************/

	/**
	 * [戻る]ボタン<br>
	 * 保存先の設定からホーム画面へ
	 * 
	 * @param event
	 */
	public void columnFromTitle(ActionEvent event) throws IOException {
		// 画面を保存先の指定に切り替える
		saveStackPane.setVisible(false);
		homePane.setVisible(true);
	}

	/**
	 * [参照]ボタン<br>
	 * Excelファイル出力先を指定するExplorerを開く
	 * 
	 * @param event
	 */
	public void saveFileExplorer(ActionEvent event) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(filePath.substring(0, filePath.lastIndexOf("\\") + 1)));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XLSX Files", "*.xlsx"),
				new ExtensionFilter("XLSM Files", "*.xlsm"));
		fileChooser.setTitle(myResource.getString("W07.specify_jobname.excel_file_chooosser.title"));

		File saveFile = fileChooser.showSaveDialog(null);

		// 参照元と出力先のExcelファイルの拡張子が異なった場合にエラーになる。
		if ((fileSelectTextField.getText().endsWith(".xlsm") && saveFile.getPath().endsWith(".xlsx"))
				|| (fileSelectTextField.getText().endsWith(".xlsx") && saveFile.getPath().endsWith(".xlsm"))) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_20));
			return;
		}

		if (null != saveFile)
			excelOutputTextField.setText(saveFile.getPath());
	}

	/**
	 * [参照]ボタン<br>
	 * ジョブの保存先を指定するExplorerを開く
	 * 
	 * @param event
	 */
	public void saveJobExplorer(ActionEvent event) throws IOException {

		File saveFile = null;
		while (saveFile == null || !saveFile.getPath().startsWith(JOBPATH)) {

			// フォルダが無い場合は作成する
			File directory = new File(JOBPATH);
			directory.mkdirs();

			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File(JOBPATH));
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Job Files", "*.xml"));
			fileChooser.setTitle(myResource.getString("W07.specify_jobname.job_file_chooosser.title"));
			saveFile = fileChooser.showSaveDialog(null);

			// キャンセル時
			if (null == saveFile)
				break;

			if (!saveFile.getPath().startsWith(JOBPATH)) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(LoggerMessageKey.Error.ERROR_W07_11));
			}
		}
		if (null != saveFile)
			jobOutputTextField.setText(saveFile.getPath());
	}

	/**
	 * [保存]ボタン
	 * 
	 * @param event
	 */
	public void save(ActionEvent event) throws IOException {

		if (StringUtils.isEmpty(excelOutputTextField.getText())) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_12));
			return;
		}

		if (StringUtils.isEmpty(jobOutputTextField.getText())) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_13));
			return;
		}

		// 警告表示用フラグ
		boolean excelOverwriteFlag = false;
		boolean jobOverwriteFlag = false;

		// 保存先が元のExcelと等しく上書きにチェックが入っている場合(=元のExcelが上書きされてしまう場合)に警告を表示
		if (excelOutputTextField.getText().equals(fileSelectTextField.getText()) && rewriteCheckBox.isSelected()) {
			excelOverwriteFlag = true;
		}
		File file = new File(jobOutputTextField.getText());

		// ファイルが存在した場合は上書きバリデーションのフラグを立てる
		if (file.exists()) {
			jobOverwriteFlag = true;
		}

		if (jobOverwriteFlag || excelOverwriteFlag) {
			Alert alert = new Alert(AlertType.WARNING, "", ButtonType.YES, ButtonType.NO);
			alert.setTitle(myResource.getString("common.warn.dialog.title"));
			alert.getDialogPane().setHeaderText(myResource.getString("W07.specify_jobname.warn_dialog.header"));

			if (jobOverwriteFlag && excelOverwriteFlag) {
				alert.setContentText(myResource.getString("W07.specify_jobname.warn_dialog.msg") + "\n"
						+ myResource.getString("W07.specify_jobname.warn_dialog.msg2"));
			} else if (!jobOverwriteFlag && excelOverwriteFlag) {
				alert.setContentText(myResource.getString("W07.specify_jobname.warn_dialog.msg"));
			} else if (jobOverwriteFlag && !excelOverwriteFlag) {
				alert.setContentText(myResource.getString("W07.specify_jobname.warn_dialog.msg2"));
			}

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() != ButtonType.YES) {
				return;
			}
		}

		try {

			String newJobPath = jobOutputTextField.getText().replace(JOBPATH, "");
			// 編集時に上書きするのであれば既存のパラメータを削除する。別名で保存の場合は削除しない。
			if (null != excelJob && excelJob.getJobPath().equals(newJobPath)) {
				// 削除予定のパラメータを消去する。
				for (String paramName : deleteParamList) {
					File deleteParam = new File(PARAMPATH + paramName + ".dat");

					if (deleteParam.exists())
						deleteParam.delete();

					for (int i = 0; i < excelDefinitionList.size(); i++) {
						if (null != excelDefinitionList.get(i).getParameterFileName()
								&& excelDefinitionList.get(i).getParameterFileName().equals(paramName)) {
							ExcelDefinition newExcelDefinition = excelDefinitionList.get(i);
							newExcelDefinition.setParameterFileName(null);
							excelDefinitionList.set(i, newExcelDefinition);
						}
					}
				}
			}

			// パラメータをファイルとして保存する
			File directory = new File(PARAMPATH);
			if (!directory.exists())
				directory.mkdirs();
			for (Entry<String, HashMap<String, Object>> e : paramFileNameMap.entrySet()) {

				// 書き込み用のbyte配列を作成
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(e.getValue());
				byte[] bytes = baos.toByteArray();

				// ファイルがあればそれを読み込み、初期化をスキップ
				File filebytedata = new File(PARAMPATH + e.getKey() + ".dat");

				// ファイルを作成してデータを書き込み
				filebytedata.createNewFile();
				FileOutputStream fos = new FileOutputStream(filebytedata);
				fos.write(bytes);

				fos.close();
				oos.close();
				baos.close();
			}

			JAXBContext context = JAXBContext.newInstance(ExcelJob.class);

			excelJob = new ExcelJob();
			excelJob.setExcelDefinitionList(excelDefinitionList);
			excelJob.setJobPath(newJobPath); // ジョブタイトル
			excelJob.setExcelPath(filePath); // Excelパス
			excelJob.setRewriteFlag(rewriteCheckBox.isSelected());// 上書き
			excelJob.setSaveExcelPath(excelOutputTextField.getText());// 保存先

			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(excelJob, file);

			logger.info("Save completed to Excel job.");

			showDialog(AlertType.INFORMATION, myResource.getString("W07.specify_jobname.dialog.title"),
					myResource.getString("W07.specify_jobname.dialog.msg"));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		excelJobStage.hide();
		excelJobStage.close();
	}

	/**
	 * ウィンドウタイトルバーを利用して閉じた場合の処理
	 * 
	 * @param t
	 * @throws InterruptedException
	 */
	public void close(WindowEvent t) throws InterruptedException {

		Stage stage = (Stage) t.getSource();

		stage.hide();
		stage.close();
		logger.info("Close the excel paste wizard stage.");
	}
}