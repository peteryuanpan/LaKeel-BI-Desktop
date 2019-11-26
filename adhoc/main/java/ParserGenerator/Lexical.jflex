package com.legendapl.lightning.adhoc.calculate.field;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.model.Field;

%%

%standalone
%public
%class LexicalParser
%implements Iterator<LexicalToken>

%{

	public static class NoSuchFieldException extends RuntimeException{
		/**
		*
		*/
		private static final long serialVersionUID = 1L;

		NoSuchFieldException(String s) {
			super(s);
		}

		NoSuchFieldException() {
			super();
		}
    }

	public static class InvaildSymbolException extends RuntimeException{
		/**
		*
		*/
		private static final long serialVersionUID = 1L;

		InvaildSymbolException(String s) {
			super(s);
		}

		InvaildSymbolException() {
			super();
		}
    }

	private Map<String, Field> label2Field;

	private List<LexicalToken> words = new ArrayList<>();

	private List<String> subFields = new ArrayList<>();

	private int currentIndex = 0;

	public List<String> getSubFields() {
		return subFields;
	}

	public LexicalParser(Map<String, Field> label2Field) {
		this.label2Field = label2Field;
	}

	public void lexicalParse(String sentence) {
		words.clear();
		currentIndex = 0;
		subFields.clear();
		zzAtEOF = false;
		this.zzReader = new StringReader(sentence);
		while (!zzAtEOF) {
			try {
				yylex();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean hasNext() {
		return words.size() > currentIndex;
	}

	@Override
	public LexicalToken next() {
		return words.get(currentIndex++);
	}


%}


/*-*
 * PATTERN DEFINITIONS:
 */
digit           = [0-9]
fieldName       = \"[^\"]+\"
string          = '[^']*'
integer         = {digit}+
real            = {integer}\.{integer}
and             = (a|A)(n|N)(d|D)
or              = (o|O)(r|R)
in              = (i|I)(n|N)
not             = (n|N)(o|O)(t|T)
dateString      = d{string}
timestampString = ts{string}
timeString      = t{string}
whitespace      = [ \n\t]+



%%

/**
 * LEXICAL RULES:
 */
{fieldName}     { if (label2Field.get(yytext().subSequence(1, yytext().length() - 1)) != null) {
						words.add(new LexicalToken(LexicalType.FIELDNAME,
								yytext().subSequence(1, yytext().length() - 1)));
						subFields.add(label2Field.get(yytext().subSequence(1, yytext().length() - 1)).getResourceId());
					} else {
						throw new NoSuchFieldException(yytext());
					}
				}
{string}        { words.add(new LexicalToken(LexicalType.CHARS, yytext().subSequence(1, yytext().length()-1)));}
{dateString}         { words.add(new LexicalToken(LexicalType.DateType, null));
					   words.add(new LexicalToken(LexicalType.LEFT_PAREN, null));
					   words.add(new LexicalToken(LexicalType.CHARS, yytext().subSequence(2, yytext().length()-1)));
					   words.add(new LexicalToken(LexicalType.RT_PAREN, null));
					 }
{timestampString}     { words.add(new LexicalToken(LexicalType.TimestampType, null));
					   words.add(new LexicalToken(LexicalType.LEFT_PAREN, null));
					   words.add(new LexicalToken(LexicalType.CHARS, yytext().subSequence(3, yytext().length()-1)));
					   words.add(new LexicalToken(LexicalType.RT_PAREN, null));
					 }
{timestampString}     { words.add(new LexicalToken(LexicalType.TimeType, null));
					   words.add(new LexicalToken(LexicalType.LEFT_PAREN, null));
					   words.add(new LexicalToken(LexicalType.CHARS, yytext().subSequence(2, yytext().length()-1)));
					   words.add(new LexicalToken(LexicalType.RT_PAREN, null));
					 }

IsNull          { words.add(new LexicalToken(LexicalType.IsNull, null));}
{not}           { words.add(new LexicalToken(LexicalType.NOT, null));}
Absolute        { words.add(new LexicalToken(LexicalType.Absolute, null));}
DayName         { words.add(new LexicalToken(LexicalType.DayName, null));}
DayNumber       { words.add(new LexicalToken(LexicalType.DayNumber, null));}
MonthName       { words.add(new LexicalToken(LexicalType.MonthName, null));}
MonthNumber     { words.add(new LexicalToken(LexicalType.MonthNumber, null));}
Year            { words.add(new LexicalToken(LexicalType.Year, null));}
Length          { words.add(new LexicalToken(LexicalType.Length, null));}
Round           { words.add(new LexicalToken(LexicalType.Round, null));}
ElapsedDays     { words.add(new LexicalToken(LexicalType.ElapsedDays, null));}
ElapsedWeeks    { words.add(new LexicalToken(LexicalType.ElapsedWeeks, null));}
ElapsedMonths   { words.add(new LexicalToken(LexicalType.ElapsedMonths, null));}
ElapsedQuarters { words.add(new LexicalToken(LexicalType.ElapsedQuarters, null));}
ElapsedSemis    { words.add(new LexicalToken(LexicalType.ElapsedSemis, null));}
ElapsedYears    { words.add(new LexicalToken(LexicalType.ElapsedYears, null));}
ElapsedHours    { words.add(new LexicalToken(LexicalType.ElapsedHours, null));}
ElapsedMinutes  { words.add(new LexicalToken(LexicalType.ElapsedMinutes, null));}
ElapsedSeconds  { words.add(new LexicalToken(LexicalType.ElapsedSeconds, null));}
Contains        { words.add(new LexicalToken(LexicalType.Contains, null));}
EndsWith        { words.add(new LexicalToken(LexicalType.EndsWith, null));}
StartsWith      { words.add(new LexicalToken(LexicalType.StartsWith, null));}
Concatenate     { words.add(new LexicalToken(LexicalType.Concatenate, null));}
Mid             { words.add(new LexicalToken(LexicalType.Mid, null));}
IF              { words.add(new LexicalToken(LexicalType.IF, null));}
Today           { words.add(new LexicalToken(LexicalType.Today, null));}
Date            { words.add(new LexicalToken(LexicalType.DateType, null));}
Timestamp       { words.add(new LexicalToken(LexicalType.TimestampType, null));}
Time            { words.add(new LexicalToken(LexicalType.TimeType, null));}
Integer         { words.add(new LexicalToken(LexicalType.IntegerType, null));}
Decimal         { words.add(new LexicalToken(LexicalType.DecimalType, null));}
Boolean         { words.add(new LexicalToken(LexicalType.BooleanType, null));}
{and}           { words.add(new LexicalToken(LexicalType.AND, null));}
{or}            { words.add(new LexicalToken(LexicalType.OR, null));}
true            { words.add(new LexicalToken(LexicalType.TRUE, null));}
false           { words.add(new LexicalToken(LexicalType.FALSE, null));}
","             { words.add(new LexicalToken(LexicalType.COMMA, null));}
"*"             { words.add(new LexicalToken(LexicalType.MULTIS, null));}
"+"             { words.add(new LexicalToken(LexicalType.PLUS, null));}
"-"             { words.add(new LexicalToken(LexicalType.MINUS, null));}
"%"             { words.add(new LexicalToken(LexicalType.PECENT, null));}
"/"             { words.add(new LexicalToken(LexicalType.DIVIDE, null));}
"("             { words.add(new LexicalToken(LexicalType.LEFT_PAREN, null));}
")"             { words.add(new LexicalToken(LexicalType.RT_PAREN, null));}
"=="             { words.add(new LexicalToken(LexicalType.EQ, null));}
"<"             { words.add(new LexicalToken(LexicalType.LESS, null));}
">"             { words.add(new LexicalToken(LexicalType.GTR, null));}
"<="            { words.add(new LexicalToken(LexicalType.LESS_EQ, null));}
">="            { words.add(new LexicalToken(LexicalType.GTR_EQ, null));}
"!="            { words.add(new LexicalToken(LexicalType.NOT_EQ, null));}
":"             { words.add(new LexicalToken(LexicalType.COLON, null));}
{in}            { words.add(new LexicalToken(LexicalType.IN, null));}
{integer}       { words.add(new LexicalToken(LexicalType.INT, yytext()));}
{real}          { words.add(new LexicalToken(LexicalType.REAL, yytext()));}
{whitespace}    { }
.               { throw new NoSuchFieldException(yytext());}
