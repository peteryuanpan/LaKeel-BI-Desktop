package com.legendapl.lightning.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.model.ServerInformation;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * サーバ選択画面のサーバボタンのコントローラクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class C03ServerButtonController extends AnchorPane {

	@FXML
	private Label serverName;
	@FXML
	private Label address;
	@FXML
	private JFXButton dataBaseButton;
	@FXML
	private MaterialDesignIconView dataBaseIcon;
	@FXML
	private JFXButton settingButton;

	private ServerInformation serverInfo;

	public C03ServerButtonController() {
		this(new ServerInformation());
	}

	public C03ServerButtonController(ServerInformation serverInfo) {
		// カスタムコントロールを読み込み
		URL url = getClass().getResource("/view/C03ServerButton.fxml");
		FXMLLoader ldr = new FXMLLoader(url, ResourceBundle.getBundle(Constant.Application.MY_BUNDLE));

		// このインスタンス自身がルートオブジェクト
		ldr.setRoot(this);

		// このインスタンス自身がコントローラ
		ldr.setController(this);

		try {
			// ルートを指定済みなので、このインスタンスにFXMLがロードされる.
			ldr.load();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		// サーバ情報をセット
		this.setServerInfo(serverInfo);
		serverName.setText(serverInfo.getName());
		address.setText(serverInfo.getAddress());

		if (Constant.ServerInfo.STATUS_NG.equals(serverInfo.getStatus())) {
			dataBaseIcon.setFill(Color.RED);
		}

	}

	/**
	 * サーバ選択時にログイン画面を開くイベント処理
	 * 
	 * @param event
	 */
	public void serverSelected(MouseEvent event) {
		new W01SelectServerController().serverSelected(event, this.serverInfo);
	}

	/**
	 * データソース設定画面開くイベント処理
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void setupDataSource(MouseEvent event) throws IOException {

		ResourceBundle myResource = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);
		ResourceBundle messageRes = ResourceBundle.getBundle("messages");

		// サーバ情報にパスワードがセットされていない場合、ログインダイアログを表示
		if (StringUtils.isEmpty(serverInfo.getPassword())) {
			// カスタムダイアログを作成
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle(myResource.getString("W03.login_dialog.title"));
			dialog.setHeaderText(myResource.getString("W03.login_dialog.header"));
			ButtonType loginButtonType = new ButtonType(myResource.getString("common.button.login"),
					ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

			// ユーザ名、パスワードを設定するフィールドを作成
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			int gridRow = 0;
			// 組織名が含まれている場合は組織名テキストフィールドを追加
			JFXTextField organizationName = new JFXTextField();
			if (!StringUtils.isEmpty(serverInfo.getOrganizationName())) {
				organizationName.setPromptText(myResource.getString("W03.login_dialog.organization_name"));
				organizationName.setText(serverInfo.getOrganizationName());
				organizationName.setEditable(false);
				grid.add(new Label(myResource.getString("W03.login_dialog.organization_name") + ":"), 0, gridRow);
				grid.add(organizationName, 1, gridRow++);
			}
			JFXTextField username = new JFXTextField();
			username.setPromptText(myResource.getString("W03.login_dialog.user_name"));
			username.setText(serverInfo.getUserName());
			username.setEditable(false);
			JFXPasswordField password = new JFXPasswordField();
			password.setPromptText(myResource.getString("W03.login_dialog.password"));

			grid.add(new Label(myResource.getString("W03.login_dialog.user_name") + ":"), 0, gridRow);
			grid.add(username, 1, gridRow++);
			grid.add(new Label(myResource.getString("W03.login_dialog.password") + ":"), 0, gridRow);
			grid.add(password, 1, gridRow);

			Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
			loginButton.setDisable(true);

			// ユーザ名がある場合ログインボタンを有効化
			username.textProperty().addListener((observable, oldValue, newValue) -> {
				loginButton.setDisable(newValue.trim().isEmpty());
			});
			password.textProperty().addListener((observable, oldValue, newValue) -> {
				loginButton.setDisable(newValue.trim().isEmpty());
			});

			dialog.getDialogPane().setContent(grid);

			// パスワードフィールドをフォーカス
			Platform.runLater(() -> password.requestFocus());

			// ログインボタン押下時にユーザ名とパスワードを結果として返す形に変更
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == loginButtonType) {
					return new Pair<>(organizationName.getText().isEmpty() ? username.getText()
							: username.getText() + "|" + organizationName.getText(), password.getText());
				}
				return null;
			});

			Optional<Pair<String, String>> result = dialog.showAndWait();
			if (result.isPresent()) {
				Constant.ServerInfo.userName = result.get().getKey();
				Constant.ServerInfo.password = result.get().getValue();
			} else {
				return;
			}

			// テスト接続を実施
			RestClientConfiguration configuration = new RestClientConfiguration(
					"http://" + serverInfo.getAddress() + ":" + serverInfo.getPort() + "/" + serverInfo.getBIName());
			JasperserverRestClient client = new JasperserverRestClient(configuration);
			try {
				Session session = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password);
				if (null == session) {
					throw new com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException();
				}
			} catch (Exception e) {
				// テスト接続後に失敗したらダイアログを表示
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle(myResource.getString("common.error.dialog.title"));
				alert.setHeaderText(messageRes.getString("ERROR_W03_02"));
				Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
				alertStage.getIcons().add(new Image("/images/LightningIcon.png"));

				alertStage.showAndWait();
				return;
			}
		}

		new W01SelectServerController().setupDataSource(event, this.serverInfo);
	}

	/**
	 * サーバ設定画面を開くイベント処理
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void setupServer(MouseEvent event) throws IOException {
		new W01SelectServerController().setupServer(event, this.serverInfo);
	}

	public ServerInformation getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInformation serverInfo) {
		this.serverInfo = serverInfo;
	}
}
