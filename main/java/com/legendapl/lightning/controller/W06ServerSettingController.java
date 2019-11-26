package com.legendapl.lightning.controller;

import java.io.File;
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
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.NumberValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Info;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Warn;
import com.legendapl.lightning.model.ServerInformation;
import com.legendapl.lightning.validation.RegValidator;
import com.legendapl.lightning.validation.SizeValidator;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * サーバ設定画面
 *
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class W06ServerSettingController extends C00ControllerBase {
	private static final String EM1 = "1em";
	private static final String ERROR = "error";

	@FXML
	private JFXTextField serverName;
	@FXML
	private JFXTextField address;
	@FXML
	private JFXTextField port;
	@FXML
	private JFXTextField BIServerName;
	@FXML
	private JFXTextField organizationName;
	@FXML
	private JFXTextField userName;
	@FXML
	private JFXPasswordField password;

	@FXML
	private JFXCheckBox savePasswordCheckbox;
	@FXML
	private JFXCheckBox useHttpsCheckbox;
	@FXML
	private HBox toolBox;
	@FXML
	private JFXButton saveButton;
	@FXML
	private JFXButton deleteButton;

	@FXML
	private StackPane root;

	@FXML
	private JFXDialog overwriteDialog;
	@FXML
	private JFXButton overwriteAcceptButton;
	@FXML
	private JFXButton overwriteCancelButton;

	@FXML
	private JFXDialog deleteDialog;
	@FXML
	private JFXButton deleteAcceptButton;
	@FXML
	private JFXButton deleteCancelButton;
	@FXML
	private JFXSnackbar snackbar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// オブジェクトの読み込み
		logger.debug(serverInfo);

		if (null == serverInfo) {
			toolBox.getChildren().remove(deleteButton);
		}

		if (null != serverInfo) {
			// サーバ名ボックスに文字列をセット
			serverName.setText(serverInfo.getName());
			// アドレスボックスに文字列をセット
			address.setText(serverInfo.getAddress());
			// ポートボックスに数値をセット
			port.setText(String.valueOf(serverInfo.getPort()));
			// BIサーバ名ボックスに文字列をセット
			BIServerName.setText(serverInfo.getBIName());
			// 組織名ボックスに文字列をセット
			organizationName.setText(serverInfo.getOrganizationName());
			// ユーザ名ボックスに文字列をセット
			userName.setText(serverInfo.getUserName());
			// パスワードボックスに文字列をセット
			logger.debug("password is " + serverInfo.getPassword());
			password.setText(serverInfo.getPassword());
			// パスワード保存チェックボックスに文字列をセット
			if (serverInfo.getPassword()!=null && !serverInfo.getPassword().isEmpty()) {
				savePasswordCheckbox.setSelected(true);
			}
			// httpsで通信するクボックスに文字列をセット
			useHttpsCheckbox.setSelected(serverInfo.getUseHttps());
		}
		// パスワードと組織名を除く要素に必須入力のバリデータを設定
		addRequireFieldValidator(serverName);
		addRequireFieldValidator(address);
		addRequireFieldValidator(port);
		addRequireFieldValidator(BIServerName);
		addRequireFieldValidator(userName);
		addRegValidator(serverName);
		addSizeValidator(serverName);

		// コンポーネント種類が異なるパスワード入力欄にバリデータを追加
		RequiredFieldValidator validator = new RequiredFieldValidator();
		validator.setMessage(myResource.getString("common.message.required"));
		validator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING).size(EM1)
				.styleClass(ERROR).build());
		password.getValidators().add(validator);
		password.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				password.validate();
			}
		});

		// ポート番号に数値のバリデータを追加
		NumberValidator numValidator = new NumberValidator();
		port.getValidators().add(numValidator);
		numValidator.setMessage(myResource.getString("common.message.number_required"));
		numValidator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING).size(EM1)
				.styleClass(ERROR).build());
		port.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				port.validate();
			}
		});

		// 上書き保存ダイアログを非表示に設定
		root.getChildren().remove(overwriteDialog);
		// 上書き保存ダイアログではいを押した際の処理を設定
		overwriteAcceptButton.setOnMouseClicked((e) -> exportServerSetting());
		// 上書き保存ダイアログではいを押した際の処理を設定
		overwriteCancelButton.setOnMouseClicked((e) -> overwriteDialog.close());

		// 削除ダイアログを非表示に設定
		root.getChildren().remove(deleteDialog);
		// 削除ダイアログではいを押した際の処理を設定
		deleteAcceptButton.setOnMouseClicked((e) -> removeServerInformationFromList());
		// 削除ダイアログではいを押した際の処理を設定
		deleteCancelButton.setOnMouseClicked((e) -> deleteDialog.close());

		// スナックバーをセット
		snackbar.registerSnackbarContainer(root);

	}

	/**
	 * テキストフィールド（JFXTextField）に対して必須入力のバリデータを設定する
	 *
	 * @param validator
	 */
	private void addRequireFieldValidator(JFXTextField field) {
		// 必須入力バリデータを作成
		RequiredFieldValidator validator = new RequiredFieldValidator();
		validator.setMessage(myResource.getString("common.message.required"));
		validator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING).size(EM1)
				.styleClass(ERROR).build());

		// 必須入力バリデータをセット
		field.getValidators().add(validator);
		field.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				field.validate();
			}
		});
	}

	/**
	 *
	 *
	 * @param validator
	 */
	private void addRegValidator(JFXTextField field) {

		RegValidator illegalValidator = new RegValidator("[^/\\?\\*\\|<>\"\\:\\\\]*");
		illegalValidator.setMessage(myResource.getString("common.message.illegal"));
		illegalValidator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING)
				.size(EM1).styleClass(ERROR).build());

		field.getValidators().add(illegalValidator);
		field.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				field.validate();
			}
		});

		RegValidator spaceValidator = new RegValidator("^[^ ].*");
		spaceValidator.setMessage(myResource.getString("common.message.spaceError"));
		spaceValidator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING).size(EM1)
				.styleClass(ERROR).build());

		field.getValidators().add(spaceValidator);
		field.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				field.validate();
			}
		});
	}

	private void addSizeValidator(JFXTextField field) {

		SizeValidator validator = new SizeValidator(100);
		validator.setMessage(myResource.getString("common.message.oversize"));
		validator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING).size(EM1)
				.styleClass(ERROR).build());

		field.getValidators().add(validator);
		field.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				field.validate();
			}
		});
	}

	/**
	 * 入力されたサーバ設定情報を元にBIサーバにテスト接続する
	 *
	 * @param event
	 */
	public void testConnect(ActionEvent event) throws IOException {
		// バリデーション結果が全て正しい場合のみ実施
		if (!validate()) {
			logger.debug("Parameter is not valid");
			return;
		}

		// サーバ情報を設定
		String url = (useHttpsCheckbox.isSelected() ? "https" : "http") + "://" +
					 address.getText() + ":" + port.getText() + "/" + BIServerName.getText();
		RestClientConfiguration configuration = new RestClientConfiguration(url);
		logger.debug(url);
		JasperserverRestClient client = new JasperserverRestClient(configuration);
		try {
			// テスト接続を実施
			logger.debug("userName is " + 
					     (organizationName.getText().isEmpty() ? 
					      userName.getText() : userName.getText() + "|" + organizationName.getText()));
			logger.debug("pawssword is " + password.getText());
			Session session = client.authenticate(organizationName.getText().isEmpty() ? userName.getText()
					: userName.getText() + "|" + organizationName.getText(), password.getText());
			logger.debug(session);
			logger.info(messageRes.getString(Info.INFO_W06_01));
			snackbar.fireEvent(new SnackbarEvent(messageRes.getString(Info.INFO_W06_01)));

			// 保存ボタンを活性化
			saveButton.disableProperty().set(false);

		} catch (Exception ex) {
			logger.error(messageRes.getString(Error.ERROR_W06_01), ex);
			snackbar.fireEvent(new SnackbarEvent(messageRes.getString(Error.ERROR_W06_01)));

			// 保存ボタンを不活性化
			saveButton.disableProperty().set(true);

			return;
		}
	}

	/**
	 * 保存時処理を実施
	 *
	 * @param event
	 */
	public void save(ActionEvent event) throws IOException {

		// バリデーション結果が全て正しい場合のみ実施
		if (!validate()) {
			logger.debug("Parameter is not valid");
			return;
		}

		if (serverName.getText().equals("excelJob")) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					myResource.getString("W06.message.duplicate_header"),
					myResource.getString("common.error.dialog.header"));
			return;
		}

		// 同じ名前のサーバ情報が保存されていた場合はエラーダイアログを表示し、保存処理を中断
		for (ServerInformation server : serverInfoList.getServers()) {
			// idが一致する場合は同じ名前でも許可
			if (null != serverInfo && !StringUtils.isEmpty(serverInfo.getId())
					&& server.getId().equals(serverInfo.getId()))
				continue;

			if (server.getName().equals(serverName.getText())) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						myResource.getString("W06.message.duplicate_header"),
						myResource.getString("W06.message.duplicate_body"));
				logger.debug("Server setting file already exist.");
				return;
			}
		}

		// サーバ情報が保存されていた場合はダイアログを表示
		if (!serverInfoList.getServers().isEmpty() && null != serverInfo
				&& null != serverInfoList.get(serverInfo.getId())) {
			logger.debug("Server setting file already exist.");
			overwriteDialog.setTransitionType(DialogTransition.CENTER);
			overwriteDialog.show(root);
			return;
		}

		// 入力されたサーバ設定情報を保存する
		exportServerSetting();
	}

	/**
	 * 削除処理を実施
	 *
	 * @param event
	 */
	public void delete(ActionEvent event) throws IOException {
		deleteDialog.setTransitionType(DialogTransition.CENTER);
		deleteDialog.show(root);
	}

	/**
	 * 入力されたサーバ設定情報を保存する
	 */
	private void exportServerSetting() {
		// 入力項目をインスタンス化
		ServerInformation serverInfo = new ServerInformation(serverName.getText(), address.getText(),
				port.getText().isEmpty() ? 80 : Integer.parseInt(port.getText()),
				BIServerName.getText(), organizationName.getText(), userName.getText(),
				savePasswordCheckbox.isSelected() ? password.getText() : null, 
				null, useHttpsCheckbox.isSelected());

		// 新規接続先の場合、新たにidを設定しフォルダを作成
		if (null == C00ControllerBase.serverInfo) {

			int newId = serverInfoList.getServers().size();
			if (null != serverInfoList.getServers()) {
				for (ServerInformation e : serverInfoList.getServers()) {
					newId = newId < Integer.valueOf(e.getId()) ? Integer.valueOf(e.getId()) : newId;
				}
			}

			serverInfo.setId(String.valueOf(newId + 1));
			serverInfoList.add(serverInfo);

			File newdir = new File(Constant.Application.DATA_SOURCE_FILE_PATH + serverInfo.getName());
			newdir.mkdir();

		} else {
			// 既存の接続先の場合、上書き先の位置を特定
			serverInfo.setId(C00ControllerBase.serverInfo.getId());

			// 接続先先情報変更のため、最近表示したアイテムをクリア
			serverInfo.clearRecentItem();

			// サーバ情報が保存されており、サーバ名が変更されていた場合はフォルダをリネーム
			ServerInformation exist = serverInfoList.get(C00ControllerBase.serverInfo.getId());
			if (!serverName.getText().equals(exist.getName())) {

				logger.debug(Constant.ServerInfo.workspace);
				File originFolder = new File(Constant.ServerInfo.workspace + exist.getName());
				File renameFolder = new File(Constant.ServerInfo.workspace + serverName.getText());

				// サーバ名のフォルダが存在していた場合は名前を変更
				if (originFolder.exists()) {
					logger.info("Rename:" + originFolder.getAbsolutePath() + " to " + renameFolder.getAbsolutePath());
					try {
						if (originFolder.renameTo(renameFolder)) {
							logger.info(messageRes.getString(Info.INFO_W06_02));
						} else {
							logger.error(messageRes.getString(Warn.WARN_W06_01));
						}
					} catch (SecurityException e) {
						logger.error(e.getMessage(), e);
						// 変更前のフォルダが存在しない場合はフォルダを作成
						renameFolder.mkdir();
						logger.warn(renameFolder.getAbsolutePath() + " is created.");
					} catch (NullPointerException e) {
						logger.error(e.getMessage(), e);
						// 変更前のフォルダが存在しない場合はフォルダを作成
						renameFolder.mkdir();
						logger.warn(renameFolder.getAbsolutePath() + " is created.");
					}
				} else {
					// 変更前のフォルダが存在しない場合はフォルダを作成
					renameFolder.mkdir();
					logger.warn(renameFolder.getAbsolutePath() + " is created.");
				}

				File originSettingFolder = new File(Constant.Application.DATA_SOURCE_FILE_PATH + exist.getName());
				File renameSettingFolder = new File(Constant.Application.DATA_SOURCE_FILE_PATH + serverName.getText());

				// サーバ名のフォルダが存在していた場合は名前を変更
				if (originSettingFolder.exists()) {
					logger.info("Rename:" + originSettingFolder.getAbsolutePath() + " to "
							+ renameSettingFolder.getAbsolutePath());

					try {
						if (originSettingFolder.renameTo(renameSettingFolder)) {
							logger.info(messageRes.getString(Info.INFO_W06_02));
						} else {
							logger.error(messageRes.getString(Warn.WARN_W06_01));
						}
					} catch (SecurityException e) {
						logger.error(e.getMessage(), e);
						// 変更前のフォルダが存在しない場合はフォルダを作成
						renameSettingFolder.mkdir();
						logger.warn(renameSettingFolder.getAbsolutePath() + " is created.");
					} catch (NullPointerException e) {
						logger.error(e.getMessage(), e);
						// 変更前のフォルダが存在しない場合はフォルダを作成
						renameSettingFolder.mkdir();
						logger.warn(renameSettingFolder.getAbsolutePath() + " is created.");
					}
				} else {
					// 変更前のフォルダが存在しない場合はフォルダを作成
					renameSettingFolder.mkdir();
					logger.warn(renameSettingFolder.getAbsolutePath() + " is created.");
				}

			}
			serverInfoList.replace(serverInfo);
		}

		File file = new File(Constant.Application.SERVERS_FILE_PATH);
		serverInfoList.savePreferenceDataToFile(file);

		logger.info(serverInfo.toString() + " is saved.");

		// ウィンドウを閉じる
		serverSettingStage.hide();
		serverSettingStage = null;

		// サーバ選択画面を再表示する
		new W01SelectServerController().start(primaryStage);
	}

	/**
	 * サーバ情報を削除する
	 */
	private void removeServerInformationFromList() {
		// サーバリストからサーバ情報を削除
		serverInfoList.remove(serverInfo);
		File file = new File(Constant.Application.SERVERS_FILE_PATH);
		serverInfoList.savePreferenceDataToFile(file);

		// フォルダを削除
		try {
			deleteDirectory(Constant.Application.DATA_SOURCE_FILE_PATH + serverInfo.getName());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(serverInfo.toString() + " is deleted.");

		// ウィンドウを閉じる
		serverSettingStage.hide();
		serverSettingStage = null;

		// サーバ選択画面を再表示する
		new W01SelectServerController().start(primaryStage);
	}

	/**
	 * 対象パスのディレクトリの削除を行う.<BR>
	 * ディレクトリ配下のファイル等が存在する場合は<BR>
	 * 配下のファイルをすべて削除します.
	 *
	 * @param dirPath
	 *            削除対象ディレクトリパス
	 * @throws Exception
	 */
	public static void deleteDirectory(final String dirPath) throws Exception {
		File file = new File(dirPath);
		recursiveDeleteFile(file);
	}

	/**
	 * 対象のファイルオブジェクトの削除を行う.<BR>
	 * ディレクトリの場合は再帰処理を行い、削除する。
	 *
	 * @param file
	 *            ファイルオブジェクト
	 * @throws Exception
	 */
	private static void recursiveDeleteFile(final File file) throws Exception {
		// 存在しない場合は処理終了
		if (!file.exists()) {
			return;
		}
		// 対象がディレクトリの場合は再帰処理
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				recursiveDeleteFile(child);
			}
		}
		// 対象がファイルもしくは配下が空のディレクトリの場合は削除する
		file.delete();
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
		validationList.add(serverName.validate());
		validationList.add(address.validate());
		validationList.add(port.validate());
		validationList.add(BIServerName.validate());
		validationList.add(userName.validate());
		validationList.add(password.validate());

		// 1つでもバリデーション結果がfalseがあった場合はfalseを返す
		return !validationList.contains(false);
	}

	/**
	 * サーバ設定ダイアログを閉じる
	 *
	 * @param event
	 */
	public void cancel(ActionEvent event) throws IOException {
		serverSettingStage.hide();
		serverSettingStage = null;
	}

}
