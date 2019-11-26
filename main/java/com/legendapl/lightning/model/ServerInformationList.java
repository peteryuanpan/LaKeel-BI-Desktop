package com.legendapl.lightning.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.crypt.CryptUtil;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * 複数のサーバ情報を格納するクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */

@XmlRootElement
public class ServerInformationList {

	// 多言語対応バンドルファイル
	protected ResourceBundle myResource = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);
	protected Logger logger = Logger.getLogger(getClass());

	private List<ServerInformation> servers = new ArrayList<ServerInformation>();

	@XmlElementWrapper(name = "servers")
	@XmlElement(name = "server")
	public List<ServerInformation> getServers() {
		return servers;
	}

	public void setServers(List<ServerInformation> servers) {
		this.servers = servers;
	}

	public ServerInformationList() {
		super();
	}

	@Override
	public String toString() {
		return "ServerInformationList [servers=" + servers + "]";
	}

	public void clear() {
		this.servers.clear();
	}

	public void add(ServerInformation server) {
		this.servers.add(server);
	}

	public Object get(int index) {
		return this.servers.get(index);
	}

	public ServerInformation set(int index, ServerInformation element) {
		return this.servers.set(index, element);
	}

	/**
	 * サーバ情報を取得
	 * 
	 */
	public ServerInformation get(String id) {
		for (ServerInformation server : servers) {
			if (server.getId().equals(id)) {
				return server;
			}
		}
		return null;
	}

	/**
	 * サーバ情報を置換
	 * 
	 */
	public void replace(ServerInformation serverInfo) {
		for (ServerInformation server : servers) {
			if (server.getId().equals(serverInfo.getId())) {
				servers.set(servers.indexOf(server), serverInfo);
			}
		}
	}

	/**
	 * サーバ情報を削除
	 * 
	 */
	public void remove(ServerInformation serverInfo) {
		int id = -1;
		for (ServerInformation server : servers) {
			if (server.getId().equals(serverInfo.getId())) {
				id = servers.indexOf(server);
				break;
			}
		}
		if (-1 != id) {
			servers.remove(id);
		}
	}

	/**
	 * サーバリスト情報を読み込み
	 * 
	 * @param file
	 */
	public void loadPreferenceDataFromFile(File file) {

		try (BufferedReader rd = new BufferedReader(new FileReader(file))) {
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

			try (StringReader srd = new StringReader(decodedXMLString)) {
				JAXBContext context = JAXBContext.newInstance(ServerInformationList.class);
				Unmarshaller um = context.createUnmarshaller();

				ServerInformationList wrapper = (ServerInformationList) um.unmarshal(srd);

				this.setServers(wrapper.getServers());
			}

		} catch (Exception e) { // catches ANY exception
			logger.error(e);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(myResource.getString("common.error.dialog.title"));
			alert.setHeaderText(myResource.getString("main.load_error.dialog.header"));
			alert.setContentText(myResource.getString("main.load_error.dialog.body") + file.getPath());
			alert.showAndWait();
		}
	}

	private CryptUtil crypter = null;

	private CryptUtil getCrypter() {
		if (crypter == null) {
			crypter = CryptUtil.getInstance(Constant.Application.XML_CRYPT_PASSWORD);
		}
		return crypter;
	}

	/**
	 * サーバリスト情報を保存
	 */
	public void savePreferenceDataToFile() {
		savePreferenceDataToFile(new File(Constant.Application.SERVERS_FILE_PATH));
	}

	/**
	 * サーバリスト情報を保存
	 * 
	 * @param file
	 */
	public void savePreferenceDataToFile(File file) {
		try {
			// JAXBContext context =
			JAXBContext context = JAXBContext.newInstance(ServerInformationList.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// デバッグ時、Constant.Application.ENCRYPTをfalseにすることで暗号化しない状態のservers.xmlを作成
			if (!Constant.Application.ENCRYPT) {
				m.marshal(this, file);
				return;
			}

			StringWriter sw = new StringWriter();
			m.marshal(this, sw);

			Writer wr = null;
			try {
				String encryptedXMLString = getCrypter().encryptByAES(sw.toString());
				wr = new FileWriter(file);
				wr.write(encryptedXMLString);
				wr.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				if (wr != null) {
					try {
						wr.close();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

		} catch (Exception e) { // catches ANY exception
			logger.error(e.getMessage(), e);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(myResource.getString("common.error.dialog.title"));
			alert.setHeaderText(myResource.getString("main.load_error.dialog.header"));
			alert.setContentText(myResource.getString("main.load_error.dialog.body") + file.getPath());

			alert.showAndWait();
		}
	}

}
