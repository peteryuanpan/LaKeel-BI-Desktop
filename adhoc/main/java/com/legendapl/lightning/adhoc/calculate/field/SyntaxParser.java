//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";

//#line 2 "test.y"
package com.legendapl.lightning.adhoc.calculate.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;
import com.legendapl.lightning.adhoc.model.Field;

//#line 28 "Parser.java"

public class SyntaxParser {

	boolean yydebug; // do I want debug output?
	int yynerrs; // number of errors so far
	int yyerrflag; // was there an error?
	int yychar; // the current working character

	// ########## MESSAGES ##########
	// ###############################################################
	// method: debug
	// ###############################################################
	void debug(String msg) {
		if (yydebug)
			System.out.println(msg);
	}

	// ########## STATE STACK ##########
	final static int YYSTACKSIZE = 500; // maximum stack size
	int statestk[] = new int[YYSTACKSIZE]; // state stack
	int stateptr;
	int stateptrmax; // highest index of stackptr
	int statemax; // state when highest index reached
	// ###############################################################
	// methods: state stack push,pop,drop,peek
	// ###############################################################

	final void state_push(int state) {
		try {
			stateptr++;
			statestk[stateptr] = state;
		} catch (ArrayIndexOutOfBoundsException e) {
			int oldsize = statestk.length;
			int newsize = oldsize * 2;
			int[] newstack = new int[newsize];
			System.arraycopy(statestk, 0, newstack, 0, oldsize);
			statestk = newstack;
			statestk[stateptr] = state;
		}
	}

	final int state_pop() {
		return statestk[stateptr--];
	}

	final void state_drop(int cnt) {
		stateptr -= cnt;
	}

	final int state_peek(int relative) {
		return statestk[stateptr - relative];
	}

	// ###############################################################
	// method: init_stacks : allocate and prepare stacks
	// ###############################################################
	final boolean init_stacks() {
		stateptr = -1;
		val_init();
		return true;
	}

	// ###############################################################
	// method: dump_stacks : show n levels of the stacks
	// ###############################################################
	void dump_stacks(int count) {
		int i;
		System.out.println("=index==state====value=     s:" + stateptr + "  v:" + valptr);
		for (i = 0; i < count; i++)
			System.out.println(" " + i + "    " + statestk[i] + "      " + valstk[i]);
		System.out.println("======================");
	}

	// ########## SEMANTIC VALUES ##########
	// public class ParserVal is defined in ParserVal.java

	String yytext;// user variable to return contextual strings
	ParserVal yyval; // used to return semantic vals from action routines
	ParserVal yylval;// the 'lval' (result) I got from yylex()
	ParserVal valstk[];
	int valptr;

	// ###############################################################
	// methods: value stack push,pop,drop,peek.
	// ###############################################################
	void val_init() {
		valstk = new ParserVal[YYSTACKSIZE];
		yyval = new ParserVal();
		yylval = new ParserVal();
		valptr = -1;
	}

	void val_push(ParserVal val) {
		if (valptr >= YYSTACKSIZE)
			return;
		valstk[++valptr] = val;
	}

	ParserVal val_pop() {
		if (valptr < 0)
			return new ParserVal();
		return valstk[valptr--];
	}

	void val_drop(int cnt) {
		int ptr;
		ptr = valptr - cnt;
		if (ptr < 0)
			return;
		valptr = ptr;
	}

	ParserVal val_peek(int relative) {
		int ptr;
		ptr = valptr - relative;
		if (ptr < 0)
			return new ParserVal();
		return valstk[ptr];
	}

	final ParserVal dup_yyval(ParserVal val) {
		ParserVal dup = new ParserVal();
		dup.ival = val.ival;
		dup.dval = val.dval;
		dup.sval = val.sval;
		dup.obj = val.obj;
		return dup;
	}

