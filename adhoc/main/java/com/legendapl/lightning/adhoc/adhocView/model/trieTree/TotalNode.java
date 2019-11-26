package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import com.legendapl.lightning.adhoc.common.AdhocUtils;

public class TotalNode implements Comparable<Object>{

	@Override
	public int compareTo(Object o) {
		// TODO : if o instanceof TotalNode ?
		return 0;
	}

	@Override
	public String toString() {
		return AdhocUtils.getString("P121.crossTable.total");
	}

}
