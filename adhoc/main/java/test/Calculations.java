package test;

import java.util.Arrays;
import java.util.List;

import com.legendapl.lightning.adhoc.common.CalculateType;
import com.legendapl.lightning.adhoc.common.DataFormat;

public final class Calculations {

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

	public static final List<DataFormat> stringDataFormats = null;

	public static final List<CalculateType> stringCalculations = Arrays.asList(
			CalculateType.None, CalculateType.CountAll, CalculateType.CountDistinct);

	public static final List<CalculateType> numberCalculations = Arrays.asList(
			CalculateType.None, CalculateType.CountAll, CalculateType.CountDistinct,
			CalculateType.Maximum, CalculateType.Minimum, CalculateType.Sum);

	public static final List<CalculateType> dateCalculations = Arrays.asList(
			CalculateType.None, CalculateType.CountAll, CalculateType.CountDistinct,
			CalculateType.Maximum, CalculateType.Minimum);

}
