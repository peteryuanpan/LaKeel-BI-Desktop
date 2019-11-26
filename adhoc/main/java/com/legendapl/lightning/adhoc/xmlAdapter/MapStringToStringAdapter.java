package com.legendapl.lightning.adhoc.xmlAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MapStringToStringAdapter extends XmlAdapter<MapStringToStringElement[], Map<String, String>>  {

	@Override
	public Map<String, String> unmarshal(MapStringToStringElement[] elements) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		for (MapStringToStringElement element : elements) {
			map.put(element.getKey(), element.getValue());
		}
		return map;
	}

	@Override
	public MapStringToStringElement[] marshal(Map<String, String> map) throws Exception {
		MapStringToStringElement[] elements = new MapStringToStringElement[map.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			elements[i] = new MapStringToStringElement(entry.getKey(), entry.getValue());
			i = i + 1;
		}
		return elements;
	}

}

class MapStringToStringElement {
	
	private String key;
	private String value;
	  
	public MapStringToStringElement() { //Required by JAXB
		key = new String();
		value = new String();
	}

	public MapStringToStringElement(String key, String value){
		this.key = key;
		this.value = value;
	}

	@XmlAttribute(name = "key")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@XmlAttribute(name = "value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
