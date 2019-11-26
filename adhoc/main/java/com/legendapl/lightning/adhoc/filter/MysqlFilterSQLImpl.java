package com.legendapl.lightning.adhoc.filter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;

/**
 * 共通なFilterのモデルから、MysqlのWhere句を作成する
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class MysqlFilterSQLImpl extends GenericFilterSQL {
	private static final String fieldId = "F";
	private static final String value = "A";
	private static final String value1 = "B";
	private static final String numberValue = "'A'";
	private static final String numberValue1 = "'B'";
	private static final String EqualPattern = "F = 'A'";
	private static final String EqualPatternNull = "F is null";
	private static final String NotEqualPattern = "(F != 'A' or F is null)";
	private static final String NotEqualPatternNull = "F is not null";
	private static final String BeforePattern = "F < 'A'";
	private static final String AfterPattern = "F > 'A'";
	private static final String BeforeOnPattern = "F <= 'A'";
	private static final String AfterOnPattern = "F >= 'A'";
	private static final String BetweenPattern = "F between 'A' and 'B'";
	private static final String NotBetweenPattern = "((F not between 'A' and 'B') or F is null)";
	private static final String timeStampEqualPattern = "F = timestamp('A')";
	private static final String timeStampEqualNotEqualPattern = "(F != timestamp('A') or F is null)";
	private static final String timeStampBeforePattern = "F < timestamp('A')";
	private static final String timeStampAfterPattern = "F > timestamp('A')";
	private static final String timeStampBetweenPattern = "F between timestamp('A') and timestamp('B')";
	private static final String timeStampNotBetweenPattern = "((F not between timestamp('A') and timestamp('B')) or F is null)";
	private static final String timeStampBeforeOnPattern = "F <= timestamp('A')";
	private static final String timeStampAfterOnPattern = "F >= timestamp('A')";
	private static final String varcharContainPattern = "F like '%A%'";
	private static final String varcharNotContainPattern = "not (F like '%A%')";
	private static final String varcharStartsPattern = "F like 'A%'";
	private static final String varcharNotStartsPattern = "not (F like 'A%')";
	private static final String varcharEndsPattern = "F like '%A'";
	private static final String varcharNotEndsPattern = "not (F like '%A')";
	private static final String varcharInPattern = "F in (A)";
	private static final String varcharInPatternNull = "F is null";
	private static final String varcharInPatternNullPlural = "(F in (A) or F is null)";
	
	private static final String varcharNotInPattern = "((F not in (A)) or F is null)";
	private static final String varcharNotInPatternNull = "(F is not null)";
	private static final String varcharNotInPatternNullPlural = "((F not in (A)) and F is not null)";

	private static MysqlFilterSQLImpl instance;

	public static MysqlFilterSQLImpl getInstance() {
		if (instance == null) {
			instance = new MysqlFilterSQLImpl();
		}
		return instance;
	}

	private MysqlFilterSQLImpl() {
		super();
		dateEqual = filter -> doEqualsPattern(filter, EqualPattern, EqualPatternNull, EqualPatternNull, "0",
				AdhocConstants.DataTime.dateFormatter);

		dateNotEqual = filter -> doEqualsPattern(filter, NotEqualPattern, NotEqualPatternNull, NotEqualPatternNull, "0",
				AdhocConstants.DataTime.dateFormatter);

		dateBefore = filter -> BeforePattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				filter.getValue().toString());

		dateAfter = filter -> AfterPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				filter.getValue().toString());

		dateBeforeOn = filter -> BeforeOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, filter.getValue().toString());

		dateAfterOn = filter -> AfterOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				filter.getValue().toString());

		dateBetween = filter -> BetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, filter.getValue().toString()).replace(value1, filter.getValue1().toString());

		dateNotBetween = filter -> NotBetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, filter.getValue().toString()).replace(value1, filter.getValue1().toString());

		timeEqual = filter -> doEqualsPattern(filter, EqualPattern, EqualPatternNull, EqualPatternNull, "2",
				AdhocConstants.DataTime.timeFormatter);

		timeNotEqual = filter -> doEqualsPattern(filter, NotEqualPattern, NotEqualPatternNull, NotEqualPatternNull, "2",
				AdhocConstants.DataTime.timeFormatter);

		timeBefore = filter -> BeforePattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				((LocalTime) filter.getValue()).format(AdhocConstants.DataTime.timeFormatter));

		timeAfter = filter -> AfterPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				((LocalTime) filter.getValue()).format(AdhocConstants.DataTime.timeFormatter));

		timeBeforeOn = filter -> BeforeOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalTime) filter.getValue()).format(AdhocConstants.DataTime.timeFormatter));

		timeAfterOn = filter -> AfterOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				((LocalTime) filter.getValue()).format(AdhocConstants.DataTime.timeFormatter));

		timeBetween = filter -> BetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalTime) filter.getValue()).format(AdhocConstants.DataTime.timeFormatter))
				.replace(value1, ((LocalTime) filter.getValue1()).format(AdhocConstants.DataTime.timeFormatter));

		timeNotBetween = filter -> NotBetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalTime) filter.getValue()).format(AdhocConstants.DataTime.timeFormatter))
				.replace(value1, ((LocalTime) filter.getValue1()).format(AdhocConstants.DataTime.timeFormatter));

		//TODO
		timeStampEqual = filter -> doEqualsPattern(filter, timeStampEqualPattern, EqualPatternNull, EqualPatternNull,
				"2", AdhocConstants.DataTime.timestampFormatter);

		timeStampEqualNotEqual = filter -> doEqualsPattern(filter, timeStampEqualNotEqualPattern, NotEqualPatternNull,
				NotEqualPatternNull, "2", AdhocConstants.DataTime.timestampFormatter);

		timeStampBefore = filter -> timeStampBeforePattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter));

		timeStampAfter = filter -> timeStampAfterPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter));

		timeStampBeforeOn = filter -> timeStampBeforeOnPattern
				.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter));

		timeStampAfterOn = filter -> timeStampAfterOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter));

		timeStampBetween = filter -> timeStampBetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter))
				.replace(value1,
						((LocalDateTime) filter.getValue1()).format(AdhocConstants.DataTime.timestampFormatter));

		timeStampBetweenNotBetween = filter -> timeStampNotBetweenPattern
				.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(value, ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter))
				.replace(value1,
						((LocalDateTime) filter.getValue1()).format(AdhocConstants.DataTime.timestampFormatter));

		bitEqual = filter -> {
			if (filter.getValue().toString().equals("true")) {
				return EqualPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(numberValue,
						"1");
			} else if (filter.getValue().toString().equals("false")) {
				return EqualPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(numberValue,
						"0");
			} else {
				return EqualPatternNull.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()));
			}
		};

		bitNotEqual = filter -> {
			if (filter.getValue().toString().equals("true")) {
				return NotEqualPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(numberValue,
						"1");
			} else if (filter.getValue().toString().equals("false")) {
				return NotEqualPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(numberValue,
						"0");
			} else {
				return NotEqualPatternNull.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()));
			}
		};

		numberEqual = filter -> doEqualsPattern(filter, EqualPattern, EqualPatternNull, EqualPatternNull, "0", null);

		numberNotEqual = filter -> doEqualsPattern(filter, NotEqualPattern, NotEqualPatternNull, NotEqualPatternNull, "0", null);

		numberLT = filter -> BeforePattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(numberValue, filter.getValue().toString());

		numberGT = filter -> AfterPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(numberValue, filter.getValue().toString());

		numberLTEQ = filter -> BeforeOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(numberValue, filter.getValue().toString());

		numberGTEQ = filter -> AfterOnPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(numberValue, filter.getValue().toString());

		numberBetween = filter -> BetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(numberValue, filter.getValue().toString())
				.replace(numberValue1, filter.getValue1().toString());

		numberNotBetween = filter -> NotBetweenPattern.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase()))
				.replace(numberValue, filter.getValue().toString())
				.replace(numberValue1, filter.getValue1().toString());

		varcharEqual = filter -> doEqualsPattern(filter, EqualPattern, EqualPatternNull, EqualPattern, "1", null);

		varcharNotEqual = filter -> doEqualsPattern(filter, NotEqualPattern, NotEqualPatternNull, NotEqualPattern, "1", null);

		varcharContain = filter -> repalceLikeSql(filter, varcharContainPattern);

		varcharNotContain = filter -> repalceLikeSql(filter, varcharNotContainPattern);

		varcharStarts = filter -> repalceLikeSql(filter, varcharStartsPattern);

		varcharNotStarts = filter -> repalceLikeSql(filter, varcharNotStartsPattern);

		varcharEnds = filter -> repalceLikeSql(filter, varcharEndsPattern);

		varcharNotEnds = filter -> repalceLikeSql(filter, varcharNotEndsPattern);

		// これは特殊です
		varcharIn = filter -> doInPattern(filter, varcharInPattern ,varcharInPatternNull ,varcharInPatternNullPlural, false);
		// これは特殊です
		varcharNotIn = filter -> doInPattern(filter, varcharNotInPattern ,varcharNotInPatternNull ,varcharNotInPatternNullPlural, false);
		// 数値型の「次のいずれか」
		numberIn = filter -> doInPattern(filter, varcharInPattern ,varcharInPatternNull ,varcharInPatternNullPlural, false);
		// 数値型の「次のいずれかでない」
		numberNotIn = filter -> doInPattern(filter, varcharNotInPattern ,varcharNotInPatternNull ,varcharNotInPatternNullPlural, false);

		// ロジック型の「次のいずれか」と「次のいずれかでない」
		bitIn = filter -> doInPattern(filter, varcharInPattern ,varcharInPatternNull ,varcharInPatternNullPlural, true);

		bitNotIn = filter -> doInPattern(filter, varcharNotInPattern ,varcharNotInPatternNull ,varcharNotInPatternNullPlural, true);
		
		initMap();
	}

	/**
	 * [%][/]あり場合変義符を付け
	 * @param filter
	 * @param sql
	 * @return
	 */
	private String repalceLikeSql(Filter filter, String sql) {
		String varcharValue = filter.getHighValue().toString();
		varcharValue = varcharValue.substring(1, varcharValue.length() - 1);
		// 以下の場合、変義符を付け
		if (varcharValue.contains("%") || varcharValue.contains("_")) {
			varcharValue = varcharValue.replaceAll("\\%", "\\\\%").replaceAll("\\_", "\\\\_");
		}
		return sql.replace(fieldId, SQLUtils.DBsurround(filter.getFieldId().toLowerCase())).replace(value,
				varcharValue);
	}

	/**
	 * 入力値を取得 0:数字型 1:文字列型 2:日付型
	 * 
	 * @param filter
	 * @param typeFlg
	 * @return
	 */
	private String getfilterValue(Filter filter, String typeFlg, DateTimeFormatter dateTimeFormatter) {
		String filterValue = "";
		if ("0".equals(typeFlg)) {
			return filter.getValue().toString();
		} else if ("1".equals(typeFlg)) {
			return filterValue = filter.getHighValue().toString();
		} else if ("2".equals(typeFlg)) {
			// Nullを判断
			if ("[NULL]".equals(filter.getValue())) {
				return "";
			}
			if (AdhocConstants.DataTime.timestampFormatter.equals(dateTimeFormatter)) {
				return ((LocalDateTime) filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter);
			} else if (AdhocConstants.DataTime.timeFormatter.equals(dateTimeFormatter)) {
				return ((LocalTime) filter.getValue()).format(dateTimeFormatter);
			} else {
				return filter.getValue().toString();
			}
		}
		return filterValue;
	}

	/**
	 * 次と等しいと次と等しくない共通
	 * 
	 * @param filter
	 * @param timeformatter
	 * @return
	 */
	private String doEqualsPattern(Filter filter, String pattern, String patternNull, String patternBlank,
			String typeFlg, DateTimeFormatter dateTimeFormatter) {
		// テーブルの値を取得
		String filterValue = getfilterValue(filter, typeFlg, dateTimeFormatter);
		// コラム名を取得
		String tableName = SQLUtils.DBsurround(filter.getFieldId().toLowerCase());
		// NULLを判断
		if ("null".equals(filterValue.replaceAll("'", "")) || "[NULL]".equals(filterValue.replaceAll("'", ""))) {
			return patternNull.replace(fieldId, tableName);
		} else {
			// ブランクを判断
			if ("[------]".equals(filterValue.replaceAll("'", "")) || (filterValue.replaceAll("'", "").isEmpty())) {
				return patternBlank.replace(fieldId, tableName).replace(numberValue, "''");
			} else {
				// 値がそれ以外の場合
				if ("2".equals(typeFlg)) {
					return pattern.replace(fieldId, tableName).replace(value, filterValue);
				} else {
					return pattern.replace(fieldId, tableName).replace(numberValue, filterValue);
				}
			}
		}
	}

	/**
	 * 次のいずれかでない共通
	 * 
	 * @param filter
	 * @return
	 */
	private String doInPattern(Filter filter, String pattern, String patternNull, String patternNullPlural,
			Boolean isBitFlg) {
		String[] paramValue = filter.getHighValue().toString().split(",");
		String filterValue = filter.getHighValue().toString();
		String tableName = SQLUtils.DBsurround(filter.getFieldId().toLowerCase());
		// 入力された値が一つの場合
		if (paramValue.length == 1) {
			// NULLを判断
			if ("null".equals(filterValue.replaceAll("'", "")) || "[NULL]".equals(filterValue.replaceAll("'", ""))) {
				return patternNull.replace(fieldId, tableName).replace(value, filterValue);
			} else {
				// ブランクを判断
				if ("[------]".equals(filterValue.replaceAll("'", "")) || (filterValue.replaceAll("'", "").isEmpty())) {
					return pattern.replace(fieldId, tableName).replace(value, "''");
				} else {
					// 値がそれ以外の場合,ビット型を判断
					if (isBitFlg) {
						return doBitInPattern(pattern, tableName, filterValue);
					} else {
						return pattern.replace(fieldId, tableName).replace(value, filterValue);
					}
				}
			}
		} else {
			// 入力された値が複数の場合
			Boolean nullFlg = false;
			String param = "";
			for (int i = 0; i < paramValue.length; i++) {
				if ("null".equals(paramValue[i].trim().replaceAll("'", ""))
						|| "[NULL]".equals(paramValue[i].trim().replaceAll("'", ""))) {
					nullFlg = true;
				} else if ("[------]".equals(paramValue[i].trim().replaceAll("'", ""))
						|| (filterValue.replaceAll("'", "").isEmpty())) {
					param = param + "," + "''";
				} else {
					param = param + "," + paramValue[i];
				}
			}
			param = param.substring(1, param.length()).trim();
			// NULLが存在する場合,ビット型を判断
			if (nullFlg) {
				if (isBitFlg) {
					return doBitInPattern(patternNullPlural, tableName, param);
				} else {
					return patternNullPlural.replace(fieldId, tableName).replace(value, param);
				}

			} else {
				if (isBitFlg) {
					return doBitInPattern(pattern, tableName, param);
				} else {
					return pattern.replace(fieldId, tableName).replace(value, param);
				}
			}
		}
	}

	/**
	 * ビット型SQLを設定
	 * 
	 * @param pattern
	 * @param tableName
	 * @param param
	 * @return
	 */
	private String doBitInPattern(String pattern, String tableName, String param) {
		if (param.equals("'true'")) {
			return pattern.replace(fieldId, tableName).replace(value, "'1'");
		} else if (param.equals("'false'")) {
			return pattern.replace(fieldId, tableName).replace(value, "'0'");
		} else {
			return pattern.replace(fieldId, tableName).replace(value, "'0' , '1'");
		}
	}
}
