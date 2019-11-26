package com.legendapl.lightning.service;

import java.lang.invoke.MethodHandles;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.dto.authority.RolesListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientListOfValues;
import com.jaspersoft.jasperserver.dto.resources.ClientQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Debug;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * SQLを含んだ入力コントロールの候補値を取得するクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
public class InputControlService {

	protected static ResourceBundle messageRes = ResourceBundle.getBundle("messages");
	private static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static ClientListOfValues clientListOfValues;
	private static ClientQuery clientQuery;
	private static LinkedHashMap<String, String> parameterListMap;

	private static String regex;
	private static Pattern pattern;
	private static Matcher matcher;

	/**
	 * コンポーネントにバインドするデータを取得するメソッド
	 * 
	 * @param clientInputControl
	 * @return parameterListMap
	 */
	public static LinkedHashMap<String, String> getStaticItems(ClientInputControl clientInputControl) {

		clientListOfValues = (ClientListOfValues) clientInputControl.getListOfValues();
		parameterListMap = new LinkedHashMap<String, String>();

		for (int j = 0; j < clientListOfValues.getItems().size(); j++) {
			parameterListMap.put(clientListOfValues.getItems().get(j).getValue(),
					clientListOfValues.getItems().get(j).getLabel());
		}

		return parameterListMap;
	}

	/**
	 * コンポーネントにバインドするデータを取得するメソッド(SQLを含む入力コントロール)
	 * 
	 * @param clientInputControl
	 * @param clientReportUnit
	 * @param parameterMap
	 * @return parameterListMap
	 */
	public static LinkedHashMap<String, String> getQueryItems(ClientInputControl clientInputControl,
			ClientReportUnit clientReportUnit, Map<String, Object> parameterMap) {

		clientQuery = (ClientQuery) clientInputControl.getQuery();

		/** 実際のSQL文 */
		String sql = clientQuery.getValue();

		/**
		 * パラメータ($P{}構文)がSQLに含まれているかどうか確認し、 含まれている場合は該当のパラメータの値が既に入力済みか確認する。
		 * 入力済み(!=null)であればSQLの$P{}構文の箇所を 実際の入力された値に置換する。
		 */
		// 正規表現パターン
		regex = "\\$P\\{(.+?)\\}";
		// PatternとMacherの定義
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(sql);
		// 抽出した文字列 例:(IN, hoge, hoge)
		String $P;

		while (matcher.find()) {
			// スペースをトリムする
			$P = matcher.group(1).trim();
			for (Map.Entry<String, Object> map : parameterMap.entrySet()) {
				// 組み込みパラメータLoggedInUsernameの場合
				if ($P.equals(Constant.BuildInParam.LOGGEDINUSERNAME)) {
					sql = sql.replace(matcher.group(0), "\"" + Constant.ServerInfo.userName + "\"");
				}
				// 通常のパラメータの場合
				if ($P.equals(map.getKey())) {
					if (null == map.getValue()) {
						logger.debug(MessageFormat.format(messageRes.getString(Debug.DEBUG_W04_01),
								clientQuery.getUri().substring(clientQuery.getUri().lastIndexOf("/") + 1),
								map.getKey()));
						/** 1つでも未定義のパラメータがあればクエリは実行できない */
						return null;
					} else {
						// 単一値のみを持つ$Pであるかをチェック
						// $P{パラメータ名}を置換
						sql = sql.replace(matcher.group(0), "\"" + map.getValue().toString() + "\"");
					}
				}
			}
		}

		/**
		 * 組み込みパラメータ($X{}構文)がSQLに含まれているかどうか確認し、
		 * 含まれている場合はログインユーザを元にAPIを実行し、値を取得して、 SQLの該当部分を置換する
		 * 
		 * 例: <br>
		 * before: $X{ IN, hoge, LoggedINUserRoles} <br>
		 * after: hoge IN ("ROLE_USER", "ROLE_SUPERUSER")
		 */
		// 正規表現パターン
		regex = "\\$X\\{(.+)\\}";
		// PatternとMacherの定義
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(sql);
		// 抽出した文字列 例:(IN, hoge, hoge)
		String $X;
		// $Xをカンマでスプリットした配列
		String[] $XArray;

		while (matcher.find()) {
			$X = matcher.group(1);
			$XArray = $X.split(",");
			StringBuilder substitution = new StringBuilder();
			substitution.append($XArray[1] + " " + $XArray[0] + "(");
			// スペースをトリムする
			for (int i = 0; i < $XArray.length; i++) {
				$XArray[i] = $XArray[i].trim();
			}
			switch ($XArray[2]) {
			// TODO: 他の組み込みパラメータにも対応
			case Constant.BuildInParam.LOGGEDINUSERROLES:
				RolesListWrapper rolesListWrapper = ExecuteAPIService.getLoggedInUserRoles();
				for (int k = 0; k < rolesListWrapper.getRoleList().size(); k++) {
					substitution.append("\"" + rolesListWrapper.getRoleList().get(k).getName());
					if (rolesListWrapper.getRoleList().size() != k + 1)
						substitution.append("\", ");
					else
						substitution.append("\")");
				}
				sql = sql.replace(matcher.group(0), substitution.toString());
				break;
			case Constant.BuildInParam.LOGGEDINUSERNAME:
				substitution.append("\"" + Constant.ServerInfo.userName + "\")");
				break;
			default:
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) parameterMap.get($XArray[2]);
				if (null == list) {
					return null;
				}
				for (int k = 0; k < list.size(); k++) {
					substitution.append("\"" + list.get(k));
					if (list.size() != k + 1)
						substitution.append("\", ");
					else
						substitution.append("\")");
				}
				sql = sql.replace(matcher.group(0), substitution.toString());
			}
		}

