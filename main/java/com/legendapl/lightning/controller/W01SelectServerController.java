package com.legendapl.lightning.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jfoenix.controls.JFXButton;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Info;
import com.legendapl.lightning.model.ObservableDataSource;
import com.legendapl.lightning.model.ServerInformation;
import com.legendapl.lightning.service.DataSourceServiceImpl;
import com.legendapl.lightning.service.ExecuteAPIService;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * サーバ選択画面
 *
 * @author taka
 *
 */
public class W01SelectServerController extends C00ControllerBase {

	// ウィンドウ位置
	private double xOffset = 0;
	private double yOffset = 0;

	// 現在利用中ロケール
	@FXML
	private ComboBox<String> locale;

	// サーバーボタンリスト
	@FXML
	private TilePane serversListPane;

	// 新規接続先追加ボタン
	@FXML
	private JFXButton addConnectionButton;

	// 言語表示用データ変換用マップ
	static private Map<String, String> name2LangMap = new HashMap<String, String>();
	static private Map<String, String> lang2NameMap = new HashMap<String, String>();
	static {
		name2LangMap.put("日本語", Locale.JAPANESE.getLanguage());
		name2LangMap.put("English", Locale.US.getLanguage());
		lang2NameMap.put(Locale.JAPANESE.getLanguage(), "日本語");
		lang2NameMap.put(Locale.US.getLanguage(), "English");
	}

