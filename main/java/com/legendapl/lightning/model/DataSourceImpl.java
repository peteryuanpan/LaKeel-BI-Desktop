package com.legendapl.lightning.model;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.legendapl.lightning.common.constants.Constant;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

/**
 * データソース実装
 * 
 * @author taka
 *
 */
@XmlRootElement(name = "jdbcDataAdapter")
public class DataSourceImpl implements ObservableDataSource {
	
	protected static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

	// 画面に表示されるが、XMLにない項目
	@XmlTransient
	protected SimpleStringProperty dataSourcePathProperty;
	@XmlTransient
	protected SimpleBooleanProperty selectedProperty;
	@XmlTransient
	private String pwdShow;

	// XMLに在って画面にも表示されるデータ項目
	@XmlTransient
	protected SimpleStringProperty nameProperty;
	@XmlTransient
	protected SimpleStringProperty typeProperty;
	@XmlTransient
	protected SimpleStringProperty serverAddressProperty;
	@XmlTransient
	protected SimpleStringProperty portProperty;
	@XmlTransient
	protected SimpleStringProperty schemaProperty;
	@XmlTransient
	protected SimpleStringProperty usernameProperty;
	protected String password;
	@XmlTransient
	protected SimpleObjectProperty<Status> statusProperty;

	// XMLにあるが、画面に表示されないデータ項目
	protected String driver;
	protected String version;
	protected String timezone;
	protected String label;
	protected String description;
	protected String createDate;
	protected String updateDate;
	protected boolean dirty;
	protected String connectionUrl;
	
	// XMLにない、画面にも表示されない項目
	@XmlTransient
	protected String localFileUrl;

	public DataSourceImpl() {
		dataSourcePathProperty = new SimpleStringProperty("");
		nameProperty = new SimpleStringProperty("");
		typeProperty = new SimpleStringProperty("");
		serverAddressProperty = new SimpleStringProperty("");
		portProperty = new SimpleStringProperty("");
		schemaProperty = new SimpleStringProperty("");
		usernameProperty = new SimpleStringProperty("");
		statusProperty = new SimpleObjectProperty<Status>(Status.UNKNOWN);
		selectedProperty = new SimpleBooleanProperty(false);
		dirty = false;
	}

	@XmlElement(name = "folder")
	@Override
	public String getDataSourcePath() {
		return dataSourcePathProperty.getValue();
	}

	public void setDataSourcePath(String dataSourcePath) {
		dataSourcePathProperty.set(dataSourcePath);
	}

	@Override
	public String getName() {
		return nameProperty.getValue();
	}

	public void setName(String name) {
		nameProperty.set(name);
	}

	@XmlElement(name = "connectionUrl")
	public String getUrl() {
		return this.connectionUrl;
	}

