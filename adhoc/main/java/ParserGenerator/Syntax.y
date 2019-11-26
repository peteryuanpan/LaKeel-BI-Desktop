%{
package com.legendapl.lightning.adhoc.calculate.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.adhoc.model.Field;

%}

/* YACC Declarations */

%token NOT
%token LEFT_PAREN
%token RT_PAREN
%token FIELDNAME
%token INT
%token REAL
%token CHARS
%token ILLEGAL
%token IsNull
%token Absolute
%token DayName
%token DayNumber
%token MonthName
%token MonthNumber
%token Year
%token Length
%token Round
%token ElapsedDays
%token ElapsedWeeks
%token ElapsedMonths
%token ElapsedQuarters
%token ElapsedSemis
%token ElapsedYears
%token ElapsedHours
%token ElapsedMinutes
%token ElapsedSeconds
%token Contains
%token EndsWith
%token StartsWith
%token Concatenate
%token Mid
%token IF
%token Today
%token TRUE
%token FALSE
%token COMMA
%token DATESIMPLE
%token TIMESIMPLE
%token TIMESTAMPSIMPLE
%token DateType
%token TimestampType
%token TimeType
%token IntegerType
%token DecimalType
%token BooleanType
%left MINUS PLUS AND OR EQ GTR LESS LESS_EQ GTR_EQ NOT_EQ IN COLON
%left PECENT
%left MULTIS DIVIDE
%left NEG /* negation--unary minus */

/* Grammar follows */
%%
ObjectExpression : DoubleExpression { jexlExpression = $1.sval; dataType = DataType.DOUBLE; }
| IntegerExpression { jexlExpression = $1.sval; dataType = DataType.INTEGER; }
| TextExpression { jexlExpression = $1.sval; dataType = DataType.STRING; }
| DateExpression { jexlExpression = $1.sval; dataType = DataType.DATE; }
| TimeExpression { jexlExpression = $1.sval; dataType = DataType.TIME; }
| TimestampExpression { jexlExpression = $1.sval; dataType = DataType.TIMESTAMP; }
| BooleanExpression { jexlExpression = $1.sval; dataType = DataType.BOOLEAN; }
;

