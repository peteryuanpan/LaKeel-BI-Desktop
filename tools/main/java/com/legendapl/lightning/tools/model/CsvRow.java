package com.legendapl.lightning.tools.model;

import java.util.List;

/**
 * CSVの行のモデル
 *
 * @author LAC_楊
 * @since 2017/9/7
 */
public abstract class CsvRow {

	/**
	 * 特定の列の値を取得
	 *
	 * @param index その列のindex
	 * @return その列の値。　列が存在しない場合はnull
	 */
	public String get(int index) {
		if(index < 0 || index >= size()) return null;
		return getAllValues().get(index);
	}

	/**
	 * 特定の列の値を取得
	 *
	 * @param colName その列の名前
	 * @return その列の値。　列は存在しない場合はnull
	 */
	public String get(String colName) {
		return get(getIndex(colName));
	}


	/**
	 * 列の数を取得
	 *
	 * @return 列の数
	 */
	public int size() {
		return getHeader().size();
	}

	/**
	 * ヘッドを取得
	 *
	 * @return ヘッド
	 */
	public abstract List<String> getHeader();

	/**
	 * この行のすべての値を取得
	 *
	 * @return 値リスト
	 */
	public abstract List<String> getAllValues();

	/**
	 * 特定の列のindexを取得
	 *
	 * @param colName その列の名前
	 * @return その列のindex。　列は存在しない場合は-1
	 */
	public abstract int getIndex(String colName);

	/**
	 * この行の行番号(1から)を取得
	 *
	 * @return 行番号
	 */
	public abstract int getRowNo();
}
