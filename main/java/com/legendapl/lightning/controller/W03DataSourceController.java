package com.legendapl.lightning.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AccessDeniedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Info;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Warn;
import com.legendapl.lightning.model.DataSource.Status;
import com.legendapl.lightning.model.ObservableDataSource;
import com.legendapl.lightning.service.DataSourceService;
import com.legendapl.lightning.service.DataSourceServiceImpl;
import com.legendapl.lightning.service.ExecuteAPIService;
import com.legendapl.lightning.tools.controller.P81UserAnchorPane;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * データソース設定画面
 * 
 * @author taka
 *
 */
public class W03DataSourceController extends C00ControllerBase {

	private static String xmlPassword = Constant.Application.XML_CRYPT_PASSWORD;

	public static void setXmlPassword(String xmlPassword) {
		W03DataSourceController.xmlPassword = xmlPassword;
	}

	@FXML
	private StackPane dataSourceStackPane;

	@FXML
	private FlowPane waitPane;

	@FXML
	private TableView<ObservableDataSource> dataSourceList;

	@FXML
	private CheckBox selectAll;

	@FXML
	void doSelectAll(ActionEvent event) {
		logger.debug("isSelected: " + selectAll.isSelected());
		dataSourceList.getItems().forEach((t) -> {
			t.setSelected(selectAll.isSelected());
		});
	}

	@FXML
	private TableColumn<ObservableDataSource, String> colSchema;

	@FXML
	private TableColumn<ObservableDataSource, Boolean> colSelect;

	@FXML
	private TableColumn<ObservableDataSource, String> colName;

	@FXML
	private TableColumn<ObservableDataSource, String> colType;

	@FXML
	private TableColumn<ObservableDataSource, String> colServerAddress;

	@FXML
	private TableColumn<ObservableDataSource, String> colPassword;

	@FXML
	private TableColumn<ObservableDataSource, String> colPort;

	@FXML
	private TableColumn<ObservableDataSource, String> colDataSourcePath;

	@FXML
	private TableColumn<ObservableDataSource, String> colUsername;

	@FXML
	private TableColumn<ObservableDataSource, Status> colStatus;

	@FXML
	private HBox buttonGroupLeft;

	@FXML
	private HBox buttonGroupRight;

	@FXML
	private Button btnImport;

	@FXML
	private Button btnExport;

	@FXML
	private Button btnGet;

	@FXML
	private Button btnTest;

	@FXML
	private Button btnCancel;

	@FXML
	private Button btnSave;

	@FXML
	private StackPane nonPermissionPane;

	private DataSourceService dao;

	private Thread thread;

	// 列幅の比率
	public static final double colSelectRatio = 0.05;
	public static final double colNameRatio = 0.1;
	public static final double colDataSourcePathRatio = 0.2;
	public static final double colTypeRatio = 0.05;
	public static final double colServerAddressRatio = 0.1;
	public static final double colPortRatio = 0.05;
	public static final double colSchemaRatio = 0.05;
	public static final double colUsernameRatio = 0.1;
	public static final double colPasswordRatio = 0.2;
	public static final double colStatusRatio = 0.05;

