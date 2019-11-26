package com.legendapl.lightning.controller;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.model.Preferences;
import com.legendapl.lightning.model.ServerInformation;
import com.legendapl.lightning.model.ServerInformationList;

import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * コントローラの親
 * 
 * 注意事項： そのインスタンスは画面ごとに生成されるため、画面間で共有したい情報はスタティック属性で管理することが必要である。
 * 
 * 主な機能： ・バンドルファイルの管理 ・画面の表示制御
 * 
 * @author taka
 *
 */
public abstract class C00ControllerBase implements Initializable {

	// 多言語対応バンドルファイル
	protected ResourceBundle myResource = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);
	protected ResourceBundle messageRes = ResourceBundle.getBundle("messages");
	protected Logger logger = Logger.getLogger(getClass());

	protected static Stage primaryStage; // 初期画面
	protected static Stage loginStage; // ログイン画面
	protected static Stage serverSettingStage; // サーバ設定画面
	protected static Stage currentStage; // 現在のメイン画面
	protected static Stage excelJobStage; // Excelジョブ作成画面

	protected static C00ControllerBase controller;// FXMLに対応するコントローラー

	// アプリケーション設定ファイル
	protected static File file = new File(Constant.Application.PREFERENCES_FILE_PATH);
	protected static JAXBContext context;
	protected static Preferences preferences;

	// ウィンドウサイズの取得
	protected static Rectangle virtualBounds;
	protected static GraphicsEnvironment ge;

	// 新規作成されるモードレス画面用ステージ
	Stage stage;
	// 画面遷移時の受け渡し用オブジェクト
	protected static Object object;

	/* ワークスペースフォルダ */
	protected static String workSpaceFolderPath;

	/* 標準エクスポート先フォルダ */
	protected static String defaultExportFolderPath;

	// サーバリスト
	public static ServerInformationList serverInfoList = new ServerInformationList();

	// 保持するサーバ情報
	public static ServerInformation serverInfo = new ServerInformation();

	// 表示するレポート実行画面リスト
	public static List<Stage> reportStageList = new ArrayList<Stage>();

	// 画面タイトル名の取得
	protected String getTitle(String windowId) {
		return myResource.getString(windowId + ".window.title");
	}
	
	// 画面タイトル名の取得
	protected String getTitle(String windowId, ResourceBundle bundle) {
		return bundle.getString(windowId + ".window.title");
	}

	/**
	 * ダイアログ画面を表示する
	 * 
	 * @param event
	 *            親画面を取得するためのイベントオブジェクト
	 * @param dialogFXML
	 *            開こうとしているダイアログのFXML定義ファイル
	 * @param title
	 *            開こうとしている画面のタイトル
	 * @return 開いたダイアログの画面オブジェクト
	 * @throws IOException
	 */
	protected Stage showDialogPane(Event event, String dialogFXML, String title) throws IOException {
		return showPane(event, dialogFXML, title, Modality.WINDOW_MODAL, null);
	}
	
	/**
	 * 画面を表示する
	 */
	protected Stage showPane(Object obj, String paneFXML, String title, Modality modal, Stage _stage)
			throws IOException {
		return showPane(obj, paneFXML, title, modal, _stage, ResourceBundle.getBundle(Constant.Application.MY_BUNDLE));
	}

	/**
	 * 画面を表示する
	 * 
	 * @param obj
	 *            親画面を取得するためのイベントオブジェクトでEventクラスかWindowクラスで受け取る
	 * @param dialogFXML
	 *            開こうとしているダイアログのFXML定義ファイル
	 * @param title
	 *            開こうとしている画面のタイトル
	 * @param modal
	 *            Modality.WINDOW_MODALかModality.APPLICATION_MODALならモーダル画面、その他（nullを含む）ならモードレス画面
	 * @param _stage
	 *            モードレス画面の場合、現在画面に再表示したいときに、指定する。nullなら新しい画面が作成される。
	 * @return 開いた画面オブジェクト
	 * @throws IOException
	 */
	protected Stage showPane(Object obj, String paneFXML, String title, Modality modal, Stage _stage, ResourceBundle bundle)
			throws IOException {
		Stage stage = _stage;

		// モードレス画面の場合新しい画面を作成
		if (_stage == null || modal == Modality.WINDOW_MODAL || modal == Modality.APPLICATION_MODAL
				|| modal == Modality.NONE) {
			stage = new Stage();
		}

		// 呼び出すダイアログのFXMLを開く
		FXMLLoader fxmlloader = new FXMLLoader(C00ControllerBase.class.getResource(paneFXML), bundle);
		// initializeを呼び出し
		Parent root = fxmlloader.load();
		// fxmlで指定されたControllerを格納(Excelジョブ作成画面の場合は格納しない)
		if (!paneFXML.equals("/view/W07ExcelPasteWizardAnchorPane.fxml"))
			setController(fxmlloader.getController());

		// ステージのフェードタイムを設定
		FadeTransition ft = new FadeTransition(Duration.millis(Constant.Graphic.STAGE_FADE_TIME), root);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		ft.play();

		stage.setScene(new Scene(root));
		root.requestFocus();

		// タイトルを設定
		stage.setTitle(title);

		// アイコンを設定
		stage.getIcons().add(new Image("/images/LightningIcon.png"));

		// モーダルの指定を反映
		if (modal != null) {
			stage.initModality(modal);
		} else {
			// モーダルの指定がnullの場合はアプリケーションのメインウィンドウとして扱い、ウィンドウを閉じた際にアプリケーション自体を終了
			stage.setOnCloseRequest((WindowEvent t) -> {

				// Alertダイアログの利用
				Alert alert = new Alert(AlertType.WARNING, "", ButtonType.YES, ButtonType.CANCEL);
				alert.setTitle(myResource.getString("common.confirmation.dialog.title"));
				alert.getDialogPane().setHeaderText(myResource.getString("common.confirmation.dialog.header"));

				// アイコンを設定
				Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
				alertStage.getIcons().add(new Image("/images/LightningIcon.png"));

				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == ButtonType.YES) {
					// サーバリスト情報ファイルを保存(最近表示されたアイテムを反映）
					serverInfoList.savePreferenceDataToFile();
					logger.info("bye!");
					Stage st = (Stage) t.getSource();

					try {
						// 現在のウィンドウ幅を取得し、セットする。
						preferences.setMainWindowHeight(String.valueOf(st.getHeight()));
						preferences.setMainWindowWidth(String.valueOf(st.getWidth()));

						// 現在のウィンドウの位置を取得し、セットする。
						preferences.setMainWindowX(String.valueOf(st.getX()));
						preferences.setMainWindowY(String.valueOf(st.getY()));

						// 上書き保存
						Marshaller m = context.createMarshaller();
						m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
						m.marshal(preferences, file);
					} catch (JAXBException e) {
						logger.error(e.getMessage(), e);
					}

					System.exit(Constant.Application.NORMAL_EXIT);
				} else
					t.consume();
			});

		}

		// eventがnullの場合処理を中断
		if (null == obj) {
			return stage;
		}

		Event event = null;
		Window window = null;
		if (obj instanceof Event) {
			event = (Event) obj;
			if (event.getSource() instanceof Node
					&& (_stage == null || modal == Modality.WINDOW_MODAL || modal == Modality.APPLICATION_MODAL)
					&& modal != Modality.NONE) {
				Node node = (Node) (event.getSource());
				stage.initOwner(node.getScene().getWindow());
				// stage.show();
			}
		} else if (obj instanceof Window) {
			window = (Window) obj;
			stage.initOwner(window);
		} else
			return stage;

		if (_stage != null) {
			stage.setWidth(_stage.getWidth());
			stage.setHeight(_stage.getHeight());
		}
		return stage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		C00ControllerBase.primaryStage = primaryStage;
	}

	public C00ControllerBase getController() {
		return controller;
	}

	public void setController(C00ControllerBase controller) {
		C00ControllerBase.controller = controller;
	}

	public void setObject(Object object) {
		C00ControllerBase.object = object;
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void loadPreferences() {

		try {
			// preferences.xmlの読み出し
			context = JAXBContext.newInstance(Preferences.class);
			Unmarshaller um = context.createUnmarshaller();
			preferences = (com.legendapl.lightning.model.Preferences) um.unmarshal(file);

		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void loadWindowSize() {
		virtualBounds = new Rectangle();
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i = 0; i < gc.length; i++) {
				virtualBounds = virtualBounds.union(gc[i].getBounds());
			}
		}
	}

	/*
	 * ダイアログを表示
	 * 
	 * @param type
	 * 
	 * @param titleText
	 * 
	 * @param headerText
	 */
	public Optional<ButtonType> showDialog(AlertType type, String titleText, String headerText) {
		return showDialog(type, titleText, headerText, null);
	}

	/*
	 * ダイアログを表示
	 * 
	 * @param type
	 * 
	 * @param titleText
	 * 
	 * @param headerText
	 * 
	 * @param contentText
	 */
	public Optional<ButtonType> showDialog(AlertType type, String titleText, String headerText, String contentText) {
		Alert alert = new Alert(type);
		alert.setTitle(titleText);
		alert.setHeaderText(headerText);
		if (!StringUtils.isEmpty(contentText)) {
			alert.setContentText(contentText);
		}
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("/images/LightningIcon.png"));
		return alert.showAndWait();
	}

}
