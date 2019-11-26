package com.legendapl.lightning;

import java.io.File;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.controller.C100AdhocBaseAnchorPane;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.adhoc.service.XMLTransferService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;

public class P121AdhocAnchorPaneMain extends Application {
	
	private Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args) throws JRException {
		launch(args);
	}

	@Override public void start(Stage primaryStage) throws Exception {
		try {
			File file = new File("adhoc/adhoc_domain.xml");
			Topic topic = XMLTransferService.loadTopicFromFile(file);
			topic.setOthers();
			Adhoc adhoc = new Adhoc(topic);
			adhoc.setOthers();
			adhoc.setTopicName(file.getName());
			ShareDataService.share(adhoc);
			
			C100AdhocBaseAnchorPane.adhocStage = primaryStage;
			AnchorPane pane = (AnchorPane) FXMLLoader.load(
					getClass().getResource("/view/P121AdhocAnchorPane.fxml"),
					ResourceBundle.getBundle("AdhocBundleMessage"));
			Scene scene = new Scene(pane);
			primaryStage.setScene(scene);
			primaryStage.setTitle("LaKeel BI for Desktop");
			primaryStage.getIcons().add(new Image("/images/LightningIcon.png"));
			primaryStage.setMinWidth(AdhocConstants.Graphic.ADHOC_STAGE_MIN_WIDTH);
			primaryStage.setMinHeight(AdhocConstants.Graphic.ADHOC_STAGE_MIN_HEIGHT);
			primaryStage.setMaximized(true);
			primaryStage.show();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}

}
