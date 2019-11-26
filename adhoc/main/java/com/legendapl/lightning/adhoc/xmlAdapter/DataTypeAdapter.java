package com.legendapl.lightning.adhoc.xmlAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.legendapl.lightning.adhoc.common.DataType;

public class DataTypeAdapter extends XmlAdapter<String, DataType> {

	@Override
	public DataType unmarshal(String name) throws Exception {
		return DataType.getDataTypeByName(name);
	}

	@Override
	public String marshal(DataType dataType) throws Exception {
		return dataType.getName();
	}

}
