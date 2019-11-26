
package com.legendapl.lightning.tools.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.legendapl.lightning.tools.common.Constants;

/**
 * 設定画面のコントローラクラス
 * 
 * @author
 * @since
 *
 */
public class P89SettingAnchorPane extends C80ToolsMenuPane {
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		currentToolName = Constants.SETTING;
	}
}
