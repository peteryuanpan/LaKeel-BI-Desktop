package com.legendapl.lightning.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;

/**
 * データソース実装
 * 
 * @author taka
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jdbcDataSource")
public class DataSoureceWrapper extends ClientJdbcDataSource {

	private static final long serialVersionUID = 1L;

	public DataSoureceWrapper() {
		super();
	}

	public DataSoureceWrapper(ClientJdbcDataSource source) {
		super(source);
	}

}
