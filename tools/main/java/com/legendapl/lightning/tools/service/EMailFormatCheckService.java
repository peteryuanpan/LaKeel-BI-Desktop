package com.legendapl.lightning.tools.service;

import java.util.regex.Pattern;

public class EMailFormatCheckService {
	
	/**
	 * email = local@domain
	 */
	private String email;
	private String local;
	private String domain;
	
	/**
	 * 1. max and min length check
	 * 2. char type and usage check
	 */
	private static final String expression_User = 	  "^[A-Za-z0-9@!#$%&'*+-/=?^_`{|}~,\\./[\\u4E00-\\u9FA5]/]{1,64}@[A-Za-z0-9\\./[\\u4E00-\\u9FA5]/]{1,64}$";
	private static final String expression_Schedule = "^[A-Za-z0-9@!#$%&'*+-/=?^_`{|}~\\s*\\./[\\u4E00-\\u9FA5]/]{1,64}@[A-Za-z0-9\\s*\\.]{4,64}$";
	
	private Pattern patternMail;
	
	public EMailFormatCheckService () {
		email = new String();
	}
	
	public EMailFormatCheckService (final String mail) {
		email = mail;
	}
	
	public void setEmail(final String mail) {
		email = mail;
	}
	
	/**
	 * Email format check for user
	 * @return boolean
	 */
	public boolean forUser() {
		if (email == null || email.isEmpty()) {
			return false;
		}
		
		if (!specialJudgeForAt()) {
			return false;
		}
		
		// 全体に関する規定  形式  ローカル部@ドメイン部
		// ローカル部  最大長  最小長  タイプ  文字
		// ドメイン部  最大長  最小長  タイプ  文字
		patternMail = Pattern.compile(expression_User);
		if (!patternMail.matcher(email).matches()) {
			return false;
		}
		
		local = new String();
		domain = new String();
		getLocalAndDomain();
		
		// 先頭と末尾以外で使用可能は不可
		if (domain.charAt(0) == '.' || domain.charAt(domain.length()-1) == '.') {
			return false;
		}
		
		// 連続して使用は不可
		final String pp[] = domain.split(Pattern.quote("."));
		for (final String p : pp ) {
			if (p.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Email format check for schedule
	 * @return boolean
	 */
	public boolean forSchedule() {
		if (email == null || email.isEmpty()) {
			return false;
		}
		
		if (!specialJudgeForAt()) {
			return false;
		}
		
		// 全体に関する規定  形式  ローカル部@ドメイン部
		// ローカル部  最大長  最小長  タイプ  文字
		// ドメイン部  最大長  最小長  タイプ  文字
		patternMail = Pattern.compile(expression_Schedule);
		if (!patternMail.matcher(email).matches()) {
			return false;
		}
		
		local = new String();
		domain = new String();
		getLocalAndDomain();
		
		if (!judgeDotForSchedule(local) || 
			!judgeDotForSchedule(domain) || 
			!judgeDotForScheduleDomain(domain)) {
			return false;
		}
		
		return true;
	}
	
	private boolean specialJudgeForAt() {
		if (howMany(email, '@') >= 2) {
			final int p = getLast(email, '@');
			if (p < 2) return false;
			if (email.charAt(0) != '"') return false;
			if (email.charAt(p-1) != '"') return false;
			StringBuilder sb = new StringBuilder(email);
			sb.deleteCharAt(p-1);
			sb.deleteCharAt(0);
			email = sb.toString();
		}
		return true;
	}
	
	private void getLocalAndDomain() {
		final int p = getLast(email, '@');
		local = email.substring(0, p);
		domain = email.substring(p+1);
	}
	
	private boolean judgeDotForSchedule(final String s) {
		if (s != null) {
			// 先頭と末尾以外で使用可能は不可
			if (s.charAt(0) == '.' || s.charAt(s.length()-1) == '.') {
				return false;
			}
			
			final String pp[] = s.split(Pattern.quote("."));
			for (final String p : pp ) {
				// 連続して使用は不可
				// 2つのドットの間は空できません
				// 最初のドットの前は空できません
				if (isEmptyStr(p)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean judgeDotForScheduleDomain(final String s) {
		if (s != null) {
			// 少なくとも1つのドットあるが必要です
			if (!s.contains(".")) {
				return false;
			}
			
			// 最後のドットの後ろが2文字(スペースを含まない)以上でなければならない
			final String pp[] = s.split(Pattern.quote("."));
			if (pp.length < 2) {
				return false;
			}
			final String lastp = pp[pp.length-1];
			int n = 0;
			for (int i = 0; i < lastp.length(); i++) {
				if (lastp.charAt(i) != ' ') {
					n = n + 1;
				}
			}
			if (n < 2) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isEmptyStr(final String s) {
		if (s != null ) {
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) != ' ') {
					return false;
				}
			}
		}
		return true;
	}
	
	private int howMany(final String s, final char c) {
		int n = 0;
		if (s != null) {
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == c) {
					n = n + 1;
				}
			}
		}
		return n;
	}
	
	private int getLast(final String s, final char c) {
		int p = email.length() - 1;
		while(p >= 0) {
			if (email.charAt(p) == c) break;
			p --;
		}
		return p;
	}
}
