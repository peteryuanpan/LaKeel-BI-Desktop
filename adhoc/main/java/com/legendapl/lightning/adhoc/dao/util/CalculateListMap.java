package com.legendapl.lightning.adhoc.dao.util;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.AdhocField;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.common.CalculateType;
import com.legendapl.lightning.adhoc.common.DataFormat;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableValueTreeFactory;

/**
 * クロス集計とテーブルの合計との計算関数
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.05.08
 */
public class CalculateListMap {
	
	protected static Logger logger = Logger.getLogger(CalculateListMap.class);

	@FunctionalInterface
	private static interface CalDFSQLFun {
		String parseField(Supplier<Stream<Object>> supplier, DataFormat df);
	}

	private static class Key {
		protected CalculateType calType;
		protected DataType dataType;

		Key(CalculateType calType, DataType dataType) {
			this.calType = calType;
			this.dataType = dataType;
		}

		private static final Map<DataType, Integer> dataTypeMap = Collections.unmodifiableMap(new HashMap<DataType, Integer>() {

					private static final long serialVersionUID = 5566507281244294861L;
					{
						// decimal
						put(DataType.BIGDECIMAL, 0);
						put(DataType.FLOAT, 0);
						put(DataType.DOUBLE, 0);
						// string
						put(DataType.BOOLEAN, 1);
						put(DataType.STRING, 1);
						// int
						put(DataType.BYTE, 2);
						put(DataType.SHORT, 2);
						put(DataType.LONG, 2);
						put(DataType.INTEGER, 2);
						// date
						put(DataType.DATE, 3);
						put(DataType.TIME, 3);
						put(DataType.TIMESTAMP, 3);
					}
				});

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Key))
				return false;
			Key key = (Key) o;
			if (calType == CalculateType.None && key.calType == calType) {
				return true;
			}
			return calType == key.calType && dataTypeMap.get(dataType) == dataTypeMap.get(key.dataType);
		}

		@Override
		public int hashCode() {
			if (calType == CalculateType.None) {
				return calType.getIndex();
			}
			return dataTypeMap.get(dataType) * 8 + calType.getIndex();
		}

		@Override
		public String toString() {
			return "Key [calType=" + calType + ", dataType=" + dataType + "]";
		}
	}

	private static final CalDFSQLFun NoneFun = (supplier, df) -> "";

	private static final CalDFSQLFun CountAllFun = (supplier, df) -> "" + supplier.get().count();

	private static final CalDFSQLFun CountDisFun = (supplier, df) -> "" + supplier.get().distinct().count();

	private static final CalDFSQLFun IntCountAllFun = (supplier, df) -> df.parse(supplier.get().count());

	private static final CalDFSQLFun IntCountDisFun = (supplier, df) -> df.parse(supplier.get().distinct().count());

	private static final CalDFSQLFun IntSumFun = (supplier, df) -> df
			.parse(supplier.get().mapToLong(data -> ((Number) data).longValue()).sum());

	private static final CalDFSQLFun DecimalSumFun = (supplier, df) -> df
			.parse(supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).sum());

	private static final CalDFSQLFun IntAvgFun = (supplier, df) -> DataFormat.FloatTwoDecimal
			.parse(supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).average().getAsDouble());

	private static final CalDFSQLFun DecimalAvgFun = (supplier, df) -> df
			.parse(supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).average().getAsDouble());

	private static final CalDFSQLFun IntMaxFun = (supplier, df) -> df
			.parse(supplier.get().mapToLong(data -> ((Number) data).longValue()).max().getAsLong());

	private static final CalDFSQLFun DecimalMaxFun = (supplier, df) -> df
			.parse(supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).max().getAsDouble());

	private static final CalDFSQLFun DateMaxFun = (supplier, df) -> df
			.parse(supplier.get().map(data -> (Date) data).max(Date::compareTo).get());

	private static final CalDFSQLFun IntMinFun = (supplier, df) -> df
			.parse(supplier.get().mapToLong(data -> ((Number) data).longValue()).min().getAsLong());

	private static final CalDFSQLFun DecimalMinFun = (supplier, df) -> df
			.parse(supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).min().getAsDouble());

	private static final CalDFSQLFun DateMinFun = (supplier, df) -> df
			.parse(supplier.get().map(data -> (Date) data).min(Date::compareTo).get());

	private static final CalDFSQLFun IntMedianFun = (supplier, df) -> {
		long size = supplier.get().count();
		return df.parse(size % 2 == 0
				? supplier.get().mapToLong(data -> ((Number) data).longValue()).sorted().skip(size / 2 - 1).findFirst().getAsLong()
				: supplier.get().mapToLong(data -> ((Number) data).longValue()).sorted().skip(size / 2).findFirst().getAsLong());
	};

	private static final CalDFSQLFun DecimalMedianFun = (supplier, df) -> {
		long size = supplier.get().count();
		return df.parse(size % 2 == 0
				? supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).sorted().skip(size / 2 - 1).findFirst().getAsDouble()
				: supplier.get().mapToDouble(data -> ((Number) data).doubleValue()).sorted().skip(size / 2).findFirst().getAsDouble());
	};

	private static final CalDFSQLFun DateMedianFun = (supplier, df) -> {
		long size = supplier.get().count();
		return df.parse(
				size % 2 == 0 ? supplier.get().map(data -> (Date) data).sorted().skip(size / 2 - 1).findFirst().get()
						: supplier.get().map(data -> (Date) data).sorted().skip(size / 2).findFirst().get());
	};

	private static final Map<Key, CalDFSQLFun> CalDBSQLFunMap = Collections.unmodifiableMap(new HashMap<Key, CalDFSQLFun>() {
				
				private static final long serialVersionUID = -7203899695371814626L;
				{
					put(new Key(CalculateType.None, DataType.UNKNOW), NoneFun);

					put(new Key(CalculateType.CountAll, DataType.BYTE), IntCountAllFun);

					put(new Key(CalculateType.CountAll, DataType.FLOAT), CountAllFun);

					put(new Key(CalculateType.CountAll, DataType.DATE), CountAllFun);

					put(new Key(CalculateType.CountAll, DataType.BOOLEAN), CountAllFun);

					put(new Key(CalculateType.CountDistinct, DataType.BYTE), IntCountDisFun);

					put(new Key(CalculateType.CountDistinct, DataType.FLOAT), CountDisFun);

					put(new Key(CalculateType.CountDistinct, DataType.DATE), CountDisFun);

					put(new Key(CalculateType.CountDistinct, DataType.BOOLEAN), CountDisFun);

					put(new Key(CalculateType.Average, DataType.BYTE), IntAvgFun);

					put(new Key(CalculateType.Average, DataType.FLOAT), DecimalAvgFun);

					put(new Key(CalculateType.Sum, DataType.BYTE), IntSumFun);

					put(new Key(CalculateType.Sum, DataType.FLOAT), DecimalSumFun);

					put(new Key(CalculateType.Maximum, DataType.BYTE), IntMaxFun);

					put(new Key(CalculateType.Maximum, DataType.FLOAT), DecimalMaxFun);

					put(new Key(CalculateType.Maximum, DataType.DATE), DateMaxFun);

					put(new Key(CalculateType.Minimum, DataType.BYTE), IntMinFun);

					put(new Key(CalculateType.Minimum, DataType.FLOAT), DecimalMinFun);

					put(new Key(CalculateType.Minimum, DataType.DATE), DateMinFun);

					put(new Key(CalculateType.Median, DataType.BYTE), IntMedianFun);

					put(new Key(CalculateType.Median, DataType.FLOAT), DecimalMedianFun);

					put(new Key(CalculateType.Median, DataType.DATE), DateMedianFun);
				}
			});

	//テーブルの関数
	public static String getCalculatedValue(AdhocField field, List<TableViewData> datas) {
		String res;
		try {
			Key key = new Key(field.getCalculateType(), field.getDataType());
			res = CalDBSQLFunMap.get(key).parseField(
					() -> datas.stream().map(data -> data.getValueByField(field)).filter(Objects::nonNull), field.getDataFormat());
		} catch (Exception e) {
			res = "";
		}
		return res;
	}

	//クロス集計の関数
	public static String getCalculatedCTValue(CrossTableField field, List<Object> datas) {
		String res;
		try {
			Key key = new Key(field.getCalculateType(), CrossTableValueTreeFactory.getDataType(field));
			res = CalDBSQLFunMap.get(key).parseField(
					() -> datas.stream().filter(Objects::nonNull), field.getDataFormat());
		} catch (Exception e) {
			res = "";
		}
		return res;
	}

}
