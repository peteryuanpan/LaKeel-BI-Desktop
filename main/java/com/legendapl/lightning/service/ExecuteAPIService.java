package com.legendapl.lightning.service;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.dto.authority.RolesListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.dto.resources.ClientInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientListOfValues;
import com.jaspersoft.jasperserver.dto.resources.ClientQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableFile;
import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.roles.RolesParameter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.resources.ResourceSearchParameter;
import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.BadRequestException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jaspersoft.jasperserver.jaxrs.client.core.operationresult.OperationResult;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.API;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.model.DataSourceImpl;
import com.legendapl.lightning.model.ServerInformation;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * JasperReports ServerのREST APIを実行するサービスクラス
 * Jaspersoftからリリースされているjrs-rest-java-clientというライブラリを使用しています。
 * https://github.com/Jaspersoft/jrs-rest-java-client
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
public class ExecuteAPIService {

	protected static ResourceBundle messageRes = ResourceBundle.getBundle("messages");
	protected static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

	// REST Clientの設定を定義する。(プロパティファイルの読み込み)
	protected static RestClientConfiguration rest = RestClientConfiguration
			.loadConfiguration(Constant.ServerInfo.property);
	// REST Clientをインスタンス化
	protected static JasperserverRestClient client = new JasperserverRestClient(rest);

	// サーバ情報
	protected static ServerInformation serverInformation;

	/**
	 * API実行時の接続先を設定
	 * 
	 * @param serverInfo
	 */
	public static void setClientConfiguration(ServerInformation serverInfo) {
		serverInformation = serverInfo;
		rest.setJasperReportsServerUrl(serverInformation.getUrl());
		logger.debug(serverInformation.getUrl());
	}

