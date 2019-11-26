package com.legendapl.lightning.adhoc.filter;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.FilterType;
import com.legendapl.lightning.adhoc.common.OperationType;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.xmlAdapter.FilterValueAdapter;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"label", "value", "value1", "op", "lock", "resourceId", "fieldId", "express", "values", "filterType"})
public class Filter {
	private Boolean lock;
	private String express;
	private String resourceId;
	private String fieldId;
	private String label;
	private Object value;
	private Object value1;
    private List<String> values;
	private OperationType op;
	private FilterType filterType;

	private String LOCKED = AdhocUtils.getString("P112.filter.locked");
	private String UNLOCKED = AdhocUtils.getString("P112.filter.unLocked");

	// JAXBのため、削除しないでください
	public Filter() {
		// TODO
	}

	public Filter(Field field) {
		fieldId = field.getId();
		resourceId = field.getResourceId();
		label = field.getLabel();
		lock = false;
		filterType = field.getDataType().getFilterType();
	}

	@XmlTransient
	public String getLocked() {
		return lock ? LOCKED : UNLOCKED;
	}

	@XmlAttribute(name = "express")
	public String getExpress() {
		return express;
	}

	public void setExpress(String express) {
		this.express = express;
	}

	@XmlAttribute(name = "lock")
	public Boolean getLock() {
		return lock;
	}

	public void setLock(Boolean lock) {
		this.lock = lock;
	}

	@XmlAttribute(name = "resourceId")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@XmlAttribute(name = "label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlAttribute(name = "OperationType")
	public OperationType getOp() {
		return op;
	}

	public void setOp(OperationType op) {
		this.op = op;
	}

	@XmlAttribute(name = "fieldId")
	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	@XmlJavaTypeAdapter(type = java.lang.Object.class, value = FilterValueAdapter.class)
	@XmlAttribute(name = "LowValue")
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@XmlJavaTypeAdapter(type = java.lang.Object.class, value = FilterValueAdapter.class)
	@XmlAttribute(name = "HighValue")
	public Object getValue1() {
		return value1;
	}

	public void setValue1(Object value1) {
		this.value1 = value1;
	}

	@XmlTransient
	public Object getLowValue() {
		return value;
	}

	public void setLowValue(Object loValue) {
		this.value = loValue;
	}

	@XmlTransient
	public Object getHighValue() {
		return value1;
	}

	public void setHighValue(Object hiValue) {
		this.value1 = hiValue;
	}

	@XmlElementWrapper(name = "Values")
	@XmlElement(name = "Value")
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getDescription() {
    	return "";
    }

    @XmlAttribute(name = "FilterType")
	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	@Override
	public String toString() {
		return "Filter [fieldId=" + fieldId + ", value=" + value + ", value1=" + value1 + ", values=" + values + ", op="
				+ op + ", filterType=" + filterType + "]";
	}

}
