package com.legendapl.lightning.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Cellの位置指定を文字列⇔数値の相互変換を行うサービスクラス<br>
 * <br>
 * example.<br>
 * "B10" → 1, 10<br>
 * 702,20 → "AAA20"
 * 
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class CellConversionService {

	protected Logger logger = Logger.getLogger(getClass());

	/** 列のインデックス */
	private int columnNum;

	/** 列のアルファベット */
	private String columnAlphabet;

	/** 行のインデックス */
	private int rowNum;

	/** アルファベットのリスト */
	public static final List<String> alphabetList = new ArrayList<String>(Arrays.asList("A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));

	public CellConversionService() {
	}

	/**
	 * コンストラクタ(プレビュー画面からセルを選択した場合)
	 * 
	 * @param columnNum
	 * @param rowNum
	 * 
	 */
	public CellConversionService(int columnNum, int rowNum) {
		this.columnNum = columnNum;
		this.rowNum = rowNum;
	}

	/**
	 * コンストラクタ(テキスト入力でセルを選択した場合)
	 * 
	 * @param columnAlphabet
	 * @param rowNum
	 * 
	 */
	public CellConversionService(String columnAlphabet, int rowNum) {
		columnAlphabet = columnAlphabet.toUpperCase();
		String regex = "^[0-9]+$";

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(columnAlphabet);

		if (m.find())
			this.columnNum = Integer.parseInt(columnAlphabet) - 1;
		else
			this.columnAlphabet = columnAlphabet;

		this.rowNum = rowNum;
	}

	/**
	 * セルを文字列として返す<br>
	 * example. "A10"
	 * 
	 * @return cellString
	 */
	public String getCellString() {

		StringBuilder cellStringBuilder = new StringBuilder();
		String cellString = null;

		// 列位置がアルファベットで定義されている場合
		if (!StringUtils.isEmpty(columnAlphabet)) {
			cellStringBuilder.append(columnAlphabet);
			cellStringBuilder.append(rowNum);
			cellString = cellStringBuilder.toString();
			return cellString;
		}
		// 列位置が数値で定義されている場合
		else {
			if (columnNum > 16384) {
				logger.error("The upper limit of the column is 16384 (XFD).");
				return null;
			} else if (columnNum >= 702) {
				int quotient = (columnNum - 26) / (26 * 26);
				int quotientSecond = (columnNum - 26) % (26 * 26) / 26;
				int remainder = columnNum % 26;

				cellStringBuilder.append(alphabetList.get(quotient - 1)).append(alphabetList.get(quotientSecond))
						.append(alphabetList.get(remainder)).append(rowNum);

				cellString = cellStringBuilder.toString();
				return cellString;
			} else if (columnNum >= 26) {
				int quotient = columnNum / 26;
				int remainder = columnNum % 26;

				cellStringBuilder.append(alphabetList.get(quotient - 1)).append(alphabetList.get(remainder))
						.append(rowNum);
				cellString = cellStringBuilder.toString();
				return cellString;

			} else {
				cellStringBuilder.append(alphabetList.get(columnNum)).append(rowNum);
				cellString = cellStringBuilder.toString();
				return cellString;
			}
		}
	}

	/**
	 * セルを文字列として受け取り、行番号を返す<br>
	 * 
	 * @return rowNIndex
	 */
	public int getRowIndex(String cellString) {
		String regex = ".([0-9]+$)";

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(cellString);

		Integer rowIndex = null;
		if (m.find())
			rowIndex = Integer.parseInt(m.group(1)) - 1;

		return rowIndex;
	}

	/**
	 * セルを文字列として受け取り、列番号を返す<br>
	 * 
	 * @return columnIndex
	 */
	public Integer getColumnIndex(String cellString) {
		String regex = "([A-Z]+).*";

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(cellString);

		String columnAlphabet = null;
		if (m.find())
			columnAlphabet = m.group(1);

		Integer columnIndex = null;
		char[] alphabetArray = columnAlphabet.toCharArray();
		switch (alphabetArray.length) {
		case 1:
			for (int i = 0; i < alphabetList.size(); i++) {
				if (alphabetList.get(i).equals(String.valueOf(alphabetArray[0]))) {
					columnIndex = i;
					return columnIndex;
				}
			}
		case 2:
			for (int i = 0; i < alphabetList.size(); i++) {
				if (alphabetList.get(i).equals(String.valueOf(alphabetArray[0]))) {
					columnIndex = i * 26;
				}
			}
			for (int i = 0; i < alphabetList.size(); i++) {
				if (alphabetList.get(i).equals(String.valueOf(alphabetArray[1]))) {
					columnIndex += i + 26;
					return columnIndex;
				}
			}
		case 3:
			for (int i = 0; i < alphabetList.size(); i++) {
				if (alphabetList.get(i).equals(String.valueOf(alphabetArray[0]))) {
					columnIndex = i * 26 * 26;
				}
			}
			for (int i = 0; i < alphabetList.size(); i++) {
				if (alphabetList.get(i).equals(String.valueOf(alphabetArray[1]))) {
					columnIndex += i * 26;
				}
			}
			for (int i = 0; i < alphabetList.size(); i++) {
				if (alphabetList.get(i).equals(String.valueOf(alphabetArray[2]))) {
					columnIndex += i + 702;
					return columnIndex;
				}
			}
		}

		return null;
	}

	/**
	 * セルを文字列として受け取り、列のアルファベットを返す<br>
	 * 
	 * @return columnIndex
	 */
	public String getColumnAlphabet(String cellString) {
		String regex = "([A-Z]+).*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(cellString);

		String columnAlphabet = null;
		if (m.find())
			columnAlphabet = m.group(1);

		return columnAlphabet;
	}
}
