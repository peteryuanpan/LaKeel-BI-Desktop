package com.legendapl.lightning.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Info;
import com.legendapl.lightning.service.ExecuteAPIService;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * ログイン画面
 * 
 * @author taka
 *
 */
public class W02LoginController extends C00ControllerBase {

	private static final String EM1 = "1em";
	private static final String ERROR = "error";

	@FXML
	private JFXTextField organizationBox;
	@FXML
	private JFXTextField userNameBox;
	@FXML
	private JFXPasswordField passwordBox;
	@FXML
	private JFXCheckBox keepCheckBox;
	@FXML
	private JFXButton loginButton;
	@FXML
	private JFXButton cancelButton;
	@FXML
	private StackPane stackPane;
	@FXML
	private JFXSnackbar snackbar;

	/**
	 * 初期化
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// パスワードボックスのフォーカス
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				passwordBox.requestFocus();
				passwordBox.selectEnd();
			}
		});

		logger.info(serverInfo);

		// 組織名ボックスに文字列をセット
		organizationBox.setText(serverInfo.getOrganizationName());
		// ユーザ名ボックスに文字列をセット
		userNameBox.setText(serverInfo.getUserName());
		// パスワードボックスに文字列をセット
		passwordBox.setText(serverInfo.getPassword());

		// ユーザ名ボックスに必須入力バリデータを作成
		RequiredFieldValidator validator = new RequiredFieldValidator();
		validator.setMessage(myResource.getString("common.message.required"));
		validator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING).size(EM1)
				.styleClass(ERROR).build());

		// 必須入力バリデータをセット
		userNameBox.getValidators().add(validator);
		userNameBox.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				userNameBox.validate();
			}
		});

		// ユーザ名ボックスにコンポーネント種類が異なるパスワード入力欄にバリデータを追加
		RequiredFieldValidator passwordValidator = new RequiredFieldValidator();
		passwordValidator.setMessage(myResource.getString("common.message.required"));
		passwordValidator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING)
				.size(EM1).styleClass(ERROR).build());
		passwordBox.getValidators().add(passwordValidator);
		passwordBox.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				passwordBox.validate();
			}
		});

		// パスワードフィールドでEnterキーを押した際にログインボタンの処理を実施
		passwordBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (keyEvent.getCode() == KeyCode.ENTER) {
					try {
						login(new ActionEvent());
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
		// スナックバーをセット
		snackbar.registerSnackbarContainer(stackPane);

		logger.info("Login Dialog Launched.");
	}

	public void login(ActionEvent event) throws IOException {

		// バリデーション結果が全て正しい場合のみ実施
		if (!validate()) {
			logger.debug("Parameter is not valid");
			return;
		}

		// テスト接続を実施
		RestClientConfiguration configuration = new RestClientConfiguration(serverInfo.getUrl());
		logger.info(serverInfo.getUrl());
		
		JasperserverRestClient client = new JasperserverRestClient(configuration);
		try {
			logger.info(organizationBox.getText().isEmpty() ? "userName :" + userNameBox.getText() + " password :**"
					: "userName :" + userNameBox.getText() + "|" + organizationBox.getText() + " password :**");
			Session session = client.authenticate(organizationBox.getText().isEmpty() ? userNameBox.getText()
					: userNameBox.getText() + "|" + organizationBox.getText(), passwordBox.getText());
			logger.info(session);
			if (null == session) {
				throw new com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException();
			}
			logger.info(messageRes.getString(Info.INFO_W02_01));
		} catch (Exception ex) {
			// テスト接続後に失敗したらSnackbar表示
			snackbar.fireEvent(new SnackbarEvent(messageRes.getString(Error.ERROR_W02_01)));
			logger.error(messageRes.getString(Error.ERROR_W02_01), ex);
			return;
		}

		// 入力を保存と設定されていた場合、情報をファイルへ保存
		if (keepCheckBox.isSelected()) {
			if (!StringUtils.isEmpty(organizationBox.getText()))
				serverInfo.setOrganizationName(organizationBox.getText());
			serverInfo.setUserName(userNameBox.getText());
			serverInfo.setPassword(passwordBox.getText());
			serverInfoList.replace(serverInfo);
			serverInfoList.savePreferenceDataToFile();
		}

		// API実行時の接続先を設定
		ExecuteAPIService.setClientConfiguration(serverInfo);
		Constant.ServerInfo.workspace += serverInfo.getName();
		Constant.ServerInfo.userName = organizationBox.getText().isEmpty() ? userNameBox.getText()
				: userNameBox.getText() + "|" + organizationBox.getText();
		Constant.ServerInfo.password = passwordBox.getText();

		// ログインダイアログ画面の非表示
		loginStage.hide();
		loginStage = null;

		// サーバ選択ダイアログ画面の非表示
		primaryStage.hide();
		primaryStage = null;

		// ホーム画面を開く
		currentStage = showPane(event, "/view/P01HomeAnchorPane.fxml",
				getTitle("P01") + " (" + serverInfo.getName() + ")", null, null);
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
	}

	public void loginCancel(ActionEvent event) throws IOException {
		loginStage.hide();
		loginStage = null;
		C00ControllerBase.serverInfo = null;
	}

	/**
	 * 入力項目のバリデーションを実施する
	 * 
	 * @return
	 */
	private Boolean validate() {
		// バリデーション結果をリストとして保持
		ArrayList<Boolean> validationList = new ArrayList<Boolean>();

		// 各バリデーション結果をリストに追加
		validationList.add(userNameBox.validate());
		validationList.add(passwordBox.validate());

		// 1つでもバリデーション結果がfalseがあった場合はfalseを返す
		return !validationList.contains(false);
	}

}
