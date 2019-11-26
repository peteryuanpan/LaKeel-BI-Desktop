package com.legendapl.lightning.adhoc.filter.editor;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.DatabaseService;

import javafx.scene.layout.AnchorPane;

public abstract class GeneralEditor {

	protected static DatabaseInfo databaseInfo;

	protected ValidationSupport support = new ValidationSupport();
	protected ValidationSupport support1 = new ValidationSupport();
	protected boolean isRange = false;

	protected static String VALUE_SHOULD_BE_NOT_EMPTY = "ERROR_TOPIC_FILTER_VALUE_SHOULD_BE_NOT_EMPTY";
	protected static String WRONG_NUMBER_FORMAT = "ERROR_TOPIC_FILTER_WRONG_NUMBER_FORMAT";
	protected static String NUMBER_OUT_OF_BOUNDS = "ERROR_TOPIC_FILTER_NUMBER_OUT_OF_BOUNDS";
	protected static String NUMBER_RANGE_ERROR = "ERROR_TOPIC_FILTER_NUMBER_RANGE_ERROR";
	protected static String DATE_RANGE_ERROR = "ERROR_TOPIC_FILTER_DATE_RANGE_ERROR";
	protected static String WRONG_DATE_FORMAT = "ERROR_TOPIC_FILTER_WRONG_DATE_FORMAT";
	protected static String DATE_FORMAT_ERROR = "ERROR_TOPIC_FILTER_DATE_FORMAT_ERROR";
	protected static String INCORRECT_INTEGER_VALUE = "ERROR_TOPIC_FILTER_INCORRECT_INTEGER_VALUE";
	protected static String TOO_LONG_TEXT = "ERROR_TOPIC_FILTER_TOO_LONG_TEXT";
	protected static String INCORRECT_ATTRIBUTE_PATTERN = "ERROR_TOPIC_FILTER_INCORRECT_ATTRIBUTE_PATTERN";
	protected static DatabaseService dbService;
	protected static Adhoc adhoc;

	public GeneralEditor(Field field) {
		ValidationDecoration cssDecorator = new StyleClassValidationDecoration();
	    support.setValidationDecorator(cssDecorator);
	    support1.setValidationDecorator(cssDecorator);
	}

	public abstract void generateEditorPane(AnchorPane editorPane, Filter filter);

	public abstract boolean checkFilter(Filter filter);

	public abstract void fillFilter(Filter filter);

	public static void setDatabaseInfo(DatabaseInfo databaseInfo) {
		GeneralEditor.databaseInfo = databaseInfo;
		dbService = new DatabaseService(databaseInfo.getDataSource());
	}

	public static void setDatabaseInfo(Adhoc adhoc) {
		GeneralEditor.adhoc = adhoc;
		dbService = new DatabaseService(adhoc);
	}
}