		// $P{LoggedInUsername}が含まれる場合にユーザ名に置換する。
		if (sql.contains("$P{LoggedInUsername}"))
			sql = sql.replace("$P{LoggedInUsername}", "\"" + Constant.ServerInfo.userName + "\"");

		/** SQLを実行する */
		java.sql.Connection conn = null;

		try {
			ClientJdbcDataSource clientJdbcDataSource = ExecuteAPIService.getDatasource(clientReportUnit);

			DecryptionDatasourceService decryptionDatasourceService = new DecryptionDatasourceService();
			String password = decryptionDatasourceService.getPassword(clientJdbcDataSource);

			if (null == password)
				return null;

			conn = DriverManager.getConnection(clientJdbcDataSource.getConnectionUrl(),
					clientJdbcDataSource.getUsername(), password);

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			StringBuilder labelColumns;
			String labelColumn;
			String valueColumn;

			// クエリの実行結果から,<実際の値,画面に表示する値>のマップを作成
			parameterListMap = new LinkedHashMap<String, String>();
			while (rs.next()) {
				labelColumns = new StringBuilder();
				for (int i = 0; i < clientInputControl.getVisibleColumns().size(); i++) {
					labelColumns.append(rs.getString(clientInputControl.getVisibleColumns().get(i)));
					/** ラベル間にデリミタを挿入 */
					if (clientInputControl.getVisibleColumns().size() != i + 1)
						labelColumns.append(" | ");
				}
				labelColumn = labelColumns.toString();
				valueColumn = rs.getString(clientInputControl.getValueColumn());
				parameterListMap.put(valueColumn, labelColumn);
			}

		} catch (SQLException e) {
			logger.error(messageRes.getString(Error.ERROR_W04_03), e);
			logger.error(sql);
			parameterListMap = null;
		} finally {
			try {
				if (null != conn)
					conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return parameterListMap;
	}

	/**
	 * コンポーネントにバインドするラベルリストを取得するメソッド
	 * 
	 * @param parameterListMap
	 * @return ObservableList<String>
	 */
	public static ObservableList<String> getLabelItemList(LinkedHashMap<String, String> parameterListMap) {
		ObservableList<String> parameterItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : parameterListMap.entrySet()) {
			parameterItems.add(map.getValue());
		}

		return parameterItems;
	}

}