Double : REAL { $$ = $1; }
| Round LEFT_PAREN NumberExpression RT_PAREN { $$ = new ParserVal("fun:Round(" + $3.sval + ")"); }
| Round LEFT_PAREN NumberExpression COMMA IntegerExpression RT_PAREN { $$ = new ParserVal("fun:Round(" + $3.sval + ", " + $5.sval + ")"); }
| Absolute LEFT_PAREN DoubleExpression RT_PAREN { $$ = new ParserVal("fun:Absolute(" + $3.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA DoubleExpression COMMA DoubleExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA IntegerExpression COMMA DoubleExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA DoubleExpression COMMA IntegerExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| DecimalType LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:DecimalType(" + $3.sval + ")"); }
;

Integer : INT { $$ = $1; }
| DayNumber LEFT_PAREN DateExpression RT_PAREN { $$ = new ParserVal("fun:DayNumber(" + $3.sval + ")"); }
| DayNumber LEFT_PAREN TimestampExpression RT_PAREN { $$ = new ParserVal("fun:DayNumber(" + $3.sval + ")"); }
| MonthNumber LEFT_PAREN DateExpression RT_PAREN { $$ = new ParserVal("fun:MonthNumber(" + $3.sval + ")"); }
| MonthNumber LEFT_PAREN TimestampExpression RT_PAREN { $$ = new ParserVal("fun:MonthNumber(" + $3.sval + ")"); }
| Year LEFT_PAREN DateExpression RT_PAREN { $$ = new ParserVal("fun:Year(" + $3.sval + ")"); }
| Year LEFT_PAREN TimestampExpression RT_PAREN { $$ = new ParserVal("fun:Year(" + $3.sval + ")"); }
| Length LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:Length(" + $3.sval + ")"); }
| ElapsedDays LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedDays(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedHours LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedHours(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedMinutes LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedMinutes(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedQuarters LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedQuarters(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedSeconds LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedSeconds(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedSemis LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedSemis(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedWeeks LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedWeeks(" + $3.sval + ", " + $5.sval + ")"); }
| ElapsedYears LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN { $$ = new ParserVal("fun:ElapsedYears(" + $3.sval + ", " + $5.sval + ")"); }
| Absolute LEFT_PAREN IntegerExpression RT_PAREN { $$ = new ParserVal("fun:Absolute(" + $3.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA IntegerExpression COMMA IntegerExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| IntegerType LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:IntegerType(" + $3.sval + ")"); }
;

String : CHARS { $$ = $1; }
| DayName LEFT_PAREN DateExpression RT_PAREN { $$ = new ParserVal("fun:DayName(" + $3.sval + ")"); }
| MonthName LEFT_PAREN DateExpression RT_PAREN { $$ = new ParserVal("fun:MonthName(" + $3.sval + ")"); }
| DayName LEFT_PAREN TimestampExpression RT_PAREN { $$ = new ParserVal("fun:DayName(" + $3.sval + ")"); }
| MonthName LEFT_PAREN TimestampExpression RT_PAREN { $$ = new ParserVal("fun:MonthName(" + $3.sval + ")"); }
| Concatenate LEFT_PAREN TextExpression StringParam RT_PAREN { $$ = new ParserVal("fun:Concatenate(" + $3.sval + $4.sval + ")"); }
| Mid LEFT_PAREN TextExpression COMMA IntegerExpression COMMA IntegerExpression RT_PAREN { $$ = new ParserVal("fun:Mid(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA TextExpression COMMA TextExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
;

StringParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA TextExpression StringParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;

Date : DATESIMPLE { $$ = $1; }
| Today LEFT_PAREN IntegerExpression RT_PAREN { $$ = new ParserVal("fun:Today(" + $3.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA DateExpression COMMA DateExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| DateType LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:DateType(" + $3.sval + ")"); }
;

Time : TIMESIMPLE { $$ = $1; }
| TimeType LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:TimeType(" + $3.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA TimeExpression COMMA TimeExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
;

Timestamp: TIMESTAMPSIMPLE { $$ = $1; }
| TimestampType LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:TimestampType(" + $3.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA TimestampExpression COMMA TimestampExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
;

Boolean : TRUE { $$ = $1; }
| FALSE { $$ = $1; }
| Contains LEFT_PAREN TextExpression COMMA TextExpression RT_PAREN { $$ = new ParserVal("fun:Contains(" + $3.sval + ", " + $5.sval + ")"); }
| StartsWith LEFT_PAREN TextExpression COMMA TextExpression RT_PAREN { $$ = new ParserVal("fun:StartsWith(" + $3.sval + ", " + $5.sval + ")"); }
| EndsWith LEFT_PAREN TextExpression COMMA TextExpression RT_PAREN { $$ = new ParserVal("fun:EndsWith(" + $3.sval + ", " + $5.sval + ")"); }
| IsNull LEFT_PAREN ObjectExpression RT_PAREN { $$ = new ParserVal("fun:IsNull(" + $3.sval + ")"); }
| IF LEFT_PAREN BooleanExpression COMMA BooleanExpression COMMA BooleanExpression RT_PAREN { $$ = new ParserVal("fun:IF(" + $3.sval + ", " + $5.sval + ", " + $7.sval + ")"); }
| BooleanType LEFT_PAREN TextExpression RT_PAREN { $$ = new ParserVal("fun:BooleanType(" + $3.sval + ")"); }
;

NumberExpression : DoubleExpression { $$ = $1; }
| IntegerExpression { $$ = $1; }
;

DateCommonExpression : DateExpression { $$ = $1; }
| TimeExpression { $$ = $1; }
| TimestampExpression { $$ = $1; }
;

TextExpression : String { $$ = $1; }
;

DateExpression : Date { $$ = $1; }
;

TimeExpression : Time { $$ = $1; }
;

TimestampExpression : Timestamp { $$ = $1; }
;

DoubleExpression : Double { $$ = $1; }
| MINUS DoubleExpression %prec NEG { $$ = new ParserVal("-" + $2.sval); }
| LEFT_PAREN DoubleExpression RT_PAREN { $$ = new ParserVal("(" + $2.sval + ")"); }
| DoubleExpression PLUS DoubleExpression { $$ = new ParserVal($1.sval + " + " + $3.sval); }
| DoubleExpression MINUS DoubleExpression { $$ = new ParserVal($1.sval + " - " + $3.sval); }
| DoubleExpression MULTIS DoubleExpression { $$ = new ParserVal($1.sval + " * " + $3.sval); }
| DoubleExpression DIVIDE DoubleExpression { $$ = new ParserVal($1.sval + " / " + $3.sval); }
| IntegerExpression PLUS DoubleExpression { $$ = new ParserVal($1.sval + " + " + $3.sval); }
| IntegerExpression MINUS DoubleExpression { $$ = new ParserVal($1.sval + " - " + $3.sval); }
| IntegerExpression MULTIS DoubleExpression { $$ = new ParserVal($1.sval + " * " + $3.sval); }
| IntegerExpression DIVIDE DoubleExpression { $$ = new ParserVal($1.sval + " / " + $3.sval); }
| DoubleExpression PLUS IntegerExpression { $$ = new ParserVal($1.sval + " + " + $3.sval); }
| DoubleExpression MINUS IntegerExpression { $$ = new ParserVal($1.sval + " - " + $3.sval); }
| DoubleExpression MULTIS IntegerExpression { $$ = new ParserVal($1.sval + " * " + $3.sval); }
| DoubleExpression DIVIDE IntegerExpression{ $$ = new ParserVal($1.sval + " / " + $3.sval); }
| DoubleExpression PECENT DoubleExpression { $$ = new ParserVal("fun:Percent(" + $1.sval + ", " + $3.sval + ")"); }
| IntegerExpression PECENT IntegerExpression { $$ = new ParserVal("fun:Percent(" + $1.sval + ", " + $3.sval + ")"); }
| DoubleExpression PECENT IntegerExpression { $$ = new ParserVal("fun:Percent(" + $1.sval + ", " + $3.sval + ")"); }
| IntegerExpression PECENT DoubleExpression { $$ = new ParserVal("fun:Percent(" + $1.sval + ", " + $3.sval + ")"); }
;

IntegerExpression : Integer { $$ = $1; }
| MINUS IntegerExpression %prec NEG { $$ = new ParserVal("-" + $2.sval); }
| LEFT_PAREN IntegerExpression RT_PAREN { $$ = new ParserVal("(" + $2.sval + ")"); }
| IntegerExpression PLUS IntegerExpression { $$ = new ParserVal($1.sval + " + " + $3.sval); }
| IntegerExpression MINUS IntegerExpression { $$ = new ParserVal($1.sval + " - " + $3.sval); }
| IntegerExpression MULTIS IntegerExpression { $$ = new ParserVal($1.sval + " * " + $3.sval); }
| IntegerExpression DIVIDE IntegerExpression { $$ = new ParserVal($1.sval + " / " + $3.sval); }
;

BooleanExpression : Boolean { $$ = $1; }
| NOT LEFT_PAREN BooleanExpression RT_PAREN { $$ = new ParserVal("fun:NOT(" + $3.sval + ")"); }
| LEFT_PAREN BooleanExpression RT_PAREN { $$ = new ParserVal("(" + $2.sval + ")"); }
| BooleanExpression AND BooleanExpression { $$ = new ParserVal($1.sval + " and " + $3.sval); }
| BooleanExpression OR BooleanExpression { $$ = new ParserVal($1.sval + " or " + $3.sval); }
| BooleanExpression GTR BooleanExpression { $$ = new ParserVal($1.sval + " > " + $3.sval); }
| BooleanExpression LESS BooleanExpression { $$ = new ParserVal($1.sval + " < " + $3.sval); }
| BooleanExpression GTR_EQ BooleanExpression { $$ = new ParserVal($1.sval + " >= " + $3.sval); }
| BooleanExpression LESS_EQ BooleanExpression { $$ = new ParserVal($1.sval + " <= " + $3.sval); }
| BooleanExpression NOT_EQ BooleanExpression { $$ = new ParserVal($1.sval + " != " + $3.sval); }
| BooleanExpression EQ BooleanExpression { $$ = new ParserVal($1.sval + " == " + $3.sval); }
| NumberExpression GTR NumberExpression { $$ = new ParserVal($1.sval + " > " + $3.sval); }
| NumberExpression LESS NumberExpression { $$ = new ParserVal($1.sval + " < " + $3.sval); }
| NumberExpression GTR_EQ NumberExpression { $$ = new ParserVal($1.sval + " >= " + $3.sval); }
| NumberExpression LESS_EQ NumberExpression { $$ = new ParserVal($1.sval + " <= " + $3.sval); }
| NumberExpression NOT_EQ NumberExpression { $$ = new ParserVal($1.sval + " != " + $3.sval); }
| NumberExpression EQ NumberExpression { $$ = new ParserVal($1.sval + " == " + $3.sval); }
| DateExpression GTR DateExpression { $$ = new ParserVal($1.sval + " > " + $3.sval); }
| DateExpression LESS DateExpression { $$ = new ParserVal($1.sval + " < " + $3.sval); }
| DateExpression GTR_EQ DateExpression { $$ = new ParserVal($1.sval + " >= " + $3.sval); }
| DateExpression LESS_EQ DateExpression { $$ = new ParserVal($1.sval + " <= " + $3.sval); }
| DateExpression NOT_EQ DateExpression { $$ = new ParserVal($1.sval + " != " + $3.sval); }
| DateExpression EQ DateExpression { $$ = new ParserVal($1.sval + " == " + $3.sval); }
| DateExpression IN LEFT_PAREN DateExpression COLON DateExpression RT_PAREN { $$ = new ParserVal("fun:INRange(" + $1.sval + ", " + $4.sval + ", " + $6.sval + ")"); }
| TimeExpression GTR TimeExpression { $$ = new ParserVal($1.sval + " > " + $3.sval); }
| TimeExpression LESS TimeExpression { $$ = new ParserVal($1.sval + " < " + $3.sval); }
| TimeExpression GTR_EQ TimeExpression { $$ = new ParserVal($1.sval + " >= " + $3.sval); }
| TimeExpression LESS_EQ TimeExpression { $$ = new ParserVal($1.sval + " <= " + $3.sval); }
| TimeExpression NOT_EQ TimeExpression { $$ = new ParserVal($1.sval + " != " + $3.sval); }
| TimeExpression EQ TimeExpression { $$ = new ParserVal($1.sval + " == " + $3.sval); }
| TimeExpression IN LEFT_PAREN TimeExpression COLON TimeExpression RT_PAREN { $$ = new ParserVal("fun:INRange(" + $1.sval + ", " + $4.sval + ", " + $6.sval + ")"); }
| TimestampExpression GTR TimestampExpression { $$ = new ParserVal($1.sval + " > " + $3.sval); }
| TimestampExpression LESS TimestampExpression { $$ = new ParserVal($1.sval + " < " + $3.sval); }
| TimestampExpression GTR_EQ TimestampExpression { $$ = new ParserVal($1.sval + " >= " + $3.sval); }
| TimestampExpression LESS_EQ TimestampExpression { $$ = new ParserVal($1.sval + " <= " + $3.sval); }
| TimestampExpression NOT_EQ TimestampExpression { $$ = new ParserVal($1.sval + " != " + $3.sval); }
| TimestampExpression EQ TimestampExpression { $$ = new ParserVal($1.sval + " == " + $3.sval); }
| TimestampExpression IN LEFT_PAREN TimestampExpression COLON TimestampExpression RT_PAREN { $$ = new ParserVal("fun:INRange(" + $1.sval + ", " + $4.sval + ", " + $6.sval + ")"); }
| TextExpression GTR TextExpression { $$ = new ParserVal($1.sval + " > " + $3.sval); }
| TextExpression LESS TextExpression { $$ = new ParserVal($1.sval + " < " + $3.sval); }
| TextExpression GTR_EQ TextExpression { $$ = new ParserVal($1.sval + " >= " + $3.sval); }
| TextExpression LESS_EQ TextExpression { $$ = new ParserVal($1.sval + " <= " + $3.sval); }
| TextExpression NOT_EQ TextExpression { $$ = new ParserVal($1.sval + " != " + $3.sval); }
| TextExpression EQ TextExpression { $$ = new ParserVal($1.sval + " == " + $3.sval); }
| TextExpression IN LEFT_PAREN TextList RT_PAREN { $$ = new ParserVal("fun:INSet(" + $1.sval + ", " + $4.sval + ")"); }
| DateExpression IN LEFT_PAREN DateList RT_PAREN { $$ = new ParserVal("fun:INSet(" + $1.sval + ", " + $4.sval + ")"); }
| TimeExpression IN LEFT_PAREN TimeList RT_PAREN { $$ = new ParserVal("fun:INSet(" + $1.sval + ", " + $4.sval + ")"); }
| TimestampExpression IN LEFT_PAREN TimestampList RT_PAREN { $$ = new ParserVal("fun:INSet(" + $1.sval + ", " + $4.sval + ")"); }
| NumberExpression IN LEFT_PAREN NumberList RT_PAREN { $$ = new ParserVal("fun:INSet(" + $1.sval + ", " + $4.sval + ")"); }
| BooleanExpression IN LEFT_PAREN BooleanList RT_PAREN { $$ = new ParserVal("fun:INSet(" + $1.sval + ", " + $4.sval + ")"); }
| NumberExpression IN LEFT_PAREN NumberExpression COLON NumberExpression RT_PAREN { $$ = new ParserVal("fun:INRange(" + $1.sval + ", " + $4.sval + ", " + $6.sval + ")"); }
;

TextList: /* empty */ { $$ = new ParserVal(""); }
| TextExpression TextParam { $$ = new ParserVal($1.sval + $2.sval); }
;

TextParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA TextExpression TextParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;

DateList: /* empty */ { $$ = new ParserVal(""); }
| DateExpression DateParam { $$ = new ParserVal($1.sval + $2.sval); }
;

DateParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA DateExpression DateParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;

TimeList: /* empty */ { $$ = new ParserVal(""); }
| TimeExpression TimeParam { $$ = new ParserVal($1.sval + $2.sval); }
;

TimeParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA TimeExpression TimeParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;

TimestampList: /* empty */ { $$ = new ParserVal(""); }
| TimestampExpression TimestampParam { $$ = new ParserVal($1.sval + $2.sval); }
;

TimestampParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA TimestampExpression TimestampParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;

NumberList: /* empty */ { $$ = new ParserVal(""); }
| NumberExpression NumberParam { $$ = new ParserVal($1.sval + $2.sval); }
;

NumberParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA NumberExpression NumberParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;

BooleanList: /* empty */ { $$ = new ParserVal(""); }
| BooleanExpression BooleanParam { $$ = new ParserVal($1.sval + $2.sval); }
;

BooleanParam : /* empty */ { $$ = new ParserVal(""); }
| COMMA BooleanExpression BooleanParam { $$ = new ParserVal(", " + $2.sval + $3.sval); }
;
%%


		private Map<Integer, Short> tokenMap = Collections.unmodifiableMap(new HashMap<Integer, Short>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 7811070876391728640L;

		{
			put(LexicalType.AND, AND);
			put(LexicalType.OR, OR);
			put(LexicalType.MULTIS, MULTIS);
			put(LexicalType.PLUS, PLUS);
			put(LexicalType.MINUS, MINUS);
			put(LexicalType.DIVIDE, DIVIDE);
			put(LexicalType.LEFT_PAREN, LEFT_PAREN);
			put(LexicalType.RT_PAREN, RT_PAREN);
			put(LexicalType.EQ, EQ);
			put(LexicalType.GTR, GTR);
			put(LexicalType.LESS, LESS);
			put(LexicalType.LESS_EQ, LESS_EQ);
			put(LexicalType.GTR_EQ, GTR_EQ);
			put(LexicalType.NOT_EQ, NOT_EQ);
			put(LexicalType.COLON, COLON);
			put(LexicalType.FIELDNAME, FIELDNAME);
			put(LexicalType.INT, INT);
			put(LexicalType.REAL, REAL);
			put(LexicalType.CHARS, CHARS);
			put(LexicalType.ILLEGAL, ILLEGAL);
			put(LexicalType.IsNull, IsNull);
			put(LexicalType.Absolute, Absolute);
			put(LexicalType.DayName, DayName);
			put(LexicalType.DayNumber, DayNumber);
			put(LexicalType.MonthName, MonthName);
			put(LexicalType.MonthNumber, MonthNumber);
			put(LexicalType.Year, Year);
			put(LexicalType.Length, Length);
			put(LexicalType.Round, Round);
			put(LexicalType.ElapsedDays, ElapsedDays);
			put(LexicalType.ElapsedWeeks, ElapsedWeeks);
			put(LexicalType.ElapsedMonths, ElapsedMonths);
			put(LexicalType.ElapsedQuarters, ElapsedQuarters);
			put(LexicalType.ElapsedSemis, ElapsedSemis);
			put(LexicalType.ElapsedYears, ElapsedYears);
			put(LexicalType.ElapsedHours, ElapsedHours);
			put(LexicalType.ElapsedMinutes, ElapsedMinutes);
			put(LexicalType.ElapsedSeconds, ElapsedSeconds);
			put(LexicalType.Contains, Contains);
			put(LexicalType.EndsWith, EndsWith);
			put(LexicalType.StartsWith, StartsWith);
			put(LexicalType.Concatenate, Concatenate);
			put(LexicalType.Mid, Mid);
			put(LexicalType.IF, IF);
			put(LexicalType.DateType, DateType);
			put(LexicalType.TimestampType, TimestampType);
			put(LexicalType.TimeType, TimeType);
			put(LexicalType.IntegerType, IntegerType);
			put(LexicalType.DecimalType, DecimalType);
			put(LexicalType.BooleanType, BooleanType);
			put(LexicalType.Today, Today);
			put(LexicalType.TRUE, TRUE);
			put(LexicalType.FALSE, FALSE);
			put(LexicalType.COMMA, COMMA);
			put(LexicalType.NOT, NOT);
			put(LexicalType.IN, IN);
			put(LexicalType.PECENT, PECENT);
		}
	});

	private Map<DataType, Short> dataTypeMap = Collections.unmodifiableMap(new HashMap<DataType, Short>() {
		/**
		 *
		 */
		private static final long serialVersionUID = -9026429262234963007L;

		{
			put(DataType.BIGDECIMAL, REAL);
			put(DataType.BOOLEAN, TRUE);
			put(DataType.BYTE, INT);
			put(DataType.DATE, DATESIMPLE);
			put(DataType.TIME, TIMESIMPLE);
			put(DataType.TIMESTAMP, TIMESTAMPSIMPLE);
			put(DataType.DOUBLE, REAL);
			put(DataType.FLOAT, REAL);
			put(DataType.INTEGER, INT);
			put(DataType.LONG, INT);
			put(DataType.SHORT, INT);
			put(DataType.STRING, CHARS);
			put(DataType.UNKNOW, ILLEGAL);
		}
	});

	private LexicalParser lexicalParser;

	private String jexlExpression = "";

	public SyntaxParser(LexicalParser lexicalParser) {
		this.lexicalParser = lexicalParser;
	}

	private DataType dataType;

	private Map<String, Field> label2Field;

	public void setLabel2Field(Map<String, Field> label2Field) {
		this.label2Field = label2Field;
	}

	public String getJexlExpression() {
		return jexlExpression;
	}

	void yyerror(String s) {
		dataType = DataType.UNKNOW;
		System.out.println("par:" + s);
	}

	int yylex() {
		LexicalToken s;
		if (!lexicalParser.hasNext())
			return 0;
		//TODO  catch exception
		s = lexicalParser.next();
		switch(s.getType()) {
		case LexicalType.FIELDNAME:
			yylval = new ParserVal(SQLUtils.jexlResourceId(label2Field.get(s.getValue()).getResourceId()));
			return dataTypeMap.get(label2Field.get(s.getValue()).getDataType());
		case LexicalType.CHARS:
			yylval = new ParserVal("\"" + s.getValue() + "\"");
			return CHARS;
		case LexicalType.FALSE:
			yylval = new ParserVal("false");
			return FALSE;
		case LexicalType.TRUE:
			yylval = new ParserVal("true");
			return TRUE;
		case LexicalType.REAL:
			yylval = new ParserVal(s.getValue().toString());
			return REAL;
		case LexicalType.INT:
			yylval = new ParserVal(s.getValue().toString());
			return INT;
		default:
			return tokenMap.get(s.getType());
		}
	}

	public DataType dotest(String formula) {
		lexicalParser.lexicalParse(formula);
		yyparse();
		return dataType;
	}

	public static void main(String args[]) {
		SyntaxParser par = new SyntaxParser(false);
		par.dotest("");
	}