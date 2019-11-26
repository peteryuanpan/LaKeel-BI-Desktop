package com.legendapl.lightning.adhoc.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.crypt.CryptUtil;
import com.legendapl.lightning.model.DataSourceImpl;

/**
 * データソース情報
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.02.26
 */
@XmlType(propOrder = {"localFileUri"})
public class DatabaseInfo {

	private DataSourceImpl dataSource;

	private String localFileUri;

	protected CryptUtil crypter = null;

	public DatabaseInfo() {
		dataSource = null;
		localFileUri = null;
		crypter = null;
	}
	
	public DatabaseInfo(String localFileUri) {
		this.localFileUri = localFileUri;
	}

	@XmlTransient
	public CryptUtil getCrypter() {
		if (crypter == null) {
			crypter = CryptUtil.getInstance(Constant.Application.XML_CRYPT_PASSWORD);
		}
		return crypter;
	}
	
	public void setCrypter(CryptUtil crypter) {
		this.crypter = crypter;
	}

	@XmlAttribute(name = "localFileUri")
	public String getLocalFileUri() {
		return localFileUri;
	}

	public void setLocalFileUri(String localFileUri) {
		this.localFileUri = localFileUri;
	}

	@XmlTransient
	public DataSourceImpl getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(DataSourceImpl dataSource) {
		this.dataSource = dataSource;
	}
	
	@XmlTransient
	@SuppressWarnings("resource")
	public DataSourceImpl getDataSourceFromLocal() throws Exception {
		BufferedReader rd = new BufferedReader(new FileReader(new File(localFileUri)));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}
		if (sb.length() > 0) { // 最後の改行を削除する
			int l = sb.lastIndexOf(System.lineSeparator());
			sb.delete(l, sb.length());
		}
		// 復号化
		String decodedXMLString;

		if (sb.toString().trim().startsWith("<?xml")) { // 設定ファイルは平文？
			decodedXMLString = sb.toString();
		} else {
			decodedXMLString = getCrypter().deryptByAES(sb.toString());
		}

		StringReader srd = new StringReader(decodedXMLString);
		dataSource = JAXB.unmarshal(srd, DataSourceImpl.class);
		dataSource.setLocalFileUrl(localFileUri);
		return dataSource;
	}

}
