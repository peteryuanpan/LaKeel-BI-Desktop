package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.ColumnSearchTrack;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.common.CalculateType;

/**
 * 単行データの中で数字の値を格納する辞書ツリー
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings("rawtypes")
public class CTViewTrieTree extends TrieTree<CTViewTrieTreeNode>{

	public CTViewTrieTree() {
		super();
	}

	public String getColumnCellValue(ColumnSearchTrack columnSearchTrack) {
		CrossTableField measure = columnSearchTrack.getMeasure();
		return getColumnCellValue(columnSearchTrack, measure);
	}

	public String getColumnCellValue(ColumnSearchTrack columnSearchTrack, CrossTableField measure) {
		CTViewTrieTreeNode node = search(columnSearchTrack.getFieldNames());
		if (null == node) return "";
		CalculatedNumber result = node.getColumnCellValue(measure);
		if (null != result) return result.format();
		if (null == measure) return "";
		CalculateType calType = measure.getCalculateType();
		if (calType == CalculateType.CountAll) return "0";
		if (calType == CalculateType.CountDistinct) return "0";
		return "";
	}

	@Override
	protected CTViewTrieTreeNode getInstance() {
		return new CTViewTrieTreeNode();
	}

	@Override
	protected CTViewTrieTreeNode getInstance(Comparable name) {
		return new CTViewTrieTreeNode(name);
	}

}