	// #### end semantic value section ####
	public final static short NOT = 257;
	public final static short LEFT_PAREN = 258;
	public final static short RT_PAREN = 259;
	public final static short FIELDNAME = 260;
	public final static short INT = 261;
	public final static short REAL = 262;
	public final static short CHARS = 263;
	public final static short ILLEGAL = 264;
	public final static short IsNull = 265;
	public final static short Absolute = 266;
	public final static short DayName = 267;
	public final static short DayNumber = 268;
	public final static short MonthName = 269;
	public final static short MonthNumber = 270;
	public final static short Year = 271;
	public final static short Length = 272;
	public final static short Round = 273;
	public final static short ElapsedDays = 274;
	public final static short ElapsedWeeks = 275;
	public final static short ElapsedMonths = 276;
	public final static short ElapsedQuarters = 277;
	public final static short ElapsedSemis = 278;
	public final static short ElapsedYears = 279;
	public final static short ElapsedHours = 280;
	public final static short ElapsedMinutes = 281;
	public final static short ElapsedSeconds = 282;
	public final static short Contains = 283;
	public final static short EndsWith = 284;
	public final static short StartsWith = 285;
	public final static short Concatenate = 286;
	public final static short Mid = 287;
	public final static short IF = 288;
	public final static short Today = 289;
	public final static short TRUE = 290;
	public final static short FALSE = 291;
	public final static short COMMA = 292;
	public final static short DATESIMPLE = 293;
	public final static short TIMESIMPLE = 294;
	public final static short TIMESTAMPSIMPLE = 295;
	public final static short DateType = 296;
	public final static short TimestampType = 297;
	public final static short TimeType = 298;
	public final static short IntegerType = 299;
	public final static short DecimalType = 300;
	public final static short BooleanType = 301;
	public final static short MINUS = 302;
	public final static short PLUS = 303;
	public final static short AND = 304;
	public final static short OR = 305;
	public final static short EQ = 306;
	public final static short GTR = 307;
	public final static short LESS = 308;
	public final static short LESS_EQ = 309;
	public final static short GTR_EQ = 310;
	public final static short NOT_EQ = 311;
	public final static short IN = 312;
	public final static short COLON = 313;
	public final static short PECENT = 314;
	public final static short MULTIS = 315;
	public final static short DIVIDE = 316;
	public final static short NEG = 317;
	public final static short YYERRCODE = 256;
	final static short yylhs[] = { -1, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10,
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 14, 14, 14, 14, 15, 15, 15,
			16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 9, 9, 11, 11, 11, 3, 4, 5, 6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 18, 18, 24, 24,
			19, 19, 25, 25, 20, 20, 26, 26, 21, 21, 27, 27, 22, 22, 28, 28, 23, 23, 29, 29, };
	final static short yylen[] = { 2, 1, 1, 1, 1, 1, 1, 1, 1, 4, 6, 4, 8, 8, 8, 4, 1, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6,
			6, 6, 6, 6, 4, 8, 4, 1, 4, 4, 4, 4, 5, 8, 8, 0, 3, 1, 4, 8, 4, 1, 4, 8, 1, 4, 8, 1, 1, 6, 6, 6, 4, 8, 4, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 2, 3, 3, 3, 3, 3, 1, 4,
			3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 7, 3, 3, 3, 3, 3, 3, 7, 3, 3, 3, 3, 3, 3, 7,
			3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 7, 0, 2, 0, 3, 0, 2, 0, 3, 0, 2, 0, 3, 0, 2, 0, 3, 0, 2, 0, 3, 0, 2, 0,
			3, };
	final static short yydefred[] = { 0, 0, 0, 16, 8, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 55, 56, 45, 49, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 72, 0, 91, 68, 69, 70, 71, 98, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 73, 92, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74, 93, 100, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65, 66, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 77, 85, 78, 86, 0, 0, 0, 0, 0, 0, 81, 96, 82, 97, 141, 136,
			137, 139, 138, 140, 0, 0, 120, 115, 116, 118, 117, 119, 0, 0, 127, 122, 123, 125, 124, 126, 0, 0, 134, 129,
			130, 132, 131, 133, 0, 101, 102, 108, 103, 104, 106, 105, 107, 0, 114, 109, 110, 112, 111, 113, 0, 99, 60,
			11, 32, 0, 36, 38, 17, 18, 37, 39, 19, 20, 21, 22, 0, 23, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 92, 46, 0, 0, 0, 0, 48, 53, 50, 34, 15, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 97, 0, 0, 150, 142,
			0, 0, 0, 154, 143, 0, 0, 0, 158, 144, 0, 0, 0, 162, 145, 0, 170, 147, 0, 0, 166, 146, 0, 0, 10, 0, 24, 30,
			27, 29, 31, 25, 26, 28, 57, 59, 58, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 152, 156, 121, 160, 128, 164, 135, 172, 168, 148, 41, 12,
			14, 13, 33, 42, 47, 51, 54, 61, 0, 0, };
	final static short yydgoto[] = { 42, 154, 155, 61, 62, 63, 64, 49, 50, 51, 52, 181, 53, 308, 54, 55, 56, 57, 328,
			331, 334, 337, 341, 339, 375, 380, 385, 390, 397, 393, };
	final static short yysindex[] = { 689, -245, 689, 0, 0, 0, -222, -220, -211, -209, -204, -195, -192, -165, -163,
			-124, -110, -90, -82, -80, -74, -61, -60, -58, -57, -56, -46, -41, -40, -39, 0, 0, 0, 0, 0, -38, -37, -23,
			-22, -19, -18, 734, 0, 377, 381, 414, 501, 524, 548, 471, 0, 558, 0, 0, 0, 0, 0, 0, 689, -170, -92, 414,
			501, 524, 548, 493, 689, 734, -197, -197, -197, -197, -197, -253, 734, 336, 336, 336, 336, 336, 336, 336,
			336, -253, -253, -253, -253, -253, 689, 769, -253, -253, -253, -253, -253, -253, 734, -5, 0, 0, 734, 734,
			734, 734, 734, 734, 734, 734, 734, 734, -253, -253, -253, -253, -253, -253, -4, -146, -146, -146, -146,
			-146, -146, -3, -257, -257, -257, -257, -257, -257, -2, -226, -226, -226, -226, -226, -226, -1, 689, 689,
			689, 689, 689, 689, 689, 689, 16, 734, 734, 734, 734, 734, 734, 17, 377, 381, 624, 0, 0, 0, -239, -34, -25,
			18, -93, -44, 3, 20, 24, 25, 27, 28, 33, 34, 36, 37, -247, 40, 0, 0, 0, -123, 23, 38, 39, 41, 58, 63, 65,
			67, 68, 69, 70, 81, 241, 769, 54, 71, 769, -259, 130, 135, 142, 145, 146, 156, -170, -92, 689, -201, -198,
			-201, -198, -264, -228, 0, 0, 0, 0, -201, -198, -201, -198, -264, -228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -253,
			161, 0, 0, 0, 0, 0, 0, -146, 162, 0, 0, 0, 0, 0, 0, -257, 164, 0, 0, 0, 0, 0, 0, -226, 0, 0, 0, 0, 0, 0, 0,
			0, 689, 0, 0, 0, 0, 0, 0, 734, 0, 0, 0, 0, 689, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 689, 0, 0, 769, 689, 336, 336,
			336, 336, 336, 336, 336, 336, -253, -253, -253, -253, 176, 769, 689, -238, 769, 689, 0, 0, 769, 769, 769,
			769, 0, 0, 0, 0, 0, 0, 768, 144, 179, 689, -283, 180, 689, -281, 181, 689, -274, 182, 777, 190, -273, 191,
			798, 807, -65, 828, 196, 197, 199, 200, 201, 202, 203, 205, 206, 208, 209, 70, 0, -89, -151, 386, 75, 343,
			436, 918, 837, -43, 858, -186, -186, 0, 0, 734, -253, 0, 0, 867, -146, -146, 0, 0, 888, -257, -257, 0, 0,
			897, -226, -226, 0, 0, 689, 0, 0, 734, 734, 0, 0, -197, -253, 0, 336, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			769, 734, 734, -253, -146, -257, -226, 689, 769, -151, 386, 144, -146, 150, 214, -257, 185, 221, -226, 189,
			227, 777, 225, 229, 232, 233, 239, 240, 50, 129, 169, 220, 269, 235, 236, 260, 262, 633, 224, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 769, 131, };
	final static short yyrindex[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 270, 2, 5, 6, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 612, 755,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 594, 604, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 282, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 364, 402, 427, 457, 195, 252, 0, 0, 0, 0, 482, 514, 537, 569, 308, 334, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 283, 0, 0, 0, 0, 0, 0, 0, 284, 0, 0, 0, 0, 0, 0, 0, 309, 0, 0, 0, 0, 0, 0, 0, 311, 0, 0, 0, 0,
			0, 0, 0, 0, 314, 0, 0, 0, 0, 0, 0, 315, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 327, 0, 0, 328, 0, 0,
			329, 0, 0, 337, 0, 338, 0, 339, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 282, 0, 0, 612, 755, 0, 0,
			0, 0, 0, 0, 0, -244, -242, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 327, 0, 328, 0, 0, 329,
			0, 0, 337, 0, 338, 339, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };
	final static short yygindex[] = { 192, 53, 1, 285, 111, 46, 4, 183, 0, 57, 0, -54, 0, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, -127, -112, 60, 165, 55, 166, };
	final static int YYTABLESIZE = 1230;
	static short yytable[];
	static {
		yytable();
	}

