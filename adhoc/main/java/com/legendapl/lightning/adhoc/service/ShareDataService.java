package com.legendapl.lightning.adhoc.service;

import java.util.HashMap;
import java.util.Map;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.adhoc.model.BaseModel;
import com.legendapl.lightning.adhoc.model.Domain;
import com.legendapl.lightning.adhoc.model.Topic;

/**
 * データを共有する
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class ShareDataService {

	private static Map<ModelType, BaseModel> shareMap = new HashMap<ModelType, BaseModel>();

	private static boolean calculatedField = false;

	public static void clear() {
		shareMap.clear();
	}

	public static void share(BaseModel object) {
		shareMap.put(object.getModelType(), object);
	}

	public static BaseModel getObject(ModelType type) {
		return (AdhocUtils.getClassType(type)).cast(shareMap.get(type));
	}

	public static Topic loadTopic() {
		return Topic.class.cast(shareMap.get(ModelType.TOPIC));
	}

	public static Domain loadDomain() {
		return Domain.class.cast(shareMap.get(ModelType.DOMAIN));
	}

	public static Adhoc loadAdhoc() {
		return Adhoc.class.cast(shareMap.get(ModelType.ADHOC));
	}

	public static boolean isCalculatedField() {
		return calculatedField;
	}

	public static void setCalculatedField(boolean calculatedField) {
		ShareDataService.calculatedField = calculatedField;
	}

}
