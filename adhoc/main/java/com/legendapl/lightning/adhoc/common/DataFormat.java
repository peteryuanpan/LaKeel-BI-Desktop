package com.legendapl.lightning.adhoc.common;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.legendapl.lightning.adhoc.adhocView.model.trieTree.TotalNode;

/**
 * データ　フォーマットの変更
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.27
 */
public enum DataFormat {

	// Time
	TimehhMMSS("P121.crossTable.TimehhMMSS", "hide,medium", 0, new ParseToString() {
		//午後　01:01:01
		private final DateFormat TIME_FORMAT_JA = new SimpleDateFormat("a hh:mm:ss", Locale.JAPANESE);
		//01:01:01 PM
		private final DateFormat TIME_FORMAT_EN = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);

		private DateFormat TIME_FORMAT = TIME_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return TIME_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			TIME_FORMAT = isJapanese ? TIME_FORMAT_JA : TIME_FORMAT_EN;

		}
	}),
	TimeHHMMSS("P121.crossTable.TimeHHMMSS", "hide,short", 1, new ParseToString() {
		//13:01:01
		private final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
		@Override
		public String parse(Object result) {
			return TIME_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	TimehhMM("P121.crossTable.TimehhMM", "hide,long", 2, new ParseToString() {
		//午後　01:01
		private final DateFormat TIME_FORMAT_JA = new SimpleDateFormat("a hh:mm", Locale.JAPANESE);
		//01:01 PM
		private final DateFormat TIME_FORMAT_EN = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

		private DateFormat TIME_FORMAT = TIME_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return TIME_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			TIME_FORMAT = isJapanese ? TIME_FORMAT_JA : TIME_FORMAT_EN;

		}
	}),
	TimeHHMM("P121.crossTable.TimeHHMM", "HH:mm:ss", 3, new ParseToString() {
		//13:01
		private final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
		@Override
		public String parse(Object result) {
			return TIME_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	// Date
	DateYYMMDD("P121.crossTable.DateYYMMDD", "medium,hide", 4, new ParseToString() {
		//2018/01/02
		private final DateFormat DATE_FORMAT_JA = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPANESE);
		//Jan 02, 2018
		private final DateFormat DATE_FORMAT_EN = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

		private DateFormat DATE_FORMAT = DATE_FORMAT_JA;

		@Override
		public String parse(Object result) {
			return DATE_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DATE_FORMAT = isJapanese ? DATE_FORMAT_JA : DATE_FORMAT_EN;

		}
	}),
	DateYMD("P121.crossTable.DateYMD", "short,hide", 5, new ParseToString() {
		//18/01/02
		private final DateFormat DATE_FORMAT_JA = new SimpleDateFormat("yy/MM/dd", Locale.JAPANESE);
		//01/02/18
		private final DateFormat DATE_FORMAT_EN = new SimpleDateFormat("MM/dd/YY", Locale.ENGLISH);

		private DateFormat DATE_FORMAT = DATE_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return DATE_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DATE_FORMAT = isJapanese ? DATE_FORMAT_JA : DATE_FORMAT_EN;
		}
	}),
	// TimeStamp
	TimeStampYYMMDD("P121.crossTable.TimeStampYYMMDD", "medium,hide", 6, new ParseToString() {
		//2018/01/02
		private final DateFormat DATE_FORMAT_JA = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPANESE);
		//Jan 02, 2018
		private final DateFormat DATE_FORMAT_EN = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

		private DateFormat DATE_FORMAT = DATE_FORMAT_JA;

		@Override
		public String parse(Object result) {
			return DATE_FORMAT.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {
			DATE_FORMAT = isJapanese ? DATE_FORMAT_JA : DATE_FORMAT_EN;

		}
	}),
	TimeStampYMD("P121.crossTable.TimeStampYMD", "short,hide", 7, new ParseToString() {
		//18/01/02
		private final DateFormat DATE_FORMAT_JA = new SimpleDateFormat("yy/MM/dd", Locale.JAPANESE);
		//01/02/18
		private final DateFormat DATE_FORMAT_EN = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);

		private DateFormat DATE_FORMAT = DATE_FORMAT_JA;

		@Override
		public String parse(Object result) {
			return DATE_FORMAT.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {
			DATE_FORMAT = isJapanese ? DATE_FORMAT_JA : DATE_FORMAT_EN;

		}
	}),
	TimeStampYYMMDDHHMMSS("P121.crossTable.TimeStampYYMMDDHHMMSS", "medium,medium", 8, new ParseToString() {
		//2018/01/02 13:01:01
		private final DateFormat TIMESTAMP_FORMAT_JA = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE);
		//Jan 02, 2018
		private final DateFormat TIMESTAMP_FORMAT_EN = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH);
		private DateFormat TIMESTAMP_FORMAT = TIMESTAMP_FORMAT_JA;

		@Override
		public String parse(Object result) {
			return TIMESTAMP_FORMAT.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {
			TIMESTAMP_FORMAT = isJapanese ? TIMESTAMP_FORMAT_JA : TIMESTAMP_FORMAT_EN;

		}
	}),
	TimeStampHHMMSS("P121.crossTable.TimeStampHHMMSS", "hide,medium", 9, new ParseToString() {
		//13:01:01
		private final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
		@Override
		public String parse(Object result) {
			return TIMESTAMP_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	// Float
	FloatTwoDecimal("P121.crossTable.FloatTwoDecimal", "#,##0.00", 10, new ParseToString() {
		//-1,234.56
		private final DecimalFormat df = HALF_UP(new DecimalFormat("#,##0.00"));
		@Override
		public String parse(Object result) {
			return df.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {

		}
	}),
	FloatInteger("P121.crossTable.FloatInteger", "0", 11, new ParseToString() {
		//-1234
		private final DecimalFormat df = HALF_UP(new DecimalFormat("#"));
		@Override
		public String parse(Object result) {
			return df.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {

		}
	}),
	FloatNoMinusDecimalCoin1("P121.crossTable.FloatNoMinusDecimalCoin1", "¥#,##0.00;(¥#,##0.00)", 12, new ParseToString() {
		//(¥1,234.56)
		private final DecimalFormat df = HALF_UP(new DecimalFormat("'¥'#,##0.00;('¥'#,##0.00)"));

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {

		}
	}),
	FloatIntegerCoin1("P121.crossTable.FloatIntegerCoin1", "¥#,##0;(¥#,##0)", 13, new ParseToString() {
		//(¥1,234)
		private final DecimalFormat df = HALF_UP(new DecimalFormat("'¥',###;('¥',###)"));

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	FloatDecimalCoin1("P121.crossTable.FloatDecimalCoin1", "¥#,##0.000#############", 14, new ParseToString() {
		//-¥1,234.56
		private final DecimalFormat df = HALF_UP(new DecimalFormat("'¥'#,##0.00"));

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	FloatNoMinusDecimalCoin2("P121.crossTable.FloatNoMinusDecimalCoin2", "$#,##0.00;($#,##0.00)", 15, new ParseToString() {
		//($1,234.56)
		private final DecimalFormat df = HALF_UP(new DecimalFormat("'$'#,##0.00;('$'#,##0.00)"));

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}

	}),
	FloatIntegerCoin2("P121.crossTable.FloatIntegerCoin2", "$#,##0.00;($#,##0.00)", 16, new ParseToString() {
		//($1,234)
		private final DecimalFormat df = HALF_UP(new DecimalFormat("'$',###;('$',###)"));

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	FloatDecimalCoin2("P121.crossTable.FloatDecimalCoin2", "$#,##0.000#############", 17, new ParseToString() {
		//-$1,234.56
		private final DecimalFormat df = HALF_UP(new DecimalFormat("'$'#,##0.00"));

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	// Integer
	IntegerHasComma("P121.crossTable.IntegerHasComma", "#,##0", 18, new ParseToString() {
		//-1,234
		private final DecimalFormat df = new DecimalFormat(",###");

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	IntegerNoComma("P121.crossTable.IntegerNoComma", "0", 19, new ParseToString() {
		//-1234
		@Override
		public String parse(Object result) {
			return AdhocUtils.toString(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	}),
	IntegerCoin1("P121.crossTable.IntegerCoin1", "¥#,##0;(¥#,##0)", 20, new ParseToString() {
		//(¥1,234)
		private final DecimalFormat df = new DecimalFormat("'¥',###;('¥',###)");

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {

		}
	}),
	IntegerCoin2("P121.crossTable.IntegerCoin2", "$#,##0;($#,##0)", 21, new ParseToString() {
		//($1,234)
		private final DecimalFormat df = new DecimalFormat("'$',###;('$',###)");

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {

		}

	}),
	IntegerNoMinus("P121.crossTable.IntegerNoMinus", "#,##0;(#,##0)", 22, new ParseToString() {
		//(1,234)
		private final DecimalFormat df = new DecimalFormat(",###;(,###)");

		@Override
		public String parse(Object result) {
			return df.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {

		}
	}),

	// Other
	GenericFormat("", "", 23, new ParseToString() {

		@Override
		public String parse(Object result) {
			return AdhocUtils.toString(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {


		}
	});

	private static final DecimalFormat HALF_UP(DecimalFormat df) {
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df;
	}

	static {
		AdhocUtils.isJapanese.addListener((ob, old, newValue) -> {
			for(DataFormat dataFormat: DataFormat.values()) {
				dataFormat.changeLanguage(newValue);
			}
		});
	}

	private String name;
	private String defaultDataFormat;
	private Integer index;
	private ParseToString parseFun;

	DataFormat(String name, String defaultDataFormat, Integer index, ParseToString parseFun) {
		this.name = name;
		this.defaultDataFormat = defaultDataFormat;
		this.index = index;
		this.parseFun = parseFun;
	}

	private void changeLanguage(boolean isJapanese) {
		parseFun.changeLanguage(isJapanese);
	}

	@Override public String toString() {
		try {
			return AdhocUtils.getString(name);
		} catch (Exception e) {
			return null;
		}
	}

	public String parse(Object result) {
		if(result == null)
			return "";
		if(result instanceof TotalNode) {
			return result.toString();
		}
		// result can't be null
		return parseFun.parse(result);
	}

	public String getName() {
		return name;
	}

	public String getDefaultDataFormat() {
		return defaultDataFormat;
	}

	public Integer getIndex() {
		return index;
	}

	public String getId() {
		return index.toString();
	}

	public static DataFormat getDataFormat(String name) {
		for (DataFormat dataFormat : DataFormat.values()) {
			if (null != dataFormat.name && dataFormat.name.equals(name)) {
				return dataFormat;
			}
		}
		return null;
	}

}