	static void yytable() {
		yytable = new short[] { 315, 44, 3, 60, 48, 4, 5, 6, 7, 378, 5, 383, 293, 58, 8, 95, 10, 94, 388, 395, 277, 158,
				182, 183, 184, 185, 186, 187, 188, 1, 379, 244, 384, 26, 27, 174, 66, 33, 67, 389, 396, 37, 99, 316,
				317, 294, 47, 68, 95, 69, 94, 103, 104, 43, 70, 59, 318, 319, 95, 95, 94, 94, 252, 71, 316, 317, 72, 44,
				162, 34, 48, 36, 165, 167, 169, 171, 173, 318, 319, 180, 180, 180, 180, 180, 180, 180, 180, 108, 109,
				157, 199, 163, 29, 73, 98, 74, 32, 207, 34, 35, 36, 210, 212, 214, 216, 218, 220, 222, 224, 226, 228,
				46, 47, 102, 103, 104, 107, 108, 109, 43, 161, 179, 179, 179, 179, 179, 179, 179, 179, 318, 319, 176,
				100, 101, 75, 253, 254, 255, 256, 257, 258, 416, 236, 29, 102, 103, 104, 32, 76, 206, 35, 100, 101, 209,
				211, 213, 215, 217, 219, 221, 223, 225, 227, 102, 103, 104, 281, 158, 77, 296, 245, 246, 247, 248, 249,
				250, 78, 46, 79, 164, 166, 168, 170, 172, 80, 65, 178, 178, 178, 178, 178, 178, 178, 178, 401, 87, 311,
				81, 82, 314, 83, 84, 85, 415, 269, 270, 271, 272, 273, 274, 105, 106, 86, 316, 317, 282, 279, 87, 88,
				89, 90, 91, 107, 108, 109, 278, 318, 319, 237, 238, 239, 240, 241, 242, 279, 92, 93, 316, 317, 94, 95,
				156, 346, 347, 348, 349, 350, 351, 352, 353, 318, 319, 89, 208, 235, 243, 251, 259, 160, 316, 317, 3,
				283, 336, 4, 5, 6, 7, 100, 101, 2, 194, 318, 319, 268, 275, 280, 105, 106, 284, 102, 103, 104, 285, 286,
				45, 287, 288, 1, 107, 108, 109, 289, 290, 291, 344, 292, 333, 295, 454, 180, 180, 180, 180, 180, 180,
				180, 180, 90, 464, 359, 361, 312, 367, 365, 297, 455, 369, 370, 371, 372, 260, 261, 262, 263, 264, 265,
				266, 267, 313, 298, 299, 340, 300, 88, 63, 63, 63, 63, 63, 63, 63, 179, 179, 179, 179, 179, 179, 179,
				179, 301, 45, 316, 317, 330, 302, 364, 303, 175, 304, 305, 306, 307, 360, 76, 318, 319, 418, 189, 190,
				191, 192, 193, 309, 425, 200, 201, 202, 203, 204, 205, 110, 111, 112, 113, 114, 115, 116, 465, 320, 468,
				326, 434, 435, 321, 229, 230, 231, 232, 233, 234, 322, 84, 440, 323, 324, 440, 178, 178, 178, 178, 178,
				178, 178, 178, 325, 443, 445, 447, 329, 332, 363, 335, 414, 453, 451, 424, 75, 466, 431, 432, 100, 101,
				316, 317, 358, 374, 440, 376, 381, 386, 391, 378, 102, 103, 104, 318, 319, 442, 394, 398, 338, 437, 438,
				87, 403, 404, 83, 405, 406, 407, 408, 409, 342, 410, 411, 450, 412, 413, 444, 446, 105, 106, 456, 343,
				475, 442, 383, 345, 467, 458, 388, 80, 107, 108, 109, 460, 87, 463, 428, 429, 457, 462, 366, 469, 470,
				368, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 87, 439, 89, 377, 439, 95, 382, 474, 395, 387, 471,
				327, 472, 100, 101, 419, 421, 316, 317, 468, 2, 449, 418, 420, 310, 102, 103, 104, 79, 439, 318, 319,
				43, 149, 153, 89, 138, 139, 140, 141, 142, 143, 144, 145, 146, 89, 89, 89, 89, 89, 89, 89, 89, 89, 89,
				89, 89, 89, 90, 157, 94, 161, 105, 106, 169, 165, 436, 64, 64, 64, 64, 64, 64, 64, 107, 108, 109, 151,
				155, 159, 354, 355, 356, 357, 88, 63, 362, 163, 171, 167, 459, 90, 0, 461, 0, 64, 452, 0, 0, 0, 0, 90,
				90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 90, 76, 177, 29, 88, 0, 0, 32, 33, 34, 35, 36, 37, 419, 88,
				88, 88, 88, 88, 88, 88, 88, 88, 88, 88, 88, 88, 117, 118, 119, 120, 121, 122, 123, 76, 0, 0, 426, 0, 84,
				0, 0, 0, 0, 76, 76, 76, 76, 76, 76, 76, 76, 76, 76, 76, 76, 417, 100, 101, 0, 0, 105, 106, 441, 75, 0,
				105, 106, 0, 102, 103, 104, 84, 107, 108, 109, 0, 0, 107, 108, 109, 448, 84, 84, 84, 84, 84, 84, 84, 84,
				84, 84, 84, 84, 83, 0, 0, 75, 110, 111, 112, 113, 114, 115, 116, 0, 420, 75, 75, 75, 75, 75, 75, 75, 75,
				75, 75, 75, 75, 80, 124, 125, 126, 127, 128, 129, 130, 83, 0, 0, 159, 0, 0, 0, 0, 0, 0, 83, 83, 83, 83,
				83, 83, 83, 83, 83, 83, 83, 83, 0, 0, 95, 80, 138, 139, 140, 141, 142, 143, 144, 145, 146, 80, 80, 80,
				80, 80, 80, 80, 80, 80, 80, 80, 80, 79, 138, 139, 140, 141, 142, 143, 144, 145, 146, 95, 117, 118, 119,
				120, 121, 122, 123, 0, 0, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 94, 79, 124, 125, 126, 127,
				128, 129, 130, 0, 0, 79, 79, 79, 79, 79, 79, 79, 79, 79, 79, 79, 79, 0, 0, 63, 131, 132, 133, 134, 135,
				136, 137, 94, 0, 64, 147, 148, 149, 150, 151, 152, 153, 94, 94, 94, 94, 94, 94, 94, 94, 94, 94, 94, 94,
				276, 0, 0, 63, 0, 0, 0, 0, 0, 473, 0, 0, 0, 64, 0, 63, 63, 63, 63, 63, 63, 63, 63, 63, 63, 64, 64, 64,
				64, 64, 64, 64, 64, 64, 64, 63, 63, 63, 63, 63, 63, 63, 0, 0, 0, 138, 139, 140, 141, 142, 143, 144, 145,
				146, 138, 139, 140, 141, 142, 143, 144, 145, 146, 1, 2, 0, 0, 3, 4, 5, 0, 6, 7, 8, 9, 10, 11, 12, 13,
				14, 15, 16, 0, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 33, 34, 35, 36, 37,
				38, 39, 40, 41, 96, 0, 0, 3, 4, 0, 0, 0, 7, 0, 9, 0, 11, 12, 13, 14, 15, 16, 0, 17, 18, 19, 20, 21, 22,
				0, 0, 0, 0, 0, 97, 0, 0, 0, 0, 195, 0, 0, 3, 0, 0, 38, 39, 196, 41, 9, 0, 11, 12, 13, 0, 15, 16, 0, 17,
				18, 19, 20, 21, 22, 0, 0, 0, 0, 0, 197, 0, 0, 373, 64, 64, 64, 64, 64, 64, 64, 38, 392, 0, 198, 138,
				139, 140, 141, 142, 143, 144, 145, 146, 138, 139, 140, 141, 142, 143, 144, 145, 146, 399, 0, 0, 0, 0, 0,
				0, 0, 0, 400, 0, 0, 138, 139, 140, 141, 142, 143, 144, 145, 146, 138, 139, 140, 141, 142, 143, 144, 145,
				146, 402, 0, 0, 0, 0, 0, 0, 0, 0, 422, 0, 0, 138, 139, 140, 141, 142, 143, 144, 145, 146, 138, 139, 140,
				141, 142, 143, 144, 145, 146, 423, 0, 0, 0, 0, 0, 0, 0, 0, 427, 0, 0, 138, 139, 140, 141, 142, 143, 144,
				145, 146, 138, 139, 140, 141, 142, 143, 144, 145, 146, 430, 0, 0, 0, 0, 0, 0, 0, 0, 433, 0, 0, 138, 139,
				140, 141, 142, 143, 144, 145, 146, 138, 139, 140, 141, 142, 143, 144, 145, 146, 421, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 131, 132, 133, 134, 135, 136, 137, };
	}

	static short yycheck[];
	static {
		yycheck();
	}

