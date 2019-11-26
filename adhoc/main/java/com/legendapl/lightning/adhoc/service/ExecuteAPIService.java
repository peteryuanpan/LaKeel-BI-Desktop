package com.legendapl.lightning.adhoc.service;

import java.io.File;

import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientSemanticLayerDataSource;
import com.jaspersoft.jasperserver.jaxrs.client.core.operationresult.OperationResult;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;

/**
 * JasperReports ServerのREST APIを実行するサービスクラス<br>
 * Jaspersoftからリリースされているjrs-rest-java-clientというライブラリを使用しています。<br>
 * https://github.com/Jaspersoft/jrs-rest-java-client<br>
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class ExecuteAPIService extends com.legendapl.lightning.tools.service.ExecuteAPIService {

	@SuppressWarnings("rawtypes")
	public static DatabaseInfo getDatabase(String domainUri) {
		ClientResource domain = getClientResource(domainUri);
		if (domain instanceof ClientSemanticLayerDataSource) {
			ClientJdbcDataSource clientJdbcDataSource = getDatasource((ClientSemanticLayerDataSource) domain);
			File file = new File(
					Constant.Application.DATA_SOURCE_FILE_PATH + ServerInfo.workspace.substring(ServerInfo.workspace.lastIndexOf("/") + 1) + clientJdbcDataSource.getUri() + ".xml");
			String localFileUri = file.getAbsolutePath();
			return new DatabaseInfo(localFileUri);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static ClientResource getClientResource(String path) {
		OperationResult<ClientResource> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.resourcesService().resource(path).details();
		ClientResource resource = result.getEntity();
		return resource;
	}

	// TODO : not only for ClientJdbcData
	// TODO : test for Exceptions
	public static ClientJdbcDataSource getDatasource(ClientSemanticLayerDataSource domain) {

		if (null == domain.getDataSource()) {
			ClientJdbcDataSource nonDatasource = new ClientJdbcDataSource();
			return nonDatasource;
		}

		ClientJdbcDataSource clientJdbcDataSource = new ClientJdbcDataSource();
		OperationResult<? extends ClientJdbcDataSource> datasource = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(domain.getDataSource().getUri()).get(clientJdbcDataSource.getClass());

		clientJdbcDataSource = datasource.getEntity();

		// ver1.0ではjdbcデータソースのみの対応なので、 他のデータソースの場合は実行不可とする。
		if (null == clientJdbcDataSource.getConnectionUrl()) {
			return null;
		}

		// TODO : what about http://172.17.2.110:8080/lakeelbi/localhostxxxx...?
		// localhost問題に対応
		if (clientJdbcDataSource.getConnectionUrl().contains("localhost")) {
			String connectionUrl = clientJdbcDataSource.getConnectionUrl()
					.replace("localhost",serverInformation.getAddress());
			clientJdbcDataSource.setConnectionUrl(connectionUrl);
		}

		return clientJdbcDataSource;
	}
}