	// TODO : 新しいデータ型が追加されたときにパターンとインデックスの値をよりよく設定してください
	public void setUrl(String url) {
		setConnectionUrl(url);
		Pattern p = Pattern.compile("^jdbc:([a-zA-Z]+):");
		Matcher m = p.matcher(url);
		
		int typePropertyIndex = 1;
		int serverAddressPropertyIndex = 2;
		int portPropertyIndex = 3;
		int schemaPropertyIndex = 4;
		
		if (m.find()) {
			typeProperty.set(m.group(typePropertyIndex));
		} else {
			logger.warn("setUrl: Bad format of url: " + url);
			return;
		}
		
		switch (typeProperty.get()) {
		case Constant.DBInfo.MYSQL: /*jdbc:mysql://localhost:3306/dbname*/
			p = Pattern.compile("^jdbc:([a-zA-Z]+)://([a-zA-Z\\.0-9]+):?([0-9]+|null)?/(.+)\\??$");
			break;
		case Constant.DBInfo.ORACLE: /*jdbc:oracle:thin:@localhost:1521:orcl*/
			p = Pattern.compile("^jdbc:([a-zA-Z]+):thin:/?/?@([a-zA-Z\\.0-9]+):([0-9]+|null)[/:](.+)$");
			break;
		case Constant.DBInfo.SQLSERVER: /*jdbc:sqlserver://localhost:1433;databaseName=dbname*/
			p = Pattern.compile("^jdbc:([a-zA-Z]+)://([a-zA-Z\\.0-9]+):([0-9]+|null)?;[dD]atabaseN?a?m?e?=(.+)\\??$");
			break;
		case Constant.DBInfo.TIBCOSOFTWARE:
			/*jdbc:tibcosoftware:oracle://localhost:1521;SID=ORCL*/
			p = Pattern.compile("^jdbc:([a-zA-Z]+):([a-zA-Z]+)://([a-zA-Z\\.0-9]+):([0-9]+|null)?;SID=(.+)\\??$");
			m = p.matcher(url);
			if (m.find() && Constant.DBInfo.ORACLE.equals(m.group(2))) {
				typePropertyIndex = 2; serverAddressPropertyIndex = 3; portPropertyIndex = 4; schemaPropertyIndex = 5;
				break;
			}
			/*jdbc:tibcosoftware:sqlserver://localhost:1433;databaseName=dbname*/
			p = Pattern.compile("^jdbc:([a-zA-Z]+):([a-zA-Z]+)://([a-zA-Z\\.0-9]+):([0-9]+|null)?;[dD]atabaseN?a?m?e?=(.+)\\??$");
			m = p.matcher(url);
			if (m.find() && Constant.DBInfo.SQLSERVER.equals(m.group(2))) {
				typePropertyIndex = 2; serverAddressPropertyIndex = 3; portPropertyIndex = 4; schemaPropertyIndex = 5;
				break;
			}
		default:
			logger.warn("unknow type property found: " + typeProperty.get() + ", url: " + url);
			return;
		}
		
		m = p.matcher(url);
		if (m.find()) {
			typeProperty.set(m.group(typePropertyIndex));
			serverAddressProperty.set(m.group(serverAddressPropertyIndex));
			schemaProperty.set(m.group(schemaPropertyIndex));

			if (StringUtils.isEmpty(m.group(portPropertyIndex))) {
				switch (typeProperty.get()) {
				case Constant.DBInfo.MYSQL:
					portProperty.set(Constant.DBInfo.MYSQLPORT);
					break;
				case Constant.DBInfo.ORACLE:
					portProperty.set(Constant.DBInfo.ORACLEPORT);
					break;
				case Constant.DBInfo.SQLSERVER:
					portProperty.set(Constant.DBInfo.SQLSERVERPORT);
					break;
				}
			} else {
				portProperty.set(m.group(portPropertyIndex));
			}
			
			logger.debug("url: " + url);
			logger.debug("typeProperty: " + typeProperty.get());
			logger.debug("serverAddressProperty: " + serverAddressProperty.get());
			logger.debug("portProperty: " + portProperty.get());
			logger.debug("schemaProperty: " + schemaProperty.get());
			
		} else {
			logger.warn("Matcher can not match the url ["+url+"].");
		}
	}

	protected void setConnectionUrl(String url) {
		this.connectionUrl = url;
	}

	@Override
	public String getType() {
		return typeProperty.getValue();
	}

	@Override
	public String getServerAddress() {
		return serverAddressProperty.getValue();
	}

	@Override
	public String getPort() {
		return portProperty.getValue();
	}

	@Override
	public String getSchema() {
		return schemaProperty.getValue();
	}

	@XmlElement(name = "connectionUser")
	@Override
	public String getUsermame() {
		return usernameProperty.getValue();
	}

	public void setUsermame(String username) {
		usernameProperty.set(username);
	}

	@XmlElement(name = "connectionPassword")
	@Override
	public String getPassword() {
		return password;
	}
	
	@Override
	public Status getStatus() {
		return statusProperty.getValue();
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setStatus(Status status) {
		statusProperty.set(status);
	}

	@Override
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	@Override
	public ObservableValue<String> dataSourcePathProperty() {
		return dataSourcePathProperty;
	}

	@Override
	public ObservableValue<String> nameProperty() {
		return nameProperty;
	}

	@Override
	public ObservableValue<String> typeProperty() {
		return typeProperty;
	}

	@Override
	public ObservableValue<String> serverAddressProperty() {
		return serverAddressProperty;
	}

	@Override
	public ObservableValue<String> portProperty() {
		return portProperty;
	}

	@Override
	public ObservableValue<String> schemaProperty() {
		return schemaProperty;
	}

	@Override
	public ObservableValue<String> usernameProperty() {
		return usernameProperty;
	}

	@Override
	public ObservableValue<Status> statusProperty() {
		return statusProperty;
	}

	@Override
	public ObservableValue<Boolean> selectedProperty() {
		return selectedProperty;
	}

	@XmlTransient
	@Override
	public boolean isSelected() {
		return selectedProperty.get();
	}

	@Override
	public void setSelected(boolean selected) {
		selectedProperty.set(selected);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "creationDate")
	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	@XmlTransient
	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	@XmlTransient
	@Override
	public String getPwdShow() {
		return pwdShow;
	}
	
	@Override
	public void setPwdShow(String pwdShow) {
		this.pwdShow = pwdShow;
	}
	
	@XmlTransient
	public String getLocalFileUrl() {
		return localFileUrl;
	}
	
	public void setLocalFileUrl(String localFileUrl) {
		this.localFileUrl = localFileUrl;
	}
}
