package com.legendapl.lightning.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;
import com.legendapl.lightning.common.crypt.CryptUtil;

/**
 * ローカルのデータソースファイル(.xml)から情報を取得する
 * 
 * @author Legend Applications, LaKeel BI development team.
 * @author panyuan
 * @since 2018.01.05
 */
public class LocalDataSourceService {
	
	private Logger logger = Logger.getLogger(getClass());
	
	private File localFile;
	
	public LocalDataSourceService(ClientJdbcDataSource clientJdbcDataSource) {
		String serverName = ServerInfo.workspace.substring(ServerInfo.workspace.lastIndexOf("/") + 1);
		this.localFile = new File(
				Constant.Application.DATA_SOURCE_FILE_PATH + serverName + clientJdbcDataSource.getUri() + ".xml");
	}
	
	public LocalDataSourceService(File localFile) {
		this.localFile = localFile;
	}

	/**
	 * 情報を取得
	 * 
	 * @param tag (Constant.DataSourceTag.XXX)
	 * @return information
	 */
	@SuppressWarnings("resource")
	public String get(String tag) {
		
		if (null == localFile) {
			logger.warn("localFile is null.");
			return null;
		} else if (!localFile.exists()) {
			logger.warn("localFile not existed.");
			return null;
		}

		try {
			// データソースの定義ファイル(xml)を取得する
			FileReader filereader = new FileReader(localFile);
			BufferedReader br = new BufferedReader(filereader);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append(System.lineSeparator());
			}
			if (sb.length() > 0) { // 最後の改行を削除する
				int l = sb.lastIndexOf(System.lineSeparator());
				sb.delete(l, sb.length());
			}
			
			String datasourceXML;
			if (sb.toString().trim().startsWith("<?xml")) { // 設定ファイルは平文？
				datasourceXML = sb.toString();
			} else {
				CryptUtil cryptUtil = new CryptUtil(Constant.Application.XML_CRYPT_PASSWORD);
				datasourceXML = cryptUtil.deryptByAES(sb.toString());
			}

			// データソースの定義ファイル(xml)から情報を抽出する
			String regex = "<"+tag+">(.+)</"+tag+">";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(datasourceXML);
			if (matcher.find()) {
				return matcher.group(1);
			} else {
				return null;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