	@FXML
	void doGet(ActionEvent event) {
		logger.debug("doGet started");

		buttonGroupLeft.setDisable(true);
		buttonGroupRight.setDisable(true);

		try {
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(waitPane);
			// BIサーバ上のデータソースをすべて保存
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					try {
						boolean gotAllDataSourceFromAPI = ExecuteAPIService.getAllDatasource(
								Constant.Application.DATA_SOURCE_FILE_PATH + serverInfo.getName(), dao);

						// スレッドが中断されていた場合、これ以降の処理を中止
						if (Thread.currentThread().isInterrupted()) {
							return null;
						}
						if (!gotAllDataSourceFromAPI) {
							// パスワードが間違っていたりして接続に失敗した場合、エラーダイアログの表示
							Platform.runLater(() -> {
								showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
										messageRes.getString(Error.ERROR_W03_02));
							});
							logger.error(messageRes.getString(Error.ERROR_W03_02));
							// 接続失敗時にユーザ名とパスワードを、サーバ情報に設定された値にリセット
							Constant.ServerInfo.userName = StringUtils.isEmpty(serverInfo.getOrganizationName())
									? serverInfo.getUserName()
									: serverInfo.getUserName() + "|" + serverInfo.getOrganizationName();
							Constant.ServerInfo.password = serverInfo.getPassword();
							Platform.runLater(() -> {
								dataSourceStackPane.getChildren().clear();
								dataSourceStackPane.getChildren().add(dataSourceList);
								buttonGroupLeft.setDisable(false);
								buttonGroupRight.setDisable(false);
							});
							return null;
						}

						// データソース取得成功時は画面にアイテムを追加
						List<ObservableDataSource> dss = dao.getDataSources();
						Platform.runLater(() -> {
							dataSourceList.getItems().setAll(dss);
							setPwdShow(dataSourceList);
							logger.debug("doGet ended");
							dataSourceStackPane.getChildren().clear();
							dataSourceStackPane.getChildren().add(dataSourceList);
							buttonGroupLeft.setDisable(false);
							buttonGroupRight.setDisable(false);
						});

					} catch (Exception e) {
						// エラーダイアログの表示
						Platform.runLater(() -> {
							showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									messageRes.getString(Error.ERROR_99_HEADER));
							dataSourceStackPane.getChildren().clear();
							dataSourceStackPane.getChildren().add(dataSourceList);
							buttonGroupLeft.setDisable(false);
							buttonGroupRight.setDisable(false);
						});
						logger.error(messageRes.getString(Error.ERROR_99_HEADER), e);
					}
					return null;
				}
			};
			thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();

			logger.debug("doGet background task started");

		} catch (Exception e) {
			// エラーダイアログの表示
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(Error.ERROR_99_HEADER));
			logger.error(messageRes.getString(Error.ERROR_99_HEADER), e);
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(dataSourceList);
			buttonGroupLeft.setDisable(false);
			buttonGroupRight.setDisable(false);
		}
	}

	@FXML
	void doTest(ActionEvent event) {

		buttonGroupLeft.setDisable(true);
		buttonGroupRight.setDisable(true);

		try {
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(waitPane);

			List<String> ngDataSources = new ArrayList<String>();

			// BIサーバ上のデータソースをすべて保存
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					dataSourceList.getItems().forEach((t) -> {
						if (t.isSelected()) {
							t.setDirty(true);
							Platform.runLater(() -> {
								btnSave.setDisable(false);
							});
							try {
								t.setStatus(Status.UNKNOWN);
								Class.forName(t.getDriver());
								Connection con = DriverManager.getConnection(t.getUrl(), t.getUsermame(),
										t.getPassword());
								con.close();
								t.setStatus(Status.OK);

							} catch (ClassNotFoundException | SQLException | RuntimeException e) {
								t.setStatus(Status.NG);
								ngDataSources.add(t.getLabel());
								logger.warn(MessageFormat.format(messageRes.getString(Warn.WARN_W03_02), t.getName(),
										e.getLocalizedMessage()));
								logger.error(e.getMessage(), e);
							}
						}
					});

					Platform.runLater(() -> {
						if (ngDataSources.size() > 0) {
							// エラーダイアログの表示
							showDialog(AlertType.WARNING, btnTest.getText(),
									messageRes.getString(Warn.WARN_W03_03_HEADER),
									ngDataSources.stream().collect(Collectors.joining(", ")));
							logger.warn(messageRes.getString(Warn.WARN_W03_03_HEADER));
						}

						dataSourceStackPane.getChildren().clear();
						dataSourceStackPane.getChildren().add(dataSourceList);

						buttonGroupLeft.setDisable(false);
						buttonGroupRight.setDisable(false);
					});
					return null;
				}
			};

			thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();

		} catch (Exception e) {
			// エラーダイアログの表示
			showDialog(AlertType.ERROR, btnTest.getText(), messageRes.getString(Error.ERROR_99_HEADER));
			logger.error(messageRes.getString(Error.ERROR_99_HEADER), e);

			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(dataSourceList);
			buttonGroupLeft.setDisable(false);
			buttonGroupRight.setDisable(false);
		}
	}

	@FXML
	void doSave(ActionEvent event) {
		// preferencesを読み込み
		loadPreferences();

		try {
			// 現在のウィンドウ幅を取得し、セットする。
			preferences.setDataSourceWindowHeight(String.valueOf(C00ControllerBase.currentStage.getHeight()));
			preferences.setDataSourceWindowWidth(String.valueOf(C00ControllerBase.currentStage.getWidth()));

			// 現在のウィンドウの位置を取得し、セットする。
			preferences.setDataSourceWindowX(String.valueOf(C00ControllerBase.currentStage.getX()));
			preferences.setDataSourceWindowY(String.valueOf(C00ControllerBase.currentStage.getY()));

			// 上書き保存
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(preferences, file);

			C00ControllerBase.currentStage = null;

		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		}

		dao.saveDataSources(dataSourceList.getItems());

		serverInfoList.savePreferenceDataToFile();

		btnSave.setDisable(true);
		showDialog(AlertType.INFORMATION, btnTest.getText(), messageRes.getString(Info.INFO_W03_03));

		// ワークスペースフォルダをルートに変更
		Constant.ServerInfo.workspace = Constant.ServerInfo.workspace.substring(0,
				Constant.ServerInfo.workspace.lastIndexOf("/")) + "/";

		// サーバ選択画面を再表示する
		dataSourceList.getScene().getWindow().hide();
		new W01SelectServerController().start(primaryStage);
	}

	private boolean hasDirty = false;

	@FXML
	void doCancel(ActionEvent event) {

		hasDirty = false;
		dataSourceList.getItems().forEach((item) -> {
			hasDirty = hasDirty || item.isDirty();
		});

		// 確認ダイアログの表示
		if (hasDirty) {
			Optional<ButtonType> bType = showDialog(AlertType.CONFIRMATION, btnCancel.getText(),
					messageRes.getString(Warn.WARN_W03_01_HEADER));
			if (bType.get() == ButtonType.CANCEL) {
				return;
			}
		}

		// ワークスペースフォルダをルートに変更
		Constant.ServerInfo.workspace = Constant.ServerInfo.workspace.substring(0,
				Constant.ServerInfo.workspace.lastIndexOf("/")) + "/";

		// ユーザ名とパスワードを、サーバ情報に設定された値にリセット
		Constant.ServerInfo.userName = StringUtils.isEmpty(serverInfo.getOrganizationName()) ? serverInfo.getUserName()
				: serverInfo.getUserName() + "|" + serverInfo.getOrganizationName();
		Constant.ServerInfo.password = serverInfo.getPassword();

		// スレッドを中断
		if (null != thread && thread.isAlive()) {
			thread.interrupt();
		}

		dataSourceStackPane.getScene().getWindow().hide();
	}

	/**
	 * テーブルセルで使えるパスワードセル
	 * 
	 * @author taka
	 *
	 */
	private class PasswordCell extends TableCell<ObservableDataSource, String> {

		final PasswordField passwordField = new PasswordField();

		PasswordCell(TableColumn<ObservableDataSource, String> p) {

			passwordField.setOnKeyTyped((e) -> {
				logger.debug("passwordField: keyTyped: typed:[" + e.getCharacter() + "], anchor:"
						+ passwordField.getAnchor() + ", caret position=" + passwordField.getCaretPosition()
						+ " -> text=[" + passwordField.getText() + "]");
				if (e.getCharacter().charAt(0) < ' ' || e.getCharacter().charAt(0) == 0x7f) { // 制御キーなら編集済なので編集後処理をしてから戻る
					logger.debug("passwordField: keyTyped: typed:[" + e.getCharacter().charAt(0) + "], anchor:"
							+ passwordField.getAnchor() + ", caret position=" + passwordField.getCaretPosition()
							+ " -> text=[" + passwordField.getText() + "]");
					postEdit();
					return;
				}
				// 印刷可能の文字なら編集する
				int a = passwordField.getAnchor();
				int c = passwordField.getCaretPosition();
				if (a < c) {
					passwordField.deleteText(a, c);
					passwordField.insertText(a, e.getCharacter());
				} else if (c < a) {
					passwordField.deleteText(c, a);
					passwordField.insertText(c, e.getCharacter());
					a = c;
				} else {
					passwordField.insertText(a, e.getCharacter());
				}
				e.consume(); // デフォルト編集をしないように編集済とマークする
				logger.debug("passwordField: keyTyped: typed:[" + e.getCharacter() + "], anchor:"
						+ passwordField.getAnchor() + ", caret position=" + passwordField.getCaretPosition()
						+ " -> text=[" + passwordField.getText() + "]");
				postEdit();
			});
		}

		/**
		 * 編集後処理：
		 * 
		 * パスワードが変わったなら「状態」をUNKNOWNにし、ダーティフラグをセットする。
		 */
		private void postEdit() {
			if (passwordField.getText() != null && !passwordField.getText().equals(
					PasswordCell.this.getTableView().getItems().get(PasswordCell.this.getIndex()).getPassword())) {
				ObservableDataSource item = PasswordCell.this.getTableView().getItems()
						.get(PasswordCell.this.getIndex());
				item.setPassword(passwordField.getText());
				item.setPwdShow(passwordField.getText());
				item.setStatus(Status.UNKNOWN);
				item.setDirty(true);
			}
		}

		@Override
		protected void updateItem(String t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				passwordField.setText(t);
				setGraphic(passwordField);
			}
		}

	}

	class StatusCell extends TableCell<ObservableDataSource, Status> {

		@Override
		protected void updateItem(Status item, boolean empty) {

			super.updateItem(item, empty);
			if (item != null) {
				this.getTableRow().getStyleClass().clear();
				switch (item) {
				case NG:
					this.getTableRow().getStyleClass().add("ng-data-source");
					break;
				default:
					break;
				}
				setText(item.getValue());
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		/**
		 * 列幅の設定
		 * 
		 * 初期化メソッドではcurrentStageが取得できないため、別スレッドで設定を行う
		 */
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Platform.runLater(() -> {
					if (currentStage != null) {
						double activeWidth = currentStage.getWidth();
						colSelect.setPrefWidth(activeWidth * colSelectRatio);
						colName.setPrefWidth(activeWidth * colNameRatio);
						colDataSourcePath.setPrefWidth(activeWidth * colDataSourcePathRatio);
						colType.setPrefWidth(activeWidth * colTypeRatio);
						colServerAddress.setPrefWidth(activeWidth * colServerAddressRatio);
						colPort.setPrefWidth(activeWidth * colPortRatio);
						colSchema.setPrefWidth(activeWidth * colSchemaRatio);
						colUsername.setPrefWidth(activeWidth * colUsernameRatio);
						colPassword.setPrefWidth(activeWidth * colPasswordRatio);
						colStatus.setPrefWidth(activeWidth * colStatusRatio);
					}
				});
				return null;
			}
		}).start();

		// API実行時の接続先を設定
		ExecuteAPIService.setClientConfiguration(serverInfo);
		Constant.ServerInfo.workspace += serverInfo.getName();
		Constant.ServerInfo.userName = serverInfo.getOrganizationName().isEmpty() ? serverInfo.getUserName()
				: serverInfo.getUserName() + "|" + serverInfo.getOrganizationName();
		if (StringUtils.isEmpty(Constant.ServerInfo.password))
			Constant.ServerInfo.password = serverInfo.getPassword();

		// 空のときの表示を設定
		dataSourceList.setPlaceholder(new Label(myResource.getString("W03.table.message.empty")));

		// ObservableDataSource
		colSelect.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, Boolean>("selected"));
		colSelect.setCellFactory((t) -> new CheckBoxTableCell<ObservableDataSource, Boolean>() {
			@Override
			public void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null) {
					if (!item) {
						selectAll.setSelected(false);
						btnTest.setDisable(true);
						dataSourceList.getItems().forEach((x) -> {
							if (x.isSelected()) {
								btnTest.setDisable(false);
							}
						});
					}
					if (item) {
						btnTest.setDisable(false);
					}
				}
			}
		});
		colName.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("label"));
		colDataSourcePath.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("dataSourcePath"));
		colType.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("type"));
		colServerAddress.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("serverAddress"));
		colPort.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("port"));
		colSchema.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("schema"));
		colUsername.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("username"));
		colPassword.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, String>("pwdShow"));
		colPassword.setCellFactory((p) -> new PasswordCell(p));

		colStatus.setCellValueFactory(new PropertyValueFactory<ObservableDataSource, Status>("status"));
		colStatus.setCellFactory((t) -> new StatusCell());

		btnTest.setDisable(true);
		btnSave.setDisable(true);

		dataSourceStackPane.getChildren().clear();
		dataSourceStackPane.getChildren().add(waitPane);

		try {
			dao = new DataSourceServiceImpl(Constant.Application.DATA_SOURCE_FILE_PATH + serverInfo.getName(),
					messageRes, xmlPassword);
			List<ObservableDataSource> dss = dao.getDataSources();
			dataSourceList.getItems().setAll(dss);
			setPwdShow(dataSourceList);
		} catch (Exception e) {
			// エラーダイアログの表示
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(Error.ERROR_99_HEADER));
			logger.error(messageRes.getString(Error.ERROR_99_HEADER), e);
			throw new RuntimeException(e);
		}

		dataSourceStackPane.getChildren().clear();
		dataSourceStackPane.getChildren().add(dataSourceList);

		loadPreferences();

		// 管理者権限(ROLE_ADMINISTRATOR)を保有していなければデータソースの閲覧を不可にする。
		if (null == preferences.getDatasourceEditable() || !preferences.getDatasourceEditable()) {
			try {
				// 管理者権限を保有していなければ以下のAPIの実行に失敗する。
				com.legendapl.lightning.tools.service.ExecuteAPIService.getRole();
				nonPermissionPane.setVisible(false);
			} catch (AccessDeniedException | AuthenticationFailedException e) {
				// buttonGroupRight.setVisible(false);
				btnGet.setVisible(false);
				btnTest.setVisible(false);
				btnSave.setVisible(false);
			}
		} else
			nonPermissionPane.setVisible(false);

	}

	/**
	 * データソースをインポート
	 */
	private P81UserAnchorPane P81UserAnchorPane = new P81UserAnchorPane();
	private List<String> checkZipFileErrorTxtList = null;
	private List<String> newFileUriList = null;

	/**
	 * データソースをインポート
	 * 
	 * @param event
	 * @author panyuan
	 */
	@FXML
	void doImport(ActionEvent event) {

		final File zipFile;

		try {
			// インポートファイル選択
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(myResource.getString("W03.button.data_source_import"));
			fileChooser.setInitialDirectory(new File(Constant.Application.DATA_SOURCE_FILE_PATH));
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("ZIP Files", "*.zip"));
			zipFile = fileChooser.showOpenDialog(C00ControllerBase.currentStage);

			if (zipFile == null) {
				logger.info("Import cannceled.");
				return;
			}
			logger.info("Choosen file: " + zipFile.getPath());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}

		buttonGroupLeft.setDisable(true);
		buttonGroupRight.setDisable(true);

		try {
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(waitPane);

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					logger.info("Import started.");
					try {
						// zipファイルチェック
						if (!checkZipFile(zipFile.getPath())) {
							// エラーメッセージ
							logger.info("Check zip failed.");
							P81UserAnchorPane.showInfo(AlertType.ERROR,
									myResource.getString("common.error.dialog.title"),
									messageRes.getString("ERROR_W04_IMPORT_TITLE"), checkZipFileErrorTxtList);

							Platform.runLater(() -> {
								dataSourceStackPane.getChildren().clear();
								dataSourceStackPane.getChildren().add(dataSourceList);
								buttonGroupLeft.setDisable(false);
								buttonGroupRight.setDisable(false);
							});
							return null;
						}

						// zipファイル解凍 (追加または上書き)
						unzipFile(Constant.Application.DATA_SOURCE_FILE_PATH + serverInfo.getName() + "/",
								zipFile.getPath());

						// 「Unknow」にステータスをすべてセットと保存
						List<ObservableDataSource> dss = dao.getDataSources();
						dss.forEach((d) -> {
							d.setStatus(Status.UNKNOWN);
							d.setDirty(true);
						});
						dao.saveDataSources(dss);

						// データソース取得、テーブル更新
						dss = dao.getDataSources();
						dataSourceList.getItems().setAll(dss);
						setPwdShow(dataSourceList);

					} catch (Exception e) {
						// エラーメッセージ
						logger.info("Import failed.");
						logger.error(e.getMessage(), e);
						P81UserAnchorPane.showInfo(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
								messageRes.getString("ERROR_99_HEADER"), null);

						Platform.runLater(() -> {
							dataSourceStackPane.getChildren().clear();
							dataSourceStackPane.getChildren().add(dataSourceList);
							buttonGroupLeft.setDisable(false);
							buttonGroupRight.setDisable(false);
						});
						return null;
					}

					Platform.runLater(() -> {
						// 成功メッセージ
						Alert alert = new Alert(AlertType.INFORMATION, "", ButtonType.YES);
						alert.setTitle(myResource.getString("common.message.dialog.title"));
						alert.setHeaderText(messageRes.getString("INFO_W03_04"));
						alert.showAndWait();

						dataSourceStackPane.getChildren().clear();
						dataSourceStackPane.getChildren().add(dataSourceList);
						buttonGroupLeft.setDisable(false);
						buttonGroupRight.setDisable(false);

						// ワークスペースフォルダをルートに変更
						Constant.ServerInfo.workspace = Constant.ServerInfo.workspace.substring(0,
								Constant.ServerInfo.workspace.lastIndexOf("/")) + "/";

						// Windowを閉じる
						dataSourceList.getScene().getWindow().hide();
						new W01SelectServerController().start(primaryStage);
					});
					logger.info("Import finished.");
					return null;
				}
			};

			thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();

		} catch (Exception e) {
			logger.error(messageRes.getString("ERROR_99_HEADER"), e);
			P81UserAnchorPane.showInfo(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString("ERROR_99_HEADER"), null);
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(dataSourceList);
			buttonGroupLeft.setDisable(false);
			buttonGroupRight.setDisable(false);
		}
	}

	/**
	 * zipファイルをチェック<br>
	 * <br>
	 * 1. XML拡張子以外のファイルが存在しています<br>
	 * 2. XML拡張子のファイルが存在していません<br>
	 * 3. ZIPファイルが破損しているため、開くことができません<br>
	 * 4. ZIPファイルにパスワードを設定されているため、インポートできません<br>
	 * 5. 予期せぬエラーが発生しました<br>
	 * 
	 * @param zipFilePath
	 * @return true/false
	 * @throws IOException
	 * @author panyuan
	 */
	private boolean checkZipFile(String zipFilePath) throws IOException {

		boolean result = true;
		boolean findXML = false;
		checkZipFileErrorTxtList = new ArrayList<String>();

		if (defineZipFile(zipFilePath) < 0) {
			// ZIPファイルが破損しているため、開くことができません
			logger.error(messageRes.getString("ERROR_W04_IMPORT_TXT_04"));
			checkZipFileErrorTxtList.add(messageRes.getString("ERROR_W04_IMPORT_TXT_03"));
			result = false;
			return result;
		}

		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath), Charset.forName("MS932"));
			ZipEntry zipEntry = zis.getNextEntry();

			while (zipEntry != null) {

				if (!zipEntry.isDirectory()) {

					String filePath = zipEntry.getName();
					filePath = filePath.replace("/", "\\");
					logger.debug("check: " + filePath);

					if (filePath.endsWith(".xml")) {
						findXML = true;
					}

					if (!filePath.endsWith(".xml")) {
						// XML拡張子以外のファイルが存在しています
						logger.error("Check failed on [" + filePath + "]");
						logger.error(messageRes.getString("ERROR_W04_IMPORT_TXT_01"));
						checkZipFileErrorTxtList.add(messageRes.getString("ERROR_W04_IMPORT_TXT_01"));
						result = false;
						break;
					}
				}

				zipEntry = zis.getNextEntry();
			}

			if (result && !findXML) {
				// XML拡張子のファイルが存在していません
				logger.error(messageRes.getString("ERROR_W04_IMPORT_TXT_02"));
				checkZipFileErrorTxtList.add(messageRes.getString("ERROR_W04_IMPORT_TXT_02"));
				result = false;
			}

			zis.closeEntry();
			zis.close();

		} catch (Exception e) {
			if (e instanceof java.util.zip.ZipException) {
				if ("encrypted ZIP entry not supported".equals(e.getMessage())) {
					// ZIPファイルにパスワードを設定されているため、インポートできません
					logger.error(messageRes.getString("ERROR_W04_IMPORT_TXT_04"));
					checkZipFileErrorTxtList.add(messageRes.getString("ERROR_W04_IMPORT_TXT_04"));
				} else if ("invalid distance too far back".equals(e.getMessage())) {
					// ZIPファイルが破損しているため、開くことができません
					logger.error(messageRes.getString("ERROR_W04_IMPORT_TXT_04"));
					checkZipFileErrorTxtList.add(messageRes.getString("ERROR_W04_IMPORT_TXT_03"));
				} else {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.error(e.getMessage(), e);
			}
			result = false;
		}

		if (!result && checkZipFileErrorTxtList.isEmpty()) {
			// 予期せぬエラーが発生しました
			logger.error(messageRes.getString("ERROR_W04_IMPORT_TXT_05"));
			checkZipFileErrorTxtList.add(messageRes.getString("ERROR_W04_IMPORT_TXT_05"));
			result = false;
		}

		return result;
	}

	/**
	 * ファイルがzipファイルかどうかを判断する<br>
	 * -1: いいえ<br>
	 * 0: 知らない<br>
	 * 1: はい<br>
	 * 
	 * @param zipFilePath
	 * @return -1,0,1
	 * @author panyuan
	 */
	private int defineZipFile(String zipFilePath) {
		try {
			RandomAccessFile raf = new RandomAccessFile(zipFilePath, "r");
			long n = raf.readInt();
			raf.close();
			if (n != 0x504B0304) {
				logger.debug("Not a zip file.");
				return -1;
			}
			logger.debug("Is a zip file.");
			return 1;

		} catch (Exception e) {
			logger.debug("Unknow whether is a zip file.");
			return 0;
		}
	}

	/**
	 * 「outPath」に「zipFilePath」を解凍する<br>
	 * 
	 * @param outPath
	 * @param zipFilePath
	 * @throws IOException
	 * @author panyuan
	 */
	private void unzipFile(String outPath, String zipFilePath) throws IOException {

		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath), Charset.forName("MS932"));
		ZipEntry zipEntry = zis.getNextEntry();
		newFileUriList = new ArrayList<String>();

		while (zipEntry != null) {

			if (!zipEntry.isDirectory()) {

				String outFilePath = outPath + zipEntry.getName();
				outFilePath = outFilePath.replace("/", "\\");
				logger.info("unzip: " + outFilePath);

				newFileUriList.add(outFilePath);

				File outFile = new File(outFilePath);
				if (!outFile.exists()) {
					new File(outFile.getParent()).mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(outFile);
				byte[] buffer = new byte[1024]; // TODO: 1024 -> ?
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}

			zipEntry = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();
	}

	/**
	 * 画面を表示する際にパスワードの文字数を統一するため
	 * 
	 * @param dataSourceList
	 * @author panyuan
	 */
	private void setPwdShow(TableView<ObservableDataSource> dataSourceList) {
		dataSourceList.getItems().forEach((d) -> {
			if (d.getPassword() != null && !d.getPassword().isEmpty()) {
				d.setPwdShow("******");
			} else {
				d.setPwdShow("");
			}
		});
	}

	/**
	 * データソースをエクスポート
	 * 
	 * @param event
	 * @author panyuan
	 */
	@FXML
	void doExport(ActionEvent event) {

		final File outFile;

		try {
			// エクスポートパス選択
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(myResource.getString("W03.button.data_source_export"));
			fileChooser.setInitialDirectory(new File(Constant.Application.DATA_SOURCE_FILE_PATH));
			fileChooser.setInitialFileName(serverInfo.getName());
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("ZIP Files", "*.zip"));
			outFile = fileChooser.showSaveDialog(C00ControllerBase.currentStage);

			if (outFile == null) {
				logger.info("Export cannceled.");
				return;
			}
			logger.info("Choosen file: " + outFile.getPath());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}

		buttonGroupLeft.setDisable(true);
		buttonGroupRight.setDisable(true);

		try {
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(waitPane);

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					logger.info("Export started.");
					try {
						// データソース取得
						File filesToZip = new File(Constant.Application.DATA_SOURCE_FILE_PATH + serverInfo.getName());

						// データソース圧縮、zipファイルで保存
						ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile.getPath()),
								Charset.forName("MS932"));
						for (File file : filesToZip.listFiles()) {
							zipFile(file, file.getName(), zos);
						}
						zos.close();

					} catch (Exception e) {
						// エラーメッセージ
						logger.info("Export failed.");
						logger.error(e.getMessage(), e);
						P81UserAnchorPane.showInfo(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
								messageRes.getString("ERROR_99_HEADER"), null);
						Platform.runLater(() -> {
							dataSourceStackPane.getChildren().clear();
							dataSourceStackPane.getChildren().add(dataSourceList);
							buttonGroupLeft.setDisable(false);
							buttonGroupRight.setDisable(false);
						});
						return null;
					}

					// 成功メッセージ
					P81UserAnchorPane.showInfo(AlertType.INFORMATION,
							myResource.getString("common.message.dialog.title"), messageRes.getString("INFO_W03_05"),
							null);

					Platform.runLater(() -> {
						dataSourceStackPane.getChildren().clear();
						dataSourceStackPane.getChildren().add(dataSourceList);
						buttonGroupLeft.setDisable(false);
						buttonGroupRight.setDisable(false);
					});
					logger.info("Export finished.");
					return null;
				}
			};

			thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();

		} catch (Exception e) {
			logger.error(messageRes.getString("ERROR_99_HEADER"), e);
			P81UserAnchorPane.showInfo(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString("ERROR_99_HEADER"), null);
			dataSourceStackPane.getChildren().clear();
			dataSourceStackPane.getChildren().add(dataSourceList);
			buttonGroupLeft.setDisable(false);
			buttonGroupRight.setDisable(false);
		}
	}

	/**
	 * 「fileToZip」を圧縮して「zos」に保存する
	 * 
	 * @param fileToZip
	 * @param fileName (fileToZip.getName())
	 * @param zipOut
	 * @throws IOException
	 * @author panyuan
	 */
	private void zipFile(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {

		logger.info("zip: " + fileToZip.getPath());

		if (fileToZip.isHidden()) {
			logger.warn("The file is hidden. Stop this recursion.");
			return;
		}

		if (fileToZip.isDirectory()) {
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zos);
			}
			return;
		}

		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024]; // TODO: 1024 -> ?
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		fis.close();
	}
}
