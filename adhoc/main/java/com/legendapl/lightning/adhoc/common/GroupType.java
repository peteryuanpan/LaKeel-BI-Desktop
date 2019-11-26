package com.legendapl.lightning.adhoc.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import com.legendapl.lightning.adhoc.adhocView.model.trieTree.TotalNode;

public enum GroupType {
	Year("P121.crossTable.Year", 0, new ParseToString() {

		private final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

		@Override
		public String parse(Object result) {
			return YEAR_FORMAT.format(result);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {

		}

	}),
	Quarter("P121.crossTable.Quarter", 1, new ParseToString() {

		private final DateTimeFormatter QUARTER_FORMAT = DateTimeFormatter.ofPattern("'Q'q yyyy");

		@Override
		public String parse(Object result) {

			return new Date(((Date)result).getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(QUARTER_FORMAT);
		}

		@Override
		public void changeLanguage(boolean isJapanese) {

		}

	}),
	Month("P121.crossTable.Month", 2, new ParseToString() {

		//1月　2018
		private final DateFormat MONTH_FORMAT_JA = new SimpleDateFormat("MMMM yyyy", Locale.JAPANESE);
		//January, 2018
		private final DateFormat MONTH_FORMAT_EN = new SimpleDateFormat("MMMM, yyyy", Locale.ENGLISH);
		private DateFormat MONTH_FORMAT = MONTH_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return MONTH_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			MONTH_FORMAT = isJapanese ? MONTH_FORMAT_JA : MONTH_FORMAT_EN;
		}

	}),
	Day("P121.crossTable.Day", 3, new ParseToString() {

		//2018/01/02
		private final DateFormat DAY_FORMAT_JA = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPANESE);
		//Jan 2, 2018
		private final DateFormat DAY_FORMAT_EN = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
		private DateFormat DAY_FORMAT = DAY_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return DAY_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DAY_FORMAT = isJapanese ? DAY_FORMAT_JA : DAY_FORMAT_EN;
		}

	}),
	Hour("P121.crossTable.Hour", 4, new ParseToString() {
		//13:00
		private final DateFormat HOUR_FORMAT_JA = new SimpleDateFormat("H':00'", Locale.JAPANESE);
		//1:00 PM
		private final DateFormat HOUR_FORMAT_EN = new SimpleDateFormat("h':00' a", Locale.ENGLISH);
		private DateFormat HOUR_FORMAT = HOUR_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return HOUR_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			HOUR_FORMAT = isJapanese ? HOUR_FORMAT_JA : HOUR_FORMAT_EN;

		}

	}),
	Minute("P121.crossTable.Minute", 5, new ParseToString() {

		//13:01
		private final DateFormat MIN_FORMAT_JA = new SimpleDateFormat("H:mm", Locale.JAPANESE);
		//1:01 PM
		private final DateFormat MIN_FORMAT_EN = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
		private DateFormat MIN_FORMAT = MIN_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return MIN_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			MIN_FORMAT = isJapanese ? MIN_FORMAT_JA : MIN_FORMAT_EN;

		}

	}),
	Second("P121.crossTable.Second", 6, new ParseToString() {

		//13:01:01
		private final DateFormat SECOND_FORMAT_JA = new SimpleDateFormat("H:mm:ss", Locale.JAPANESE);
		//01:01:01 PM
		private final DateFormat SECOND_FORMAT_EN = new SimpleDateFormat("h:mm:ss a", Locale.ENGLISH);
		private DateFormat SECOND_FORMAT = SECOND_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return SECOND_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			SECOND_FORMAT = isJapanese ? SECOND_FORMAT_JA : SECOND_FORMAT_EN;

		}

	}),
	MilliSecond("P121.crossTable.MilliSecond", 7, new ParseToString() {

		//01:01:01.111
		private final DateFormat MS_FORMAT_JA = new SimpleDateFormat("HH:mm:ss.SSS", Locale.JAPANESE);
		//1:01:01.0001 PM
		private final DateFormat MS_FORMAT_EN = new SimpleDateFormat("h:mm:ss.SSS a", Locale.ENGLISH);
		private DateFormat MS_FORMAT = MS_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return MS_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			MS_FORMAT = isJapanese ? MS_FORMAT_JA : MS_FORMAT_EN;
		}

	}),
	HourByDay("P121.crossTable.HourByDay", 8, new ParseToString() {

		//18/01/01 1:00
		private final DateFormat DAY_FORMAT_JA = new SimpleDateFormat("yy/MM/dd H':00'", Locale.JAPANESE);
		//1/2/18 1:00 AM
		private final DateFormat DAY_FORMAT_EN = new SimpleDateFormat("M/d/y h':00' a", Locale.ENGLISH);
		private DateFormat DAY_FORMAT = DAY_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return DAY_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DAY_FORMAT = isJapanese ? DAY_FORMAT_JA : DAY_FORMAT_EN;
		}

	}),
	MinByDay("P121.crossTable.MinByDay", 9, new ParseToString() {

		//18/01/01 1:01
		private final DateFormat DAY_FORMAT_JA = new SimpleDateFormat("yy/MM/dd H:mm", Locale.JAPANESE);
		//1/2/18 1:01 AM
		private final DateFormat DAY_FORMAT_EN = new SimpleDateFormat("M/d/y h:mm a", Locale.ENGLISH);
		private DateFormat DAY_FORMAT = DAY_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return DAY_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DAY_FORMAT = isJapanese ? DAY_FORMAT_JA : DAY_FORMAT_EN;
		}

	}),
	SecByDay("P121.crossTable.SecByDay", 10, new ParseToString() {

		//18/01/01 1:01:01
		private final DateFormat DAY_FORMAT_JA = new SimpleDateFormat("yy/MM/dd H:mm:ss", Locale.JAPANESE);
		//1/2/18 1:01:01 AM
		private final DateFormat DAY_FORMAT_EN = new SimpleDateFormat("M/d/y h:mm:ss a", Locale.ENGLISH);
		private DateFormat DAY_FORMAT = DAY_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return DAY_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DAY_FORMAT = isJapanese ? DAY_FORMAT_JA : DAY_FORMAT_EN;
		}

	}),
	MSByDay("P121.crossTable.MSByDay", 11, new ParseToString() {

		//1 01, 2018 01:01:01.111 午前
		private final DateFormat DAY_FORMAT_JA = new SimpleDateFormat("MMM dd, yyyy h:mm:ss.SSS a", Locale.JAPANESE);
		//Jan 01, 2018 1:01:01 AM
		private final DateFormat DAY_FORMAT_EN = new SimpleDateFormat("MMM dd, yyyy h:mm:ss.SSS a", Locale.ENGLISH);
		private DateFormat DAY_FORMAT = DAY_FORMAT_JA;
		@Override
		public String parse(Object result) {
			return DAY_FORMAT.format(result);
		}
		@Override
		public void changeLanguage(boolean isJapanese) {
			DAY_FORMAT = isJapanese ? DAY_FORMAT_JA : DAY_FORMAT_EN;

		}

	});

	static {
		AdhocUtils.isJapanese.addListener((ob, old, newValue) -> {
			for(GroupType groupType: GroupType.values()) {
				groupType.changeLanguage(newValue);
			}
		});
	}

	private String name;
	private Integer index;
	private ParseToString parseFun;

	private GroupType(String name, Integer index, ParseToString parseFun) {
		this.name = name;
		this.index = index;
		this.parseFun = parseFun;
	}

	private void changeLanguage(boolean isJapanese) {
		parseFun.changeLanguage(isJapanese);

	}

	@Override
	public String toString() {
		try {
			return AdhocUtils.getString(name);
		} catch (Exception e) {
			return null;
		}
	}

	public Integer getIndex() {
		return index;
	}

	public String getId() {
		return index.toString();
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

}
