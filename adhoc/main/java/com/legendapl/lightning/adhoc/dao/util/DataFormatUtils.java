package com.legendapl.lightning.adhoc.dao.util;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.TableField;

public class DataFormatUtils {

	public static String parseGroupString(List<Object> datas, List<TableField> groups) {
		List<String> dataString = new ArrayList<>();
		for(int i=0;i<datas.size();i++) {
			Object data = datas.get(i);
			if(data != null) {
				dataString.add(groups.get(i).getDataFormat().parse(data));
			} else {
				dataString.add("");
			}
		}
		String result = dataString.toString();
		return result.substring(1, result.length()-1);
	}
}
