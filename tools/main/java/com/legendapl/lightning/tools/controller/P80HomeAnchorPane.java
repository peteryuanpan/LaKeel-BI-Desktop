
package com.legendapl.lightning.tools.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.legendapl.lightning.tools.common.Constants;

/**
 * 外部支援ツール画面のコントローラクラス
 * 
 * @author
 * @since
 *
 */
public class P80HomeAnchorPane extends C80ToolsMenuPane {
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		currentToolName = Constants.HOME;
		logger.info("Management page: " + currentToolName);
	}
}
