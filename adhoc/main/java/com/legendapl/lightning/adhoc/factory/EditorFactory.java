package com.legendapl.lightning.adhoc.factory;

import com.legendapl.lightning.adhoc.common.AdhocExceptions.NoSuchDataTypeException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.NoSuchOperationTypeException;
import com.legendapl.lightning.adhoc.common.OperationType;
import com.legendapl.lightning.adhoc.filter.editor.BooleanSelectEditor;
import com.legendapl.lightning.adhoc.filter.editor.ComboSelectEditor;
import com.legendapl.lightning.adhoc.filter.editor.DateRangeEditor;
import com.legendapl.lightning.adhoc.filter.editor.DateValueEditor;
import com.legendapl.lightning.adhoc.filter.editor.GeneralEditor;
import com.legendapl.lightning.adhoc.filter.editor.MultipleValuesEditor;
import com.legendapl.lightning.adhoc.filter.editor.NumberRangeEditor;
import com.legendapl.lightning.adhoc.filter.editor.NumberValueEditor;
import com.legendapl.lightning.adhoc.filter.editor.StringValueEditor;
import com.legendapl.lightning.adhoc.filter.editor.TimeRangeEditor;
import com.legendapl.lightning.adhoc.filter.editor.TimeStampRangeEditor;
import com.legendapl.lightning.adhoc.filter.editor.TimeStampValueEditor;
import com.legendapl.lightning.adhoc.filter.editor.TimeValueEditor;
import com.legendapl.lightning.adhoc.model.Field;

/**
 *　データタイプと操作タイプによって、該当GeneralEditorを返す
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class EditorFactory {

	public static GeneralEditor getEditor(Field field, OperationType opType) {
		switch (field.getDataType().getFilterType()) {
		case STRING:
			return getStringEditor(field, opType);
		case DATE:
			return getDateEditor(field, opType);
		case TIME:
			return getTimeEditor(field, opType);
		case NUMBER:
			return getNumberEditor(field, opType);
		case BOOLEAN:
			return getBooleanEditor(field, opType);
		case TIMESTAMP:
			return getTimeStampEditor(field, opType);
		default:
			throw new NoSuchDataTypeException();
		}
	}

	private static GeneralEditor getTimeStampEditor(Field field, OperationType opType) {
		GeneralEditor valueEditor = null;
		switch (opType) {
		case equals:
        case isNotEqualTo:
        case isAfter:
        case isBefore:
        case isOnOrAfter:
        case isOnOrBefore :
            valueEditor = new TimeStampValueEditor(field);
            break;
        case isBetween:
        case isNotBetween:
        	valueEditor = new TimeStampRangeEditor(field);
            break;
		default:
			throw new NoSuchOperationTypeException();
		}
		return valueEditor;
	}

	private static GeneralEditor getBooleanEditor(Field field, OperationType opType) {
		GeneralEditor valueEditor = null;
		switch (opType) {
		case equals:
		case isNotEqualTo:
			valueEditor = new BooleanSelectEditor(field);
			break;
		default:
			throw new NoSuchOperationTypeException();
		}
		return valueEditor;
	}

	private static GeneralEditor getNumberEditor(Field field, OperationType opType) {
		GeneralEditor valueEditor = null;
		switch (opType) {
		case equals:
		case isNotEqualTo:
		case isGreaterThan:
		case lessThan:
		case isGreaterThanOrEqualTo:
		case isLessThanOrEqualTo:
			valueEditor = new NumberValueEditor(field);
			break;
		case isBetween:
		case isNotBetween:
			valueEditor = new NumberRangeEditor(field);
			break;
		default:
			throw new NoSuchOperationTypeException();
		}
		return valueEditor;
	}

	private static GeneralEditor getTimeEditor(Field field, OperationType opType) {
		GeneralEditor valueEditor = null;
		switch (opType) {
		case equals:
        case isNotEqualTo:
        case isAfter:
        case isBefore:
        case isOnOrAfter:
        case isOnOrBefore :
            valueEditor = new TimeValueEditor(field);
            break;
        case isBetween:
        case isNotBetween:
        	valueEditor = new TimeRangeEditor(field);
            break;
		default:
			throw new NoSuchOperationTypeException();
		}
		return valueEditor;
	}

	private static GeneralEditor getDateEditor(Field field, OperationType opType) {
		GeneralEditor valueEditor = null;
		switch (opType) {
		case equals:
		case isNotEqualTo:
		case isAfter:
		case isBefore:
		case isOnOrAfter:
		case isOnOrBefore:
			valueEditor = new DateValueEditor(field);
			break;
		case isBetween:
		case isNotBetween:
			valueEditor = new DateRangeEditor(field);
			break;
		default:
			throw new NoSuchOperationTypeException();
		}
		return valueEditor;
	}

	public static GeneralEditor getStringEditor(Field field, OperationType opType) {
		GeneralEditor valueEditor = null;
		switch (opType) {
		case isOneOf:
		case isNotOneOf:
			valueEditor = new MultipleValuesEditor(field);
			break;
		case equals:
		case isNotEqualTo:
			valueEditor = new ComboSelectEditor(field);
			break;
		case contains:
		case doesNotContain:
		case startsWith:
		case doesNotStartWith:
		case endsWith:
		case doesNotEndWith:
			valueEditor = new StringValueEditor(field);
			break;
		default:
			throw new NoSuchOperationTypeException();
		}
		return valueEditor;
	}

}