	static void yycheck() {
		yycheck = new short[] { 259, 0, 0, 2, 0, 0, 0, 0, 0, 292, 263, 292, 259, 258, 267, 259, 269, 259, 292, 292, 259,
				259, 76, 77, 78, 79, 80, 81, 82, 0, 313, 288, 313, 286, 287, 288, 258, 294, 258, 313, 313, 298, 41, 302,
				303, 292, 0, 258, 292, 258, 292, 315, 316, 0, 258, 2, 315, 316, 302, 303, 302, 303, 288, 258, 302, 303,
				258, 66, 67, 295, 66, 297, 68, 69, 70, 71, 72, 315, 316, 75, 76, 77, 78, 79, 80, 81, 82, 315, 316, 259,
				89, 288, 289, 258, 41, 258, 293, 96, 295, 296, 297, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 0,
				66, 314, 315, 316, 314, 315, 316, 66, 67, 75, 76, 77, 78, 79, 80, 81, 82, 315, 316, 74, 302, 303, 258,
				131, 132, 133, 134, 135, 136, 292, 288, 289, 314, 315, 316, 293, 258, 96, 296, 302, 303, 100, 101, 102,
				103, 104, 105, 106, 107, 108, 109, 314, 315, 316, 259, 259, 258, 292, 124, 125, 126, 127, 128, 129, 258,
				66, 258, 68, 69, 70, 71, 72, 258, 2, 75, 76, 77, 78, 79, 80, 81, 82, 259, 0, 195, 258, 258, 198, 258,
				258, 258, 292, 147, 148, 149, 150, 151, 152, 302, 303, 258, 302, 303, 259, 259, 258, 258, 258, 258, 258,
				314, 315, 316, 259, 315, 316, 117, 118, 119, 120, 121, 122, 259, 258, 258, 302, 303, 258, 258, 58, 296,
				297, 298, 299, 300, 301, 302, 303, 315, 316, 0, 258, 258, 258, 258, 258, 66, 302, 303, 259, 259, 259,
				259, 259, 259, 259, 302, 303, 0, 88, 315, 316, 258, 258, 258, 302, 303, 259, 314, 315, 316, 259, 259, 0,
				259, 259, 259, 314, 315, 316, 259, 259, 258, 294, 259, 251, 258, 426, 296, 297, 298, 299, 300, 301, 302,
				303, 0, 259, 309, 310, 258, 312, 310, 292, 428, 316, 317, 318, 319, 138, 139, 140, 141, 142, 143, 144,
				145, 258, 292, 292, 275, 292, 0, 306, 307, 308, 309, 310, 311, 312, 296, 297, 298, 299, 300, 301, 302,
				303, 292, 66, 302, 303, 243, 292, 310, 292, 73, 292, 292, 292, 292, 310, 0, 315, 316, 292, 83, 84, 85,
				86, 87, 292, 373, 90, 91, 92, 93, 94, 95, 306, 307, 308, 309, 310, 311, 312, 259, 259, 259, 208, 388,
				389, 259, 110, 111, 112, 113, 114, 115, 259, 0, 399, 259, 259, 402, 296, 297, 298, 299, 300, 301, 302,
				303, 259, 415, 416, 417, 258, 258, 310, 258, 357, 423, 421, 373, 0, 259, 383, 384, 302, 303, 302, 303,
				259, 292, 433, 259, 259, 259, 259, 292, 314, 315, 316, 315, 316, 402, 259, 259, 268, 395, 396, 259, 259,
				259, 0, 259, 259, 259, 259, 259, 280, 259, 259, 420, 259, 259, 416, 417, 302, 303, 259, 291, 474, 430,
				292, 295, 259, 259, 292, 0, 314, 315, 316, 259, 292, 259, 378, 379, 431, 437, 310, 259, 259, 313, 302,
				303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 399, 259, 329, 402, 0, 332, 292, 292, 335,
				259, 235, 259, 302, 303, 292, 292, 302, 303, 259, 259, 419, 292, 292, 292, 314, 315, 316, 0, 427, 315,
				316, 259, 259, 259, 292, 304, 305, 306, 307, 308, 309, 310, 311, 312, 302, 303, 304, 305, 306, 307, 308,
				309, 310, 311, 312, 313, 314, 259, 259, 0, 259, 302, 303, 259, 259, 392, 306, 307, 308, 309, 310, 311,
				312, 314, 315, 316, 259, 259, 259, 304, 305, 306, 307, 259, 0, 310, 259, 259, 259, 434, 292, -1, 436,
				-1, 0, 422, -1, -1, -1, -1, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 259, 288,
				289, 292, -1, -1, 293, 294, 295, 296, 297, 298, 292, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311,
				312, 313, 314, 306, 307, 308, 309, 310, 311, 312, 292, -1, -1, 374, -1, 259, -1, -1, -1, -1, 302, 303,
				304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 292, 302, 303, -1, -1, 302, 303, 400, 259, -1, 302,
				303, -1, 314, 315, 316, 292, 314, 315, 316, -1, -1, 314, 315, 316, 418, 302, 303, 304, 305, 306, 307,
				308, 309, 310, 311, 312, 313, 259, -1, -1, 292, 306, 307, 308, 309, 310, 311, 312, -1, 292, 302, 303,
				304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 259, 306, 307, 308, 309, 310, 311, 312, 292, -1, -1,
				259, -1, -1, -1, -1, -1, -1, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, -1, -1, 259,
				292, 304, 305, 306, 307, 308, 309, 310, 311, 312, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312,
				313, 259, 304, 305, 306, 307, 308, 309, 310, 311, 312, 292, 306, 307, 308, 309, 310, 311, 312, -1, -1,
				302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 259, 292, 306, 307, 308, 309, 310, 311, 312,
				-1, -1, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, -1, -1, 259, 306, 307, 308, 309,
				310, 311, 312, 292, -1, 259, 306, 307, 308, 309, 310, 311, 312, 302, 303, 304, 305, 306, 307, 308, 309,
				310, 311, 312, 313, 259, -1, -1, 292, -1, -1, -1, -1, -1, 259, -1, -1, -1, 292, -1, 304, 305, 306, 307,
				308, 309, 310, 311, 312, 313, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 306, 307, 308, 309, 310,
				311, 312, -1, -1, -1, 304, 305, 306, 307, 308, 309, 310, 311, 312, 304, 305, 306, 307, 308, 309, 310,
				311, 312, 257, 258, -1, -1, 261, 262, 263, -1, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275,
				-1, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, -1, 293, 294, 295, 296,
				297, 298, 299, 300, 301, 302, 258, -1, -1, 261, 262, -1, -1, -1, 266, -1, 268, -1, 270, 271, 272, 273,
				274, 275, -1, 277, 278, 279, 280, 281, 282, -1, -1, -1, -1, -1, 288, -1, -1, -1, -1, 258, -1, -1, 261,
				-1, -1, 299, 300, 266, 302, 268, -1, 270, 271, 272, -1, 274, 275, -1, 277, 278, 279, 280, 281, 282, -1,
				-1, -1, -1, -1, 288, -1, -1, 292, 306, 307, 308, 309, 310, 311, 312, 299, 292, -1, 302, 304, 305, 306,
				307, 308, 309, 310, 311, 312, 304, 305, 306, 307, 308, 309, 310, 311, 312, 292, -1, -1, -1, -1, -1, -1,
				-1, -1, 292, -1, -1, 304, 305, 306, 307, 308, 309, 310, 311, 312, 304, 305, 306, 307, 308, 309, 310,
				311, 312, 292, -1, -1, -1, -1, -1, -1, -1, -1, 292, -1, -1, 304, 305, 306, 307, 308, 309, 310, 311, 312,
				304, 305, 306, 307, 308, 309, 310, 311, 312, 292, -1, -1, -1, -1, -1, -1, -1, -1, 292, -1, -1, 304, 305,
				306, 307, 308, 309, 310, 311, 312, 304, 305, 306, 307, 308, 309, 310, 311, 312, 292, -1, -1, -1, -1, -1,
				-1, -1, -1, 292, -1, -1, 304, 305, 306, 307, 308, 309, 310, 311, 312, 304, 305, 306, 307, 308, 309, 310,
				311, 312, 292, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 306, 307, 308, 309, 310, 311, 312, };
	}

