package com.bocloud.paas.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

	public static Date stringToDate(String date) {
		SimpleDateFormat format = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss", Locale.US);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}
}