	public void start(Stage primaryStage) {
		try {
			BorderPane page = (BorderPane) FXMLLoader.load(
					getClass().getResource("/view/W01SelectServerBorderPane.fxml"),
					ResourceBundle.getBundle(Constant.Application.MY_BUNDLE));

			FadeTransition ft = new FadeTransition(Duration.millis(500), page);
			ft.setFromValue(0.0);
			ft.setToValue(1.0);
			ft.play();

			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.setTitle("LaKeel BI for Desktop");
			C01ToolbarController.setPrimaryStage(primaryStage);

			setWindowsTitileBarTransparent(primaryStage);

			// アイコンを設定
			primaryStage.getIcons().add(new Image("/images/LightningIcon.png"));

			// 画面を表示
			primaryStage.show();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	// 初期化
	/*
	 * (non-Javadoc)
	 *
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,java.util.
	 * ResourceBundle)
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("URL=" + arg0 + ", ResourceBulder=" + arg1);

		// preferencesを読み込み
		loadPreferences();

		ObservableList<String> langs = FXCollections.observableArrayList(name2LangMap.keySet());
		locale.setItems(langs); // 選択可能言語リストのセット
		locale.getSelectionModel().select(lang2NameMap.get(Locale.getDefault().getLanguage()));// 現在言語のセット

		// サーバ情報の読み出し
		File file = new File(Constant.Application.SERVERS_FILE_PATH);
		if (file.exists()) {
			serverInfoList.loadPreferenceDataFromFile(file);
		}

		// 定義されたサーバー一覧の読み込み
		for (ServerInformation server : serverInfoList.getServers()) {
			logger.info("load & added:" + server);

			try {
				DataSourceServiceImpl dao = new DataSourceServiceImpl(
						Constant.Application.DATA_SOURCE_FILE_PATH + server.getName(), messageRes,
						Constant.Application.XML_CRYPT_PASSWORD);
				List<ObservableDataSource> dss = dao.getDataSources();

				// テストをしていないデータソースの数
				int unknownNum = 0;

				// データソースが1つもない時(新規作成時)
				if (dss.size() == 0)
					server.setStatus(Constant.ServerInfo.STATUS_NG);

				// データソースがある場合、全てのステータスを確認する。
				for (int i = 0; i < dss.size(); i++) {
					// 1つでもNGがあればNGをセット
					if (Constant.ServerInfo.STATUS_NG.equals(dss.get(i).getStatus().getValue())) {
						server.setStatus(Constant.ServerInfo.STATUS_NG);
						break;
					}

					// 全てUNKNOWNであればNGをセット(取得はしたがパスワードを未定義の場合)
					if ("".equals(dss.get(i).getStatus().getValue()) && StringUtils.isEmpty(dss.get(i).getPassword())) {
						unknownNum++;
						if (unknownNum == dss.size()) {
							server.setStatus(Constant.ServerInfo.STATUS_NG);
							break;
						}
					}

					// for文が最終週まで到達(NGがない)すればOKをセット
					if (i + 1 == dss.size()) {
						server.setStatus(Constant.ServerInfo.STATUS_OK);
					}

				}
			} catch (Exception e) {
				// エラーダイアログの表示
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(Error.ERROR_99_HEADER));
				logger.error(messageRes.getString(Error.ERROR_99_HEADER), e);
				throw new RuntimeException(e);
			}

			C03ServerButtonController serverButton = new C03ServerButtonController(server);
			serversListPane.getChildren().add(serverButton);
		}
	}

	/**
	 * サーバ選択時にログイン画面を開くイベント処理
	 *
	 * @param event
	 */
	public void serverSelected(MouseEvent event, ServerInformation serverInfo) {
		logger.debug(event.getSource().toString() + " is clicked");
		logger.debug(event.getSource().getClass().getCanonicalName());

		if (null == serverInfo) {
			logger.warn("Server information is empty!");
			return;
		}

		try {
			// サーバ情報を設定
			C00ControllerBase.serverInfo = serverInfo;

			// パスワードが保存されている場合、直接ホーム画面へ遷移
			if (!serverInfo.getPassword().isEmpty()) {

				// テスト接続を実施
				RestClientConfiguration configuration = new RestClientConfiguration(serverInfo.getUrl());
				logger.debug(serverInfo.getUrl());

				JasperserverRestClient client = new JasperserverRestClient(configuration);
				try {
					Session session = client.authenticate(
							serverInfo.getOrganizationName().isEmpty() ? serverInfo.getUserName()
									: serverInfo.getUserName() + "|" + serverInfo.getOrganizationName(),
							serverInfo.getPassword());
					logger.info(session);
					if (null == session) {
						throw new com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException();
					}
					logger.info(messageRes.getString(Info.INFO_W02_01));

					// テスト接続に成功した場合ホーム画面へ移動
					// API実行時の接続先を設定
					ExecuteAPIService.setClientConfiguration(serverInfo);
					Constant.ServerInfo.workspace += serverInfo.getName();
					Constant.ServerInfo.userName = serverInfo.getOrganizationName().isEmpty() ? serverInfo.getUserName()
							: serverInfo.getUserName() + "|" + serverInfo.getOrganizationName();
					Constant.ServerInfo.password = serverInfo.getPassword();
					Constant.ServerInfo.serverName = serverInfo.getName();

					// サーバ選択ダイアログ画面の非表示
					primaryStage.hide();
					primaryStage = null;

					// ホーム画面を開く
					currentStage = showPane(event, "/view/P01HomeAnchorPane.fxml",
							getTitle("P01") + " (" + serverInfo.getName() + ")", null, null);
					// ウィンドウ最小幅を設定
					currentStage.setMinWidth(Constant.Graphic.HOME_STAGE_MIN_WIDTH);
					currentStage.setMinHeight(Constant.Graphic.HOME_STAGE_MIN_HEIGHT);

					// 画面サイズの取得
					loadWindowSize();
					Double displayWidth = virtualBounds.getWidth();
					// ウィンドウ位置が記録されているか確認。
					if (preferences.getMainWindowY() != null && preferences.getMainWindowX() != null) {
						// 記録されたウィンドウ位置
						Double preferencesX = Double.parseDouble(preferences.getMainWindowX());
						Double preferencesY = Double.parseDouble(preferences.getMainWindowY());
						Double preferencesWidth = Double.parseDouble(preferences.getMainWindowWidth());
						Double preferencesHeight = Double.parseDouble(preferences.getMainWindowHeight());

						// preferences.xmlからウィンドウ幅を取得
						currentStage.setWidth(preferencesWidth);
						currentStage.setHeight(preferencesHeight);

						// 記録されている位置が現在のディスプレイの範囲以内か確認
						// サブディスプレイが左側の場合
						if (virtualBounds.x < 0) {
							if (preferencesX >= virtualBounds.x - preferencesWidth) {
								currentStage.setX(preferencesX);
								currentStage.setY(preferencesY);
							} else {
								currentStage.setX(0);
								currentStage.setY(0);
							}
						}
						// サブディスプレイが右側、もしくは無い場合
						if (virtualBounds.x >= 0) {
							if (0 <= preferencesX && preferencesX <= displayWidth - 20) {
								currentStage.setX(preferencesX);
								currentStage.setY(preferencesY);
							} else {
								currentStage.setX(0);
								currentStage.setY(0);
							}
						}
					}

					currentStage.show();
					return;
				} catch (Exception ex) {
					// テスト接続後に失敗したらAlert表示
					showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"), "ログイン失敗",
							"サーバ情報に設定された情報ではログインできませんでした。\nユーザ名とパスワードを確かめてログインしてください。");
					logger.error(messageRes.getString(Error.ERROR_W02_01), ex);
				}
			}

			String dialogFXML = "/view/W02LoginAnchorPane.fxml";
			loginStage = showDialogPane(event, dialogFXML, getTitle("W02") + "(" + serverInfo.getName() + ")");

			setWindowsTitileBarTransparent(loginStage);

			loginStage.show();
			logger.debug("Returned from login dialog: stage = " + loginStage.getClass().getCanonicalName());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 選択した言語に切替
	 *
	 * @param event
	 */
	public void langSelected(ActionEvent event) {

		String selectedLang = name2LangMap.get(locale.getSelectionModel().getSelectedItem());
		Locale selectedLocale = Locale.forLanguageTag(selectedLang);
		Locale.setDefault(selectedLocale);

		/* ツリーのため */
		langSelectedForTools(selectedLang);

		/* レポートエラーのため */
		langSelectedForReportErrors(selectedLang);
		
		/* アドホックーのため */
		langSelectedForAdhoc(selectedLang);

		logger.debug("Default locale:" + Locale.getDefault());
		logger.debug("Default language:" + Locale.getDefault().getLanguage());

		start(primaryStage);
	}

	/**
	 * 選択した言語に切替、ツリーのため
	 * 
	 * @param language
	 * @author panyuan
	 */
	private void langSelectedForTools(String language) {
		com.legendapl.lightning.tools.common.Constants.changeLanguage(language);
	}

	/**
	 * 選択した言語に切替、レポートエラーのため
	 * 
	 * @param language
	 * @author panyuan
	 */
	private void langSelectedForReportErrors(String language) {
		com.legendapl.lightning.common.constants.ReportErrors.changeLanguage(language);
	}
	
	/**
	 * 選択した言語に切替、アドホックのため
	 * 
	 * @param language
	 * @author panyuan
	 */
	private void langSelectedForAdhoc(String language) {
		com.legendapl.lightning.adhoc.common.AdhocUtils.changeLanguage(language);
	}

	/**
	 * データソース設定画面開くイベント処理
	 *
	 * @param event
	 * @throws IOException
	 */
	public void setupDataSource(MouseEvent event, ServerInformation serverInfo) throws IOException {
		logger.debug(event.getSource().getClass().getCanonicalName() + " is clicked");

		// サーバ情報をセット
		C00ControllerBase.serverInfo = serverInfo;

		Stage stage = showDialogPane(event, "/view/W03DataSourceListAnchorPane.fxml",
				getTitle("W03") + "(" + serverInfo.getName() + ")");

		// データソース管理画面を現在のステージにセット
		C00ControllerBase.currentStage = stage;

		// 画面サイズの取得
		loadWindowSize();
		Double displayWidth = virtualBounds.getWidth();
		// ウィンドウ位置が記録されているか確認。
		if (preferences.getDataSourceWindowY() != null && preferences.getDataSourceWindowX() != null) {
			// 記録されたウィンドウ位置
			Double preferencesX = Double.parseDouble(preferences.getDataSourceWindowX());
			Double preferencesY = Double.parseDouble(preferences.getDataSourceWindowY());
			Double preferencesWidth = Double.parseDouble(preferences.getDataSourceWindowWidth());
			Double preferencesHeight = Double.parseDouble(preferences.getDataSourceWindowHeight());

			// preferences.xmlからウィンドウ幅を取得
			stage.setWidth(preferencesWidth);
			stage.setHeight(preferencesHeight);

			// 記録されている位置が現在のディスプレイの範囲以内か確認

			// サブディスプレイが左側の場合
			if (virtualBounds.x < 0) {

				if (preferencesX >= virtualBounds.x - preferencesWidth) {
					stage.setX(preferencesX);
					stage.setY(preferencesY);
				} else {
					stage.setX(0);
					stage.setY(0);
				}
			}
			// サブディスプレイが右側、もしくは無い場合
			if (virtualBounds.x >= 0) {
				if (0 <= preferencesX && preferencesX <= displayWidth - 20) {
					stage.setX(preferencesX);
					stage.setY(preferencesY);
				} else {
					stage.setX(0);
					stage.setY(0);
				}
			}
		}

		stage.setOnCloseRequest((WindowEvent t) -> {
			logger.info("close&cancel");
			Stage st = (Stage) t.getTarget();

			try {
				// 現在のウィンドウ幅を取得し、セットする。
				preferences.setDataSourceWindowHeight(String.valueOf(st.getHeight()));
				preferences.setDataSourceWindowWidth(String.valueOf(st.getWidth()));

				// 現在のウィンドウの位置を取得し、セットする。
				preferences.setDataSourceWindowX(String.valueOf(st.getX()));
				preferences.setDataSourceWindowY(String.valueOf(st.getY()));

				// 上書き保存
				Marshaller m = context.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				m.marshal(preferences, file);
			} catch (JAXBException e) {
				logger.error(e.getMessage(), e);
			}
			((W03DataSourceController) controller).doCancel(null);
		});

		// ウィンドウ最小幅を設定
		stage.setMinWidth(Constant.Graphic.DATA_SOURCE_STAGE_MIN_WIDTH);
		stage.setMinHeight(Constant.Graphic.DATA_SOURCE_STAGE_MIN_HEIGHT);
		stage.show();
	}

	/**
	 * サーバ設定画面を開くイベント処理
	 *
	 * @param event
	 * @throws IOException
	 */
	public void setupServer(MouseEvent event, ServerInformation serverInfo) throws IOException {
		logger.debug(event.getSource().toString() + " is clicked");

		// サーバ情報をセット
		C00ControllerBase.serverInfo = serverInfo;

		serverSettingStage = showDialogPane(event, "/view/W06ServerSettingAnchorPane.fxml",
				getTitle("W06").split("/")[0] + " (" + serverInfo.getName() + ")");

		setWindowsTitileBarTransparent(serverSettingStage);

		serverSettingStage.show();
	}

	/**
	 * 新規接続先追加ダイアログを開くイベント処理
	 *
	 * @param event
	 * @throws IOException
	 */
	public void addServer(MouseEvent event) throws IOException {
		// 受け渡すサーバ情報をnullに設定
		serverInfo = null;

		logger.debug(event.getSource().toString() + " is clicked");
		serverSettingStage = showDialogPane(event, "/view/W06ServerSettingAnchorPane.fxml",
				getTitle("W06").split("/")[1]);

		setWindowsTitileBarTransparent(serverSettingStage);

		serverSettingStage.show();
	}

	/**
	 * ウィンドウタイトルバーを消去し、ドラッグによる画面異動イベントを追加する
	 *
	 * @param stage
	 */
	private void setWindowsTitileBarTransparent(Stage stage) {
		// ウィンドウ枠・タイトルバーを非表示に設定
		stage.getScene().getRoot().setEffect(new DropShadow());
		stage.getScene().setFill(Color.TRANSPARENT);
		if (!stage.getStyle().equals(StageStyle.TRANSPARENT)) {
			stage.initStyle(StageStyle.TRANSPARENT);
		}

		// マウス・ボタン押下時のウィンドウ位置取得
		stage.getScene().getRoot().setOnMousePressed(mouseevent -> {
			xOffset = mouseevent.getSceneX();
			yOffset = mouseevent.getSceneY();
		});
		// マウス・ボタンドラッグ時のウィンドウ位置調整
		stage.getScene().getRoot().setOnMouseDragged(mouseevent -> {
			stage.setX(mouseevent.getScreenX() - xOffset);
			stage.setY(mouseevent.getScreenY() - yOffset);
		});
	}

	/**
	 *
	 * アプリケーション終了処理
	 *
	 * @param event
	 * @throws IOException
	 */
	public void shutdown(MouseEvent event) throws IOException {
		logger.debug(event.getSource().toString() + " is clicked");
		logger.info("bye!");
		System.exit(Constant.Application.NORMAL_EXIT);
	}

}
