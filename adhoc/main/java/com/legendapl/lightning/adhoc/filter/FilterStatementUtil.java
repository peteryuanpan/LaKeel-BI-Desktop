package com.legendapl.lightning.adhoc.filter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.FilterType;
import com.legendapl.lightning.adhoc.common.OperationType;

/**
 *　ドメインのXMLのFilterStringを共通なFilterのモデルに変換する
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class FilterStatementUtil {

	private static final Pattern a2b = Pattern.compile("(\\S+)\\s(\\S+)\\s(.*)");
	private static final Pattern hasNot = Pattern.compile("^not\\s\\((.*)\\)$");
	private static final Pattern varcharHasNot = Pattern.compile("^not\\s(\\S+\\(.*)");
	private static final Pattern varchar = Pattern.compile("(\\S+)\\((\\S+),\\s(\\S+)\\)$");
	private static final Pattern datePattern = Pattern.compile("[^']+'([^']+)'");
	private static final Pattern brackePattern = Pattern.compile("^\\((.*)\\)$");


	@FunctionalInterface
	private static interface GetOpType {
		public OperationType parse(boolean hasNot, String value);
	}

	public static Filter parseFilter(String content) {
		Matcher mat = hasNot.matcher(content);
		Matcher matVarchar = varcharHasNot.matcher(content);
		if (mat.find()) {
			return parse(true, mat.group(1));
		} else if (matVarchar.find()) {
			return parseVarchar(true, matVarchar.group(1));
		} else {
			return parse(false, content);
		}
	}

	private static Filter parse(boolean hasNot, String content) {
		Matcher mat = a2b.matcher(content);
		String isNull = "null";
		if (mat.find()) {
			Filter filter = new Filter();
			filter.setFieldId(mat.group(1));
			String op = mat.group(2);
			String value = mat.group(3);
			// NULL場合「'」を付け
			if (value.contains(isNull)) {
				value = value.replaceAll("null", "'null'");
			}
			filter.setOp(subFilterOpMap.get(op).parse(hasNot, value));
			filter.setFilterType(parseFilterType(value));
			fillFilterValue(filter, value);
			return filter;
		} else {
			return parseVarchar(hasNot, content);
		}
	}

	private static Filter parseVarchar(boolean hasNot, String content) {
		Matcher mat = varchar.matcher(content);
		if (mat.find()) {
			Filter filter = new Filter();
			filter.setFieldId(mat.group(2));
			String op = mat.group(1);
			String value = mat.group(3);
			filter.setOp(subFilterOpMap.get(op).parse(hasNot, value));
			filter.setFilterType(parseFilterType(value));
			fillFilterValue(filter, value);
			return filter;
		} else {
			return null;
		}
	}

	private static void fillFilterValue(Filter filter, String value) {
		Matcher noBracke = brackePattern.matcher(value);
		if(noBracke.find()) {
			value = noBracke.group(1);
		}
		switch(filter.getFilterType()) {
		case NUMBER:
			if(value.contains(":")) {
				String[] values = value.split(":");
				filter.setHighValue(values[1]);
				filter.setLowValue(values[0]);
			} else {
				filter.setValue(value);
			}
			break;
		case BOOLEAN:
			filter.setValue(value);
			break;

		case STRING:
			//P112 と異なる
			filter.setHighValue(value);
			break;
		case DATE:
			String[] dateValues = new String[2];
			Matcher matcher = datePattern.matcher(value);
			int index = 0;
			while(matcher.find()) {
				dateValues[index++] = matcher.group(1);
			}
			if(dateValues[1] != null) {
				filter.setLowValue(LocalDate.parse(dateValues[0]));
				filter.setHighValue(LocalDate.parse(dateValues[1]));
			} else {
				filter.setValue(LocalDate.parse(dateValues[0]));
			}
			break;
		case TIME:
			dateValues = new String[2];
			matcher = datePattern.matcher(value);
			index = 0;
			while(matcher.find()) {
				dateValues[index++] = matcher.group(1);
			}
			// 元はHHmmTimeFormatter
			if(dateValues[1] != null) {
				filter.setLowValue(LocalTime.parse(dateValues[0], AdhocConstants.DataTime.timeFormatter));
				filter.setHighValue(LocalTime.parse(dateValues[1], AdhocConstants.DataTime.timeFormatter));
			} else {
				filter.setValue(LocalTime.parse(dateValues[0], AdhocConstants.DataTime.timeFormatter));
			}
			break;
		case TIMESTAMP:
			dateValues = new String[2];
			matcher = datePattern.matcher(value);
			index = 0;
			while(matcher.find()) {
				dateValues[index++] = matcher.group(1);
			}
			// データチェック 桁数未満の場合 タイムを追加
			if (dateValues[0].length() != 19) {
				dateValues[0] = dateValues[0] + " 00:00:00";
			}
			if (dateValues[1] != null) {
				if (dateValues[1].length() != 19) {
					dateValues[1] = dateValues[1] + " 00:00:00";
				}
			}
			if(dateValues[1] != null) {
				filter.setLowValue(LocalDateTime.parse(dateValues[0], AdhocConstants.DataTime.timestampFormatter));
				filter.setHighValue(LocalDateTime.parse(dateValues[1], AdhocConstants.DataTime.timestampFormatter));
			} else {
				filter.setValue(LocalDateTime.parse(dateValues[0], AdhocConstants.DataTime.timestampFormatter));
			}
			break;
		default:
			break;
		}
	}

	// [==, contains, in, endsWith, &gt;, &lt;=, !=, &gt;=, &lt;, startsWith]
	private static GetOpType equalOp = (hasNot, value) -> OperationType.equals;

	private static GetOpType notEqualOp = (hasNot, value) -> OperationType.isNotEqualTo;

	private static GetOpType containsOp = (hasNot, value) -> hasNot ? OperationType.doesNotContain : OperationType.contains;

	private static GetOpType inOp = (hasNot, value) -> value.charAt(1) == '\'' ? hasNot ? OperationType.isNotOneOf : OperationType.isOneOf
					: hasNot ? OperationType.isNotBetween : OperationType.isBetween;

	private static GetOpType startsWithOp = (hasNot, value) -> hasNot ? OperationType.doesNotStartWith : OperationType.startsWith;

	private static GetOpType endsWithOp = (hasNot, value) -> hasNot ? OperationType.doesNotEndWith : OperationType.endsWith;

	private static GetOpType gtOp = (hasNot, value) -> {
		if (value.charAt(0) == 't' || value.charAt(0) == 'd')
			return OperationType.isAfter;
		else
			return OperationType.isGreaterThan;
	};

	private static GetOpType ltOp = (hasNot, value) -> {
		if (value.charAt(0) == 't' || value.charAt(0) == 'd')
			return OperationType.isBefore;
		else
			return OperationType.lessThan;
	};

	private static GetOpType gtEqualOp = (hasNot, value) -> {
		if (value.charAt(0) == 't' || value.charAt(0) == 'd')
			return OperationType.isOnOrAfter;
		else
			return OperationType.isGreaterThanOrEqualTo;
	};

	private static GetOpType ltEqualOp = (hasNot, value) -> {
		if (value.charAt(0) == 't' || value.charAt(0) == 'd')
			return OperationType.isOnOrBefore;
		else
			return OperationType.isLessThanOrEqualTo;
	};

	private static Map<String, GetOpType> subFilterOpMap = Collections
			.unmodifiableMap(new HashMap<String, GetOpType>() {
				/**
				 *
				 */
				private static final long serialVersionUID = 7507287983103152952L;

				{
					put("==", equalOp);
					put("!=", notEqualOp);
					put("contains", containsOp);
					put("in", inOp);
					put("startsWith", startsWithOp);
					put("endsWith", endsWithOp);
					put(">", gtOp);
					put("<", ltOp);
					put(">=", gtEqualOp);
					put("<=", ltEqualOp);

				}
			});

	private static FilterType parseFilterType(String value) {
		// 値がNULLの場合、String型に変更
		String isNull = "null";
		if(value.charAt(0) == '(') {
			value = value.substring(1);
		}
		// coloum is nullに変更
		if (isNull.equals(value)|| isNull.equals(value.replace("(", "").replace(")", ""))) {
			return FilterType.STRING;
		}
		switch(value.charAt(0)) {
		case 't':
			return value.charAt(1) == 's' ? FilterType.TIMESTAMP : value.charAt(1) == '\'' ? FilterType.TIME : FilterType.BOOLEAN;
		case 'f':
			return FilterType.BOOLEAN;
		case '\'':
			return FilterType.STRING;
		case 'd':
			return FilterType.DATE;
		default:
			return FilterType.NUMBER;
		}
	}
}
