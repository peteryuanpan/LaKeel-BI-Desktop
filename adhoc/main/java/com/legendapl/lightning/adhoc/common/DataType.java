package com.legendapl.lightning.adhoc.common;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * データ　タイプ
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.02.06
 */
public enum DataType {

	//UNKONW's index should be 0
	UNKNOW("", "", 0, FilterType.UNKNOW, null, DataFormat.GenericFormat, CalculateType.None, null, null, null, null, null),

	STRING("java.lang.String", "java.lang.String", 1, FilterType.STRING,
			(rs, fieldId) -> {
				String result = rs.getString(fieldId);
				if(result == null)
					return null;
				return result;
			},
			DataFormat.GenericFormat, CalculateType.CountAll, null,
			null, Collections.noneCalculateTypes, null,
			ob -> ob == null ? null : ob.toString()
	),
	BOOLEAN("java.lang.Boolean", "java.lang.Boolean", 2, FilterType.BOOLEAN,
			(rs, fieldId) -> {
				Boolean result = rs.getBoolean(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.GenericFormat, CalculateType.CountAll, null,
			null, Collections.noneCalculateTypes, null,
			ob -> ob == null ? null : Boolean.parseBoolean(ob.toString())),
	LONG("java.lang.Long", "java.lang.Long", 3, FilterType.NUMBER,
			(rs, fieldId) -> {
				Long result = rs.getLong(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.IntegerHasComma, CalculateType.Sum, null,
			Collections.intDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : Long.parseLong(ob.toString())
	),
	TIMESTAMP("java.sql.Timestamp", "java.time.LocalDateTime", 4, FilterType.TIMESTAMP,
			(rs, fieldId) -> {
				Timestamp result = rs.getTimestamp(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.TimeStampYYMMDDHHMMSS, CalculateType.CountAll, GroupType.Day,
			Collections.timeStampDataFormats, Collections.dateCalculateTypes, Collections.timeStampGroupTypes,
			ob -> ob
	),
	BYTE("java.lang.Byte", "java.lang.Byte", 5, FilterType.NUMBER,
			(rs, fieldId) -> {
				byte result = rs.getByte(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.IntegerHasComma, CalculateType.Sum, null,
			Collections.intDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : Byte.parseByte(ob.toString())
	),
	DATE("java.util.Date", "java.time.LocalDate", 6, FilterType.DATE,
			(rs, fieldId) -> {
				Date result = rs.getDate(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.DateYYMMDD, CalculateType.CountAll, GroupType.Day,
			Collections.dateDataFormats, Collections.dateCalculateTypes, Collections.dateGroupTypes,
			ob -> ob
	),
	TIME("java.sql.Time", "java.time.LocalTime", 7, FilterType.TIME,
			(rs, fieldId) -> {
				Time result = rs.getTime(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.TimeHHMMSS, CalculateType.CountAll, GroupType.Minute,
			Collections.timeDataFormats, Collections.dateCalculateTypes, Collections.timeGroupTypes,
			ob -> ob
	),
	DOUBLE("java.lang.Double", "java.lang.Double", 8, FilterType.NUMBER,
			(rs, fieldId) -> {
				double result = rs.getDouble(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.FloatTwoDecimal, CalculateType.Sum, null,
			Collections.floatDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : Double.parseDouble(ob.toString())
	),
	FLOAT("java.lang.Float", "java.lang.Float", 9, FilterType.NUMBER,
			(rs, fieldId) -> {
				float result = rs.getFloat(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.FloatTwoDecimal, CalculateType.Sum, null,
			Collections.floatDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : Float.parseFloat(ob.toString())
	),
	INTEGER("java.lang.Integer", "java.lang.Integer", 10, FilterType.NUMBER,
			(rs, fieldId) -> {
				int result = rs.getInt(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.IntegerHasComma, CalculateType.Sum, null,
			Collections.intDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : Integer.parseInt(ob.toString())
	),
	SHORT("java.lang.Short", "java.lang.Short", 11, FilterType.NUMBER,
			(rs, fieldId) -> {					short result = rs.getShort(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.IntegerHasComma, CalculateType.Sum, null,
			Collections.intDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : Short.parseShort(ob.toString())
	),
	BIGDECIMAL("java.math.BigDecimal", "java.math.BigDecimal", 12, FilterType.NUMBER,
			(rs, fieldId) -> {
				BigDecimal result = rs.getBigDecimal(fieldId);
				if(rs.wasNull())
					return null;
				return result;
			},
			DataFormat.FloatTwoDecimal, CalculateType.Sum, null,
			Collections.floatDataFormats, Collections.numberCalculateTypes, null,
			ob -> ob == null ? null : BigDecimal.valueOf(Double.parseDouble(ob.toString()))
	);

	@FunctionalInterface
	private static interface GetDataFromDB {
		public Comparable<? extends Object> getData(ResultSet rs, String fieldId) throws SQLException;
	}

	@FunctionalInterface
	private static interface TypeConvert {
		public Object convert(Object ob);
	}

	private GetDataFromDB getDataMethod;
	private String name;
	private String adhocName;
	private Integer index;
	private FilterType filterType;
	private DataFormat defaultDataFomat;
	private CalculateType defaultCalculateType;
	private GroupType defaultGroupType;
	private List<DataFormat> dataFormats;
	private List<CalculateType> calculateTypes;
	private List<GroupType> groupTypes;
	private TypeConvert typeConvert;

	public String getName() {
		return name;
	}

	private DataType(String name, String adhocName, Integer index, FilterType filterType, GetDataFromDB getDataMethod,
			DataFormat defaultDataFomat, CalculateType defaultCalculateType, GroupType defaultGroupType,
			List<DataFormat> dataFormats, List<CalculateType> calculateTypes, List<GroupType> groupTypes, TypeConvert typeConvert) {
		this.name= name;
		this.adhocName = adhocName;
		this.index = index;
		this.filterType = filterType;
		this.getDataMethod = getDataMethod;
		this.defaultDataFomat = defaultDataFomat;
		this.defaultCalculateType = defaultCalculateType;
		this.dataFormats = dataFormats;
		this.calculateTypes = calculateTypes;
		this.defaultGroupType = defaultGroupType;
		this.groupTypes = groupTypes;
		this.typeConvert = typeConvert;
	}

	public static DataType getDataTypeByName(String name) {
		for(DataType dataType : DataType.values()) {
			if(!dataType.name.isEmpty() && dataType.name.equals(name)) {
				return dataType;
			}
		}
		return UNKNOW;
	}

	public static DataType getDataTypeByAdhocName(String name) {
		for(DataType dataType : DataType.values()) {
			if(!dataType.name.isEmpty() && dataType.name.equals(name)) {
				return dataType;
			}
		}
		return UNKNOW;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public int getIndex() {
		return index;
	}

	public String getFormatedString(ResultSet rs, String fieldId, DataFormat dataFormat) throws SQLException {
		Object result = getResult(rs, fieldId);
		return result == null ? null : dataFormat.parse(result);
	}

	public Comparable<? extends Object> getResult(ResultSet rs, String fieldId) throws SQLException {
		return getDataMethod.getData(rs, fieldId);
	}

	public DataFormat getDefaultDataFormat() {
		return defaultDataFomat;
	}

	public CalculateType getDefaultCalculateType() {
		return defaultCalculateType;
	}

	public GroupType getDefaultGroupType() {
		return defaultGroupType;
	}

	public List<DataFormat> getDataFormats() {
		return dataFormats;
	}

	public List<CalculateType> getCalculateTypes() {
		return calculateTypes;
	}

	public List<GroupType> getGroupTypes() {
		return groupTypes;
	}

	public String getAdhocName() {
		return adhocName;
	}

	public Object convert(Object ob) {
		return typeConvert.convert(ob);
	}

	public String getFormatedString(ResultSet rs, String fieldId, GroupType groupType) throws SQLException {
		Object result = getResult(rs, fieldId);
		return result == null ? null : groupType == null ? result.toString() : groupType.parse(result);
	}
}

final class Collections {

	/*-------------------------------DataFormat-------------------------------*/

	public static final List<DataFormat> intDataFormats = Arrays.asList(
			DataFormat.IntegerHasComma, DataFormat.IntegerNoComma, DataFormat.IntegerCoin1, DataFormat.IntegerCoin2, DataFormat.IntegerNoMinus);

	public static final List<DataFormat> floatDataFormats = Arrays.asList(
			DataFormat.FloatTwoDecimal, DataFormat.FloatInteger,
			DataFormat.FloatNoMinusDecimalCoin1,DataFormat.FloatIntegerCoin1, DataFormat.FloatDecimalCoin1,
			DataFormat.FloatNoMinusDecimalCoin2, DataFormat.FloatIntegerCoin2, DataFormat.FloatDecimalCoin2);

	public static final List<DataFormat> dateDataFormats = Arrays.asList(
			DataFormat.DateYYMMDD, DataFormat.DateYMD);

	public static final List<DataFormat> timeDataFormats = Arrays.asList(
			DataFormat.TimehhMMSS, DataFormat.TimeHHMMSS, DataFormat.TimehhMM, DataFormat.TimeHHMM);

	public static final List<DataFormat> timeStampDataFormats = Arrays.asList(
			DataFormat.TimeStampYYMMDD, DataFormat.TimeStampYMD, DataFormat.TimeStampYYMMDDHHMMSS, DataFormat.TimeStampHHMMSS);

	/*-------------------------------CalculateType-------------------------------*/

	public static final List<CalculateType> noneCalculateTypes = Arrays.asList(
			CalculateType.None, CalculateType.CountAll, CalculateType.CountDistinct);

	public static final List<CalculateType> numberCalculateTypes = Arrays.asList(
			CalculateType.None, CalculateType.Average, CalculateType.CountAll, CalculateType.CountDistinct,
			CalculateType.Maximum, CalculateType.Median, CalculateType.Minimum, CalculateType.Sum);

	public static final List<CalculateType> dateCalculateTypes = Arrays.asList(
			CalculateType.None, CalculateType.CountAll, CalculateType.CountDistinct,
			CalculateType.Maximum, CalculateType.Median, CalculateType.Minimum);

	/*-------------------------------GroupType-------------------------------*/

	public static final List<GroupType> dateGroupTypes = Arrays.asList(
			GroupType.Year, GroupType.Quarter, GroupType.Month, GroupType.Day);

	public static final List<GroupType> timeGroupTypes = Arrays.asList(
			GroupType.Hour, GroupType.Minute, GroupType.Second, GroupType.MilliSecond);

	public static final List<GroupType> timeStampGroupTypes = Arrays.asList(
			GroupType.Year, GroupType.Quarter, GroupType.Month, GroupType.Day, GroupType.Hour, GroupType.Minute, GroupType.Second,
			GroupType.HourByDay, GroupType.MinByDay, GroupType.SecByDay, GroupType.MSByDay);

}

