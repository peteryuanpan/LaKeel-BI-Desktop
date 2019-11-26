package com.legendapl.lightning.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * データソースI/F
 * 
 * @author taka
 *
 */
public interface DataSource {

	@XmlEnum
    public static enum Status {
        OK("OK"), NG("NG"), UNKNOWN("");
        
        private String name;
    	
    	Status(String name) {
    		this.name = name;
    	}
    	
    	public String getValue() {
    		return name;
    	}
    }

    // 画面に表示されるが、XMLにない項目
    public String getDataSourcePath();
    public boolean isSelected();
    public void setSelected(boolean selected);
    public String getPwdShow();
	public void setPwdShow(String pwdShow);

    // XMLに在って画面にも表示されるデータ項目
    public String getLabel();
    public String getType();
    public String getServerAddress();
    public String getPort();
    public String getSchema();
    public String getUsermame();
    public String getPassword();
    public Status getStatus();
    public void setPassword(String password);
    public void setStatus(Status status);

    // XMLにあるが、画面に表示されないデータ項目
    public String getName();
    public String getDriver();
    public String getUrl();
    public void setUpdateDate(String updateDate);
    public boolean isDirty();
    public void setDirty(boolean dirty);
    
//    public String getSavePassword();
//    public String getClasspath();
}
