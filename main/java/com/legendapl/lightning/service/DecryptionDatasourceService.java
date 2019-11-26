package com.legendapl.lightning.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;
import com.legendapl.lightning.common.crypt.CryptUtil;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;

/**
 * 設定されたデータソース定義ファイルを参照するサービスクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
public class DecryptionDatasourceService {

	protected ResourceBundle messageRes = ResourceBundle.getBundle("messages");
	private Logger logger = Logger.getLogger(getClass());

	/**
	 * 暗号化されたデータソース定義ファイルを復号化してパスワードを取得するメソッド
	 * 
	 * @param clientJdbcDataSource
	 * @return password
	 */
	public String getPassword(ClientJdbcDataSource clientJdbcDataSource) {

		/** サーバー名 */
		String serverName = null;

		if (null == clientJdbcDataSource)
			return null;

		try {
			serverName = ServerInfo.workspace.substring(ServerInfo.workspace.lastIndexOf("/") + 1);

			// データソースの定義ファイル(xml)を取得する
			File file = new File(
					Constant.Application.DATA_SOURCE_FILE_PATH + serverName + clientJdbcDataSource.getUri() + ".xml");
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			// Stringの文字列に定義する
			String cipher = br.readLine();
			br.close();
			// 復号化
			logger.debug("Decrypting :" + file.getAbsolutePath());
			CryptUtil cryptUtil = new CryptUtil(Constant.Application.XML_CRYPT_PASSWORD);
			String datasourceXML = cryptUtil.deryptByAES(cipher);

			// データソースの定義ファイル(xml)からパスワードを抽出する
			String regex = "<connectionPassword>(.+)</connectionPassword>";
			String password;
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(datasourceXML);
			if (matcher.find()) {
				password = matcher.group(1);
			} else {
				// 暗号化されているが、パスワードが設定されていなかった場合
				logger.error(messageRes.getString(Error.ERROR_W04_01));
				return null;
			}

			return password;

			// データソースの定義ファイルが見つからない場合
		} catch (FileNotFoundException e) {
			logger.error(messageRes.getString(Error.ERROR_W04_02) + "[" + Constant.Application.DATA_SOURCE_FILE_PATH
					+ serverName + clientJdbcDataSource.getUri() + ".xml]", e);
			return null;

			// 暗号化されておらず、パスワードが設定されていなかった場合
		} catch (IllegalArgumentException e) {
			logger.error(messageRes.getString(Error.ERROR_W04_01), e);
			return null;

		} catch (IOException e) {
			logger.error(messageRes.getString(Error.ERROR_99_HEADER), e);
			return null;
		}

	}

}
