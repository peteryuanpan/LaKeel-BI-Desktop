package com.legendapl.lightning.tools.data;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Parent;

/**
 * アドホック画面のデータ</br>
 * </br>
 *　アドホック画面データをログアウトまで保存するため、一部のデータをここで保存します。
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @author panyuan
 * @since 2018.03.06
 *
 */
public class AdhocData {
	
	public static final Map<String, Parent> roots = new HashMap<String, Parent>();
	
	/**
	 * ログアウト。データをクリアします
	 */
	public static void logout() {
		roots.clear();
	}
}
