package com.legendapl.lightning.tools.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.log4j.Logger;

import com.legendapl.lightning.tools.common.Utils;

import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.model.CsvRow;

/**
 * CSV関連のサービスクラス
 * 
 * @author LAC_楊
 * @since 2017/9/5
 */
public class CsvService {
	static final protected ResourceBundle csvRes = ResourceBundle.getBundle(Constants.CSV_BUNDLE);
	
	static final public String Splitter = csvRes.getString(Constants.CSV_SPLITTER);
	static final public String Quote = csvRes.getString(Constants.CSV_QUOTE);
	static final public String FieldSplitter = csvRes.getString(Constants.CSV_FIELD_SPLITTER);
	static final public String NameValueSplitter = csvRes.getString(Constants.CSV_NAME_VALUE_SPLITTER);
	static final public String ValuesSplitter = csvRes.getString(Constants.CSV_VALUES_SPLITTER);
		
	static final public int MAX_CSV_FILE_SIZE = 10 * 1024 * 1024;
	
	static final private char Ch_Splitter = Splitter.charAt(0);
	static final private char Ch_Quote = Quote.charAt(0);
	static final private String pt_Quote = Pattern.quote(Quote);
	static final private String str_WQuote = Quote + Quote;
	
	static final private CSVFormat csvFormat = CSVFormat.EXCEL
			.withDelimiter(Ch_Splitter)
			.withQuote(Ch_Quote)
			.withQuoteMode(QuoteMode.MINIMAL);
	
	static final protected Logger logger = Logger.getLogger(CsvService.class);
	
	/**
	 * データをCSVファイルにエクスポート
	 * 
	 * @param path CSVファイルのパス
	 * @param rows データ
	 * @throws IOException IO例外
	 */
	static public void saveDataToCsv(String path, List<? extends List<String>> rows) throws IOException {
		logger.debug(MessageFormat.format(
				"Saving data to csv, csv path : {0}, rows count : {1}, charset : {2}, splitter : 「{3}」, quote : 「{4}」",
				path, rows.size(), Constants.CSV_CHARSET, Splitter, Quote));
		
		FileOutputStream fos = null;
		OutputStreamWriter osr = null;
		BufferedWriter bw = null;

		try {
			fos = new FileOutputStream(path);
			if(Constants.CSV_CHARSET.equals("UTF-8")) {
				fos.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
			}
			osr = new OutputStreamWriter(fos, Constants.CSV_CHARSET);
			bw = new BufferedWriter(osr);
			
			for(Iterable<String> row : rows) {
				boolean first = true;
				for(String cell : row) {
					if(first) first = false;
					else bw.write(Splitter);
					
					boolean sp = cell.contains(Splitter);
					if(cell.contains(Quote)) {
						cell = cell.replaceAll(pt_Quote, str_WQuote);
						sp = true;
					}
					if(sp) {
						bw.write(Quote);
						bw.write(cell);
						bw.write(Quote);
					} else {
						bw.write(cell);
					}
				}
				bw.newLine();
			}
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			} else if(fos != null) {
				try {
					fos.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			} 
		}
		
		logger.debug("Csv saved.");
	}
	
	/**
	 * データをCSVファイルにエクスポート
	 * 
	 * @param file CSVファイル
	 * @param rows データ
	 * @throws IOException IO例外
	 */
	static public void saveDataToCsv(File file, List<? extends List<String>> rows)  throws IOException {
		saveDataToCsv(file.getPath(), rows);
	}
	
	/**
	 * CSVファイルからデータ取得
	 * 
	 * @param path CSVファイルのパス
	 * @return データ、CSVファイルの行のリスト（例外発生しない場合、最低でも1行のデータ）。
	 * @throws IOException IO例外
	 * @throws CsvFormatException CSVフォーマット不正の例外
	 */
	static public List<CsvRow> getDataFromCsv(String path) throws IOException, CsvFormatException {
		return getDataFromCsv(new File(path));
	}
			
