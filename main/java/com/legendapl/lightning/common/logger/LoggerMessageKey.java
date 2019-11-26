package com.legendapl.lightning.common.logger;

/**
 * メッセージのキーを定義したクラス
 */
public class LoggerMessageKey {

	/**
	 * Infoメッセージ
	 */
	public static class Info {
		// 接続テストが完了しました。
		final public static String INFO_W03_01 = "INFO_W03_01";
		// データソース設定を保存しました。
		final public static String INFO_W03_03 = "INFO_W03_03";

		// 接続テストに成功しました。
		final public static String INFO_W02_01 = "INFO_W02_01";

		// 接続テストに失敗しました。
		final public static String INFO_W06_01 = "INFO_W06_01";
		// ワークスペースフォルダ名変更成功
		final public static String INFO_W06_02 = "INFO_W06_02";
	}

	/**
	 * Warnメッセージ
	 */
	public static class Warn {
		// 保存していない設定があります。よろしいでしょうか？
		final public static String WARN_W03_01_HEADER = "WARN_W03_01_HEADER";
		// データソース（{0}）への接続は失敗しました。
		final public static String WARN_W03_02 = "WARN_W03_02";
		// 以下のデータソースへの接続は失敗しました。
		final public static String WARN_W03_03_HEADER = "WARN_W03_03_HEADER";
		// ワークスペースのフォルダ名を変更しました。
		final public static String WARN_W06_01 = "WARN_W06_01";
	}

	/**
	 * Debugメッセージ
	 */
	public static class Debug {
		// {0}は{1}が定義されないと実行できません。
		final public static String DEBUG_W04_01 = "DEBUG_W04_01";
	}

	/**
	 * Errorメッセージ
	 */
	public static class Error {

		// 予期せぬエラーが発生しました。
		final public static String ERROR_99_HEADER = "ERROR_99_HEADER";
		// {0}
		final public static String ERROR_99_CONTENT = "ERROR_99_CONTENT";

		// 内部エラー：指定されてデータソースフォルダは存在しないか、書込みできません：{0}
		final public static String ERROR_W03_01 = "ERROR_W03_01";
		// // データソース（{0}）への接続は失敗しました。
		// final public static String ERROR_W03_02_HEADER =
		// "ERROR_W03_02_HEADER";

		// サーバへの接続に失敗しました。パスワードが間違っている可能性があります。
		final public static String ERROR_W03_02 = "ERROR_W03_02";

		// 接続テストに失敗しました。
		final public static String ERROR_W02_01 = "ERROR_W02_01";

		// 該当のレポートは存在しません。
		final public static String ERROR_P01_01 = "ERROR_P01_01";

		// サーバとの通信がタイムアウトしました。
		final public static String ERROR_P02_01 = "ERROR_P02_01";
		// 該当のディレクトリは存在しません。
		final public static String ERROR_P02_02 = "ERROR_P02_02";

		// 接続テストに失敗しました。
		final public static String ERROR_W06_01 = "ERROR_W06_01";

		// データソースのパスワードを設定してください。
		final public static String ERROR_W04_01 = "ERROR_W04_01";
		// データソースの定義ファイルが見つかりませんでした。
		final public static String ERROR_W04_02 = "ERROR_W04_02";
		// SQLの実行に失敗しました。
		final public static String ERROR_W04_03 = "ERROR_W04_03";

		// リソースIDを入力してください。
		final public static String ERROR_W07_01 = "ERROR_W07_01";
		// 選択された帳票は存在しません。
		final public static String ERROR_W07_02 = "ERROR_W07_02";
		// 選択された帳票は利用できません。
		final public static String ERROR_W07_03 = "ERROR_W07_03";
		// Excelファイルを選択してください。
		final public static String ERROR_W07_04 = "ERROR_W07_04";
		// ファイルが存在しません。
		final public static String ERROR_W07_05 = "ERROR_W07_05";
		// セルを選択してください。
		final public static String ERROR_W07_06 = "ERROR_W07_06";
		// 列、行いずれの項目も入力してください。
		final public static String ERROR_W07_07 = "ERROR_W07_07";
		// 列は半角英語,もしくは半角数値で入力してください。
		final public static String ERROR_W07_08 = "ERROR_W07_08";
		// 行は半角数値で入力してください。
		final public static String ERROR_W07_09 = "ERROR_W07_09";
		// 1つ以上の列を選択してください。
		final public static String ERROR_W07_10 = "ERROR_W07_10";
		// ルートフォルダの配下に指定してください。
		final public static String ERROR_W07_11 = "ERROR_W07_11";
		// Excelの保存先を指定してください。
		final public static String ERROR_W07_12 = "ERROR_W07_12";
		// ジョブの保存先を指定してください。
		final public static String ERROR_W07_13 = "ERROR_W07_13";
		// 列の最大値は16384(XFD)です。
		final public static String ERROR_W07_14 = "ERROR_W07_14";
		// 行の最大値は1048576です。
		final public static String ERROR_W07_15 = "ERROR_W07_15";
		// ジョブの上限は10までです。
		final public static String ERROR_W07_16 = "ERROR_W07_16";
		// 1つ以上の定義を作成してください。
		final public static String ERROR_W07_17 = "ERROR_W07_17";
		// 選択されたシート、セルの定義は既に存在しています。
		final public static String ERROR_W07_18 = "ERROR_W07_18";
		// {0}にシート「{1}」は存在しません。
		final public static String ERROR_W07_19 = "ERROR_W07_19";
		// 参照元と出力先のExcelファイルの拡張子(xlsx or xlsm)が異なります。
		final public static String ERROR_W07_20 = "ERROR_W07_20";

	}

}
