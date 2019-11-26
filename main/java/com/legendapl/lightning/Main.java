package com.legendapl.lightning;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;
import com.legendapl.lightning.controller.W01SelectServerController;
import com.legendapl.lightning.model.Preferences;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * メインクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
public class Main extends Application {

	public static Preferences preferences;

	static Logger logger;
	static ResourceBundle resourceBundle;

	// 多言語対応バンドルファイル
	protected static ResourceBundle myResource = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);

	public static void main(String[] args) throws JRException {
		launch(args);
	}

	/**
	 * JavaFX Appスレッド
	 * 
	 * @param primaryStage
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// アプリケーション実行時の文字コードをUTF-8に設定
		System.setProperty("file.encoding", "UTF-8");
		PropertyConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("log4j.properties"));
		logger = Logger.getLogger(Main.class.getName());
		resourceBundle = ResourceBundle.getBundle("messages_ja");

		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		ServerInfo.tempDir = tempDir.getAbsolutePath();

		// クラスローダー : JDBCドライバを動的にクラスパスに追加する
		File jarFile = null;
		try {
			File dir = new File(System.getProperty("user.dir") + "\\libs\\jdbcDriver");
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				jarFile = files[i];
				if (jarFile.toString().contains(".jar")) {
					URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
					URL u = new File(jarFile.toString()).toURI().toURL();
					Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
					m.setAccessible(true);
					m.invoke(loader, new Object[] { u });
					logger.info("Add to: " + jarFile.toString());
				}
			}
		} catch (Exception e) {
			if (null != jarFile)
				logger.error("Could not add: " + jarFile.toString(), e);
			else
				logger.error(e.getMessage(), e);
		}

		@SuppressWarnings("unused")
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Constant.Application.LOCK_PORT);
			// 多重起動ではなかった
			logger.info("Multiple launch check successfully finished.");
		} catch (IOException e) {
			// 多重起動だった
			logger.error("Multiple launch check failed.");
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(myResource.getString("common.error.dialog.title"));
			alert.setHeaderText(myResource.getString("main.start_error.dialog.header"));
			alert.setContentText(myResource.getString("main.start_error.dialog.body"));
			alert.showAndWait();
			System.exit(Constant.Application.ERROR_EXIT);
		}

		// 設定フォルダの有無をチェックしない場合は作成
		File settingFolder = new File(Constant.Application.SETTING_FILE_PATH);
		if (!settingFolder.exists())
			settingFolder.mkdir();
		File dataSourceFolder = new File(Constant.Application.DATA_SOURCE_FILE_PATH);
		if (!dataSourceFolder.exists())
			dataSourceFolder.mkdir();

		// アプリケーション設定ファイル
		File file = new File(Constant.Application.PREFERENCES_FILE_PATH);
		JAXBContext context = JAXBContext.newInstance(Preferences.class);

		// アプリケーション設定ファイルが存在しなかった場合は新規作成
		if (!file.exists()) {
			preferences = new Preferences();
			try {
				Marshaller m = context.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				m.marshal(preferences, file);
			} catch (Exception e) { // catches ANY exception
				logger.error(e.getMessage(), e);
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle(myResource.getString("common.error.dialog.title"));
				alert.setHeaderText(myResource.getString("main.load_error.dialog.header"));
				alert.setContentText(myResource.getString("main.load_error.dialog.body") + file.getPath());
				alert.showAndWait();
			}
		}

		// アプリケーション設定ファイルから読み出し
		try {
			Unmarshaller um = context.createUnmarshaller();
			preferences = (com.legendapl.lightning.model.Preferences) um.unmarshal(file);

			logger.debug(preferences);
			// アプリケーション設定ファイルに設定された言語の設定を適用
			if (!preferences.getLocale().isEmpty()) {
				Locale selectedLocale = Locale.forLanguageTag(preferences.getLocale());
				Locale.setDefault(selectedLocale);

				logger.debug("Default locale:" + Locale.getDefault());
				logger.debug("Default language:" + Locale.getDefault().getLanguage());
			}

			// TODO:フォントファミリー、フォントサイズ、テーマカラーの設定

			// デフォルトのエクスポート先を設定
			if (!preferences.getDefaultExportFolderPath().isEmpty()) {
				File targetDirectory = new File(preferences.getDefaultExportFolderPath());
				// フォルダがない場合作成
				if (!targetDirectory.exists()) {
					if (targetDirectory.mkdirs()) {
						logger.debug("The export folder was maked.:" + preferences.getDefaultExportFolderPath());
					} else {
						logger.error("The export folder was not maked.:" + preferences.getDefaultExportFolderPath());
						logger.error("Check the permission to create folder.");
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle(myResource.getString("common.error.dialog.title"));
						alert.setHeaderText(myResource.getString("main.load_error.dialog.header"));
						alert.setContentText(myResource.getString("main.load_error.dialog.body") + file.getPath());
						Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
						alertStage.getIcons().add(new Image("/images/LightningIcon.png"));
						alert.showAndWait();
						System.exit(Constant.Application.ERROR_EXIT);
					}
				}
				Constant.ServerInfo.exportFolderPath = preferences.getDefaultExportFolderPath();
			}

			// ワークスペースフォルダの設定
			if (!preferences.getWorkSpaceFolderPath().isEmpty()) {
				File targetDirectory = new File(preferences.getWorkSpaceFolderPath());
				// ワークスペースフォルダがない場合作成
				if (!targetDirectory.exists()) {
					if (targetDirectory.mkdirs()) {
						logger.debug("The workspace folder was maked.:" + preferences.getWorkSpaceFolderPath());
					} else {
						logger.error("The workspace folder was not maked.:" + preferences.getWorkSpaceFolderPath());
						logger.error("Check the permission to create folder.");
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle(myResource.getString("common.error.dialog.title"));
						alert.setHeaderText(myResource.getString("main.load_error.dialog.header"));
						alert.setContentText(myResource.getString("main.load_error.dialog.body") + file.getPath());
						Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
						alertStage.getIcons().add(new Image("/images/LightningIcon.png"));
						alert.showAndWait();
						System.exit(Constant.Application.ERROR_EXIT);
					}
				}
				Constant.ServerInfo.workspace = preferences.getWorkSpaceFolderPath();
			}
		} catch (Exception e) { // catches ANY exception
			logger.error(e.getMessage(), e);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(myResource.getString("common.error.dialog.title"));
			alert.setHeaderText(myResource.getString("main.load_error.dialog.header"));
			alert.setContentText(myResource.getString("main.load_error.dialog.body") + file.getPath());
			Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
			alertStage.getIcons().add(new Image("/images/LightningIcon.png"));
			alert.showAndWait();
			System.exit(Constant.Application.ERROR_EXIT);
		}

		// サーバ選択画面を起動
		try {
			new W01SelectServerController().start(primaryStage);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// クラスロードのため、空のjrxmlの読み込み
		Thread load = new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				JRXmlLoader.load(System.getProperty("user.dir") + "\\src\\main\\resources\\Blank.jrxml");
				return null;
			}
		});
		logger.info("Load jrxml loader class");
		load.start();

	}
}