package com.legendapl.lightning.tools.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import com.jaspersoft.jasperserver.dto.authority.ClientRole;
import com.jaspersoft.jasperserver.dto.authority.ClientTenant;
import com.jaspersoft.jasperserver.dto.authority.ClientUser;
import com.jaspersoft.jasperserver.dto.authority.OrganizationsListWrapper;
import com.jaspersoft.jasperserver.dto.authority.RolesListWrapper;
import com.jaspersoft.jasperserver.dto.authority.UsersListWrapper;
import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttribute;
import com.jaspersoft.jasperserver.dto.authority.hypermedia.HypermediaAttributesListWrapper;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermission;
import com.jaspersoft.jasperserver.dto.permissions.RepositoryPermissionListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.dto.resources.ClientSemanticLayerDataSource;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.attributes.AttributesService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.organizations.OrganizationParameter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.roles.RolesParameter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.roles.RolesService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.authority.users.UsersService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.jobs.JobsParameter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.permissions.PermissionRecipient;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.reporting.ReportOutputFormat;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.resources.ResourceSearchParameter;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AccessDeniedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.BadRequestException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jaspersoft.jasperserver.jaxrs.client.core.operationresult.OperationResult;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.Job;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobSummary;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.jaxb.wrappers.JobSummaryListWrapper;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.model.Permission;

/**
 * JasperReports ServerのREST APIを実行するサービスクラス<br>
 * Jaspersoftからリリースされているjrs-rest-java-clientというライブラリを使用しています。<br>
 * https://github.com/Jaspersoft/jrs-rest-java-client<br>
 *
 * @author LAC_楊
 * @since 2017/9/5
 */
public class ExecuteAPIService extends com.legendapl.lightning.service.ExecuteAPIService {
	
	public static List<ClientRole> getRole() {
		OperationResult<RolesListWrapper> operationResult = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService().allRoles()
				.param(RolesParameter.USER, Constant.ServerInfo.userName).get();

		RolesListWrapper rolesListWrapper = operationResult.getEntity();
		return rolesListWrapper.getRoleList();

	}

