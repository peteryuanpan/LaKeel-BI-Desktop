package com.legendapl.lightning.adhoc.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.common.AdhocUtils;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * このクラスは、アラートウィンドウのサービスを提供します。
 * 
 * @author Legend Applications China, LaKeel BI development team.
 * @author panyuan
 * @since 2018.02.24
 *
 */
public class AlertWindowService {
	
	protected static Logger logger = Logger.getLogger(AlertWindowService.class);
	
	public static void showExceptionMessage(String mainErrors, Exception e) {
		String detail = ExceptionMessageService.getMessage(e);
		showError(mainErrors, detail);
	}

	public static void showInfo(String mainInfo) {
		showInfo(mainInfo, (List<String>)null);
	}
	
	public static void showInfoNotInBack(String mainInfo) {
		showNotInBack(AlertType.INFORMATION, null, mainInfo, (List<String>)null);
	}
	
	public static void showInfo(String mainInfo, String... detailInfos) {
		show(AlertType.INFORMATION, null, mainInfo, detailInfos);
	}
	
	public static void showInfoNotInBack(String mainInfo, String... detailInfos) {
		showNotInBack(AlertType.INFORMATION, null, mainInfo, detailInfos);
	}

	public static void showInfo(String mainInfo, List<String> detailInfo) {
		show(AlertType.INFORMATION, null, mainInfo, detailInfo);
	}
	
	public static void showInfoNotInBack(String mainInfo, List<String> detailInfo) {
		showNotInBack(AlertType.INFORMATION, null, mainInfo, detailInfo);
	}

	public static void showError(String mainError) {
		showError(mainError, (List<String>)null);
	}
	
	public static void showErrorNotInBack(String mainError) {
		showErrorNotInBack(mainError, (List<String>)null);
	}
	
	public static void showError(String mainError, String... detailErrors) {
		show(AlertType.ERROR, null, mainError, detailErrors);
	}
	
	public static void showErrorNotInBack(String mainError, String... detailErrors) {
		showNotInBack(AlertType.ERROR, null, mainError, detailErrors);
	}

	public static void showError(String mainError, List<String> detailError) {
		show(AlertType.ERROR, null, mainError, detailError);
	}
	
	public static void showErrorNotInBack(String mainError, List<String> detailError) {
		showNotInBack(AlertType.ERROR, null, mainError, detailError);
	}
	
	public static Alert getAlertConfirm(String mainConfirm) {
		return getAlert(AlertType.CONFIRMATION, null, mainConfirm);
	}
	
	public static void showConfirm(String mainConfirm) {
		showConfirm(mainConfirm, (List<String>)null);
	}
	
	public static void showConfirmNotInBack(String mainConfirm) {
		showConfirmNotInBack(mainConfirm, (List<String>)null);
	}
	
	public static void showConfirm(String mainConfirm, String... detailConfirms) {
		show(AlertType.CONFIRMATION, null, mainConfirm, detailConfirms);
	}
	
	public static void showConfirmNotInBack(String mainConfirm, String... detailConfirms) {
		showNotInBack(AlertType.CONFIRMATION, null, mainConfirm, detailConfirms);
	}

	public static void showConfirm(String mainConfirm, List<String> detailConfirm) {
		show(AlertType.CONFIRMATION, null, mainConfirm, detailConfirm);
	}
	
	public static void showConfirmNotInBack(String mainConfirm, List<String> detailConfirm) {
		showNotInBack(AlertType.CONFIRMATION, null, mainConfirm, detailConfirm);
	}
	
	public static void showWarn(String mainWarn) {
		showWarn(mainWarn, (List<String>)null);
	}
	
	public static void showWarnNotInBack(String mainWarn) {
		showWarnNotInBack(mainWarn, (List<String>)null);
	}
	
	public static void showWarn(String mainWarn, String... detailWarns) {
		show(AlertType.WARNING, null, mainWarn, detailWarns);
	}
	
	public static void showWarnNotInBack(String mainWarn, String... detailWarns) {
		showNotInBack(AlertType.WARNING, null, mainWarn, detailWarns);
	}
	
	public static void showWarn(String mainWarn, List<String> detailWarn) {
		show(AlertType.WARNING, null, mainWarn, detailWarn);
	}
	
