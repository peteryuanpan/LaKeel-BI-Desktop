package com.legendapl.lightning.common.crypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 暗号化・復号化ユーティリティ
 * 
 * アルゴリズム：AES/CBC/PKCS5Padding パスワードのストレッチ：10000回（SHA256）
 * 
 * @author taka
 *
 */
public class CryptUtil {

	private static final String ENCRYPT_SECRETKEYSPEC_AES = "AES";
	private static final String ENCRYPT_ALGORITHM_AES = "AES/CBC/PKCS5Padding";

	final private String SALT = "Lightning1.0LAI2017LBI";
	private byte[] saltedPassword;

	static private Map<String, CryptUtil> crypterMap = new ConcurrentHashMap<String, CryptUtil>();

	public CryptUtil(String password) {
		byte[] cipher_byte;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			cipher_byte = addSalt(password.getBytes("UTF-8"));
			md.update(cipher_byte);
			cipher_byte = md.digest();
			for (int i = 1; i < 10000; i++) {
				cipher_byte = addSalt(cipher_byte);
				md.update(cipher_byte);
				cipher_byte = md.digest();
			}
			saltedPassword = new byte[16];
			System.arraycopy(cipher_byte, 0, saltedPassword, 0, saltedPassword.length);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] addSalt(byte[] src) throws UnsupportedEncodingException {
		byte[] _salt = SALT.getBytes("UTF-8");
		byte[] x = new byte[src.length + _salt.length * 2];
		System.arraycopy(_salt, 0, x, 0, _salt.length);
		System.arraycopy(src, 0, x, _salt.length, src.length);
		System.arraycopy(_salt, 0, x, _salt.length + src.length, _salt.length);
		return x;
	}

	public static CryptUtil getInstance(String password) {

		if (crypterMap.containsKey(password)) {
			return crypterMap.get(password);
		}
		CryptUtil newCrypter = new CryptUtil(password);
		crypterMap.put(password, newCrypter);
		return newCrypter;
	}

	public String encryptByAES(String src) {

		try {
			SecretKeySpec sksSpec = new SecretKeySpec(getPassword(), ENCRYPT_SECRETKEYSPEC_AES);
			Cipher cipher;
			cipher = Cipher.getInstance(ENCRYPT_ALGORITHM_AES);
			byte[] _iv = getIV();
			AlgorithmParameterSpec iv = new IvParameterSpec(_iv);
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec, iv);
			byte[] encryptedByteArray = cipher.doFinal(src.getBytes("UTF-8"));
			byte[] encryptedAndIV = new byte[_iv.length + encryptedByteArray.length];
			System.arraycopy(_iv, 0, encryptedAndIV, 0, _iv.length);
			System.arraycopy(encryptedByteArray, 0, encryptedAndIV, _iv.length, encryptedByteArray.length);
			byte[] _encryptedBase64 = Base64.getEncoder().encode(encryptedAndIV);
			return new String(_encryptedBase64);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String deryptByAES(String src) {

		try {
			byte[] _src = Base64.getDecoder().decode(src.getBytes("UTF-8"));
			byte[] _iv = new byte[16];
			System.arraycopy(_src, 0, _iv, 0, 16);
			byte[] _encoded = new byte[_src.length - _iv.length];
			System.arraycopy(_src, _iv.length, _encoded, 0, _encoded.length);
			SecretKeySpec sksSpec = new SecretKeySpec(getPassword(), ENCRYPT_SECRETKEYSPEC_AES);
			Cipher cipher;
			cipher = Cipher.getInstance(ENCRYPT_ALGORITHM_AES);
			AlgorithmParameterSpec iv = new IvParameterSpec(_iv);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec, iv);
			byte[] decodedByteArray = cipher.doFinal(_encoded);
			return new String(decodedByteArray);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] getIV() {
		byte[] iv = new byte[16];
		for (int i = 0; i < 16; i++) {
			iv[i] = (byte) (Math.random() * 256);
		}
		return iv;
	}

	private byte[] getPassword() {
		return saltedPassword;
	}
}
