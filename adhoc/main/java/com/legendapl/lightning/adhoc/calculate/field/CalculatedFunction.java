package com.legendapl.lightning.adhoc.calculate.field;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;

public class CalculatedFunction {

	private static final SimpleDateFormat DAY_OF_WEEK_JA = new SimpleDateFormat("EEEE", Locale.JAPANESE);

	private static final SimpleDateFormat DAY_OF_WEEK_EN = new SimpleDateFormat("EEEE", Locale.ENGLISH);

	private static SimpleDateFormat DAY_OF_WEEK = DAY_OF_WEEK_JA;

	private static final SimpleDateFormat MONTH_OF_YEAR_JA = new SimpleDateFormat("MMMM", Locale.JAPANESE);

	private static final SimpleDateFormat MONTH_OF_YEAR_EN = new SimpleDateFormat("MMMM", Locale.ENGLISH);

	private static SimpleDateFormat MONTH_OF_YEAR = MONTH_OF_YEAR_JA;

	private static final Calendar calendar = Calendar.getInstance();

	private static final Pattern TruePattern = Pattern.compile("true", Pattern.CASE_INSENSITIVE);

	private static final Pattern FalsePattern = Pattern.compile("false", Pattern.CASE_INSENSITIVE);

	static {
		AdhocUtils.isJapanese.addListener((ob, old, newValue) -> {
			DAY_OF_WEEK = newValue ? DAY_OF_WEEK_JA : DAY_OF_WEEK_EN;
			MONTH_OF_YEAR = newValue ? MONTH_OF_YEAR_JA : MONTH_OF_YEAR_EN;
		});
	}

	public Number Absolute(Number number) {
		if(number == null) {
			return null;
		}
		if(number instanceof Double || number instanceof BigDecimal) {
			return Math.abs(number.doubleValue());
		} else if(number instanceof Float) {
			return Math.abs(number.floatValue());
		} else if(number instanceof Integer || number instanceof Short || number instanceof Byte) {
			return Math.abs(number.intValue());
		} else if(number instanceof Long) {
			return Math.abs(number.longValue());
		} else {
			return null;
		}
	}

	public Double Round(Number number) {
		if(number == null) {
			return null;
		}
		return Round(number, 0);
	}

	public Double Round(Number number, Integer level) {
		if(number == null || level == null) {
			return null;
		}
		int temp = 1;
		for(int i=0;i<Math.abs(level);i++) {
			temp *= 10;
		}
		if(level >= 0) {
			return ((int)(number.doubleValue() * temp + 0.5)) / (double)temp;
		} else {
			return ((int)(number.doubleValue() / temp + 0.5)) * (double)temp;
		}
	}

	public String DayName(Date date) {
		if(date == null) {
			return null;
		}
		return DAY_OF_WEEK.format(date);
	}

	public Integer DayNumber(Date date) {
		if(date == null) {
			return null;
		}
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public String MonthName(Date date) {
		if(date == null) {
			return null;
		}
		return MONTH_OF_YEAR.format(date);
	}

	public Integer MonthNumber(Date date) {
		if(date == null) {
			return null;
		}
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	public Integer Year(Date date) {
		if(date == null) {
			return null;
		}
		return calendar.get(Calendar.YEAR);
	}

	public Date Today(Integer days) {
		if(days == null) {
			return null;
		}
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, 1);
		return today.getTime();
	}

	public Integer ElapsedDays(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
	    return (int) TimeUnit.DAYS.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
	}

	public Integer ElapsedHours(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
	    return (int) TimeUnit.HOURS.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
	}

	public Integer ElapsedMinutes(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
	    return (int) TimeUnit.MINUTES.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
	}

	public Integer ElapsedQuarters(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
		return (int) (ElapsedDays(date1, date2) / 365.0 / 4);
	}

	public Integer ElapsedSeconds(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
	    return (int) TimeUnit.SECONDS.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
	}

	public Integer ElapsedSemis(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
		return (int) (ElapsedDays(date1, date2) / 365.0 / 2);
	}

	public Integer ElapsedWeeks(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
		return ElapsedDays(date1, date2) / 7;
	}

	public Integer ElapsedYears(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return null;
		}
		return ElapsedDays(date1, date2) / 365;
	}

	public String Concatenate(String... args) {
		String result = "";
		for(String arg: args) {
			if (arg == null) {
				return null;
			}
			result += arg;
		}
		return result;
	}

	public Boolean Contains(String param1, String param2) {
		if(param1 == null || param2 == null) {
			return null;
		}
		return param1.contains(param2);
	}

	public Boolean StartsWith(String param1, String param2) {
		if(param1 == null || param2 == null) {
			return null;
		}
		return param1.startsWith(param2);
	}

	public Boolean EndsWith(String param1, String param2) {
		if(param1 == null || param2 == null) {
			return null;
		}
		return param1.endsWith(param2);
	}

	public Integer Length(String param) {
		if(param == null) {
			return null;
		}
		return param.length();
	}

	public String Mid(String text, Integer start, Integer end) {
		if(text == null || start == null || end == null || start <= 0 || end <=0) {
			return null;
		}
		return text.substring(start - 1, start - 1 + end);
	}

	public Boolean IsNull(Object ob) {
		return ob == null;
	}

	public Boolean NOT(Boolean ob) {
		if(ob == null) {
			return true;
		}
		return !ob.booleanValue();
	}

	public <T extends Object> T IF(Boolean flag, T return1, T return2) {
		if(flag == null || return1 == null || return2 == null) {
			return null;
		}
		return flag ? return1 : return2;
	}

	public Double Percent(Number a, Number b) {
		if(a == null || b == null) {
			return null;
		}
		Double result = a.doubleValue() * 100 * 100 / b.doubleValue();
		if(result.isInfinite()) {
			return 0.00;
		}
		return ((int)(a.doubleValue() * 100 * 100 / b.doubleValue())) / 100.0;
	}

	public Boolean INSet(Object ob, Object... args) {
		if(ob == null) {
			return null;
		}
		for(Object arg: args) {
			if(arg != null && ob.equals(arg)) {
				return true;
			}
		}
		return false;
	}


	public Boolean INRange(Number value, Number rangeLow, Number rangeHigh) {
		if(value == null || rangeLow == null || rangeHigh == null) {
			return null;
		}
		return value.doubleValue() >= rangeLow.doubleValue() && value.doubleValue() <= rangeHigh.doubleValue();
	}


	public Boolean INRange(Date value, Date rangeLow, Date rangeHigh) {
		if(value == null || rangeLow == null || rangeHigh == null) {
			return null;
		}
		return value.compareTo(rangeLow) >= 0 && value.compareTo(rangeHigh) <= 0;
	}

	public Boolean BooleanType(String text) {
		if(text == null) {
			return null;
		}
		if((TruePattern.matcher(text).find())) {
			return true;
		} else if(FalsePattern.matcher(text).find()) {
			return false;
		} else {
			return null;
		}
	}

	public Date DateType(String text) {
		try {
			return AdhocConstants.DataTime.dateFormat.parse(text);
		} catch (ParseException e) {
			return null;
		}
	}

	public Timestamp TimestampType(String text) {
		try {
			return Timestamp.valueOf(text);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	//TODO
	//どうやった、時間だけを保存する
	public Date TimeType(String text) {
		try {
			return AdhocConstants.DataTime.timeWithMSFormat.parse(text);
		} catch (ParseException e) {
			return null;
		}
	}

	public Integer IntegerType(String text) {
		try {
			return Integer.parseInt(text);
		} catch (Exception e) {
			return null;
		}
	}

	public Double DecimalType(String text) {
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			return null;
		}
	}

}
