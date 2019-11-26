package com.legendapl.lightning.adhoc.recentItem;

import com.legendapl.lightning.adhoc.controller.P102LocalDataAnchorPane;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class AdhocHBoxCell extends HBox {
	
	private AdhocRecentItem adhocRecentItem;
	private Node icon;
	private Label label = new Label();
	private Label uri = new Label();
	private final String HOME_ICON_SIZE = "1.5em";
	
	public AdhocRecentItem getAdhocRecentItem() {
		return adhocRecentItem;
	}

	public void setAdhocRecentItem(AdhocRecentItem adhocRecentItem) {
		this.adhocRecentItem = adhocRecentItem;
	}

	public AdhocHBoxCell(AdhocRecentItem item) {
		
		// データを保存
		this.setAdhocRecentItem(item);
		
		//　アイコンをセット
		// TODO
		switch (item.getAdhocFromType()) {
		case DOMAIN:
		case TOPIC:
		case ADHOC:
		default:
			MaterialDesignIconView icon_adhoc = new MaterialDesignIconView(MaterialDesignIcon.ROCKET);
			icon_adhoc.setSize(HOME_ICON_SIZE);
			icon = icon_adhoc;
			break;
		}

		// 長さに応じてツールチップを追加
		if (70 < item.getReportLabel().length()) {
			label.setText(item.getReportLabel() + "...");
			Tooltip reportLabelTooltip = new Tooltip(item.getReportLabel());
			reportLabelTooltip.setMaxWidth(400);
			reportLabelTooltip.setWrapText(true);
			label.setTooltip(reportLabelTooltip);
		} else {
			label.setText(item.getReportLabel());
		}

		// urlラベルが右端に行くように、レポート名の幅を最大に拡大するように設定
		label.setMaxWidth(Double.MAX_VALUE);
		label.setPadding(new Insets(0, 0, 0, 10));
		HBox.setHgrow(label, Priority.ALWAYS);

		String fullUri = P102LocalDataAnchorPane.getUriForShow(item.getReportURI());
		uri.setText(fullUri);

		// 最大サイズとツールチップを追加
		uri.setMaxWidth(250);
		Tooltip urlTooltip = new Tooltip(fullUri);
		urlTooltip.setMaxWidth(400);
		urlTooltip.setWrapText(true);
		uri.setTooltip(urlTooltip);

		uri.setPadding(new Insets(0, 10, 0, 10));
		uri.getStyleClass().add("home-uri-label");

		this.getChildren().addAll(icon, label, uri);
	}
}