	/**
	 * CSVファイルからデータ取得
	 * 
	 * @param file CSVファイル
	 * @return データ、CSVファイルの行のリスト（例外発生しない場合、最低でも1行のデータ）。
	 * @throws IOException IO例外
	 * @throws CsvFormatException CSVフォーマット不正の例外
	 */
	static public List<CsvRow> getDataFromCsv(File file) throws IOException, CsvFormatException {
		logger.debug(MessageFormat.format(
				"Openning csv file : {0}, size : {1}", file.getPath(), file.length()));
		
		if(file.length() > MAX_CSV_FILE_SIZE) {
			throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_SIZE_EXCEEDS_LIMIT));
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return getDataFromCsvStream(fis);
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * CSVストリームからデータ取得
	 * 
	 * @param stream CSVストリーム
	 * @return データ、CSVデータの行のリスト（例外発生しない場合、最低でも1行のデータ）。
	 * @throws IOException IO例外
	 * @throws CsvFormatException CSVフォーマット不正の例外
	 */
	static public List<CsvRow> getDataFromCsvStream(InputStream stream)  throws IOException, CsvFormatException {
		logger.debug("Get csv from stream...");
		
		if(stream == null) return null;
		
		List<Byte> bytes = new ArrayList<Byte>(stream.available() + 2);
		int value;
		while((value = stream.read()) >= 0) {
			bytes.add((byte)value);
		}
		
		byte [] buff = new byte[bytes.size()];
		for(int i = 0; i < buff.length; i++) {
			buff[i] = bytes.get(i);
		}
		
		Object[] guess = guessEncoding(buff);
		String encode = (String) guess[0];
		int start = (Integer) guess[1];
		
		logger.debug("Csv encoding : " + encode);

		return getDataFromCsvString(new String(buff, start, buff.length - start, encode));
	}
	
	/**
	 * 文字列からCSVデータ取得
	 * 
	 * @param csvContent CSV内容の文字列
	 * @return データ、CSVデータの行のリスト（例外発生しない場合、最低でも1行のデータ）。
	 * @throws CsvFormatException CSVフォーマット不正の例外
	 */
	static public List<CsvRow> getDataFromCsvString(String csvContent) throws CsvFormatException {
		logger.debug("Csv length : " + csvContent.length());
		
		List<CSVRecord> lines = getSplitLines(csvContent);
		return getCsvRows(lines);
	}
	
	/**
	 * CSVフォーマット不正の例外
	 */
	public static class CsvFormatException extends RuntimeException {
	    /**
	     * Constructs a <code>CsvFormatException</code> with no detail message.
	     */
		public CsvFormatException() {
			super();
		}
		
	    /**
	     * Constructs a <code>CsvFormatException</code> with the
	     * specified detail message.
	     *
	     * @param   s   the detail message.
	     */
		public CsvFormatException(String s) {
			super(s);
		}
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7398142031171991776L;
		
	}
	
