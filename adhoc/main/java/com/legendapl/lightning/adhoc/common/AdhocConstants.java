package com.legendapl.lightning.adhoc.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

/**
 * 定数クラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public final class AdhocConstants {

	public static class Debug {
		public static final Boolean ALL = true;
		public static final Boolean GenerateDataView = !ALL ? ALL : true;
		public static final Boolean LayoutFlowPane = !ALL ? ALL : false;
	}

	public static class Application {
		public static final String ADHOC_FILE_PATH = "adhoc/";
		public static final String WINDOWS_DESKTOP_PATH = System.getProperty("user.home") + "\\Desktop";
	}

	public static class Graphic {
		public static final int ADHOC_STAGE_MIN_WIDTH = 1150;
		public static final int ADHOC_STAGE_MIN_HEIGHT = 700;
		public static final int TOPIC_STAGE_MIN_WIDTH = 950;
		public static final int TOPIC_STAGE_MIN_HEIGHT = 600;
	}

	public static class MenuType {
		public static final String HOME = "Home";
		public static final String SERVER = "Server";
		public static final String LOCAL = "Local";
		public static final String SELECT = "Select";
		public static final String FILTER = "Filter";
		public static final String DISPLAY = "Display";
	}

	public static class Filter {
		public static final int MAX_LENGTH = 1000;
	}

	public static class CrossTable {
		public static final FontAwesomeIcon EXPANDEX_ICON = FontAwesomeIcon.MINUS_CIRCLE;
		public static final FontAwesomeIcon COLLAPSED_ICON = FontAwesomeIcon.PLUS_CIRCLE;
	}

	public static class AdhocProperty { // TODO : debug / EXE
		public static final AdhocModelType InitialViewModelType = AdhocModelType.TABLE;
		public static final AdhocModelType InitialDataModelType = AdhocModelType.FULLDATA;
	}

	public static class DataTime {
		public static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		public static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		public static final DateFormat timeWithMSFormat = new SimpleDateFormat("HH:mm:ss.SSS");

		public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		public static final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		public static final DateTimeFormatter HHmmTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		public static final DateTimeFormatter filterTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	}

	public static class StringDataTime {
		public static final String string_timeFormat = "HH:mm:ss";
		public static final String string_timestampFormat = "yyyy-MM-dd HH:mm:ss";
		public static final String string_dateFormat = "yyyy-MM-dd";
		public static final String string_timeWithMSFormat = "HH:mm:ss.SSS";

		public static final String string_timeFormatter = "HH:mm:ss";
		public static final String string_timestampFormatter = "yyyy-MM-dd HH:mm:ss";
		public static final String string_dateFormatter = "yyyy-MM-dd";
		public static final String string_HHmmTimeFormatter = "HH:mm";

		public static final String string_filterTimestampFormatter = "yyyy-MM-dd HH:mm";
	}
}
