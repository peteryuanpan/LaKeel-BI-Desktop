package com.legendapl.lightning.common.constants;

/**
 * 定数クラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
public class Constant {

	public static final String localhost = "localhost";

	/**
	 * アプリケーション用の変数
	 */
	public static class Application {

		public static final int LOCK_PORT = 38629;
		public static final int NORMAL_EXIT = 0;
		public static final int ERROR_EXIT = -1;

		public static final String WORK_FILE_PATH = "work";
		public static final String SETTING_FILE_PATH = "setting";
		public static final String PREFERENCES_FILE_PATH = SETTING_FILE_PATH + "/preferences.xml";
		public static final String SERVERS_FILE_PATH = SETTING_FILE_PATH + "/servers.xml";
		public static final String DATA_SOURCE_FILE_PATH = SETTING_FILE_PATH + "/server/";
		public static final int RECENT_ITEM_SIZE = 10;

		public static final String MY_BUNDLE = "MyBundle";
		public static final String XML_CRYPT_PASSWORD = "33957aef0b623a7fdc3d506c0d149712c125be8cc968818f167db73fab477bd6";

		public static final Boolean ENCRYPT = true;

	}

	public static class Graphic {

		public static final int HOME_STAGE_MIN_WIDTH = 900;
		public static final int HOME_STAGE_MIN_HEIGHT = 500;
		public static final int DATA_SOURCE_STAGE_MIN_WIDTH = 600;
		public static final int DATA_SOURCE_STAGE_MIN_HEIGHT = 300;
		public static final int REPORT_STAGE_MIN_WIDTH = 900;
		public static final int REPORT_STAGE_MIN_HEIGHT = 600;
		public static final int STAGE_FADE_TIME = 100;
		public static final String HOME_REPORT_ICON = "FILE_DOCUMENT";
		public static final String HOME_REPORT_ICON_SIZE = "1.5em";
		public static final String REQURIED_CHECK_TEXT = "必須";

	}

	/**
	 * API実行時のServer設定
	 */
	public static class ServerInfo {
		public static final String STATUS_OK = "OK";
		public static final String STATUS_NG = "NG";
		public static final String property = "configuration.properties";
		public static String userName = "";
		public static String password = "";
		public static String workspace = "";
		public static String tempDir = "";
		public static String exportFolderPath = "";
		public static String serverName = "";
	}

	/**
	 * 対応のjdbcドライバークラス名
	 */
	public static class Driver {

		/** ver1.0対応DB */
		public static final String COM_MYSQL_JDBC = "com.mysql.jdbc.Driver";
		public static final String ORG_MARIADB_JDBC = "org.mariadb.jdbc.Driver";
		public static final String COM_MICROSOFT_SQLSERVER_JDBC = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		public static final String ORACLE_JDBC = "oracle.jdbc.OracleDriver";
		public static final String TIBCOSOFTWARE_JDBC_ORACLE = "tibcosoftware.jdbc.oracle.OracleDriver";
		public static final String TIBCOSOFTWARE_JDBC_SQLSERVER = "tibcosoftware.jdbc.sqlserver.SQLServerDriver";

	}

	/**
	 * API実行時に使用する変数
	 */
	public static class API {

		public static final String FALSE = "false";
		public static final String TRUE = "true";
		public static final String FOLDER = "folder";
		public static final String REPORTUNIT = "reportUnit";
		public static final String JDBCDATASOURCE = "jdbcDataSource";
		public static final String LIMIT = "500";

		public static final String[] RESOURCEEXPRESSIONS = { "subreportExpression", "imageExpression" };

	}

	/**
	 * Excel連携機能に使用する変数
	 */
	public static class ExcelJob {

		public static final String PARAM = "prm_";
		public static final String PARAMDATEFORMAT = "yyyyMMddhhmmss_";
		public static final String SAVEPARAMETERKEY = "_save_parameter_";

	}

	public static class ReportExecution {

		public static final String CSV_ENCODE = "MS932";

	}

	/**
	 * SQLに含まれる組み込みパラメータ<br>
	 * ver1.0ではLoggedInUserRoles, LoggedInUsernameのみ対応
	 * 
	 * http://community.jaspersoft.com/wiki/built-parameters-logged-user
	 */
	public static class BuildInParam {

		public static final String LOGGEDINUSERROLES = "LoggedInUserRoles";
		public static final String LOGGEDINUSERNAME = "LoggedInUsername";
	}

	/**
	 * DB情報の変数
	 */
	public static class DBInfo {

		public static final String MYSQL = "mysql";
		public static final String ORACLE = "oracle";
		public static final String SQLSERVER = "sqlserver";
		public static final String TIBCOSOFTWARE = "tibcosoftware";

		public static final String MYSQLPORT = "3306";
		public static final String ORACLEPORT = "1521";
		public static final String SQLSERVERPORT = "1433";
	}
	
	/**
	 * エクスポートのファイルのフォーマット
	 * 
	 * @author panyuan
	 */
	public static class ExportFileformat {
		
		public static final String CSV = "csv";
		public static final String XLSX = "xlsx";
		public static final String PDF = "pdf";
	}
	
	/**
	 * データソースタグ
	 * 
	 * @author panyuan
	 */
	public static class DataSourceTag {
		
		public static final String creationDate = "creationDate";
		public static final String folder = "folder";
		public static final String label = "label";
		public static final String name = "name";
		public static final String status = "status";
		public static final String timezone = "timezone";
		public static final String updateDate = "updateDate";
		public static final String connectionUrl = "connectionUrl";
		public static final String connectionPassword = "connectionPassword";
		public static final String connectionUser = "connectionUser";
		public static final String version = "version";
	}
}