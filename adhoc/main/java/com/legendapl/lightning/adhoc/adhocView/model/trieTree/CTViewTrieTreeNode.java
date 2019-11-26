package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.service.AlertWindowService;

/**
 * 単行データの中で数字の値を格納する辞書ツリーノード
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings("rawtypes")
public class CTViewTrieTreeNode extends TrieTreeNode{

	private Map<CrossTableField, CalculatedNumber> measure2Value = new HashMap<CrossTableField, CalculatedNumber>();

	public CTViewTrieTreeNode(Comparable name) {
		super(name);
	}

	public CTViewTrieTreeNode() {
		super();
	}

	public CalculatedNumber getColumnCellValue(CrossTableField measure) {
		return measure2Value.get(measure);
	}

	public static TrieTreeNode getInstance() {
		return new CTViewTrieTreeNode();
	}

	protected TrieTreeNode getInstance(Comparable name) {
		return new CTViewTrieTreeNode(name);
	}

	private void merge(Map<CrossTableField, CalculatedNumber> measureMap) {
		for(CrossTableField key: measureMap.keySet()) {
			CalculatedNumber format = measureMap.get(key);
			if(format != null) {
				CalculatedNumber keyNumber = measure2Value.get(key);
				if(keyNumber == null) {
					try {
						keyNumber = (CalculatedNumber) format.clone();
						measure2Value.put(key, keyNumber);
					} catch (CloneNotSupportedException e) {
						logger.error(e.getMessage(), e);
						AlertWindowService.showError("merge failed", e.getMessage());
					}
				} else {
					keyNumber.add(format);
				}
			}
		}
	}

	@Override
	public void combineData(TrieTreeNode root) {
		measure2Value = ((CTViewTrieTreeNode) root).measure2Value;
	}

	@Override
	public void merge(List<Comparable> columns, Map<CrossTableField, CalculatedNumber> measureMap) {
		merge(measureMap);
	}

}