	/**
	 * path配下のフォルダ一覧を取得するメソッド
	 * 
	 * @param path
	 * @return clientResourceListWrapper
	 */
	public static ClientResourceListWrapper getDirectory(String path) {

		try {
			OperationResult<ClientResourceListWrapper> result = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password) // username/password
					.resourcesService() // APIの種類を指定(resourceサービス)
					.resources().parameter(ResourceSearchParameter.FOLDER_URI, path) // フォルダのURIを指定
					.parameter(ResourceSearchParameter.RECURSIVE, Constant.API.FALSE) // RECURSIVEをfalseに指定(子の階層はフェッチしない)
					.parameter(ResourceSearchParameter.TYPE, Constant.API.FOLDER) // 取得するリソースのタイプ
					.parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT).search(); // 実行
			ClientResourceListWrapper clientResourceListWrapper = result.getEntity();
			return clientResourceListWrapper; // OperationResultから結果を取得
		} catch (ResourceNotFoundException e) {
			logger.error(messageRes.getString(Error.ERROR_P02_02), e);
			return null;
		}
	}

	/**
	 * path配下のレポートリソース一覧を取得するメソッド
	 * 
	 * @param path
	 * @return clientResourceListWrapper
	 */
	public static ClientResourceListWrapper getReportUnit(String path) {

		OperationResult<ClientResourceListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password) // username/password
				.resourcesService() // APIの種類を指定(resourceサービス)
				.resources().parameter(ResourceSearchParameter.FOLDER_URI, path) // フォルダのURIを指定
				.parameter(ResourceSearchParameter.RECURSIVE, Constant.API.FALSE) // RECURSIVEをfalseに指定(子の階層はフェッチしない)
				.parameter(ResourceSearchParameter.TYPE, Constant.API.REPORTUNIT) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT).search(); // 実行
		ClientResourceListWrapper clientResourceListWrapper = result.getEntity();
		return clientResourceListWrapper; // OperationResultから結果を取得
	}

	/**
	 * path配下のレポートリソースとフォルダ一覧を取得するメソッド
	 * 
	 * @param path
	 * @return clientResourceListWrapper
	 */
	public static ClientResourceListWrapper getReportUnitAndDirectory(String path) {

		OperationResult<ClientResourceListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password) // username/password
				.resourcesService() // APIの種類を指定(resourceサービス)
				.resources().parameter(ResourceSearchParameter.FOLDER_URI, path) // フォルダのURIを指定
				.parameter(ResourceSearchParameter.RECURSIVE, Constant.API.FALSE) // RECURSIVEをfalseに指定(子の階層はフェッチしない)
				.parameter(ResourceSearchParameter.TYPE, Constant.API.REPORTUNIT) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constant.API.FOLDER) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT).search(); // 実行
		ClientResourceListWrapper clientResourceListWrapper = result.getEntity();
		return clientResourceListWrapper; // OperationResultから結果を取得
	}

	/**
	 * ClientReportUnitを取得するメソッド
	 * 
	 * @param reportUri
	 * @return clientReportUnit
	 */
	public static ClientReportUnit getClientReportUnit(String reportUri) {

		ClientReportUnit clientReportUnit = new ClientReportUnit();
		OperationResult<? extends ClientReportUnit> reportUnit = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(reportUri).get(clientReportUnit.getClass());
		clientReportUnit = reportUnit.getEntity();

		return clientReportUnit;
	}

	/**
	 * データソースを取得するメソッド
	 * 
	 * @param clientReportUnit
	 * @return clientJdbcDataSource
	 */
	public static ClientJdbcDataSource getDatasource(ClientReportUnit clientReportUnit) {

		if (null == clientReportUnit.getDataSource()) {
			ClientJdbcDataSource nonDatasource = new ClientJdbcDataSource();
			return nonDatasource;
		}

		ClientJdbcDataSource clientJdbcDataSource = new ClientJdbcDataSource();
		OperationResult<? extends ClientJdbcDataSource> datasource = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(clientReportUnit.getDataSource().getUri()).get(clientJdbcDataSource.getClass());

		clientJdbcDataSource = datasource.getEntity();

		/**
		 * ver1.0ではjdbcデータソースのみの対応なので、 他のデータソースの場合は実行不可とする。
		 */
		if (null == clientJdbcDataSource.getConnectionUrl())
			return null;

		/**
		 * localhost問題に対応
		 * 
		 * APサーバ(BIサーバ)上のデータソースを参照する場合は、connectionUrlがlocalhostに
		 * なっていることが多いが、このツールではクライアントからSQLを実行するので、URLをAPサーバのIPアドレスに 置換しなくてはならない。
		 */
		if (clientJdbcDataSource.getConnectionUrl().contains("localhost")) {
			String connectionUrl = clientJdbcDataSource.getConnectionUrl().replace("localhost",
					serverInformation.getAddress());
			clientJdbcDataSource.setConnectionUrl(connectionUrl);
		}

		return clientJdbcDataSource;
	}

	/**
	 * BIサーバ上の全てのデータソースを取得するメソッド
	 * 
	 * @param String
	 *            workspace
	 * @return boolean
	 */
	public static boolean getAllDatasource(String workspace, DataSourceService dao) {

		try {
			// BIサーバ内のデータソースのuriをすべて取得
			OperationResult<ClientResourceListWrapper> result = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password) // username/password
					.resourcesService() // APIの種類を指定(resourceサービス)
					.resources().parameter(ResourceSearchParameter.FOLDER_URI, "/") // フォルダのURIを指定
					.parameter(ResourceSearchParameter.RECURSIVE, Constant.API.TRUE) // RECURSIVEをtrueに指定(子の階層までフェッチする)
					.parameter(ResourceSearchParameter.TYPE, Constant.API.JDBCDATASOURCE) // 取得するリソースのタイプ
					.parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT).search(); // 実行
			List<ClientResourceLookup> list = result.getEntity().getResourceLookups();

			// 取得したuriからBIサーバ内のデータソース(ClientJdbcDataSource)をすべて取得
			for (int i = 0; i < list.size(); i++) {
				ClientJdbcDataSource clientJdbcDataSource = new ClientJdbcDataSource();
				OperationResult<? extends ClientJdbcDataSource> datasource = client
						.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
						.resource(list.get(i).getUri()).get(clientJdbcDataSource.getClass());
				clientJdbcDataSource = datasource.getEntity();

				// localhost問題に対応
				if (clientJdbcDataSource.getConnectionUrl().contains("localhost")) {
					String connectionUrl = clientJdbcDataSource.getConnectionUrl().replace("localhost",
							serverInformation.getAddress());
					clientJdbcDataSource.setConnectionUrl(connectionUrl);
				}

				// 独自のエンティティクラスにデシリアライズする
				String driverClass = clientJdbcDataSource.getDriverClass();
				if (driverClass.equals(Constant.Driver.COM_MYSQL_JDBC)
						|| driverClass.equals(Constant.Driver.ORG_MARIADB_JDBC)
						|| driverClass.equals(Constant.Driver.COM_MICROSOFT_SQLSERVER_JDBC)
						|| driverClass.equals(Constant.Driver.ORACLE_JDBC)
						|| driverClass.equals(Constant.Driver.TIBCOSOFTWARE_JDBC_ORACLE)
						|| driverClass.equals(Constant.Driver.TIBCOSOFTWARE_JDBC_SQLSERVER)) {

					// サーバからデータソース情報を更新
					DataSourceImpl dataSourceImpl = new DataSourceImpl();
					dataSourceImpl.setCreateDate(clientJdbcDataSource.getCreationDate());
					dataSourceImpl.setUpdateDate(clientJdbcDataSource.getUpdateDate());
					dataSourceImpl.setUrl(clientJdbcDataSource.getConnectionUrl());
					dataSourceImpl.setDriver(clientJdbcDataSource.getDriverClass());
					dataSourceImpl.setDescription(clientJdbcDataSource.getDescription());
					dataSourceImpl.setLabel(clientJdbcDataSource.getLabel());
					dataSourceImpl.setVersion(clientJdbcDataSource.getVersion().toString());
					dataSourceImpl.setTimezone(clientJdbcDataSource.getTimezone());
					dataSourceImpl.setUsermame(clientJdbcDataSource.getUsername());
					dataSourceImpl.setDataSourcePath(
							clientJdbcDataSource.getUri().substring(0, clientJdbcDataSource.getUri().lastIndexOf("/")));
					dataSourceImpl.setName(clientJdbcDataSource.getUri()
							.substring(clientJdbcDataSource.getUri().lastIndexOf("/") + 1));

					// ディレクトリ作成
					String filePath = workspace + dataSourceImpl.getDataSourcePath();
					new File(filePath).mkdirs();

					// ローカルファイルを取得
					String fileName = dataSourceImpl.getName() + ".xml";
					File file = new File(filePath, fileName);
					dataSourceImpl.setLocalFileUrl(file.getPath());

					// ファイル保存
					if (!file.exists()) {
						saveDataSource(dataSourceImpl, file);

					} else {
						// ローカルとサーバの更新時間を取得
						LocalDataSourceService localDataSourceService = new LocalDataSourceService(file);
						String updateDate = localDataSourceService.get(Constant.DataSourceTag.updateDate);
						Date dateLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(updateDate);
						Date dateServer = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
								.parse(dataSourceImpl.getUpdateDate());
						logger.debug("file: " + filePath + "/" + fileName);
						logger.debug("dateLocal:  " + dateLocal);
						logger.debug("dateServer: " + dateServer);

						// ローカルとサーバーの更新時間を比較
						if (dateLocal != null && dateLocal.before(dateServer)) {
							String password = localDataSourceService.get(Constant.DataSourceTag.connectionPassword);
							if (password != null && !password.isEmpty()) {
								dataSourceImpl.setPassword(password);
								saveDataSource(dataSourceImpl, dao);
							} else {
								saveDataSource(dataSourceImpl, file);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	/**
	 * パスワードを設定しないデータソースを保存する<br>
	 * 
	 * @param dataSourceImpl
	 * @param file
	 * @author panyuan
	 */
	private static void saveDataSource(DataSourceImpl dataSourceImpl, File file) {
		try {
			JAXBContext context = JAXBContext.newInstance(dataSourceImpl.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(dataSourceImpl, file);
			logger.info("Saved " + dataSourceImpl.getLocalFileUrl() + ".");

		} catch (Exception e) {
			logger.error("Save dataSource failed.");
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * パスワードを設定したデータソースを保存する<br>
	 * 
	 * @param dataSourceImpl
	 * @param dao
	 * @author panyuan
	 */
	private static void saveDataSource(DataSourceImpl dataSourceImpl, DataSourceService dao) {
		try {
			dataSourceImpl.setDirty(true);
			dao.saveDataSources(Arrays.asList(dataSourceImpl));
			logger.info("Saved " + dataSourceImpl.getLocalFileUrl() + " encrypted.");

		} catch (Exception e) {
			logger.error("Save dataSource encrypted failed.");
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * jrxmlファイルをダウンロードし、パスを返すメソッド
	 * 
	 * @param clientReportUnit
	 * @return String
	 */
	public static String getJrxml(ClientReportUnit clientReportUnit) {

		OperationResult<InputStream> jrxml = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(clientReportUnit.getJrxml().getUri()).downloadBinary();
		InputStream inputStream = jrxml.getEntity();

		// ディレクトリ作成
		String filePath = Constant.Application.WORK_FILE_PATH + "/" + Constant.ServerInfo.serverName
				+ clientReportUnit.getUri();
		File directory = new File(filePath);
		directory.mkdirs();
		// ファイル保存
		int index = clientReportUnit.getJrxml().getUri().lastIndexOf("/") + 1;
		String fileName = clientReportUnit.getJrxml().getUri().replace("_jrxml", ".jrxml").substring(index);
		File file = new File(filePath, fileName);

		try {
			// Files.copy(inputStream, file.toPath(), REPLACE_EXISTING);

			BufferedReader b_reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			PrintWriter p_writer = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)));
			String s;
			while ((s = b_reader.readLine()) != null) {
				p_writer.println(new String(s.getBytes(), StandardCharsets.UTF_8));
			}
			p_writer.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return filePath + "/" + fileName;
	}

	/**
	 * 帳票が依存しているリソースファイルを取得するメソッド。 サブレポートの場合、取得したリソース(jrxml)にもリソースが含まれている場合を考慮し、
	 * replacePathメソッドを再帰的に実行する。
	 * 
	 * @param clientReportUnit
	 * @param localJrxmlPath
	 * @return
	 * @throws Exception
	 */
	public static void getResourceFile(ClientReportUnit clientReportUnit, String localJrxmlPath) throws Exception {

		List<String> jrxmlList = new ArrayList<String>();
		jrxmlList = replacePath(clientReportUnit, localJrxmlPath, "main");

		for (String str : jrxmlList) {
			replacePath(null, str, "sub");
		}

	}

	/**
	 * 帳票が依存しているリソースファイルをダウンロードするメソッド。
	 * jrxmlを編集し、リソースのパスをBIサーバのリソースID(uri)からローカルのパスへ書き換える。
	 * 
	 * 例: <br>
	 * before: CDATA["/public/photo.png"]<br>
	 * after: CDATA["C:/workspace/public/photo.png"]
	 * 
	 * 
	 * @param clientReportUnit
	 * @param localJrxmlPath
	 * @return jrxmlList
	 * @throws Exception
	 */
	public static List<String> replacePath(ClientReportUnit clientReportUnit, String localJrxmlPath, String type)
			throws Exception {

		// repo構文のリソースを取得する。
		Map<String, ClientReferenceableFile> map = null;
		if (null != clientReportUnit)
			map = clientReportUnit.getFiles();

		// 戻り値となるjrxmlファイルのリスト
		List<String> jrxmlList = new ArrayList<String>();

		// メインjrxmlの保存ディレクトリ
		String dirPath = localJrxmlPath.substring(0, localJrxmlPath.lastIndexOf("/") + 1);

		try {
			// メインjrxmlの読み込み
			FileInputStream main_jrxml = new FileInputStream(localJrxmlPath);
			InputStreamReader inputStreamReader = new InputStreamReader(main_jrxml, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			// リソースを取得するAPIの実行結果
			OperationResult<InputStream> operationResult;
			// operationResultから取得するリソースのバイナリ
			InputStream inputStream;
			// リソースのファイル名
			String fileName = null;
			// リソースがjrxml(サブレポート)の場合.jasperにコンパイルした後のファイル名
			String jasperFileName = null;
			// リソースを保存するファイルパス
			String filePath;
			File directory;
			File file;

			// jrxmlの内容を全て格納する変数
			String textData = "";
			// jrxmlの1行分が格納される変数
			String line_buffer = "";
			// 正規表現パターン
			String regex = "CDATA\\[\\\"(.+)\\\"\\]";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher;
			// jrxmlに含まれるリソースのパス
			String resourcePath;

			/**
			 * ライセンス設定
			 * 
			 * jrxmlのコンパイル時に使用する。 <br>
			 * サブレポートの場合、jrxmlをあらかじめ.jasperにコンパイルしなくてはならないのでライセンスが必要。
			 */
			DefaultJasperReportsContext.getInstance().setProperty("com.jaspersoft.jasperreports.license.location",
					System.getProperty("user.dir") + "\\jasperserver.license");

			while (null != (line_buffer = bufferedReader.readLine())) {
				// 読み込んだ1行を正規表現にマッチするかチェックする
				matcher = pattern.matcher(line_buffer);
				if (matcher.find()) {

					// 該当の1行の属性がリソース(サブレポート、画像)であるかチェックする
					boolean resourceFlag = false;
					for (int i = 0; i < API.RESOURCEEXPRESSIONS.length; i++) {
						if (line_buffer.contains(API.RESOURCEEXPRESSIONS[i])) {
							resourceFlag = true;
							break;
						}
					}

					if (resourceFlag) {
						try {
							resourcePath = matcher.group(1);

							// repo: を含む場合は消去
							if (resourcePath.contains("repo:"))
								resourcePath = resourcePath.replace("repo:", "");

							// 正規表現抽出した文字列をリソースとしてAPIを実行
							operationResult = client
									.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
									.resourcesService().resource(resourcePath).downloadBinary();
							inputStream = operationResult.getEntity();

							// ファイル名取得
							fileName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);

							// ディレクトリ作成
							filePath = Constant.ServerInfo.workspace
									+ resourcePath.substring(0, resourcePath.lastIndexOf("/") + 1);
							directory = new File(filePath);
							directory.mkdirs();

							// ローカルのパスに書き換えるため、repo:を切り取る
							line_buffer = line_buffer.replace("repo:", "");

							if (line_buffer.contains(API.RESOURCEEXPRESSIONS[0])) {

								// リソースがjrxmlの場合、.が_にエスケープされているので、置換する。
								fileName = fileName.replace("_jrxml", ".jrxml");

								// 拡張子(_jrxml)を含まない場合.jrxmlを追加(202:/public/LakeelBI/Report/JRPT_MIS_000を参照)
								if (!fileName.contains("jrxml"))
									fileName += ".jrxml";

								file = new File(filePath, fileName);
								Files.copy(inputStream, file.toPath(), REPLACE_EXISTING);

								// jrxmlのリストに追加
								jrxmlList.add(file.toPath().toString());

								jasperFileName = fileName.replace("jrxml", "jasper");

								// リソースのパス(BI
								// Server上)をダウンロードしたリソースのパス(ローカル)に置換する。
								line_buffer = line_buffer.replace(resourcePath, filePath + jasperFileName);

							} else {
								file = new File(filePath, fileName);
								Files.copy(inputStream, file.toPath(), REPLACE_EXISTING);

								line_buffer = line_buffer.replace(resourcePath, filePath + fileName);

							}

						} catch (ResourceNotFoundException e) {
							// logger.error(e.getMessage(),e);
						} catch (BadRequestException e) {
							// logger.error(e.getMessage(),e);
						}
					}
				}
				textData += line_buffer + "\n";
			}

			bufferedReader.close();
			inputStreamReader.close();
			main_jrxml.close();

			/**
			 * リソースがAPIで取得できる場合の処理
			 * 
			 */
			if (null != map) {
				try {
					// リソースファイルをすべて保存する
					for (Map.Entry<String, ClientReferenceableFile> e : map.entrySet()) {
						operationResult = client
								.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
								.resourcesService().resource(e.getValue().getUri()).downloadBinary();
						inputStream = operationResult.getEntity();

						// リソースファイルを保存
						fileName = e.getValue().getUri().substring(e.getValue().getUri().lastIndexOf("/") + 1);
						file = new File(dirPath, fileName);
						Files.copy(inputStream, file.toPath(), REPLACE_EXISTING);

						if (fileName.contains("jrxml")) {

							// jrxmlのリストに追加
							jrxmlList.add(file.toPath().toString());

							jasperFileName = fileName.replace("jrxml", "jasper");

							/**
							 * リソースのパスが記載されている箇所をローカルのリソースファイルの保存先に置換する。 <br>
							 * <br>
							 * リソースの該当箇所に[repo:]を含む場合、含まない場合やパスを含む場合、含まない場合がある。
							 * <br>
							 * jrxmlに記載されているリソースの例:<br>
							 * 1: CDATA["repo:public/test/hoge.jrxml"]<br>
							 * 2: CDATA["repo:hoge.jrxml"]<br>
							 * 3: CDATA["hoge.jrxml"]
							 * 
							 */

							// repo: を含み、パスも含む場合
							textData = textData.replace("repo:" + e.getValue().getUri(), dirPath + jasperFileName);
							// repo: を含み、パスは含まない場合
							textData = textData.replace("repo:" + fileName, dirPath + jasperFileName);
							// repo: を含まず、パスも含まない場合
							textData = textData.replace(fileName, dirPath + jasperFileName);
						} else {
							textData = textData.replace("repo:" + e.getValue().getUri(), dirPath + fileName);
							textData = textData.replace("repo:" + fileName, dirPath + fileName);
							if (textData.contains("\"" + fileName))
								textData = textData.replace(fileName, dirPath + fileName);
						}

					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

			// 書き換えた変数:textDataでメインJrxmlを上書きする
			File newJrxml = new File(localJrxmlPath);
			FileWriter fileWriter = new FileWriter(newJrxml);
			fileWriter.write(textData);
			fileWriter.close();

			if (type.equals("sub")) {
				// .jasperにコンパイル
				try {
					jasperFileName = localJrxmlPath.replace("jrxml", "jasper");
					JasperCompileManager.compileReportToFile(localJrxmlPath, jasperFileName);
				} catch (JRException e) {
					// logger.error(e.getMessage(), e);
					throw new Exception(e);
				}
			}

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}

		return jrxmlList;

	}

	/**
	 * 組み込みパラメータ[LoggedInUserRoles]の値を返すメソッド
	 * 
	 * @return RolesListWrapper
	 */
	public static RolesListWrapper getLoggedInUserRoles() {
		OperationResult<RolesListWrapper> operationResult = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService().allRoles()
				.param(RolesParameter.USER, Constant.ServerInfo.userName).get();

		return operationResult.getEntity();
	}

	/**
	 * 帳票に含まれるすべての入力コントロールを取得し、 パラメータ名とパラメータタイプのMapで返すメソッド
	 * 
	 * @param clientReportUnit
	 * @return LinkedHashMap<String, ClientInputControl>
	 */
	public static Map<String, ClientInputControl> getInputControlType(ClientReportUnit clientReportUnit) {

		// パラメータ名リスト
		List<String> parameterList = new ArrayList<String>();
		// <パラメータ名, パラメータタイプ>のHashMap
		Map<String, ClientInputControl> parameterTypeMap = new LinkedHashMap<String, ClientInputControl>();

		String parameterName = null;
		// マップとリストにパラメータ名を格納する
		for (int i = 0; i < clientReportUnit.getInputControls().size(); i++) {
			parameterName = clientReportUnit.getInputControls().get(i).getUri()
					.substring(clientReportUnit.getInputControls().get(i).getUri().lastIndexOf("/") + 1);
			parameterList.add(parameterName);
			parameterTypeMap.put(parameterName, null);
		}

		// 入力コントロールの取得(定義された入力コントロールの数だけループする)
		for (int i = 0; i < clientReportUnit.getInputControls().size(); i++) {
			// 基幹となる入力コントロールのクラスにデシリアライズ
			ClientInputControl clientInputControl = new ClientInputControl();
			OperationResult<? extends ClientInputControl> inputControl = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
					.resource(clientReportUnit.getInputControls().get(i).getUri()).get(clientInputControl.getClass());
			clientInputControl = inputControl.getEntity();

			switch (clientInputControl.getType()) {
			/** 単一チェックコンポーネント */
			case 1:
				break;
			/** 入力コンポーネント */
			case 2:
				ClientDataType clientDataType = new ClientDataType();
				OperationResult<? extends ClientDataType> dataType = client
						.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
						.resource(inputControl.getEntity().getDataType().getUri()).get(clientDataType.getClass());
				clientDataType = dataType.getEntity();

				// ClientInputControlクラスにセットする
				clientInputControl.setDataType(clientDataType);
				break;

			/** 選択コンポーネント */
			case 3:
			case 6:
			case 8:
			case 10:
				ClientListOfValues clientListOfValues = new ClientListOfValues();
				OperationResult<? extends ClientListOfValues> listOfValues = client
						.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
						.resource(inputControl.getEntity().getListOfValues().getUri())
						.get(clientListOfValues.getClass());
				clientListOfValues = listOfValues.getEntity();

				// ClientInputControlクラスにセットする
				clientInputControl.setListOfValues(clientListOfValues);
				break;

			/** 選択コンポーネント(SQL) */
			case 4:
			case 7:
			case 9:
			case 11:
				ClientQuery clientQuery = new ClientQuery();
				OperationResult<? extends ClientQuery> query = client
						.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
						.resource(inputControl.getEntity().getQuery().getUri()).get(clientQuery.getClass());
				clientQuery = query.getEntity();

				// ClientInputControlクラスにセットする
				clientInputControl.setQuery(clientQuery);
				break;
			}

			parameterTypeMap.put(parameterList.get(i), clientInputControl);

		}

		return parameterTypeMap;
	}

	public static void getInputControl(ClientReportUnit clientReportUnit,
			LinkedHashMap<String, Byte> parameterTypeMap) {

	}

}
