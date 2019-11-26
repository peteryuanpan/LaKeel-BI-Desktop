package com.legendapl.lightning.adhoc.xmlAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.DataType;

public class FilterValueAdapter extends XmlAdapter<String, Object>  {

	protected static Logger logger = Logger.getLogger(FilterValueAdapter.class);

	@Override public Object unmarshal(String value) throws Exception {
		if (null == value) return null;
		Object obj = null;
		try {
			for (DataType dataType : DataType.values()) {
				if (!dataType.equals(DataType.UNKNOW)) {
					if (value.endsWith(getStr(dataType.getAdhocName()))) {
						value = value.substring(0, value.length() - getStr(dataType.getAdhocName()).length());
						switch (dataType) {
						case STRING:
							obj = (String) value;
							break;
						case BOOLEAN:
							obj = Boolean.parseBoolean(value);
							break;
						case LONG:
							obj = Long.parseLong(value);
							break;
						case BYTE:
							obj = Byte.parseByte(value);
							break;
						case DOUBLE:
							obj = Double.parseDouble(value);
							break;
						case FLOAT:
							obj = Float.parseFloat(value);
							break;
						case INTEGER:
							obj = Integer.parseInt(value);
							break;
						case SHORT:
							obj = Short.parseShort(value);
							break;
						case BIGDECIMAL:
							obj = BigDecimal.valueOf(Double.parseDouble(value));
							break;
						case TIME:
							obj = LocalTime.parse(value);
							break;
						case TIMESTAMP:
							obj = LocalDateTime.parse(value);
							break;
						case DATE:
							obj = LocalDate.parse(value);
							break;
						default:
							throw new RuntimeException("Unknow type of object found when umarshalling Filter.class");
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return obj;
	}

	@Override
	public String marshal(Object obj) throws Exception {
		if (null == obj) return null;
		String content;
		if (obj instanceof LocalTime) {
			content = ((LocalTime)obj).format(AdhocConstants.DataTime.timeFormatter);
		} else {
			content = obj.toString();
		}
		return content + getStr(obj.getClass().getName());
	}

	private String getStr(String className) {
		return "(" + className + ")";
	}

}
