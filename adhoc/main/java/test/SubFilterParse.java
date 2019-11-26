package test;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SubFilterParse {

	static Pattern a2b = Pattern.compile("(\\S+)\\s(\\S+)\\s(.*)");
	static Pattern hasNot = Pattern.compile("^not\\s\\((.*)\\)$");
	static Pattern varcharHasNot = Pattern.compile("^not\\s(\\S+\\(.*)");
	static Pattern varchar = Pattern.compile("(\\S+)\\((\\S+),\\s(\\S+)\\)$");

	public static void main(String[] args) throws Exception {
		File file = new File("C:\\Users\\xuguangheng\\Desktop\\项目资料保存\\adhoc\\domain_filter.xlsx");
		XSSFWorkbook wb = new XSSFWorkbook(file);
		XSSFSheet sheet = wb.getSheetAt(0);
		// Iterate over each row in the sheet
		Iterator<Row> rows = sheet.rowIterator();
		while (rows.hasNext()) {
			XSSFRow row = (XSSFRow) rows.next();
			Iterator<Cell> cells = row.cellIterator();
			while (cells.hasNext()) {
				XSSFCell cell = (XSSFCell) cells.next();
				String content = cell.getStringCellValue();
				if(content != null && !content.isEmpty())
					checkNot(content);
			}
		}
	}

	public static void checkNot(String content) {
		Matcher mat = hasNot.matcher(content);
		Matcher matVarchar = varcharHasNot.matcher(content);
		if(mat.find()) {
			parse(true, mat.group(1));
		} else if(matVarchar.find()) {
			parseVarchar(true, matVarchar.group(1));
		} else {
			parse(false, content);
		}
	}

	public static void parse(boolean hasNot, String content) {
		Matcher mat = a2b.matcher(content);
		if(mat.find()) {
			Filter filter = new Filter(hasNot, mat.group(1), mat.group(2), mat.group(3));
			System.out.println(filter);
		} else {
			parseVarchar(hasNot, content);
		}
	}

	public static void parseVarchar(boolean hasNot, String content) {
		Matcher mat = varchar.matcher(content);
		if(mat.find()) {
			Filter filter = new Filter(hasNot, mat.group(2), mat.group(1), mat.group(3));
			System.out.println(filter);
		} else {
		}
	}
}

class Filter {
	private boolean hasNot = false;
	private String fieldId;
	private String op;
	private String value;

	public Filter(boolean hasNot, String fieldId, String op, String value) {
		super();
		this.hasNot = hasNot;
		this.fieldId = fieldId;
		this.op = op;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Filter [hasNot:" + hasNot + ", fieldId:" + fieldId + ", op:" + op + ", value:" + value + "]";
	}
}