	public static void showWarnNotInBack(String mainWarn, List<String> detailWarn) {
		showNotInBack(AlertType.WARNING, null, mainWarn, detailWarn);
	}
	
	public static void showText(String titleInfo, String... detailTxt) {
		show(AlertType.NONE, titleInfo, null, detailTxt);
	}
	
	public static void showTextNotInBack(String titleInfo, String... detailText) {
		showNotInBack(AlertType.NONE, titleInfo, null, detailText);
	}
	
	public static void showText(String titleInfo, List<String> detailText) {
		show(AlertType.NONE, titleInfo, null, detailText);
	}
	
	public static void showTextNotInBack(String titleInfo, List<String> detailText) {
		showNotInBack(AlertType.NONE, titleInfo, null, detailText);
	}
	
	public static void show(AlertType type, String titleInfo, String mainInfo, String... detailInfos) {
		show(type, titleInfo, mainInfo, Arrays.asList(detailInfos));
	}

	public static void showNotInBack(AlertType type, String titleInfo, String mainInfo, String... detailInfos) {
		showNotInBack(type, titleInfo, mainInfo, Arrays.asList(detailInfos));
	}

	public static void show(AlertType type, String titleInfo, String mainInfo, List<String> detailInfo) {
		List<String> detailInfoTmp = new ArrayList<String>();
		if (detailInfo != null) {
			detailInfoTmp.addAll(detailInfo);
		}
		Platform.runLater(() -> {
			showImpl(type, titleInfo, mainInfo, detailInfoTmp);
		});
	}

	public static void showNotInBack(AlertType type, String titleInfo, String mainInfo, List<String> detailInfo) {
		List<String> detailInfoTmp = new ArrayList<String>();
		if (detailInfo != null) {
			detailInfoTmp.addAll(detailInfo);
		}
		showImpl(type, titleInfo, mainInfo, detailInfoTmp);
	}
	
	private static Optional<ButtonType> showImpl(AlertType type, String titleInfo, String mainInfo, List<String> detailInfo) {
		Alert alert = getAlert(type, titleInfo, mainInfo, detailInfo);
		return alert.showAndWait();
	}
	
	public static Alert getAlert(AlertType type, String titleInfo, String mainInfo, String... detailInfo) {
		return getAlert(type, titleInfo, mainInfo, Arrays.asList(detailInfo));
	}

	public static Alert getAlert(AlertType type, String titleInfo, String mainInfo, List<String> detailInfo) {
		logger.debug( MessageFormat.format (
				"Information dialogue, title : {0}, type : {1}, mainMessage : {2}, detailMessage : {3}",
				titleInfo, type, mainInfo, detailInfo
				)
		);

		Alert alert;
		if (type == AlertType.CONFIRMATION) alert = new Alert(type, "", ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);
		else if (type != AlertType.NONE) alert = new Alert(type);
		else alert = new Alert(type, "", ButtonType.OK);

		String titleText = titleInfo == null ? getTitleForShowInfo(type) : titleInfo;
		alert.setTitle(titleText);

		String headerText = mainInfo == null ? " " : mainInfo;
		alert.setHeaderText(headerText);
		if (type != AlertType.NONE) {
			GridPane gridPane = (GridPane) alert.getDialogPane().getChildren().get(0);
			GridPane headerTextPanel = new GridPane();
	        headerTextPanel.getChildren().addAll(gridPane.getChildren());
	        Label headerLabel = (Label) headerTextPanel.getChildren().get(0);
	        headerLabel.getStylesheets().add("view/application.css");
	        headerLabel.getStyleClass().add("alert-window-text");
	        StackPane graphicContainer = new StackPane(getImageViewForShowInfo(type));
	        headerTextPanel.add(graphicContainer, 1, 0);
	        headerTextPanel.getColumnConstraints().setAll(gridPane.getColumnConstraints());
	        headerTextPanel.getStyleClass().addAll(gridPane.getStyleClass());
	        headerTextPanel.setMaxWidth(gridPane.getMaxWidth());
	        headerTextPanel.setVisible(true);
	        headerTextPanel.setManaged(true);
			alert.getDialogPane().setHeader(headerTextPanel);
		}

		if (detailInfo != null) {
			String detailText = getDetailText(type, detailInfo);
			if (detailText != null && !detailText.isEmpty()) {
				TextArea textArea = new TextArea(detailText);
				textArea.setEditable(false);
				textArea.setWrapText(true);
				textArea.getStylesheets().add("view/application.css");
				textArea.getStyleClass().add("alert-window-text");
		        alert.getDialogPane().setContent(textArea);
			}
		}
		
		if (type == AlertType.CONFIRMATION) {
			ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().getChildren().get(2);
			ObservableList<Node> buttonList = buttonBar.getButtons();
			for (Node buttonTmp : buttonList) {
				Button button = (Button) buttonTmp;
				if (button.isDefaultButton()) {
					button.setText(AdhocUtils.getString("P100.button.text.yes"));
				} else if (button.isCancelButton()) {
					button.setText(AdhocUtils.getString("P100.button.text.no"));
				} else {
					button.setText(AdhocUtils.getString("P100.button.text.cancel"));
				}
			}
		}

		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("/images/LightningIcon.png"));
		alertStage.setResizable(true);
		
