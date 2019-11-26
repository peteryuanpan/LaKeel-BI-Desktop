package com.legendapl.lightning.adhoc.recentItem;

import javax.xml.bind.annotation.XmlElement;

import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.model.RecentItem;

public class AdhocRecentItem extends RecentItem {
	
	private static final long serialVersionUID = 1L;
	
	private ModelType adhocFromType;
	
	public AdhocRecentItem() {
		// TODO
	}
	
	public AdhocRecentItem(RecentItem recentItem) {
		super();
		this.setAdhocFromType(null);
		this.setReportLabel(recentItem.getReportLabel());
		this.setReportURI(recentItem.getReportURI());
		this.setDate(recentItem.getDate());
		this.setUser(recentItem.getUser());
	}
	
	public AdhocRecentItem(ModelType adhocFromType, String label, String uri) {
		super();
		this.setAdhocFromType(adhocFromType);
		this.setReportLabel(label);
		this.setReportURI(uri);
	}

	@XmlElement(name = "adhocFromType")
	public ModelType getAdhocFromType() {
		return adhocFromType;
	}
	
	public void setAdhocFromType(ModelType adhocFromType) {
		this.adhocFromType = adhocFromType;
	}

	@Override
	public boolean equals(Object obj) {
		if (null != obj && obj instanceof AdhocRecentItem) {
			AdhocRecentItem recentItem = (AdhocRecentItem) obj;
			if (null != this.getReportURI() && null != recentItem.getReportURI() &&
					this.getReportURI().equals(recentItem.getReportURI())) {
				return true;
			}
		}
		return false;
	}
	
}
