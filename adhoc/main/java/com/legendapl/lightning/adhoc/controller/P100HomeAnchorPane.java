package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.legendapl.lightning.adhoc.common.AdhocConstants;


/**
 * データを読み込む画面のコントローラクラスです。
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class P100HomeAnchorPane extends C100AdhocMenuPane {

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		currentFieldName = AdhocConstants.MenuType.HOME;
		logger.info("Management page: " + currentFieldName);
	}
}
