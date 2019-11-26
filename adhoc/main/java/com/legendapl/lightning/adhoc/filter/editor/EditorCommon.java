package com.legendapl.lightning.adhoc.filter.editor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.filter.Filter;

public class EditorCommon {

	/**
	 * 次と等しいと次と等しくない場合 値が空かを判断する
	 * 
	 * @param filter
	 * @param isNull
	 * @param textString
	 * @return
	 */
	public Boolean doCheckNull(Filter filter) {
		if (doCheckOpIndex(filter)) {
			fillFilterNull(filter, "[NULL]");
			return true;
		}
		return false;
	}

	/**
	 * 次と等しいと次と等しくないを判断
	 * 
	 * @param filter
	 * @param isNull
	 * @param textString
	 * @return
	 */
	public Boolean doCheckOpIndex(Filter filter) {
		String opIndex = String.valueOf(filter.getOp().getIndex());
		// 次と等しいと次と等しくない場合かつ入力された値が空の場合
		if ("2".equals(opIndex) || "3".equals(opIndex)) {
			return true;
		}
		return false;
	}

	/**
	 * フィルタにNULLを設定する
	 * 
	 * @param filter
	 * @param isNull
	 * @return
	 */
	public Filter fillFilterNull(Filter filter, Object isNull) {
		filter.setValue(isNull);
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + isNull + "}");
		return filter;
	}

	/**
	 * 日付チェック
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public boolean isValidDate(String date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		if (date == null || date.trim().equals("")) {
			return true;
		}
		try {
			return sdf.format(sdf.parse(date)).equals(date);
		} catch (Exception e) {
			return false;
		}
	}

	public void changeBlank(List<String> distinctList) {
		List<String> nullList = new ArrayList<String>();
		Boolean nullFlg = false;
		Boolean blankFlg = false;
		for (int i = 0; i < distinctList.size(); i++) {
			// NUllある場合 「NULL」に変更
			if (distinctList.get(i) == null) {
				nullList.add(null);
				nullFlg = true;
			}
			// 空が[------]に変更
			if (distinctList.get(i) != null &&"".equals(distinctList.get(i).trim())) {
				nullList.add(distinctList.get(i));
				blankFlg = true;
			}
		}
		if (nullFlg) {
			distinctList.removeAll(nullList);
			distinctList.add("[NULL]");
		}
		if (blankFlg) {
			distinctList.removeAll(nullList);
			distinctList.add("[------]");
		}
	}

}
