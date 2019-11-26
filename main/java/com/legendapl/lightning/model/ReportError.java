package com.legendapl.lightning.model;

public class ReportError {
	
	private String type;
	
	private String title;
	
	private String txt;
	
	public ReportError () {
	}
	
	public ReportError(ReportError reportError) {
		this.type = reportError.getType();
		this.title = reportError.getTitle();
		this.txt = reportError.getTxt();
	}
	
	public ReportError(String type, String title, String txt) {
		this.type = type;
		this.title = title;
		this.txt = txt;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setTitle(String Title) {
		this.title = Title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTxt(String txt) {
		this.txt = txt;
	}
	
	public String getTxt() {
		return this.txt;
	}
	
	public String toString() {
		return 	"type: "+type+"\n"+
				"Title: "+title+"\n"+
				"Txt: "+txt;
	}
	
	public boolean equals(ReportError reportError) {
		return this.type != null && this.type.equals(reportError.getType()) &&
			   this.title != null && this.title.equals(reportError.getTitle()) &&
			   this.txt != null && this.txt.equals(reportError.getTxt());
	}
}