	protected static List<CsvRow> getCsvRows(List<CSVRecord> lines) throws CsvFormatException {
		List<CsvRow> csvRows = new ArrayList<CsvRow>();
		
		logger.debug(MessageFormat.format("Csv total rows count : {0}", lines == null ? 0 : lines.size()));
		
		if(lines == null) {
			throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_BAD_CSV));
		} else if(lines.size() == 0) {
			throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_HEARER_NOT_EXISTS));
		} else if(lines.get(0).size() < 2) {
			throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_COL_TOO_LESS));
		}
		List<String> headLine = new ArrayList<>(lines.get(0).size());
		lines.get(0).forEach((cell) -> headLine.add(cell == null ? "" : cell));
		
		logger.debug(MessageFormat.format("Csv columns count : {0}", headLine.size()));

		Map<String, Integer> mapNameIndex = new HashMap<String, Integer>();
		for(int i = 0; i < headLine.size(); i++) {
			if(headLine.get(i).isEmpty()) {
				throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_EMPTY_COLNAME));
			}
			if(mapNameIndex.containsKey(headLine.get(i))) {
				throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_DUPLICATE_COLNAME, headLine.get(i)));
			}
			mapNameIndex.put(headLine.get(i), i);
		}
			
		final Map<String, Integer> roMapNameIndex = Collections.unmodifiableMap(mapNameIndex);
		final List<String> roHeader = Collections.unmodifiableList(headLine);
		
		for(int j = 1; j < lines.size(); j++) {
			CSVRecord line = lines.get(j);
			
			if(line.size() > roHeader.size()) {
				throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_ROWDATA_TOO_MORE, j + 1));
			}
			List<String> tRowData = new ArrayList<String>(roHeader.size());
			line.forEach((cell) -> tRowData.add(cell == null ? "" : cell));
			for(int i = tRowData.size(); i < roHeader.size(); i++) {
				tRowData.add("");
			}
			final List<String> roValues = Collections.unmodifiableList(tRowData);
			final int line_no = (int)line.getRecordNumber();

			csvRows.add(new CsvRow() {
				@Override
				public List<String> getHeader() {
					return roHeader;
				}
				@Override
				public List<String> getAllValues() {
					return roValues;
				}
				@Override
				public int getIndex(String colName) {
					Integer index = roMapNameIndex.get(colName);
					return index == null ? -1 : index.intValue();
				}
				@Override
				public int getRowNo() {
					return line_no;
				}
			});	
		}
		
		logger.debug(MessageFormat.format("Csv data rows count : {0}", csvRows.size()));
		if(csvRows.isEmpty()) {
			throw new CsvFormatException(Utils.getString(Constants.CSV_ERROR_NO_DATA));
		}
	
		return Collections.unmodifiableList(csvRows);
	}
	
	protected static List<CSVRecord> getSplitLines(String contents) {
		Reader reader = new StringReader(contents);
		CSVParser csvParser = null;
		try {
			csvParser = new CSVParser(reader, csvFormat);
			return csvParser.getRecords();
		} catch (IOException e) {
			return null;
		} finally {
			if(csvParser != null) {
				try{
					csvParser.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static final byte ESC = 0x1B;
	static final byte CR = 0x0D;
	static final byte LF = 0x0A;
	static final byte COMMA = ',';
	static final byte QUOTATION = '\"';
	
	static final List<Byte> PossibleAscii
		= new ArrayList<Byte>(Arrays.asList(CR, LF, COMMA, QUOTATION));
	
	static final String Unicode = "Unicode";
	static final String UTF8 = "UTF-8";
	static final String UTF16BE = "UTF-16";
	static final String UTF16LE = "UTF-16LE";
	static final String JIS = "JIS";
	static final String MS932 = "MS932";
	
	static final int CHECK_LEN = 1000;
	
	protected static Object[] guessEncoding(byte[] buff) {
		String encode = MS932;
		Integer start = 0;
		
		if(buff.length < 3) {
			Object[] rst = { UTF8, 0 };
			return rst;
		}
		
		if(buff[0] == (byte)0xEF && buff[1] == (byte)0xFF) {
			encode = Unicode;
			start = 2;
		}
		else if(buff[0] == (byte)0xFF && buff[1] == (byte)0xEF) {
			encode = UTF16BE;
			start = 2;
		}
		else if(buff[0] == (byte)0xEF && buff[1] == (byte)0xBB && buff[2] == (byte)0xBF) {
			encode = UTF8;
			start = 3;
		} else {
			int len = Math.min(buff.length, CHECK_LEN);
			boolean got = false;
			
			//UTF16LE、UTF16BEの可能性を判断する
			for(int i = 0; i < len - 1; i+=2) {
				if(buff[start + i] == 0 && PossibleAscii.contains(buff[start + i + 1])) {
					encode = UTF16BE;
					got = true;
					break;
				}
				else if(buff[start + i + 1] == 0 && PossibleAscii.contains(buff[start + i])) {
					encode = UTF16LE;
					got = true;
					break;
				}
			}
			//JISの可能性を判断する
			if(!got) {
				for(int i = 0; i < len; i++) {
					if(buff[start + i] == ESC) {
						encode =  JIS;
						got = true;
						break;
					}
				}
			}
			
			//UTF8の可能性を判断する
			len = buff.length;
			if(!got) {
				boolean utf8_ok = true;
				int i = start;
				while (utf8_ok && i < len) {
					if(buff[i] > 0  && buff[i] <= 0x7F) {
						i++;
						continue;
					}
					else if(buff[i] >= (byte)0xC2 && buff[i] <= (byte)0xDF) {
						if(i + 1 == len) {
							utf8_ok = false;
							break;
						}
						else if(buff[i+1] >= (byte)0x80 && buff[i+1] <= (byte)0xBF) {
							i+= 2;
							continue;
						}
					}
					else if(buff[i] >= (byte)0xE0 && buff[i] <= (byte)0xEF) {
						if(i + 1 == len) {
							utf8_ok = false;
							break;
						}
						else if(buff[i+1] >= (byte)0x80 && buff[i+1] <= (byte)0xBF) {
							if(i + 2 == len){
								utf8_ok = false;
								break;
							}
							else if(buff[i+2] >= (byte)0x80 && buff[i+2] <= (byte)0xBF) {
								i+= 3;
								continue;
							}
						}
					}
					utf8_ok = false;
				}
				encode = utf8_ok ? UTF8 : MS932;
			}
		}
		
		Object[] rst = { encode, start } ;
		return rst;
	}
}