	public static InputStream organizationReport() {
		OperationResult<InputStream> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).reportingService()
				.report(Constants.P84_ORGANIZATION_REPORT_URI).prepareForRun(ReportOutputFormat.CSV)
				.parameter("Cascading_name_single_select", "A & U Stalker Telecommunications, Inc").run();
		InputStream report = result.getEntity();
		return report;
	}

	public static InputStream permissionReport() {
		OperationResult<InputStream> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).reportingService()
				.report(Constants.P84_PERMISSION_REPORT_URI).prepareForRun(ReportOutputFormat.CSV)
				.parameter("Cascading_name_single_select", "A & U Stalker Telecommunications, Inc").run();
		InputStream report = result.getEntity();
		return report;
	}

	public static ClientTenant getSingelOrganization(String orgId) {
		OperationResult<ClientTenant> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).organizationsService()
				.organization(orgId).get();
		return result.getEntity();
	}

	/**
	 * @author LAC_徐
	 * @since 2017/9/6 Get roles from Web sever
	 */
	public static List<ClientRole> getRoleUnit() {
		OperationResult<RolesListWrapper> operationResult = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService().allRoles()
				.get();

		RolesListWrapper rolesListWrapper = operationResult.getEntity();
		return rolesListWrapper.getRoleList();

	}

	/**
	 * @author LAC_徐
	 * @param organization
	 * @since 2017/9/6 Create a role on Web server
	 */
	public static boolean createRole(String roleID, String organization) {
		if (!organization.isEmpty()) {
			ClientRole roleCreate = new ClientRole().setName(roleID).setTenantId(organization);
			OperationResult<ClientRole> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.organization(organization).roleName(roleID).createOrUpdate(roleCreate);
			Response response = operationResult.getResponse();
			int status = response.getStatus();
			if (status == 200) {
				return true;
			} else
				return false;
		} else {
			ClientRole roleCreate = new ClientRole().setName(roleID);

			OperationResult<ClientRole> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.roleName(roleID).createOrUpdate(roleCreate);
			Response response = operationResult.getResponse();
			int status = response.getStatus();
			if (status == 200) {
				return true;
			} else
				return false;
		}

	}

	/**
	 * @author LAC_徐
	 * @param organization
	 * @since 2017/9/6 Modify a role on Web server
	 */
	public static boolean modifyRole(String oldName, String newName, String organization) {
		if (organization.isEmpty()) {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.roleName(oldName).get();

			try {
				client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
						.roleName(newName).get();
				throw new BadRequestException();
			} catch (ResourceNotFoundException e) {

			}

			ClientRole roleNew = new ClientRole().setName(newName);

			OperationResult<ClientRole> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.roleName(oldName).createOrUpdate(roleNew);

			Response response = operationResult.getResponse();
			int status = response.getStatus();
			if (status == 200) {
				return true;
			} else
				return false;
		} else {

			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.organization(organization).roleName(oldName).get();

			try {
				client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
						.organization(organization).roleName(newName).get();
				throw new BadRequestException();
			} catch (ResourceNotFoundException e) {

			}
			ClientRole roleNew = new ClientRole().setName(newName).setTenantId(organization);

			OperationResult<ClientRole> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.organization(organization).roleName(oldName).createOrUpdate(roleNew);

			Response response = operationResult.getResponse();
			int status = response.getStatus();
			if (status == 200) {
				return true;
			} else
				return false;
		}

	}

	public static List<ClientTenant> getOrganization() {
		OperationResult<OrganizationsListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).organizationsService()
				.allOrganizations().parameter(OrganizationParameter.INCLUDE_PARENTS, "true").get();
		if (result != null && result.getEntity() != null)
			return result.getEntity().getList();
		return null;
	}

	/**
	 * @author LAC_徐
	 * @param organization
	 * @since 2017/9/6 Delete a role on Web server
	 */
	public static boolean deleteRole(String roleID, String organization) {
		if (organization.isEmpty()) {
			OperationResult<ClientRole> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.roleName(roleID).delete();
			Response response = operationResult.getResponse();
			int status = response.getStatus();
			if (status == 200) {
				return true;
			} else
				return false;
		} else {
			OperationResult<ClientRole> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).rolesService()
					.organization(organization).roleName(roleID).delete();
			Response response = operationResult.getResponse();
			int status = response.getStatus();
			if (status == 200) {
				return true;
			} else
				return false;
		}

	}

	/**
	 * path配下のレポートリソース一覧を取得するメソッド
	 *
	 * @param path
	 * @return clientResourceListWrapper
	 */
	public static ClientResourceListWrapper getTargetSourceUnit(String path) {

		OperationResult<ClientResourceListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password) // username/password
				.resourcesService() // APIの種類を指定(resourceサービス)
				.resources().parameter(ResourceSearchParameter.FOLDER_URI, path) // フォルダのURIを指定
				.parameter(ResourceSearchParameter.RECURSIVE, Constant.API.FALSE) // RECURSIVEをfalseに指定(子の階層はフェッチしない)
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_JDBCDATASOURCE) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_JNDIDATASOURCE) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_ADHOC) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_DOMAIN) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_DASHBOARD) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_FOLDER) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_REPORT) // 取得するリソースのタイプ
				.parameter(ResourceSearchParameter.TYPE, Constants.P84_INPUTCONTROL) // 取得するリソースのタイプ
				// .parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT)
				.search(); // 実行
		ClientResourceListWrapper clientResourceListWrapper = result.getEntity();
		return clientResourceListWrapper; // OperationResultから結果を取得

	}

	/**
	 * path配下のレポートリソース一覧を取得するメソッド
	 *
	 * @param path
	 * @return clientResourceListWrapper
	 */
	public static ClientResourceListWrapper getSourceUnit(String path) {
		try {
			OperationResult<ClientResourceListWrapper> result = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password) // username/password
					.resourcesService() // APIの種類を指定(resourceサービス)
					.resources().parameter(ResourceSearchParameter.FOLDER_URI, path) // フォルダのURIを指定
					.parameter(ResourceSearchParameter.RECURSIVE, Constant.API.FALSE) // RECURSIVEをfalseに指定(子の階層はフェッチしない)
					.search(); // 実行
			ClientResourceListWrapper clientResourceListWrapper = result.getEntity();
			return clientResourceListWrapper; // OperationResultから結果を取得
		} catch (ResourceNotFoundException e) {
			return null;
		}

	}

	/**
	 * @author LAC_徐
	 * @param uri
	 * @since 2017/9/6 Delete a permission on Web server
	 */
	public static List<RepositoryPermission> getPermissionUnit(String uri, Set<String> alreadyAccess,
			Set<String> specialUris) {
		try {
			OperationResult<RepositoryPermissionListWrapper> operationResult = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).permissionsService()
					.resource(uri).get();

			RepositoryPermissionListWrapper repositoryPermissionListWrapper = operationResult.getEntity();
			alreadyAccess.add(uri);
			if (repositoryPermissionListWrapper != null) {
				List<RepositoryPermission> permissionList = repositoryPermissionListWrapper.getPermissions();
				return permissionList;
			} else {
				return null;
			}
		} catch (AccessDeniedException e) {
			specialUris.add(uri);
			return null;
		}
	}

	public static void createPermission(List<RepositoryPermission> permissionList) {
		RepositoryPermissionListWrapper permissionListWrapper = new RepositoryPermissionListWrapper(permissionList);
		client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).permissionsService()
				.createNew(permissionListWrapper);

		/*
		 * RepositoryPermissionListWrapper permissionListWrapper = new
		 * RepositoryPermissionListWrapper(permissionList); //OperationResult
		 * operationResult = class ExtendsPermissionsService extends
		 * PermissionsService { public ExtendsPermissionsService(SessionStorage
		 * sessionStorage) { super(sessionStorage); }
		 *
		 * @Override public OperationResult<Object>
		 * createNew(RepositoryPermissionListWrapper permissions) {
		 * JerseyRequest<Object> request =
		 * JerseyRequest.buildRequest(this.sessionStorage, Object.class, new
		 * String[] { "permissions" });
		 * request.setContentType(MimeTypeUtil.toCorrectAcceptMime(this.
		 * sessionStorage.getConfiguration(), "application/collection+{mime}"));
		 * return request.post(permissions); } }
		 *
		 * PermissionsService perm = client
		 * .authenticate(Constant.ServerInfo.userName,
		 * Constant.ServerInfo.password).permissionsService(); new
		 * ExtendsPermissionsService(perm.getSessionStorage()).createNew(
		 * permissionListWrapper);
		 *
		 */

		/*
		 * String authorization = "";
		 * if(serverInformation.getOrganizationName().isEmpty()) { authorization
		 * = serverInformation.getUserName() + ":" +
		 * serverInformation.getPassword(); } else { authorization =
		 * serverInformation.getUserName() + "|" +
		 * serverInformation.getOrganizationName() + ":" +
		 * serverInformation.getPassword(); } authorization = new
		 * String(Base64.getEncoder().encode(authorization.getBytes())); String
		 * request = "{\"permission\":["; for(RepositoryPermission permission :
		 * permissionList) { request += "{\"uri\":" + "\"" + permission.getUri()
		 * + "\",\"recipient\":\"" + permission.getRecipient() + "\"" +
		 * ",\"mask\":" + permission.getMask() + "},"; } request =
		 * request.substring(0, request.length()-1); request += "]}"; String
		 * address = "http://" + serverInformation.getAddress() + ":" +
		 * serverInformation.getPort() +"/lakeelbi/rest_v2/permissions";
		 * Utils.sendPost(address, request, authorization);
		 */
	}

	public static void deletePermission(String uri, Permission permission) {
		PermissionRecipient RoleOrUser = permission.isRole() ? PermissionRecipient.ROLE : PermissionRecipient.USER;
		String name = permission.isRole() ? permission.getRoleName().get() : permission.getUserName().get();
		if (!permission.getOrganization().get().isEmpty()) {
			name = permission.getOrganization().get() + "/" + name;
		}

		// OperationResult operationResult =
		client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).permissionsService()
				.resource(uri).permissionRecipient(RoleOrUser, name).delete();
	}

	public static boolean checkPermissionExist(String uri, Permission permission) {
		try {
			PermissionRecipient RoleOrUser = permission.isRole() ? PermissionRecipient.ROLE : PermissionRecipient.USER;
			String name = permission.isRole() ? permission.getRoleName().get() : permission.getUserName().get();
			if (!permission.getOrganization().get().isEmpty()) {
				name = permission.getOrganization().get() + "/" + name;
			}
			OperationResult<RepositoryPermission> operationResultCheck = client
					.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).permissionsService()
					.resource(uri).permissionRecipient(RoleOrUser, name).get();
			return operationResultCheck != null;
		} catch (ResourceNotFoundException resource) {
			return false;
		}

	}

	/*
	 * public static void test() {
	 * client.authenticate(Constant.ServerInfo.userName,
	 * Constant.ServerInfo.password) }
	 */
	public static List<ClientResourceLookup> getResources() {
		OperationResult<ClientResourceListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService().resources()
				.search();
		ClientResourceListWrapper resourceListWrapper = result.getEntity();
		if (resourceListWrapper != null) {
			return resourceListWrapper.getResourceLookups();
		} else {
			return null;
		}
	}

	/**
	 * @author LAC_潘
	 * @since 2017/9/7
	 */
	public static List<ClientUser> getUserList() {
		OperationResult<UsersListWrapper> operationResult = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).usersService().allUsers()
				.get();
		UsersListWrapper listWrapper = operationResult.getEntity();
		return listWrapper.getUserList();
	}

	public static List<ClientTenant> getOrganizationList() {
		OperationResult<OrganizationsListWrapper> operationResult = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).organizationsService()
				.allOrganizations().get();
		OrganizationsListWrapper listWrapper = operationResult.getEntity();
		List<ClientTenant> list = new ArrayList<ClientTenant>();
		if (listWrapper != null) {
			list = listWrapper.getList();
		}
		if (getSplitNumber(Constant.ServerInfo.userName) != 0) {
			String orgStr = Constant.ServerInfo.userName.split(Pattern.quote("|"))[1];
			ClientTenant org = new ClientTenant();
			org.setId(orgStr);
			list.add(org);
		} else { // getSplitNumber == 0
			ClientTenant org = new ClientTenant();
			org.setId(new String(""));
			list.add(org);
		}
		return list;
	}

	public static int getSplitNumber(String str) {
		int num = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '|') {
				num = num + 1;
			}
		}
		return num;
	}

	public static List<ClientRole> getRoleList(String organization) {
		RolesService service = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.rolesService();
		if (organization != null && !organization.isEmpty()) {
			service = service.organization(organization);
		}
		RolesListWrapper listWrapper = service.allRoles().get().getEntity();
		return listWrapper.getRoleList();
	}

	public static ClientUser getUserUnit(String organization, String username) {
		UsersService service = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.usersService();
		if (organization != null && !organization.isEmpty()) {
			service = service.forOrganization(organization);
		}
		return service.user(username).get().getEntity();
	}

	public static List<HypermediaAttribute> getAttributeList(String organization, String username) {
		AttributesService service = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.attributesService();
		if (organization != null && !organization.isEmpty()) {
			service = service.forOrganization(organization);
		}
		HypermediaAttributesListWrapper HyperAttr = service.forUser(username).allAttributes().get().getEntity();
		List<HypermediaAttribute> list = new ArrayList<HypermediaAttribute>();
		if (HyperAttr != null) {
			list = HyperAttr.getProfileAttributes();
		}
		return list;
	}

	public static void createOrUpdateUser(ClientUser user) {
		UsersService service = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.usersService();
		String str = user.getTenantId();
		if (str != null && !str.isEmpty()) {
			service = service.forOrganization(str);
		}
		service.user(user.getUsername()).createOrUpdate(user);
	}

	public static void createOrUpdateAttribute(ClientUser user, List<HypermediaAttribute> list) {
		AttributesService service = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.attributesService();
		if (user.getTenantId() != null && !user.getTenantId().isEmpty()) {
			service = service.forOrganization(user.getTenantId());
		}
		service.forUser(user.getUsername()).allAttributes().createOrUpdate(new HypermediaAttributesListWrapper(list));
	}

	public static void deleteUserAttribute(ClientUser user) {
		UsersService service = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.usersService();
		String str = user.getTenantId();
		if (str != null && !str.isEmpty()) {
			service = service.forOrganization(str);
		}
		service.user(user.getUsername()).delete();
	}
	/* 以上です */

	/**
	 * @author LAC_潘
	 * @since 2017/9/18
	 */
	@SuppressWarnings("rawtypes")
	public static ClientResourceLookup getResource(String path) {
		OperationResult<ClientResource> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(path).details();
		ClientResource resource = result.getEntity();
		ClientResourceLookup lookup = new ClientResourceLookup();
		lookup.setLabel(resource.getLabel());
		lookup.setUri(resource.getUri());
		return lookup;
	}

	public static List<ClientResourceLookup> getDomainList() {
		OperationResult<ClientResourceListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService().resources()
				.parameter(ResourceSearchParameter.TYPE, Constants.P85_API_TYPE_DOMAIN)
				.parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT).search();
		ClientResourceListWrapper listWrapper = result.getEntity();
		List<ClientResourceLookup> list = new ArrayList<ClientResourceLookup>();
		if (listWrapper != null) {
			list = listWrapper.getResourceLookups();
		}
		return list;
	}

	public static List<ClientResourceLookup> getFolderList() {
		OperationResult<ClientResourceListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService().resources()
				.parameter(ResourceSearchParameter.TYPE, Constants.P85_API_TYPE_FOLDER)
				.parameter(ResourceSearchParameter.LIMIT, Constant.API.LIMIT).search();
		ClientResourceListWrapper listWrapper = result.getEntity();
		List<ClientResourceLookup> list = new ArrayList<ClientResourceLookup>();
		if (listWrapper != null) {
			list = listWrapper.getResourceLookups();
		}
		return list;
	}

	public static InputStream getDomainInputStream(String uri) {
		ClientSemanticLayerDataSource domain = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(uri).get(ClientSemanticLayerDataSource.class).getEntity();

		OperationResult<InputStream> xmlResult = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(domain.getSchema().getUri()).downloadBinary();

		InputStream inputStreamSchema = xmlResult.getEntity();
		return inputStreamSchema;
	}

	public static void updateDomain(String uri, byte[] buff) {
		ClientSemanticLayerDataSource domain = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(uri).get(ClientSemanticLayerDataSource.class).getEntity();

		ClientFile schemaFile = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.resourcesService().resource(domain.getSchema().getUri()).get(ClientFile.class).getEntity();

		String content = DatatypeConverter.printBase64Binary(buff);
		schemaFile.setContent(content);

		client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
				.resource(domain.getSchema().getUri()).createOrUpdate(schemaFile);
	}
	/* 以上です */

	public static List<JobSummary> getJobSummaryList() {

		OperationResult<JobSummaryListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService().jobs().search();

		JobSummaryListWrapper jobSummaryListWrapper = result.getEntity();
		if (jobSummaryListWrapper == null) {
			return null;
		} else {
			return jobSummaryListWrapper.getJobsummary();
		}
	}

	public static void scheduleReport(List<Job> jobList) {
		for (Job job : jobList) {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService()
					.scheduleReport(job);
		}
	}

	public static void updateJob(List<Job> jobList) {
		for (Job job : jobList) {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService()
					.job(job.getId().longValue()).update(job);
		}
	}

	public static Job getJobById(long id) {
		OperationResult<Job> result = client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
				.jobsService().job(id).get();

		Job job = result.getEntity();
		return job;
	}

	public static void delJobById(List<Long> ids) {
		for (long id : ids) {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService().job(id)
					.delete();
		}
	}

	public static void disableJobById(List<Long> ids) {
		for (Long id : ids) {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService().jobs()
					.parameter(JobsParameter.JOB_ID, id.toString()).pause();
		}
	}

	public static void enableJobById(List<Long> ids) {
		for (Long id : ids) {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService().jobs()
					.parameter(JobsParameter.JOB_ID, id.toString()).resume();
		}
	}

	public static boolean isFolderExist(String url) {
		try {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
					.resources().parameter(ResourceSearchParameter.RECURSIVE, "false")
					.parameter(ResourceSearchParameter.FOLDER_URI, url)
					.parameter(ResourceSearchParameter.TYPE, "folder").search();
		} catch (ResourceNotFoundException e) {
			return false;
		}
		return true;
	}

	public static boolean isReportExist(String url) {
		try {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).resourcesService()
					.resource(url).details();
		} catch (Exception e) {
			if ("Not Found".equalsIgnoreCase(e.getMessage())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isCalendarExist(String name) {
		try {
			client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService().calendar(name)
					.get();
		} catch (Exception e) {
			if ("Not Found".equalsIgnoreCase(e.getMessage())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isOverWriteNotJobExist(String outputName, String folderUri, Long[] jobid) {
		Job criteria = new Job();
		criteria.setBaseOutputFilename(outputName);
		OperationResult<JobSummaryListWrapper> result = client
				.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password).jobsService().jobs()
				.search(criteria);

		JobSummaryListWrapper jobSummaryListWrapper = result.getEntity();
		if (jobSummaryListWrapper == null) {
			return false;
		} else {
			List<JobSummary> list = jobSummaryListWrapper.getJobsummary();
			for (JobSummary jobSummary : list) {
				criteria = getJobById(jobSummary.getId());
				if (criteria.getRepositoryDestination().isOverwriteFiles() == false
						&& criteria.getRepositoryDestination().getFolderURI() != null
						&& criteria.getRepositoryDestination().getFolderURI().toLowerCase()
								.contains(folderUri.toLowerCase())) {
				    jobid[0] = criteria.getId();
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * 組織を作成する
	 * 
	 * @param id
	 * @author panyuan
	 * @since 2018.01.18
	 */
	public static void createOrganization(String id) {
		ClientTenant organization = new ClientTenant();
		organization.setId(id);
		organization.setAlias(id);
		organization.setTenantName(id);
		client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
		      .organizationsService()
		      .organization(organization)
		      .create();
	}
	
	/**
	 * 組織を削除する
	 * 
	 * @param id
	 * @author panyuan
	 * @since 2018.01.18
	 */
	public static void deleteOrganization(String id) {
		ClientTenant organization = new ClientTenant();
		organization.setId(id);
		organization.setAlias(id);
		organization.setTenantName(id);
		client.authenticate(Constant.ServerInfo.userName, Constant.ServerInfo.password)
		      .organizationsService()
		      .organization(organization)
		      .delete();
	}
}
