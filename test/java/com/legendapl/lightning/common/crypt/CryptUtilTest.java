package com.legendapl.lightning.common.crypt;

import org.junit.Test;

import static org.junit.Assert.*;

public class CryptUtilTest {

	@Test
	public void encryptByAESTest() {

		for (int i = 0; i < 10; i++) { // 性能測定のため

			String passwrod = "test" + i;
			String src = String.format("これはテスト%04dです", i);

			System.out.println("原文：[" + src + "]");

			long startAt = System.currentTimeMillis();
			CryptUtil crypter = CryptUtil.getInstance(passwrod);
			System.out.println("初期化完了：(" + (System.currentTimeMillis() - startAt) + " ms)");

			startAt = System.currentTimeMillis();
			String encrypted = crypter.encryptByAES(src);
			System.out.println("暗号文：[" + encrypted + "](" + (System.currentTimeMillis() - startAt) + " ms)");

			startAt = System.currentTimeMillis();
			String decoded = crypter.deryptByAES(encrypted);
			System.out.println("復元した結果：[" + decoded + "](" + (System.currentTimeMillis() - startAt) + " ms)");

			assertEquals(src, decoded);
		}
	}
}
