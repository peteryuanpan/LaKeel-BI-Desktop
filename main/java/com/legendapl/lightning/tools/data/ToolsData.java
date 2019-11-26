package com.legendapl.lightning.tools.data;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Parent;

/**
 * 外部支援ツールのデータ</br>
 * </br>
 *　外部支援ツールのデータをログアウトまで保存するため、一部のデータをここで保存します。
 *
 * @author LAC_楊
 * @since 2017.09.22
 *
 */
public class ToolsData {
	public static final Map<String, Parent> roots = new HashMap<String, Parent>();
	
	/**
	 * ログアウト。データをクリアします
	 */
	public static void logout() {
		roots.clear();
	}
}