		return alert;
	}

	protected static String getDetailText(AlertType type, List<String> detailInfo) {
		String detailText = new String("");
		if (detailInfo != null && !detailInfo.isEmpty()) {
			detailText = new String("");
			for (String detail : detailInfo) {
				detailText += detail;
				detailText += "\n";
			}
		}
		return detailText;
	}

	protected static String getTitleForShowInfo(AlertType type) {
		String title = new String();
		switch (type) {
		case INFORMATION:
			title = AdhocUtils.getString("ALERT_TITLE_MESSAGE");
			break;
		case ERROR:
			title = AdhocUtils.getString("ALERT_TITLE_ERROR_MESSAGE");
			break;
		case CONFIRMATION:
			title = AdhocUtils.getString("ALERT_TITLE_CONFIRM");
			break;
		case WARNING:
			title = AdhocUtils.getString("AlERT_TITLE_WARNING_MESSAGE");
			break;
		case NONE:
			// TODO: set title
			break;
		default:
			break;
		}
		return title;
	}

	protected static ImageView getImageViewForShowInfo(AlertType type) {
		ImageView imageView;
		switch (type) {
		case INFORMATION:
			imageView = new ImageView("/images/tools.dialog/dialog-information.png");
			break;
		case ERROR:
			imageView = new ImageView("/images/tools.dialog/dialog-error.png");
			break;
		case CONFIRMATION:
			imageView = new ImageView("/images/tools.dialog/dialog-confirm.png");
			break;
		case WARNING:
			imageView = new ImageView("/images/tools.dialog/dialog-warning.png");
			break;
		case NONE:
		default:
			imageView = null;
			break;
		}
		return imageView;
	}

	/**
	 * avoid multiple alert-window
	 */
	public static class SingleAlert {
		static Alert alertError;
		static Alert alertInfo;
		static Alert alertConfirm;
		static Alert alertWarn;
		public static void init() { // PLEASE RUN IN JAVAFX THREAD
			if (null == alertError) alertError = getAlert(AlertType.ERROR, null, " ", " ");
			// TODO : other
		}
		public static void showError(String mainInfo, String... detailInfo) {
			showError(mainInfo, Arrays.asList(detailInfo));
		}
		public static void showError(String mainInfo, List<String> detailInfo) {
			show(alertError, mainInfo, detailInfo);
		}
		private static void show(Alert alert, String mainInfo, List<String> detailInfo) {
			Platform.runLater(() -> {
				GridPane headerTextPanel = (GridPane) alert.getDialogPane().getHeader();
				Label headerLabel = (Label) headerTextPanel.getChildren().get(0);
				TextArea textArea = (TextArea) alert.getDialogPane().getContent();
				String beforeText = alert.isShowing() ? textArea.getText() : "";
				String detailText = getDetailText(alert.getAlertType(), detailInfo);
				if (headerLabel.getText().equals(mainInfo)) {
					if (!beforeText.contains(detailText)) { // 複数フィールドの場合、異常情報は一度だけ提示します
						detailText = beforeText + "\n" + detailText;
						textArea.setText(detailText);
					}
				} else {
					headerLabel.setText(mainInfo);
					textArea.setText(detailText);
				}
				if (!alert.isShowing()) {
					alert.showAndWait();
				}
			});
		}
	}
	
}
