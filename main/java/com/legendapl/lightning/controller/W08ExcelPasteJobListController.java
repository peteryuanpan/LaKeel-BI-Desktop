package com.legendapl.lightning.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.JFXTreeView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey;
import com.legendapl.lightning.model.ExcelDefinition;
import com.legendapl.lightning.model.ExcelDefinitionTableRecord;
import com.legendapl.lightning.model.ExcelJob;
import com.legendapl.lightning.model.FolderResource;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Excelのジョブ一覧を表示するコントローラクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class W08ExcelPasteJobListController extends C01ToolbarController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private AnchorPane tableViewAnchorPane;

	/** 実行ボタン */
	@FXML
	private JFXButton executeButton;

	/** 新規作成 */
	@FXML
	private JFXButton createButton;
	/** 編集 */
	@FXML
	private JFXButton editButton;
	/** 削除ボタン */
	@FXML
	private JFXButton deleteButton;

	/** Excel貼り付けジョブのテーブルビュー */
	@FXML
	private JFXTreeTableView<ExcelDefinitionTableRecord> excelJobTable;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> reportLabel;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> targetColumns;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> sheet;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> cell;

	/** 画面に表示されるフォルダ表示用Treeview */
	@FXML
	private JFXTreeView<FolderResource> folderTree;

	/** ジョブの詳細に表示される貼り付け元のExcelのパス */
	@FXML
	private JFXTextField excelPath;

	/** フォルダアイコン */
	private MaterialDesignIcon folderIcon = MaterialDesignIcon.FOLDER_OUTLINE;
	private FontAwesomeIcon folderExpandedIcon = FontAwesomeIcon.FOLDER_OPEN_ALT;
	private MaterialDesignIcon jobIcon = MaterialDesignIcon.FILE;

	/** リストビューの列幅の比率 */
	public static final double excelJobTableRatio = 0.65;
	public static final double reportNameRatio = 0.3;
	public static final double targetColumnsRatio = 0.4;
	public static final double sheetRatio = 0.3;
	public static final double cellWidth = 100.0;

	/**
	 * 初期化
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("URL=" + arg0 + ", ResourceBulder=" + arg1);

		/**
		 * 列幅の設定
		 * 
		 * 初期化メソッドではcurrentStageが取得できないため、別スレッドで設定を行う
		 */
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Platform.runLater(() -> {
					double activeWidth = currentStage.getWidth() * excelJobTableRatio - cellWidth;
					reportLabel.setPrefWidth(activeWidth * reportNameRatio);
					targetColumns.setPrefWidth(activeWidth * targetColumnsRatio);
					sheet.setPrefWidth(activeWidth * sheetRatio);
					cell.setPrefWidth(cellWidth);
				});
				return null;
			}
		}).start();

		excelJobTable = (JFXTreeTableView<ExcelDefinitionTableRecord>) tableViewAnchorPane.getChildren().get(0);

		// 行の選択を不可にする。
		TreeTableViewSelectionModel<ExcelDefinitionTableRecord> selMode = excelJobTable.getSelectionModel();
		excelJobTable.setOnMousePressed(event -> {
			selMode.clearSelection();
		});

		// ジョブを選択していない時のメッセージを設定
		excelJobTable.setPlaceholder(new Label(myResource.getString("W08.table.empty_msg")));

		// ジョブが選択された際にボタンを有効化するイベントハンドラを追加
		executeButton.setDisable(true);
		deleteButton.setDisable(true);
		editButton.setDisable(true);
		folderTree.getSelectionModel().selectedItemProperty().addListener((record, oldVal, newVal) -> {
			// ジョブの場合
			if (null != newVal && newVal.getValue().getLabel().endsWith(".xml")) {
				executeButton.setDisable(false);
				editButton.setDisable(false);
				deleteButton.setDisable(false);
			}
			// ディレクトリの場合
			else {
				executeButton.setDisable(true);
				editButton.setDisable(true);
				deleteButton.setDisable(false);
			}

			// 選択したアイテムがルートディレクトリであったら削除ボタンを無効化する。
			if (0 == folderTree.getSelectionModel().getSelectedIndex())
				deleteButton.setDisable(true);
		});

		// TreeViewに設定するため、rootのTreeItemを作成
		/**
		 * 定義済みの全てのサーバのルートディレクトリが表示される不具合を確認<br>
		 * 再現は出来ていないが、この場合他の定義済みサーバに含まれるジョブを編集できてしまう可能性があるので<br>
		 * serverNameが空文字or nullの場合はエラーを出力する。
		 */
		if (StringUtils.isEmpty(C00ControllerBase.serverInfo.getName())) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_99_HEADER));
			return;
		}
		TreeItem<FolderResource> rootDir = new TreeItem<FolderResource>(
				new FolderResource(C00ControllerBase.serverInfo.getName(), Constant.Application.WORK_FILE_PATH
						+ "/excelJob/excelDefinition/" + C00ControllerBase.serverInfo.getName()),
				new MaterialDesignIconView(folderIcon));
		rootDir.setExpanded(true);

		// フォルダ表示用TreeViewに取得したツリーを設定
		folderTree.setRoot(rootDir);

		// ツリーの選択時のイベントハンドラを追加
		folderTree.setOnMouseClicked((e) -> handleMouseClicked(e));

		// ディレクトリパスを編集不可能に設定
		excelPath.setEditable(false);

		// Excelジョブのルートディレクトリが存在しない場合に作成する。
		File dir = new File(Constant.Application.WORK_FILE_PATH + "/excelJob/excelDefinition/"
				+ C00ControllerBase.serverInfo.getName());
		if (!dir.exists())
			dir.mkdirs();

		// ルートディレクトリのツリーアイテムを追加する。
		addTreeItems(rootDir);
	}

	/**
	 * フォルダツリークリック時の処理
	 * 
	 * @param event
	 */
	private void handleMouseClicked(MouseEvent event) {
		// preferencesを読み込み
		loadPreferences();
		// 列幅の更新
		double activeWidth = excelJobTable.getWidth() - cellWidth;
		reportLabel.setPrefWidth(activeWidth * reportNameRatio);
		targetColumns.setPrefWidth(activeWidth * targetColumnsRatio);
		sheet.setPrefWidth(activeWidth * sheetRatio);
		cell.setPrefWidth(cellWidth);

		Node node = event.getPickResult().getIntersectedNode();

		// 選択しているアイテムがない場合は処理を中断
		if (null == node || null == folderTree.getSelectionModel()
				|| null == folderTree.getSelectionModel().getSelectedItem()) {
			return;
		}

		TreeItem<FolderResource> item = folderTree.getSelectionModel().getSelectedItem();
		// テーブルビューに選択されたジョブの詳細を表示
		addTableViewItems(item.getValue().getPath());

		// クリックされた部分がフォルダアイコン、またはセルがダブルクリックされた場合、フォルダを展開
		if (!(node instanceof MaterialDesignIconView)
				&& !(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)) {
			return;
		}

		// 選択したフォルダをすでに開いていて、アイコン以外をダブルクリック場合はアイテムを追加せず、展開されたフォルダを折りたたむ
		if (0 != item.getChildren().size() && !(node instanceof MaterialDesignIconView)) {
			return;
		}

		// rootフォルダに対してはフォルダ展開を禁止
		if (item.getValue().getLabel().equals(Constant.ServerInfo.serverName))
			return;

		item.setExpanded(true);

		// フォルダの中身を表示
		addTreeItems(item);
	}

	/**
	 * フォルダーにリソースを追加するメソッド
	 * 
	 * @param rootDir
	 */
	public void addTreeItems(TreeItem<FolderResource> rootDir) {
		// アイテムをクリアする
		rootDir.getChildren().clear();

		// クリックされたアイテムがジョブ(.xml)の場合は処理を終了する
		if (rootDir.getValue().getPath().endsWith(".xml"))
			return;

		// クリックされたフォルダ内のリソース一覧を取得する
		File dir = new File(rootDir.getValue().getPath());

		// フォルダが存在しない場合は終了する
		if (null == dir || !dir.exists()) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_99_HEADER));
			return;
		}

		List<File> fileList = Arrays.asList(dir.listFiles());

		// 既に中身が入っていたり、他のスレッドにより追加結果が重複する場合の対策
		if (!rootDir.getChildren().isEmpty()) {
			return;
		}

		// リソースを1件ずつアイテムとして追加していく
		for (File resource : fileList) {
			/** 読み取り可能なファイルのみ表示する */
			if (!resource.canWrite()) {
				continue;
			}
			MaterialDesignIconView graphic = null;
			if (resource.toString().endsWith(".xml")) {
				graphic = new MaterialDesignIconView(jobIcon);
			} else {
				graphic = new MaterialDesignIconView(folderIcon);
				graphic.setCursor(Cursor.HAND);
			}
			rootDir.getChildren()
					.add(new TreeItem<FolderResource>(
							new FolderResource(resource.toString().substring(resource.toString().lastIndexOf("\\") + 1),
									resource.toString()),
							graphic));

			rootDir.setExpanded(true);
			resource = null;
		}

		// フォルダの展開後にアイコンを変更する
		rootDir.setGraphic(new FontAwesomeIconView(folderExpandedIcon));

		// ▶トグルにより展開／折り畳みの際のイベントを追加
		rootDir.expandedProperty().addListener((e) -> {
			if (rootDir.isExpanded()) {
				rootDir.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				logger.debug(rootDir.getValue() + "is expanded");
			} else {
				MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
				graphic.setCursor(Cursor.HAND);
				rootDir.setGraphic(graphic);
				logger.debug(rootDir.getValue() + "is collapsed");
			}
		});

		dir = null;
		fileList = null;
	}

	/**
	 * 指定されたジョブの詳細を取得し、テーブルビューのアイテムとして追加する。
	 * 
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	private void addTableViewItems(String path) {

		// クリックされたリソースがジョブ(.xml)ではない場合にテーブルをクリアする。
		if (!path.endsWith(".xml")) {
			excelJobTable.getColumns().setAll(reportLabel, targetColumns, sheet, cell);
			excelJobTable.setRoot(null);
			excelJobTable.setShowRoot(false);
			excelJobTable.setEditable(true);
			excelPath.setText("");
			return;
		}

		// ジョブ(.xml)を文字列として読み込む
		String xmlStr = null;
		Path paths = Paths.get(path);
		Stream<String> files;
		try {
			files = Files.lines(paths, Charset.forName("UTF-8"));
			xmlStr = files.collect(Collectors.joining(System.getProperty("line.separator")));
			files.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return;
		}
		StringReader srd = new StringReader(xmlStr);

		// ジョブファイル(.xml)をXMLのモデルクラス(excelJob)へデシリアライズ
		JAXBContext context;
		Unmarshaller unmarshaller;
		ExcelJob excelJob = null;
		try {
			context = JAXBContext.newInstance(ExcelJob.class);
			unmarshaller = context.createUnmarshaller();
			excelJob = (ExcelJob) unmarshaller.unmarshal(srd);
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			return;
		}

		context = null;
		unmarshaller = null;
		srd.close();

		// TableViewのレコードを定義
		ObservableList<ExcelDefinitionTableRecord> tableRecord = FXCollections.observableArrayList();

		for (ExcelDefinition excelDefinition : excelJob.getExcelDefinitionList()) {
			// ExcelDefinitionからテーブルビューのモデルクラス(ExcelDefinitionTableRecord)にデータをバインド
			ExcelDefinitionTableRecord excelDefinitionTableRecord = new ExcelDefinitionTableRecord();
			excelDefinitionTableRecord.setReportLabel(excelDefinition.getReportName());
			excelDefinitionTableRecord.setTargetColumns(excelDefinition.getTargetColumns());
			excelDefinitionTableRecord.setSheet(excelDefinition.getSheet());
			excelDefinitionTableRecord.setCell(excelDefinition.getCell());

			// テーブルにアイテムを追加
			tableRecord.add(excelDefinitionTableRecord);
		}

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
		targetColumns
				.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
					if (targetColumns.validateValue(param)) {
						return param.getValue().getValue().getTargetColumns();
					} else {
						return targetColumns.getComputedValue(param);
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
		targetColumns.setContextMenu(null);
		sheet.setContextMenu(null);
		cell.setContextMenu(null);

		excelJobTable.getColumns().setAll(reportLabel, targetColumns, sheet, cell);
		excelJobTable.setRoot(root);
		excelJobTable.setShowRoot(false);
		excelJobTable.setEditable(true);

		// Excelパスに貼り付け元のExcelのパスをバインドする
		excelPath.setText(excelJob.getExcelPath());
	}

	/**
	 * [実行]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	public void execute(ActionEvent event) throws IOException {

		// 確認ダイアログの利用
		Alert alert = new Alert(AlertType.INFORMATION, "", ButtonType.YES, ButtonType.CANCEL);
		alert.setTitle(myResource.getString("common.confirmation.dialog.title"));
		alert.getDialogPane().setHeaderText(myResource.getString("W08.execute_dialog.header"));
		alert.getDialogPane().setContentText(myResource.getString("W08.execute_dialog.msg"));

		// アイコンを設定
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("/images/LightningIcon.png"));

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() != ButtonType.YES) {
			return;
		}

		// ジョブ(.xml)を文字列として読み込む
		String xmlStr = null;
		try {
			xmlStr = Files.lines(Paths.get(folderTree.getSelectionModel().getSelectedItem().getValue().getPath()),
					Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_99_HEADER));
			return;
		}
		StringReader srd = new StringReader(xmlStr);
		ExcelJob excelJob = JAXB.unmarshal(srd, ExcelJob.class);

		if (excelJob.getExcelDefinitionList().size() > 12) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_16));
			return;
		}

		// レポート実行画面に渡すパラメータをセット
		setObject(excelJob);

		// 入力コントロールがある場合は、レポート実行画面を表示
		Stage jobExecuterStage = showPane(event, "/view/W09ExcelPasteJobExecuterAnchorPane.fxml", excelJob.getJobPath(),
				Modality.NONE, null);

		controller.setStage(jobExecuterStage);

		reportStageList.add(jobExecuterStage);
		jobExecuterStage.setResizable(false); // リサイズ禁止
		jobExecuterStage.show();

	}

	/**
	 * [編集]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	public void edit(ActionEvent event) throws IOException {

		// ジョブ(.xml)を文字列として読み込む
		String xmlStr = null;
		try {
			xmlStr = Files.lines(Paths.get(folderTree.getSelectionModel().getSelectedItem().getValue().getPath()),
					Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return;
		}
		StringReader srd = new StringReader(xmlStr);
		ExcelJob excelJob = JAXB.unmarshal(srd, ExcelJob.class);

		setObject(excelJob);

		Stage excelViewStage;
		try {
			excelViewStage = showPane(event, "/view/W07ExcelPasteWizardAnchorPane.fxml",
					myResource.getString("W07.window.title"), Modality.APPLICATION_MODAL, null);

			excelJobStage = excelViewStage;
			excelViewStage.show();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * [削除]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	public void delete(ActionEvent event) {

		// Alertダイアログの利用
		Alert alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
		alert.setTitle(myResource.getString("common.warn.dialog.title"));
		alert.getDialogPane().setHeaderText(myResource.getString("W08.warn_dialog.header"));
		alert.getDialogPane().setContentText(myResource.getString("W08.warn_dialog.msg"));

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.YES) {

			File deleteResource = new File(System.getProperty("user.dir") + "\\"
					+ folderTree.getSelectionModel().getSelectedItem().getValue().getPath());

			try {
				// ファイルの場合
				if (deleteResource.isFile()) {
					deleteParamFile(deleteResource);
					deleteResource.delete();
					// テーブルビューからアイテムを消去
					excelJobTable.getRoot().getChildren().clear();
				}
				// ディレクトリの場合
				else if (deleteResource.isDirectory()) {
					deleteChildHierarchy(deleteResource);
				}

				// ツリービューからアイテムを消去
				folderTree.getSelectionModel().getSelectedItem().getParent().getChildren()
						.remove(folderTree.getSelectionModel().getSelectedItem());
				// 選択をクリア
				folderTree.getSelectionModel().clearSelection();
				// Excelパスをクリア
				excelPath.clear();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			deleteResource = null;

		}
	}

	/**
	 * フォルダごと消去する際に再帰的に実行する
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void deleteChildHierarchy(File file) throws IOException {
		// ディレクトリの場合
		if (file.isDirectory()) {
			// 対象ディレクトリ内のファイルおよびディレクトリの一覧を取得
			File[] files = file.listFiles();

			// ファイルおよびディレクトリをすべて削除
			if (null != files && 0 != files.length) {
				for (int i = 0; i < files.length; i++) {
					// 自身をコールし、再帰的に削除する
					deleteChildHierarchy(files[i]);
				}
			}
			files = null;
		} else
			deleteParamFile(file);

		// 自ディレクトリを削除する
		file.delete();
		file = null;
	}

	/**
	 * ジョブの削除時に関連しているパラメータファイルも同時に消去する
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void deleteParamFile(File deleteResource) throws IOException {

		// ジョブ(.xml)を文字列として読み込む
		String xmlStr = null;
		Path paths = Paths.get(deleteResource.getPath());
		Stream<String> files = Files.lines(paths, Charset.forName("UTF-8"));
		xmlStr = files.collect(Collectors.joining(System.getProperty("line.separator")));
		StringReader srd = new StringReader(xmlStr);

		// Javaオブジェクトへデシリアライズ
		ExcelJob excelJob = JAXB.unmarshal(srd, ExcelJob.class);

		paths = null;
		files.close();
		srd.close();

		// パラメータが存在していた場合は消去する
		for (ExcelDefinition excelDefinition : excelJob.getExcelDefinitionList()) {
			if (!StringUtils.isEmpty(excelDefinition.getParameterFileName())) {
				File deleteParam = new File(
						"work\\excelJob\\params\\" + excelDefinition.getParameterFileName() + ".dat");

				if (deleteParam.exists())
					deleteParam.delete();
			}
		}
	}

	/**
	 * [新規作成]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	public void create(ActionEvent event) throws IOException {

		Stage excelViewStage;
		try {
			excelViewStage = showPane(event, "/view/W07ExcelPasteWizardAnchorPane.fxml",
					myResource.getString("W07.window.title"), Modality.APPLICATION_MODAL, null);

			setObject(null);
			excelJobStage = excelViewStage;
			excelViewStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}