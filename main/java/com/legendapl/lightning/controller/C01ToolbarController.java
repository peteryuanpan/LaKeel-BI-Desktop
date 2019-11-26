package com.legendapl.lightning.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AccessDeniedException;
import com.jfoenix.controls.JFXButton;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.tools.data.AdhocData;
import com.legendapl.lightning.tools.data.ToolsData;
import com.legendapl.lightning.tools.service.ExecuteAPIService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * ツールバー
 *
 * ツールバーの必要なコントローラはそれを継承すること
 *
 * 主な機能：ツールのすべてのアクションはここに実装する
 *
 * @author taka
 *
 */
public class C01ToolbarController extends C00ControllerBase {

	@FXML
	private Label loginUserName;

	@FXML
	private HBox hbox;

	@FXML
	private JFXButton tools;

	public void homeFired(ActionEvent event) throws IOException {
		currentStage = showPane(event, "/view/P01HomeAnchorPane.fxml",
				getTitle("P01") + " (" + serverInfo.getName() + ")", null, currentStage);
	}

	public void repoFired(ActionEvent event) throws IOException {
		currentStage = showPane(event, "/view/P02RepoAnchorPane.fxml",
				getTitle("P02") + " (" + serverInfo.getName() + ")", null, currentStage);
	}

	public void toolsFired(ActionEvent event) throws IOException {
		currentStage = showPane(event, "/view/P80HomeAnchorPane.fxml",
				getTitle("P80") + " (" + serverInfo.getName() + ")", null, currentStage);
	}

	public void excelFired(ActionEvent event) throws IOException {
		currentStage = showPane(event, "/view/W08ExcelPasteJobListAnchorPane.fxml",
				getTitle("W08") + " (" + serverInfo.getName() + ")", null, currentStage);
	}

	public void adhocFired(ActionEvent event) throws IOException {
		ResourceBundle bundle = ResourceBundle.getBundle("AdhocBundleMessage");
		currentStage = showPane(event, "/view/P100HomeAnchorPane.fxml",
				getTitle("P100", bundle) + " (" + serverInfo.getName() + ")", null, currentStage, bundle);
	}

	public void logoutFired(ActionEvent event) {

		// Alertダイアログの利用
		Alert alert = new Alert(AlertType.WARNING, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		alert.setTitle(myResource.getString("C01.logout_dialog.title"));
		alert.getDialogPane().setHeaderText(myResource.getString("C01.logout_dialog.header"));
		alert.getDialogPane().setContentText(myResource.getString("C01.logout_dialog.body"));

		// アイコンを設定
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("/images/LightningIcon.png"));

		Optional<ButtonType> result = alert.showAndWait();

		// 全てのレポート実行画面をクローズする。
		for (Stage stage : reportStageList) {
			stage.close();
		}
		reportStageList.clear();

		if (result.get() == ButtonType.YES) {
			// サーバリスト情報ファイルを保存(最近表示されたアイテムを反映）
			serverInfoList.savePreferenceDataToFile();
			logger.info("bye!");
			System.exit(Constant.Application.NORMAL_EXIT);
		}
		if (result.get() == ButtonType.NO) {
			logger.info("Go to SelectServer");

			try {
				// 現在のウィンドウ幅を取得し、セットする。
				preferences.setMainWindowHeight(String.valueOf(currentStage.getHeight()));
				preferences.setMainWindowWidth(String.valueOf(currentStage.getWidth()));

				// 現在のウィンドウの位置を取得し、セットする。
				preferences.setMainWindowX(String.valueOf(currentStage.getX()));
				preferences.setMainWindowY(String.valueOf(currentStage.getY()));

				// 上書き保存
				Marshaller m = context.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				m.marshal(preferences, file);
			} catch (JAXBException e) {
				logger.error(e.getMessage(), e);
			}

			// ワークスペースフォルダをルートに変更
			Constant.ServerInfo.workspace = Constant.ServerInfo.workspace.substring(0,
					Constant.ServerInfo.workspace.lastIndexOf("/")) + "/";
			logger.debug("Workspace:" + Constant.ServerInfo.workspace);

			// 外部支援ツールのデータをクリアします
			ToolsData.logout();

			// アドホック画面のデータをクリアします
			AdhocData.logout();

			currentStage.hide();
			currentStage = null;
			primaryStage = new Stage();
			new W01SelectServerController().start(primaryStage);
		}
	}

	/** ツールバー(HBox)内の外部支援ツールのインデックス */
	public static final int toolsIndex = 2;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO ログインユーザの権限チェック
		if (true) {
		}

		// TODO:ユーザ名（FullName）をセット
		// ログインユーザ名をセット
		loginUserName.setText(serverInfo.getUserName());

		// Role取得のAPIが実行可能な場合(ROLE_ADMINISTRATORを含むユーザの場合)、外部支援ツールのボタンをツールバーに表示する。
		boolean administratorFlag = false;
		try {
			List<ClientRole> roles = ExecuteAPIService.getRole();
			for (ClientRole role : roles) {
				if (role.getName().equals("ROLE_ADMINISTRATOR")) {
					administratorFlag = true;
					break;
				}
			}
		} catch (AccessDeniedException e) {
			administratorFlag = false;
		}

		if (administratorFlag)
			hbox.getChildren().set(toolsIndex, tools);
		else
			hbox.getChildren().remove(toolsIndex);

	}

}
