package com.legendapl.lightning.validation;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

public class JFXDatePickerConverter extends StringConverter<LocalDate>{

	@Override
	public String toString(LocalDate date) {
		if(date != null) {
			String month = date.getMonthValue() + "";
			String day = date.getDayOfMonth() + "";
			if(month.length() == 1) {
				month = "0" + month;
			}
			if(day.length() == 1) {
				day = "0" + day;
			}
			return date.getYear() + "-" + month + "-" + day;
		}
		else
			return null;
	}

	@Override
	public LocalDate fromString(String string) {
		if(string == null || string.isEmpty())
			return null;
		LocalDate result = null;
		Pattern pattern = Pattern.compile("([0-9]{4})[^0-9]([0-9]{1,2})[^0-9]([0-9]{1,2})");
		Matcher matcher = pattern.matcher(string);
		if(matcher.find() && matcher.groupCount() == 3) {
			int year = Integer.parseInt(matcher.group(1));
			int month = Integer.parseInt(matcher.group(2));
			int dayOfMonth = Integer.parseInt(matcher.group(3));
			try {
				result = LocalDate.of(year, month, dayOfMonth);
				return result;
			} catch(Exception e) {
				e.printStackTrace();
			}

		}
		return result;
	}


}
