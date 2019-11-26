package com.legendapl.lightning.xmlAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.recentItem.AdhocRecentItem;
import com.legendapl.lightning.adhoc.xmlAdapter.FilterValueAdapter;
import com.legendapl.lightning.model.RecentItem;

public class RecentItemAdpater extends XmlAdapter<RecentItemElement, RecentItem> {

	protected Logger logger = Logger.getLogger(FilterValueAdapter.class);
	
	@Override
	public RecentItem unmarshal(RecentItemElement element) throws Exception {
		try {
			if (null == element) return null;
			RecentItem recentItem = new RecentItem();
			recentItem.setReportLabel(element.getReportLabel());
			recentItem.setReportURI(element.getReportURI());
			recentItem.setDate(element.getDate());
			recentItem.setUser(element.getUser());
			// TODO
			if (null != element.getAdhocFromType()) {
				AdhocRecentItem adhocRecentItem = new AdhocRecentItem(recentItem);
				adhocRecentItem.setAdhocFromType(element.getAdhocFromType());
				return adhocRecentItem;
			} else {
				return recentItem;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e);
		}
	}

	@Override
	public RecentItemElement marshal(RecentItem recentItem) throws Exception {
		try {
			if (null == recentItem) return null;
			RecentItemElement element = new RecentItemElement();
			element.setReportLabel(recentItem.getReportLabel());
			element.setReportURI(recentItem.getReportURI());
			element.setDate(recentItem.getDate());
			element.setUser(recentItem.getUser());
			if (recentItem.getClass().equals(RecentItem.class)) {
				element.setAdhocFromType(null);
			} else if (recentItem.getClass().equals(AdhocRecentItem.class)) {
				element.setAdhocFromType(((AdhocRecentItem)recentItem).getAdhocFromType());
			} else {
				// TODO
			}
			return element;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e);
		}
	}

}
