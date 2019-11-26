package com.legendapl.lightning.adhoc.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.legendapl.lightning.adhoc.common.FilterType;
import com.legendapl.lightning.adhoc.common.OperationType;

/**
 *　ドメインのXMLのFilterStringを共通なFilterのモデルに変換する
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public abstract class GenericFilterSQL {

	protected static class Key {
		protected OperationType op;
		protected FilterType fp;

		public Key(OperationType op, FilterType fp) {
			super();
			this.op = op;
			this.fp = fp;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Key))
				return false;
			Key key = (Key) o;
			return op == key.op && fp == key.fp;
		}

		@Override
		public int hashCode() {
			return op.getIndex() * 7 + fp.getIndex();
		}

		@Override
		public String toString() {
			return "Key [op=" + op + ", fp=" + fp + "]";
		}

	}

	/**
	 *　@param　filter 共通なフィルタ
	 *　@return このフィルタを表示させるWhere句を作成する
	 */
	@FunctionalInterface
	protected static interface SQLFun {
		String parseFilter(Filter filter);
	}

	protected SQLFun bitEqual;
	protected SQLFun bitNotEqual;
	protected SQLFun numberEqual;
	protected SQLFun numberNotEqual;
	protected SQLFun timeEqual;
	protected SQLFun timeNotEqual;
	protected SQLFun dateEqual;
	protected SQLFun dateNotEqual;
	protected SQLFun timeStampEqual;
	protected SQLFun timeStampEqualNotEqual;
	protected SQLFun varcharEqual;
	protected SQLFun varcharNotEqual;
	protected SQLFun timeBefore;
	protected SQLFun timeAfter;
	protected SQLFun timeStampBefore;
	protected SQLFun timeStampAfter;
	protected SQLFun dateBefore;
	protected SQLFun dateAfter;
	protected SQLFun timeBeforeOn;
	protected SQLFun timeAfterOn;
	protected SQLFun timeStampBeforeOn;
	protected SQLFun timeStampAfterOn;
	protected SQLFun dateBeforeOn;
	protected SQLFun dateAfterOn;
	protected SQLFun numberGT;
	protected SQLFun numberLT;
	protected SQLFun numberGTEQ;
	protected SQLFun numberLTEQ;
	protected SQLFun numberBetween;
	protected SQLFun numberNotBetween;
	protected SQLFun timeBetween;
	protected SQLFun timeNotBetween;
	protected SQLFun dateBetween;
	protected SQLFun dateNotBetween;
	protected SQLFun timeStampBetween;
	protected SQLFun timeStampBetweenNotBetween;
	protected SQLFun varcharContain;
	protected SQLFun varcharNotContain;
	protected SQLFun varcharStarts;
	protected SQLFun varcharNotStarts;
	protected SQLFun varcharEnds;
	protected SQLFun varcharNotEnds;
	protected SQLFun varcharIn;
	protected SQLFun varcharNotIn;
	protected SQLFun numberIn;
	protected SQLFun numberNotIn;
	protected SQLFun bitIn;
	protected SQLFun bitNotIn;

	protected Map<Key, SQLFun> SQLFunMap;

	public String filter2SQL(Filter filter) {
		return SQLFunMap.get(new Key(filter.getOp(), filter.getFilterType())).parseFilter(filter);
	}

	/**
	 *　この関数はサブクラスを作成しから、呼び出さればならない
	 */
	protected final void initMap() {
		SQLFunMap = Collections.unmodifiableMap(new HashMap<Key, SQLFun>() {
			/**
			 *
			 */
			private static final long serialVersionUID = 2068487011634575314L;

			{
				put(new Key(OperationType.equals, FilterType.BOOLEAN), bitEqual);
				put(new Key(OperationType.isNotEqualTo, FilterType.BOOLEAN), bitNotEqual);
				put(new Key(OperationType.equals, FilterType.NUMBER), numberEqual);
				put(new Key(OperationType.isNotEqualTo, FilterType.NUMBER), numberNotEqual);
				put(new Key(OperationType.equals, FilterType.TIME), timeEqual);
				put(new Key(OperationType.isNotEqualTo, FilterType.TIME), timeNotEqual);
				put(new Key(OperationType.equals, FilterType.DATE), dateEqual);
				put(new Key(OperationType.isNotEqualTo, FilterType.DATE), dateNotEqual);
				put(new Key(OperationType.equals, FilterType.TIMESTAMP), timeStampEqual);
				put(new Key(OperationType.isNotEqualTo, FilterType.TIMESTAMP), timeStampEqualNotEqual);
				put(new Key(OperationType.equals, FilterType.STRING), varcharEqual);
				put(new Key(OperationType.isNotEqualTo, FilterType.STRING), varcharNotEqual);
				put(new Key(OperationType.isBefore, FilterType.TIME), timeBefore);
				put(new Key(OperationType.isAfter, FilterType.TIME), timeAfter);
				put(new Key(OperationType.isBefore, FilterType.TIMESTAMP), timeStampBefore);
				put(new Key(OperationType.isAfter, FilterType.TIMESTAMP), timeStampAfter);
				put(new Key(OperationType.isBefore, FilterType.DATE), dateBefore);
				put(new Key(OperationType.isAfter, FilterType.DATE), dateAfter);
				put(new Key(OperationType.isOnOrBefore, FilterType.TIME), timeBeforeOn);
				put(new Key(OperationType.isOnOrAfter, FilterType.TIME), timeAfterOn);
				put(new Key(OperationType.isOnOrBefore, FilterType.TIMESTAMP), timeStampBeforeOn);
				put(new Key(OperationType.isOnOrAfter, FilterType.TIMESTAMP), timeStampAfterOn);
				put(new Key(OperationType.isOnOrBefore, FilterType.DATE), dateBeforeOn);
				put(new Key(OperationType.isOnOrAfter, FilterType.DATE), dateAfterOn);
				put(new Key(OperationType.isGreaterThan, FilterType.NUMBER), numberGT);
				put(new Key(OperationType.lessThan, FilterType.NUMBER), numberLT);
				put(new Key(OperationType.isGreaterThanOrEqualTo, FilterType.NUMBER), numberGTEQ);
				put(new Key(OperationType.isLessThanOrEqualTo, FilterType.NUMBER), numberLTEQ);
				put(new Key(OperationType.isBetween, FilterType.NUMBER), numberBetween);
				put(new Key(OperationType.isNotBetween, FilterType.NUMBER), numberNotBetween);
				put(new Key(OperationType.isBetween, FilterType.TIME), timeBetween);
				put(new Key(OperationType.isNotBetween, FilterType.TIME), timeNotBetween);
				put(new Key(OperationType.isBetween, FilterType.DATE), dateBetween);
				put(new Key(OperationType.isNotBetween, FilterType.DATE), dateNotBetween);
				put(new Key(OperationType.isBetween, FilterType.TIMESTAMP), timeStampBetween);
				put(new Key(OperationType.isNotBetween, FilterType.TIMESTAMP), timeStampBetweenNotBetween);
				put(new Key(OperationType.contains, FilterType.STRING), varcharContain);
				put(new Key(OperationType.doesNotContain, FilterType.STRING), varcharNotContain);
				put(new Key(OperationType.startsWith, FilterType.STRING), varcharStarts);
				put(new Key(OperationType.doesNotStartWith, FilterType.STRING), varcharNotStarts);
				put(new Key(OperationType.endsWith, FilterType.STRING), varcharEnds);
				put(new Key(OperationType.doesNotEndWith, FilterType.STRING), varcharNotEnds);
				put(new Key(OperationType.isOneOf, FilterType.STRING), varcharIn);
				put(new Key(OperationType.isNotOneOf, FilterType.STRING), varcharNotIn);
				// 数値型の「次のいずれか」と「次のいずれかでない」を追加
				put(new Key(OperationType.isOneOf, FilterType.NUMBER), numberIn);
				put(new Key(OperationType.isNotOneOf, FilterType.NUMBER), numberNotIn);
				// ロジック型の「次のいずれか」と「次のいずれかでない」を追加
				put(new Key(OperationType.isOneOf, FilterType.BOOLEAN), bitIn);
				put(new Key(OperationType.isNotOneOf, FilterType.BOOLEAN), bitNotIn);
			}
		});
	}
}

