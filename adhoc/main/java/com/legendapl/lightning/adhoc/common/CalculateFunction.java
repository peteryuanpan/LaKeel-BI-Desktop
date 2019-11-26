package com.legendapl.lightning.adhoc.common;

/*
{   // IsNull ("FieldName")
    functions: ["IsNull"],
    code: '"' + getArg("FIELD") + '"'
},
{   // Absolute("NumberFieldName")
    functions: ["Absolute","Rank"],
    code: '"' + getArg("NUMBER_FIELD") + '"'
},
{   // DayNumber("DateFieldName")
    functions: ["DayName", "DayNumber", "MonthName", "MonthNumber", "Year"],
    code: '"' + getArg("DATE_FIELD") + '"'
},
{   // Length("TextFieldName")
    functions: ["Length"],
    code: '"' + getArg("TEXT_FIELD") + '"'
},
{   // CountAll("FieldName", 'Level')
    functions: ["Mode", "CountAll", "CountDistinct"],
    code: '"' + getArg("FIELD") + '", \'' + getArg("LEVEL") + "'"
},
{   // PercentOf("NumberFieldName", 'Level')
    functions: ["PercentOf", "Range", "StdDevP", "StdDevS", "Sum", "Average"],
    code: '"' + getArg("NUMBER_FIELD") + '", \'' + getArg("LEVEL") + "'"
},
{   // Round("NumberFieldName", Integer)
    functions: ["Round"],
    code: '"' + getArg("NUMBER_FIELD") + '", ' + getArg("INTEGER")
},
{   // WeightedAverage("NumberFieldName1", "NumberFieldName2", 'Level')
    functions: ["WeightedAverage"],
    code: '"' + getArg("NUMBER_FIELD") + '1", "' + getArg("NUMBER_FIELD") + '2", \'' + getArg("LEVEL") + "'"
},
{   // Median("NumberOrDateFieldName", 'Level')
    functions: ["Max", "Min", "Median"],
    code: '"' + getArg("NUMBER_OR_DATE_FIELD") + '", \'' + getArg("LEVEL") + "'"
},
{   // ElapsedDays("DateFieldName1", "DateFieldName2")
    functions: ["ElapsedDays", "ElapsedWeeks", "ElapsedMonths", "ElapsedQuarters", "ElapsedSemis", "ElapsedYears"],
    code: '"' + getArg("DATE_FIELD") + '1", "' + getArg("DATE_FIELD") + '2"'
},
{   // ElapsedHours("DateTimeFieldName1", "DateTimeFieldName2")
    functions: ["ElapsedHours", "ElapsedMinutes", "ElapsedSeconds"],
    code: '"' + getArg("DATETIME_FIELD") + '1", "' + getArg("DATETIME_FIELD") + '2"'
},
{   // startsWith("TextFieldName", 'text string')
    functions: ["Contains", "EndsWith", "StartsWith"],
    code: '"' + getArg("TEXT_FIELD") + '", \'' + getArg("TEXT_STRING") + "'"
},
{   // Concatenate("TextFieldName", 'text string', ...)
    functions: ["Concatenate"],
    code: '"' + getArg("TEXT_FIELD") + '", \'' + getArg("TEXT_STRING") + "', ..."
},
{   // Mid("TextFieldName", Start_Pos, Length)
    functions: ["Mid"],
    code: '"' + getArg("TEXT_FIELD") + '", ' + getArg("START_POSITION") + ", " + getArg("LENGTH")
},
{   // IF("BooleanFieldName", TrueCalc, FalseCalc)
    functions: ["IF"],
    code: '"' + getArg("BOOLEAN_FIELD") + '", ' + getArg("TRUE_CALC") + ", " + getArg("FALSE_CALC")
},
{   // Today(Integer_Offset)
    functions: ["Today"],
    code: getArg("INTEGER_OFFSET")
}
*/

public enum CalculateFunction {