	final static short YYFINAL = 42;
	final static short YYMAXTOKEN = 317;
	final static String yyname[] = { "end-of-file", null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, "NOT", "LEFT_PAREN", "RT_PAREN",
			"FIELDNAME", "INT", "REAL", "CHARS", "ILLEGAL", "IsNull", "Absolute", "DayName", "DayNumber", "MonthName",
			"MonthNumber", "Year", "Length", "Round", "ElapsedDays", "ElapsedWeeks", "ElapsedMonths", "ElapsedQuarters",
			"ElapsedSemis", "ElapsedYears", "ElapsedHours", "ElapsedMinutes", "ElapsedSeconds", "Contains", "EndsWith",
			"StartsWith", "Concatenate", "Mid", "IF", "Today", "TRUE", "FALSE", "COMMA", "DATESIMPLE", "TIMESIMPLE",
			"TIMESTAMPSIMPLE", "DateType", "TimestampType", "TimeType", "IntegerType", "DecimalType", "BooleanType",
			"MINUS", "PLUS", "AND", "OR", "EQ", "GTR", "LESS", "LESS_EQ", "GTR_EQ", "NOT_EQ", "IN", "COLON", "PECENT",
			"MULTIS", "DIVIDE", "NEG", };
	final static String yyrule[] = { "$accept : ObjectExpression", "ObjectExpression : DoubleExpression",
			"ObjectExpression : IntegerExpression", "ObjectExpression : TextExpression",
			"ObjectExpression : DateExpression", "ObjectExpression : TimeExpression",
			"ObjectExpression : TimestampExpression", "ObjectExpression : BooleanExpression", "Double : REAL",
			"Double : Round LEFT_PAREN NumberExpression RT_PAREN",
			"Double : Round LEFT_PAREN NumberExpression COMMA IntegerExpression RT_PAREN",
			"Double : Absolute LEFT_PAREN DoubleExpression RT_PAREN",
			"Double : IF LEFT_PAREN BooleanExpression COMMA DoubleExpression COMMA DoubleExpression RT_PAREN",
			"Double : IF LEFT_PAREN BooleanExpression COMMA IntegerExpression COMMA DoubleExpression RT_PAREN",
			"Double : IF LEFT_PAREN BooleanExpression COMMA DoubleExpression COMMA IntegerExpression RT_PAREN",
			"Double : DecimalType LEFT_PAREN TextExpression RT_PAREN", "Integer : INT",
			"Integer : DayNumber LEFT_PAREN DateExpression RT_PAREN",
			"Integer : DayNumber LEFT_PAREN TimestampExpression RT_PAREN",
			"Integer : MonthNumber LEFT_PAREN DateExpression RT_PAREN",
			"Integer : MonthNumber LEFT_PAREN TimestampExpression RT_PAREN",
			"Integer : Year LEFT_PAREN DateExpression RT_PAREN",
			"Integer : Year LEFT_PAREN TimestampExpression RT_PAREN",
			"Integer : Length LEFT_PAREN TextExpression RT_PAREN",
			"Integer : ElapsedDays LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedHours LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedMinutes LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedQuarters LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedSeconds LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedSemis LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedWeeks LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : ElapsedYears LEFT_PAREN DateCommonExpression COMMA DateCommonExpression RT_PAREN",
			"Integer : Absolute LEFT_PAREN IntegerExpression RT_PAREN",
			"Integer : IF LEFT_PAREN BooleanExpression COMMA IntegerExpression COMMA IntegerExpression RT_PAREN",
			"Integer : IntegerType LEFT_PAREN TextExpression RT_PAREN", "String : CHARS",
			"String : DayName LEFT_PAREN DateExpression RT_PAREN",
			"String : MonthName LEFT_PAREN DateExpression RT_PAREN",
			"String : DayName LEFT_PAREN TimestampExpression RT_PAREN",
			"String : MonthName LEFT_PAREN TimestampExpression RT_PAREN",
			"String : Concatenate LEFT_PAREN TextExpression StringParam RT_PAREN",
			"String : Mid LEFT_PAREN TextExpression COMMA IntegerExpression COMMA IntegerExpression RT_PAREN",
			"String : IF LEFT_PAREN BooleanExpression COMMA TextExpression COMMA TextExpression RT_PAREN",
			"StringParam :", "StringParam : COMMA TextExpression StringParam", "Date : DATESIMPLE",
			"Date : Today LEFT_PAREN IntegerExpression RT_PAREN",
			"Date : IF LEFT_PAREN BooleanExpression COMMA DateExpression COMMA DateExpression RT_PAREN",
			"Date : DateType LEFT_PAREN TextExpression RT_PAREN", "Time : TIMESIMPLE",
			"Time : TimeType LEFT_PAREN TextExpression RT_PAREN",
			"Time : IF LEFT_PAREN BooleanExpression COMMA TimeExpression COMMA TimeExpression RT_PAREN",
			"Timestamp : TIMESTAMPSIMPLE", "Timestamp : TimestampType LEFT_PAREN TextExpression RT_PAREN",
			"Timestamp : IF LEFT_PAREN BooleanExpression COMMA TimestampExpression COMMA TimestampExpression RT_PAREN",
			"Boolean : TRUE", "Boolean : FALSE",
			"Boolean : Contains LEFT_PAREN TextExpression COMMA TextExpression RT_PAREN",
			"Boolean : StartsWith LEFT_PAREN TextExpression COMMA TextExpression RT_PAREN",
			"Boolean : EndsWith LEFT_PAREN TextExpression COMMA TextExpression RT_PAREN",
			"Boolean : IsNull LEFT_PAREN ObjectExpression RT_PAREN",
			"Boolean : IF LEFT_PAREN BooleanExpression COMMA BooleanExpression COMMA BooleanExpression RT_PAREN",
			"Boolean : BooleanType LEFT_PAREN TextExpression RT_PAREN", "NumberExpression : DoubleExpression",
			"NumberExpression : IntegerExpression", "DateCommonExpression : DateExpression",
			"DateCommonExpression : TimeExpression", "DateCommonExpression : TimestampExpression",
			"TextExpression : String", "DateExpression : Date", "TimeExpression : Time",
			"TimestampExpression : Timestamp", "DoubleExpression : Double", "DoubleExpression : MINUS DoubleExpression",
			"DoubleExpression : LEFT_PAREN DoubleExpression RT_PAREN",
			"DoubleExpression : DoubleExpression PLUS DoubleExpression",
			"DoubleExpression : DoubleExpression MINUS DoubleExpression",
			"DoubleExpression : DoubleExpression MULTIS DoubleExpression",
			"DoubleExpression : DoubleExpression DIVIDE DoubleExpression",
			"DoubleExpression : IntegerExpression PLUS DoubleExpression",
			"DoubleExpression : IntegerExpression MINUS DoubleExpression",
			"DoubleExpression : IntegerExpression MULTIS DoubleExpression",
			"DoubleExpression : IntegerExpression DIVIDE DoubleExpression",
			"DoubleExpression : DoubleExpression PLUS IntegerExpression",
			"DoubleExpression : DoubleExpression MINUS IntegerExpression",
			"DoubleExpression : DoubleExpression MULTIS IntegerExpression",
			"DoubleExpression : DoubleExpression DIVIDE IntegerExpression",
			"DoubleExpression : DoubleExpression PECENT DoubleExpression",
			"DoubleExpression : IntegerExpression PECENT IntegerExpression",
			"DoubleExpression : DoubleExpression PECENT IntegerExpression",
			"DoubleExpression : IntegerExpression PECENT DoubleExpression", "IntegerExpression : Integer",
			"IntegerExpression : MINUS IntegerExpression", "IntegerExpression : LEFT_PAREN IntegerExpression RT_PAREN",
			"IntegerExpression : IntegerExpression PLUS IntegerExpression",
			"IntegerExpression : IntegerExpression MINUS IntegerExpression",
			"IntegerExpression : IntegerExpression MULTIS IntegerExpression",
			"IntegerExpression : IntegerExpression DIVIDE IntegerExpression", "BooleanExpression : Boolean",
			"BooleanExpression : NOT LEFT_PAREN BooleanExpression RT_PAREN",
			"BooleanExpression : LEFT_PAREN BooleanExpression RT_PAREN",
			"BooleanExpression : BooleanExpression AND BooleanExpression",
			"BooleanExpression : BooleanExpression OR BooleanExpression",
			"BooleanExpression : BooleanExpression GTR BooleanExpression",
			"BooleanExpression : BooleanExpression LESS BooleanExpression",
			"BooleanExpression : BooleanExpression GTR_EQ BooleanExpression",
			"BooleanExpression : BooleanExpression LESS_EQ BooleanExpression",
			"BooleanExpression : BooleanExpression NOT_EQ BooleanExpression",
			"BooleanExpression : BooleanExpression EQ BooleanExpression",
			"BooleanExpression : NumberExpression GTR NumberExpression",
			"BooleanExpression : NumberExpression LESS NumberExpression",
			"BooleanExpression : NumberExpression GTR_EQ NumberExpression",
			"BooleanExpression : NumberExpression LESS_EQ NumberExpression",
			"BooleanExpression : NumberExpression NOT_EQ NumberExpression",
			"BooleanExpression : NumberExpression EQ NumberExpression",
			"BooleanExpression : DateExpression GTR DateExpression",
			"BooleanExpression : DateExpression LESS DateExpression",
			"BooleanExpression : DateExpression GTR_EQ DateExpression",
			"BooleanExpression : DateExpression LESS_EQ DateExpression",
			"BooleanExpression : DateExpression NOT_EQ DateExpression",
			"BooleanExpression : DateExpression EQ DateExpression",
			"BooleanExpression : DateExpression IN LEFT_PAREN DateExpression COLON DateExpression RT_PAREN",
			"BooleanExpression : TimeExpression GTR TimeExpression",
			"BooleanExpression : TimeExpression LESS TimeExpression",
			"BooleanExpression : TimeExpression GTR_EQ TimeExpression",
			"BooleanExpression : TimeExpression LESS_EQ TimeExpression",
			"BooleanExpression : TimeExpression NOT_EQ TimeExpression",
			"BooleanExpression : TimeExpression EQ TimeExpression",
			"BooleanExpression : TimeExpression IN LEFT_PAREN TimeExpression COLON TimeExpression RT_PAREN",
			"BooleanExpression : TimestampExpression GTR TimestampExpression",
			"BooleanExpression : TimestampExpression LESS TimestampExpression",
			"BooleanExpression : TimestampExpression GTR_EQ TimestampExpression",
			"BooleanExpression : TimestampExpression LESS_EQ TimestampExpression",
			"BooleanExpression : TimestampExpression NOT_EQ TimestampExpression",
			"BooleanExpression : TimestampExpression EQ TimestampExpression",
			"BooleanExpression : TimestampExpression IN LEFT_PAREN TimestampExpression COLON TimestampExpression RT_PAREN",
			"BooleanExpression : TextExpression GTR TextExpression",
			"BooleanExpression : TextExpression LESS TextExpression",
			"BooleanExpression : TextExpression GTR_EQ TextExpression",
			"BooleanExpression : TextExpression LESS_EQ TextExpression",
			"BooleanExpression : TextExpression NOT_EQ TextExpression",
			"BooleanExpression : TextExpression EQ TextExpression",
			"BooleanExpression : TextExpression IN LEFT_PAREN TextList RT_PAREN",
			"BooleanExpression : DateExpression IN LEFT_PAREN DateList RT_PAREN",
			"BooleanExpression : TimeExpression IN LEFT_PAREN TimeList RT_PAREN",
			"BooleanExpression : TimestampExpression IN LEFT_PAREN TimestampList RT_PAREN",
			"BooleanExpression : NumberExpression IN LEFT_PAREN NumberList RT_PAREN",
			"BooleanExpression : BooleanExpression IN LEFT_PAREN BooleanList RT_PAREN",
			"BooleanExpression : NumberExpression IN LEFT_PAREN NumberExpression COLON NumberExpression RT_PAREN",
			"TextList :", "TextList : TextExpression TextParam", "TextParam :",
			"TextParam : COMMA TextExpression TextParam", "DateList :", "DateList : DateExpression DateParam",
			"DateParam :", "DateParam : COMMA DateExpression DateParam", "TimeList :",
			"TimeList : TimeExpression TimeParam", "TimeParam :", "TimeParam : COMMA TimeExpression TimeParam",
			"TimestampList :", "TimestampList : TimestampExpression TimestampParam", "TimestampParam :",
			"TimestampParam : COMMA TimestampExpression TimestampParam", "NumberList :",
			"NumberList : NumberExpression NumberParam", "NumberParam :",
			"NumberParam : COMMA NumberExpression NumberParam", "BooleanList :",
			"BooleanList : BooleanExpression BooleanParam", "BooleanParam :",
			"BooleanParam : COMMA BooleanExpression BooleanParam", };

