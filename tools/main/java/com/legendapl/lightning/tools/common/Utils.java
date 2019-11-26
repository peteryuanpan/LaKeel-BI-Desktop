package com.legendapl.lightning.tools.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 共通メソッド
 *
 * @author LAC_楊
 * @since 2017/9/5
 */
public final class Utils {
	//static private final ResourceBundle messageRes = ResourceBundle.getBundle(Constants.MESSAGE_BUNDLE);

	/**
	 * メッセージを取得
	 *
	 * @param key
	 *            キー
	 * @return String
	 */
	public static String getString(String s) {
		return s;
	}

	/**
	 * メッセージを取得
	 *
	 * @param key
	 *            キー
	 * @param args
	 *            引数
	 * @return String
	 */
	public static String getString(String s, Object... args) {
		return MessageFormat.format(s, args);
	}

	/**
	 * メッセージを取得
	 *
	 * @param key
	 *            キー
	 * @param resourceBundle
	 *            リソースバンドル
	 * @return String
	 */
	public static String getString(ResourceBundle resourceBundle, String key) {
		if (resourceBundle == null) {
			return Constants.KEY_NOT_FOUND_PREFIX + key + Constants.KEY_NOT_FOUND_SUFFIX;
		}

		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return Constants.KEY_NOT_FOUND_PREFIX + key + Constants.KEY_NOT_FOUND_SUFFIX;
		}
	}

	/**
	 * メッセージを取得
	 *
	 * @param key
	 *            キー
	 * @param resourceBundle
	 *            リソースバンドル
	 * @param args
	 *            引数
	 * @return String
	 */
	public static String getString(ResourceBundle resourceBundle, String key, Object... args) {
		return MessageFormat.format(getString(resourceBundle, key), args);
	}

	public static String dateToStr(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
		String dateStr = sdf.format(date);
		return dateStr;
	}

	public static Date strToDate(String str) throws ParseException {
		if ("".equals(str)) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
		Date date = sdf.parse(str);
		return date;
	}

	public static boolean isTimestampPattern(String str) {
        try {
            new SimpleDateFormat(str);
        } catch (Exception e) {
            return false;
        }
        return true;

	}

	public static String allToStr(Object args) {
		if (args == null || String.valueOf(args) == "null") {
			return "";
		} else {
			return String.valueOf(args);
		}
	}


	/**
	 * 無効の文字があるかどうかを判断する
	 *
	 * @param 判断したい文字列
	 * @return 無効の文字がある場合は true， でないとfalse
	 */
	public static boolean hasIllegalChar(String s) {
		if (s.isEmpty() || s == null) {
			return false;
		}
		Pattern pattern = Pattern.compile(Constants.REGEX);
		Matcher matcher = pattern.matcher(s);
		return matcher.find();
	}

	/**
	 * 無効の文字があるかどうかを判断する
	 *
	 * @param regex
	 *            無効の文字のパターン
	 * @param s
	 *            判断したい文字列
	 * @return 無効の文字がある場合はtrue， でないとfalse
	 */
	public static boolean hasIllegalChar(String regex, String s) {
		if (s.isEmpty() || s == null) {
			return false;
		}
		if (regex == null || regex.isEmpty()) {
			return hasIllegalChar(s);
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	public static String sendPost(String url, String param, String authorization) {
    	OutputStreamWriter out = null;
        BufferedReader reader = null;
        String response="";
        try {
            URL httpUrl = null;
            httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/collection+json");
            conn.setRequestProperty("Authorization", "Basic " + authorization);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            out = new OutputStreamWriter(
                    conn.getOutputStream());
            out.write(param);
            out.flush();
            reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response+=lines;
            }
            reader.close();
            conn.disconnect();

        } catch (Exception e) {
        e.printStackTrace();
        }
        finally{
        try{
            if(out!=null){
                out.close();
            }
            if(reader!=null){
                reader.close();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

        return response;
    }
}