	IsNull(0, "P122.function.IsNull", "P122.function.IsNull.withParam", "P122.function.description.IsNull"),
	Absolute(1, "P122.function.Absolute", "P122.function.Absolute.withParam", "P122.function.description.Absolute"),
	DayName(2, "P122.function.DayName", "P122.function.DayName.withParam", "P122.function.description.DayName"),
	DayNumber(3, "P122.function.DayNumber", "P122.function.DayNumber.withParam", "P122.function.description.DayNumber"),
	MonthName(4, "P122.function.MonthName", "P122.function.MonthName.withParam", "P122.function.description.MonthName"),
	MonthNumber(5, "P122.function.MonthNumber", "P122.function.MonthNumber.withParam", "P122.function.description.MonthNumber"),
	Year(6, "P122.function.Year", "P122.function.Year.withParam", "P122.function.description.Year"),
	Length(7, "P122.function.Length", "P122.function.Length.withParam", "P122.function.description.Length"),
	Round(8, "P122.function.Round", "P122.function.Round.withParam", "P122.function.description.Round"),
	ElapsedDays(9, "P122.function.ElapsedDays", "P122.function.ElapsedDays.withParam", "P122.function.description.ElapsedDays"),
	ElapsedWeeks(10, "P122.function.ElapsedWeeks", "P122.function.ElapsedWeeks.withParam", "P122.function.description.ElapsedWeeks"),
	ElapsedMonths(11, "P122.function.ElapsedMonths", "P122.function.ElapsedMonths.withParam", "P122.function.description.ElapsedMonths"),
	ElapsedQuarters(12, "P122.function.ElapsedQuarters", "P122.function.ElapsedQuarters.withParam", "P122.function.description.ElapsedQuarters"),
	ElapsedSemis(13, "P122.function.ElapsedSemis", "P122.function.ElapsedSemis.withParam", "P122.function.description.ElapsedSemis"),
	ElapsedYears(14, "P122.function.ElapsedYears", "P122.function.ElapsedYears.withParam", "P122.function.description.ElapsedYears"),
	ElapsedHours(15, "P122.function.ElapsedHours", "P122.function.ElapsedHours.withParam", "P122.function.description.ElapsedHours"),
	ElapsedMinutes(16, "P122.function.ElapsedMinutes", "P122.function.ElapsedMinutes.withParam", "P122.function.description.ElapsedMinutes"),
	ElapsedSeconds(17, "P122.function.ElapsedSeconds", "P122.function.ElapsedSeconds.withParam", "P122.function.description.ElapsedSeconds"),
	Contains(18, "P122.function.Contains", "P122.function.Contains.withParam", "P122.function.description.Contains"),
	EndsWith(19, "P122.function.EndsWith", "P122.function.EndsWith.withParam", "P122.function.description.EndsWith"),
	StartsWith(20, "P122.function.StartsWith", "P122.function.StartsWith.withParam", "P122.function.description.StartsWith"),
	Concatenate(21, "P122.function.Concatenate", "P122.function.Concatenate.withParam", "P122.function.description.Concatenate"),
	Mid(22, "P122.function.Mid", "P122.function.Mid.withParam", "P122.function.description.Mid"),
	IF(23, "P122.function.IF", "P122.function.IF.withParam", "P122.function.description.IF"),
	Date(24, "P122.function.Date", "P122.function.Date.withParam", "P122.function.description.Date"),
	Timestamp(25, "P122.function.Timestamp", "P122.function.Timestamp.withParam", "P122.function.description.Timestamp"),
	Time(26, "P122.function.Time", "P122.function.Time.withParam", "P122.function.description.Time"),
	Integer(27, "P122.function.Integer", "P122.function.Integer.withParam", "P122.function.description.Integer"),
	Decimal(28, "P122.function.Decimal", "P122.function.Decimal.withParam", "P122.function.description.Decimal"),
	Boolean(29, "P122.function.Boolean", "P122.function.Boolean.withParam", "P122.function.description.Boolean"),
	Today(30, "P122.function.Today", "P122.function.Today.withParam", "P122.function.description.Today");

	private int index;

	private String example;

	private String exampleWithParam;

	private String description;

	private CalculateFunction(int index, String example, String exampleWithParam, String description) {
		this.index = index;
		this.example = AdhocUtils.getString(example);
		this.exampleWithParam = AdhocUtils.getString(exampleWithParam);
		this.description = AdhocUtils.getString(description);
	}

	public String getDescription() {
		return description;
	}

	public int getIndex() {
		return index;
	}

	public String getExample() {
		return example;
	}

	public String getExampleWithParam() {
		return exampleWithParam;
	}

}