	// #line 300 "test.y"

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
		s = lexicalParser.next();
		switch (s.getType()) {
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

	// #line 980 "Parser.java"
	// ###############################################################
	// method: yylexdebug : check lexer state
	// ###############################################################
	void yylexdebug(int state, int ch) {
		String s = null;
		if (ch < 0)
			ch = 0;
		if (ch <= YYMAXTOKEN) // check index bounds
			s = yyname[ch]; // now get it
		if (s == null)
			s = "illegal-symbol";
		debug("state " + state + ", reading " + ch + " (" + s + ")");
	}

	// The following are now global, to aid in error reporting
	int yyn; // next next thing to do
	int yym; //
	int yystate; // current parsing state from state table
	String yys; // current token string

	// ###############################################################
	// method: yyparse : parse input and execute indicated items
	// ###############################################################
	int yyparse() {
		boolean doaction;
		init_stacks();
		yynerrs = 0;
		yyerrflag = 0;
		yychar = -1; // impossible char forces a read
		yystate = 0; // initial state
		state_push(yystate); // save it
		val_push(yylval); // save empty value
		while (true) // until parsing is done, either correctly, or w/error
		{
			doaction = true;
			if (yydebug)
				debug("loop");
			// #### NEXT ACTION (from reduction table)
			for (yyn = yydefred[yystate]; yyn == 0; yyn = yydefred[yystate]) {
				if (yydebug)
					debug("yyn:" + yyn + "  state:" + yystate + "  yychar:" + yychar);
				if (yychar < 0) // we want a char?
				{
					yychar = yylex(); // get next token
					if (yydebug)
						debug(" next yychar:" + yychar);
					// #### ERROR CHECK ####
					if (yychar < 0) // it it didn't work/error
					{
						yychar = 0; // change it to default string (no -1!)
						if (yydebug)
							yylexdebug(yystate, yychar);
					}
				} // yychar<0
				yyn = yysindex[yystate]; // get amount to shift by (shift index)
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == yychar) {
					if (yydebug)
						debug("state " + yystate + ", shifting to state " + yytable[yyn]);
					// #### NEXT STATE ####
					yystate = yytable[yyn];// we are in a new state
					state_push(yystate); // save it
					val_push(yylval); // push our lval as the input for next
										// rule
					yychar = -1; // since we have 'eaten' a token, say we need
									// another
					if (yyerrflag > 0) // have we recovered an error?
						--yyerrflag; // give ourselves credit
					doaction = false; // but don't process yet
					break; // quit the yyn=0 loop
				}

				yyn = yyrindex[yystate]; // reduce
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == yychar) { // we
																											// reduced!
					if (yydebug)
						debug("reduce");
					yyn = yytable[yyn];
					doaction = true; // get ready to execute
					break; // drop down to actions
				} else // ERROR RECOVERY
				{
					if (yyerrflag == 0) {
						yyerror("syntax error");
						yynerrs++;
					}
					if (yyerrflag < 3) // low error count?
					{
						yyerrflag = 3;
						while (true) // do until break
						{
							if (stateptr < 0) // check for under & overflow here
							{
								yyerror("stack underflow. aborting..."); // note
																			// lower
																			// case
																			// 's'
								return 1;
							}
							yyn = yysindex[state_peek(0)];
							if ((yyn != 0) && (yyn += YYERRCODE) >= 0 && yyn <= YYTABLESIZE
									&& yycheck[yyn] == YYERRCODE) {
								if (yydebug)
									debug("state " + state_peek(0) + ", error recovery shifting to state "
											+ yytable[yyn] + " ");
								yystate = yytable[yyn];
								state_push(yystate);
								val_push(yylval);
								doaction = false;
								break;
							} else {
								if (yydebug)
									debug("error recovery discarding state " + state_peek(0) + " ");
								if (stateptr < 0) // check for under & overflow
													// here
								{
									yyerror("Stack underflow. aborting..."); // capital
																				// 'S'
									return 1;
								}
								state_pop();
								val_pop();
							}
						}
					} else // discard this token
					{
						if (yychar == 0)
							return 1; // yyabort
						if (yydebug) {
							yys = null;
							if (yychar <= YYMAXTOKEN)
								yys = yyname[yychar];
							if (yys == null)
								yys = "illegal-symbol";
							debug("state " + yystate + ", error recovery discards token " + yychar + " (" + yys + ")");
						}
						yychar = -1; // read another
					}
				} // end error recovery
			} // yyn=0 loop
			if (!doaction) // any reason not to proceed?
				continue; // skip action
			yym = yylen[yyn]; // get count of terminals on rhs
			if (yydebug)
				debug("state " + yystate + ", reducing " + yym + " by rule " + yyn + " (" + yyrule[yyn] + ")");
			if (yym > 0) // if count of rhs not 'nil'
				yyval = val_peek(yym - 1); // get current semantic value
			yyval = dup_yyval(yyval); // duplicate yyval if ParserVal is used as
										// semantic value
			switch (yyn) {
			// ########## USER-SUPPLIED ACTIONS ##########
			case 1:
			// #line 68 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.DOUBLE;
			}
				break;
			case 2:
			// #line 69 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.INTEGER;
			}
				break;
			case 3:
			// #line 70 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.STRING;
			}
				break;
			case 4:
			// #line 71 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.DATE;
			}
				break;
			case 5:
			// #line 72 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.TIME;
			}
				break;
			case 6:
			// #line 73 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.TIMESTAMP;
			}
				break;
			case 7:
			// #line 74 "test.y"
			{
				jexlExpression = val_peek(0).sval;
				dataType = DataType.BOOLEAN;
			}
				break;
			case 8:
			// #line 77 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 9:
			// #line 78 "test.y"
			{
				yyval = new ParserVal("fun:Round(" + val_peek(1).sval + ")");
			}
				break;
			case 10:
			// #line 79 "test.y"
			{
				yyval = new ParserVal("fun:Round(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 11:
			// #line 80 "test.y"
			{
				yyval = new ParserVal("fun:Absolute(" + val_peek(1).sval + ")");
			}
				break;
			case 12:
			// #line 81 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 13:
			// #line 82 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 14:
			// #line 83 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 15:
			// #line 84 "test.y"
			{
				yyval = new ParserVal("fun:DecimalType(" + val_peek(1).sval + ")");
			}
				break;
			case 16:
			// #line 87 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 17:
			// #line 88 "test.y"
			{
				yyval = new ParserVal("fun:DayNumber(" + val_peek(1).sval + ")");
			}
				break;
			case 18:
			// #line 89 "test.y"
			{
				yyval = new ParserVal("fun:DayNumber(" + val_peek(1).sval + ")");
			}
				break;
			case 19:
			// #line 90 "test.y"
			{
				yyval = new ParserVal("fun:MonthNumber(" + val_peek(1).sval + ")");
			}
				break;
			case 20:
			// #line 91 "test.y"
			{
				yyval = new ParserVal("fun:MonthNumber(" + val_peek(1).sval + ")");
			}
				break;
			case 21:
			// #line 92 "test.y"
			{
				yyval = new ParserVal("fun:Year(" + val_peek(1).sval + ")");
			}
				break;
			case 22:
			// #line 93 "test.y"
			{
				yyval = new ParserVal("fun:Year(" + val_peek(1).sval + ")");
			}
				break;
			case 23:
			// #line 94 "test.y"
			{
				yyval = new ParserVal("fun:Length(" + val_peek(1).sval + ")");
			}
				break;
			case 24:
			// #line 95 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedDays(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 25:
			// #line 96 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedHours(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 26:
			// #line 97 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedMinutes(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 27:
			// #line 98 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedQuarters(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 28:
			// #line 99 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedSeconds(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 29:
			// #line 100 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedSemis(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 30:
			// #line 101 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedWeeks(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 31:
			// #line 102 "test.y"
			{
				yyval = new ParserVal("fun:ElapsedYears(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 32:
			// #line 103 "test.y"
			{
				yyval = new ParserVal("fun:Absolute(" + val_peek(1).sval + ")");
			}
				break;
			case 33:
			// #line 104 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 34:
			// #line 105 "test.y"
			{
				yyval = new ParserVal("fun:IntegerType(" + val_peek(1).sval + ")");
			}
				break;
			case 35:
			// #line 108 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 36:
			// #line 109 "test.y"
			{
				yyval = new ParserVal("fun:DayName(" + val_peek(1).sval + ")");
			}
				break;
			case 37:
			// #line 110 "test.y"
			{
				yyval = new ParserVal("fun:MonthName(" + val_peek(1).sval + ")");
			}
				break;
			case 38:
			// #line 111 "test.y"
			{
				yyval = new ParserVal("fun:DayName(" + val_peek(1).sval + ")");
			}
				break;
			case 39:
			// #line 112 "test.y"
			{
				yyval = new ParserVal("fun:MonthName(" + val_peek(1).sval + ")");
			}
				break;
			case 40:
			// #line 113 "test.y"
			{
				yyval = new ParserVal("fun:Concatenate(" + val_peek(2).sval + val_peek(1).sval + ")");
			}
				break;
			case 41:
			// #line 114 "test.y"
			{
				yyval = new ParserVal(
						"fun:Mid(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 42:
			// #line 115 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 43:
			// #line 118 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 44:
			// #line 119 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 45:
			// #line 122 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 46:
			// #line 123 "test.y"
			{
				yyval = new ParserVal("fun:Today(" + val_peek(1).sval + ")");
			}
				break;
			case 47:
			// #line 124 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 48:
			// #line 125 "test.y"
			{
				yyval = new ParserVal("fun:DateType(" + val_peek(1).sval + ")");
			}
				break;
			case 49:
			// #line 128 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 50:
			// #line 129 "test.y"
			{
				yyval = new ParserVal("fun:TimeType(" + val_peek(1).sval + ")");
			}
				break;
			case 51:
			// #line 130 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 52:
			// #line 133 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 53:
			// #line 134 "test.y"
			{
				yyval = new ParserVal("fun:TimestampType(" + val_peek(1).sval + ")");
			}
				break;
			case 54:
			// #line 135 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 55:
			// #line 138 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 56:
			// #line 139 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 57:
			// #line 140 "test.y"
			{
				yyval = new ParserVal("fun:Contains(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 58:
			// #line 141 "test.y"
			{
				yyval = new ParserVal("fun:StartsWith(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 59:
			// #line 142 "test.y"
			{
				yyval = new ParserVal("fun:EndsWith(" + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 60:
			// #line 143 "test.y"
			{
				yyval = new ParserVal("fun:IsNull(" + val_peek(1).sval + ")");
			}
				break;
			case 61:
			// #line 144 "test.y"
			{
				yyval = new ParserVal(
						"fun:IF(" + val_peek(5).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 62:
			// #line 145 "test.y"
			{
				yyval = new ParserVal("fun:BooleanType(" + val_peek(1).sval + ")");
			}
				break;
			case 63:
			// #line 148 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 64:
			// #line 149 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 65:
			// #line 152 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 66:
			// #line 153 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 67:
			// #line 154 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 68:
			// #line 157 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 69:
			// #line 160 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 70:
			// #line 163 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 71:
			// #line 166 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 72:
			// #line 169 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 73:
			// #line 170 "test.y"
			{
				yyval = new ParserVal("-" + val_peek(0).sval);
			}
				break;
			case 74:
			// #line 171 "test.y"
			{
				yyval = new ParserVal("(" + val_peek(1).sval + ")");
			}
				break;
			case 75:
			// #line 172 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " + " + val_peek(0).sval);
			}
				break;
			case 76:
			// #line 173 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " - " + val_peek(0).sval);
			}
				break;
			case 77:
			// #line 174 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " * " + val_peek(0).sval);
			}
				break;
			case 78:
			// #line 175 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " / " + val_peek(0).sval);
			}
				break;
			case 79:
			// #line 176 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " + " + val_peek(0).sval);
			}
				break;
			case 80:
			// #line 177 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " - " + val_peek(0).sval);
			}
				break;
			case 81:
			// #line 178 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " * " + val_peek(0).sval);
			}
				break;
			case 82:
			// #line 179 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " / " + val_peek(0).sval);
			}
				break;
			case 83:
			// #line 180 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " + " + val_peek(0).sval);
			}
				break;
			case 84:
			// #line 181 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " - " + val_peek(0).sval);
			}
				break;
			case 85:
			// #line 182 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " * " + val_peek(0).sval);
			}
				break;
			case 86:
			// #line 183 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " / " + val_peek(0).sval);
			}
				break;
			case 87:
			// #line 184 "test.y"
			{
				yyval = new ParserVal("fun:Percent(" + val_peek(2).sval + ", " + val_peek(0).sval + ")");
			}
				break;
			case 88:
			// #line 185 "test.y"
			{
				yyval = new ParserVal("fun:Percent(" + val_peek(2).sval + ", " + val_peek(0).sval + ")");
			}
				break;
			case 89:
			// #line 186 "test.y"
			{
				yyval = new ParserVal("fun:Percent(" + val_peek(2).sval + ", " + val_peek(0).sval + ")");
			}
				break;
			case 90:
			// #line 187 "test.y"
			{
				yyval = new ParserVal("fun:Percent(" + val_peek(2).sval + ", " + val_peek(0).sval + ")");
			}
				break;
			case 91:
			// #line 190 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 92:
			// #line 191 "test.y"
			{
				yyval = new ParserVal("-" + val_peek(0).sval);
			}
				break;
			case 93:
			// #line 192 "test.y"
			{
				yyval = new ParserVal("(" + val_peek(1).sval + ")");
			}
				break;
			case 94:
			// #line 193 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " + " + val_peek(0).sval);
			}
				break;
			case 95:
			// #line 194 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " - " + val_peek(0).sval);
			}
				break;
			case 96:
			// #line 195 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " * " + val_peek(0).sval);
			}
				break;
			case 97:
			// #line 196 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " / " + val_peek(0).sval);
			}
				break;
			case 98:
			// #line 199 "test.y"
			{
				yyval = val_peek(0);
			}
				break;
			case 99:
			// #line 200 "test.y"
			{
				yyval = new ParserVal("fun:NOT(" + val_peek(1).sval + ")");
			}
				break;
			case 100:
			// #line 201 "test.y"
			{
				yyval = new ParserVal("(" + val_peek(1).sval + ")");
			}
				break;
			case 101:
			// #line 202 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " and " + val_peek(0).sval);
			}
				break;
			case 102:
			// #line 203 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " or " + val_peek(0).sval);
			}
				break;
			case 103:
			// #line 204 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " > " + val_peek(0).sval);
			}
				break;
			case 104:
			// #line 205 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " < " + val_peek(0).sval);
			}
				break;
			case 105:
			// #line 206 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " >= " + val_peek(0).sval);
			}
				break;
			case 106:
			// #line 207 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " <= " + val_peek(0).sval);
			}
				break;
			case 107:
			// #line 208 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " != " + val_peek(0).sval);
			}
				break;
			case 108:
			// #line 209 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " == " + val_peek(0).sval);
			}
				break;
			case 109:
			// #line 210 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " > " + val_peek(0).sval);
			}
				break;
			case 110:
			// #line 211 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " < " + val_peek(0).sval);
			}
				break;
			case 111:
			// #line 212 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " >= " + val_peek(0).sval);
			}
				break;
			case 112:
			// #line 213 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " <= " + val_peek(0).sval);
			}
				break;
			case 113:
			// #line 214 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " != " + val_peek(0).sval);
			}
				break;
			case 114:
			// #line 215 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " == " + val_peek(0).sval);
			}
				break;
			case 115:
			// #line 216 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " > " + val_peek(0).sval);
			}
				break;
			case 116:
			// #line 217 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " < " + val_peek(0).sval);
			}
				break;
			case 117:
			// #line 218 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " >= " + val_peek(0).sval);
			}
				break;
			case 118:
			// #line 219 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " <= " + val_peek(0).sval);
			}
				break;
			case 119:
			// #line 220 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " != " + val_peek(0).sval);
			}
				break;
			case 120:
			// #line 221 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " == " + val_peek(0).sval);
			}
				break;
			case 121:
			// #line 222 "test.y"
			{
				yyval = new ParserVal(
						"fun:INRange(" + val_peek(6).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 122:
			// #line 223 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " > " + val_peek(0).sval);
			}
				break;
			case 123:
			// #line 224 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " < " + val_peek(0).sval);
			}
				break;
			case 124:
			// #line 225 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " >= " + val_peek(0).sval);
			}
				break;
			case 125:
			// #line 226 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " <= " + val_peek(0).sval);
			}
				break;
			case 126:
			// #line 227 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " != " + val_peek(0).sval);
			}
				break;
			case 127:
			// #line 228 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " == " + val_peek(0).sval);
			}
				break;
			case 128:
			// #line 229 "test.y"
			{
				yyval = new ParserVal(
						"fun:INRange(" + val_peek(6).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 129:
			// #line 230 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " > " + val_peek(0).sval);
			}
				break;
			case 130:
			// #line 231 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " < " + val_peek(0).sval);
			}
				break;
			case 131:
			// #line 232 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " >= " + val_peek(0).sval);
			}
				break;
			case 132:
			// #line 233 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " <= " + val_peek(0).sval);
			}
				break;
			case 133:
			// #line 234 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " != " + val_peek(0).sval);
			}
				break;
			case 134:
			// #line 235 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " == " + val_peek(0).sval);
			}
				break;
			case 135:
			// #line 236 "test.y"
			{
				yyval = new ParserVal(
						"fun:INRange(" + val_peek(6).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 136:
			// #line 237 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " > " + val_peek(0).sval);
			}
				break;
			case 137:
			// #line 238 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " < " + val_peek(0).sval);
			}
				break;
			case 138:
			// #line 239 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " >= " + val_peek(0).sval);
			}
				break;
			case 139:
			// #line 240 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " <= " + val_peek(0).sval);
			}
				break;
			case 140:
			// #line 241 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " != " + val_peek(0).sval);
			}
				break;
			case 141:
			// #line 242 "test.y"
			{
				yyval = new ParserVal(val_peek(2).sval + " == " + val_peek(0).sval);
			}
				break;
			case 142:
			// #line 243 "test.y"
			{
				yyval = new ParserVal("fun:INSet(" + val_peek(4).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 143:
			// #line 244 "test.y"
			{
				yyval = new ParserVal("fun:INSet(" + val_peek(4).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 144:
			// #line 245 "test.y"
			{
				yyval = new ParserVal("fun:INSet(" + val_peek(4).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 145:
			// #line 246 "test.y"
			{
				yyval = new ParserVal("fun:INSet(" + val_peek(4).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 146:
			// #line 247 "test.y"
			{
				yyval = new ParserVal("fun:INSet(" + val_peek(4).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 147:
			// #line 248 "test.y"
			{
				yyval = new ParserVal("fun:INSet(" + val_peek(4).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 148:
			// #line 249 "test.y"
			{
				yyval = new ParserVal(
						"fun:INRange(" + val_peek(6).sval + ", " + val_peek(3).sval + ", " + val_peek(1).sval + ")");
			}
				break;
			case 149:
			// #line 252 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 150:
			// #line 253 "test.y"
			{
				yyval = new ParserVal(val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 151:
			// #line 256 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 152:
			// #line 257 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 153:
			// #line 260 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 154:
			// #line 261 "test.y"
			{
				yyval = new ParserVal(val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 155:
			// #line 264 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 156:
			// #line 265 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 157:
			// #line 268 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 158:
			// #line 269 "test.y"
			{
				yyval = new ParserVal(val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 159:
			// #line 272 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 160:
			// #line 273 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 161:
			// #line 276 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 162:
			// #line 277 "test.y"
			{
				yyval = new ParserVal(val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 163:
			// #line 280 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 164:
			// #line 281 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 165:
			// #line 284 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 166:
			// #line 285 "test.y"
			{
				yyval = new ParserVal(val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 167:
			// #line 288 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 168:
			// #line 289 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 169:
			// #line 292 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 170:
			// #line 293 "test.y"
			{
				yyval = new ParserVal(val_peek(1).sval + val_peek(0).sval);
			}
				break;
			case 171:
			// #line 296 "test.y"
			{
				yyval = new ParserVal("");
			}
				break;
			case 172:
			// #line 297 "test.y"
			{
				yyval = new ParserVal(", " + val_peek(1).sval + val_peek(0).sval);
			}
				break;
			// #line 1817 "Parser.java"
			// ########## END OF USER-SUPPLIED ACTIONS ##########
			}// switch
				// #### Now let's reduce... ####
			if (yydebug)
				debug("reduce");
			state_drop(yym); // we just reduced yylen states
			yystate = state_peek(0); // get new state
			val_drop(yym); // corresponding value drop
			yym = yylhs[yyn]; // select next TERMINAL(on lhs)
			if (yystate == 0 && yym == 0)// done? 'rest' state and at first
											// TERMINAL
			{
				if (yydebug)
					debug("After reduction, shifting from state 0 to state " + YYFINAL + "");
				yystate = YYFINAL; // explicitly say we're done
				state_push(YYFINAL); // and save it
				val_push(yyval); // also save the semantic value of parsing
				if (yychar < 0) // we want another character?
				{
					yychar = yylex(); // get next character
					if (yychar < 0)
						yychar = 0; // clean, if necessary
					if (yydebug)
						yylexdebug(yystate, yychar);
				}
				if (yychar == 0) // Good exit (if lex returns 0 ;-)
					break; // quit the loop--all DONE
			} // if yystate
			else // else not done yet
			{ // get next state and push, for next yydefred[]
				yyn = yygindex[yym]; // find out where to go
				if ((yyn != 0) && (yyn += yystate) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
					yystate = yytable[yyn]; // get new state
				else
					yystate = yydgoto[yym]; // else go to new defred
				if (yydebug)
					debug("after reduction, shifting from state " + state_peek(0) + " to state " + yystate + "");
				state_push(yystate); // going again, so push state & val...
				val_push(yyval); // for next action
			}
		} // main loop
		return 0;// yyaccept!!
	}
	// ## end of method parse() ######################################

	// ## run() --- for Thread #######################################
	/**
	 * A default run method, used for operating this parser object in the
	 * background. It is intended for extending Thread or implementing Runnable.
	 * Turn off with -Jnorun .
	 */
	public void run() {
		yyparse();
	}
	// ## end of method run() ########################################

	// ## Constructors ###############################################
	/**
	 * Default constructor. Turn off with -Jnoconstruct .
	 *
	 */
	public SyntaxParser() {
		// nothing to do
	}

	/**
	 * Create a parser, setting the debug to true or false.
	 *
	 * @param debugMe
	 *            true for debugging, false for no debug.
	 */
	public SyntaxParser(boolean debugMe) {
		yydebug = debugMe;
	}
	// ###############################################################

}
// ################### END OF CLASS ##############################
