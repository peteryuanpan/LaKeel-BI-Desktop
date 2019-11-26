package com.legendapl.lightning.adhoc.service;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;

/**
 * バックで実行のサービスクラス
 * 
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/2/23
 */
public class BackRunService {
	
	protected Logger logger = Logger.getLogger(getClass());
	
	public interface RunFun<R> {
		R call() throws Exception;
	}
	public interface NextFun<P> {
		void call(P p);
	}
	public interface VoidRunFun {
		void call() throws Exception;
	}
	public interface VoidNextFun {
		void call();
	}
	public interface ExceptionFun {
		void call(Throwable e);
	}
	
	protected VoidNextFun comStartFun;
	protected VoidNextFun comEndFun;
	
	/**
	 * 構造
	 * @param comStartFun 共通のStart Funtion
	 * @param comEndFun   共通のEnd Funtion
	 */
	public BackRunService(VoidNextFun comStartFun, VoidNextFun comEndFun) {
		this.comStartFun = comStartFun;
		this.comEndFun = comEndFun;
	}
	
	/**
	 * 構造
	 */
	public BackRunService() {
		this(null, null);
	}
	
	/**
	 *  バックで実行</br><br/>
	 *  runFunはほかのスレッドで実行する<br/>
	 * @param runFun         実行する予定のfuntion
	 */
	public void run(VoidRunFun runFun) {
		run(runFun, null, null);
	}
	
	/**
	 *  バックで実行</br><br/>
	 *  runFunはほかのスレッドで実行する<br/>
	 *  nextFunはJavaFX Application Threadで実行する<br/>
	 * @param runFun         実行する予定のfuntion
	 * @param nextFun        実行成功の場合、次に実行する予定のfuntion
	 */
	public void run(VoidRunFun runFun, VoidNextFun nextFun) {
		run(runFun, nextFun, null);
	}
	
	/**
	 *  バックで実行</br><br/>
	 *  runFunはほかのスレッドで実行する<br/>
	 *  nextFunとexceptionFunはJavaFX Application Threadで実行する<br/>
	 * @param runFun         実行する予定のfuntion
	 * @param nextFun        実行成功の場合、次に実行する予定のfuntion
	 * @param exceptionFun   実行に例外発生の場合、実行する予定のfuntion
	 */
	public void run(VoidRunFun runFun, VoidNextFun nextFun, ExceptionFun exceptionFun) {
		run(runFun == null ? null : ()-> { runFun.call(); return (Void)null; },
			nextFun == null ? null : p -> nextFun.call(),
			exceptionFun == null ? null : e -> exceptionFun.call(e)
			);
	}
	
	/**
	 *  バックで実行</br><br/>
	 *  runFunはほかのスレッドで実行する<br/>
	 * @param runFun         実行する予定のfuntion
	 */
	public <R> void run(RunFun<R> runFun) {
		run(runFun, null, null);
	}
	
	/**
	 *  バックで実行</br><br/>
	 *  runFunはほかのスレッドで実行する<br/>
	 *  nextFunはJavaFX Application Threadで実行する<br/>
	 *  runFunの戻り値はパラメータとしてnextFunに送ります
	 * @param runFun         実行する予定のfuntion
	 * @param nextFun        実行成功の場合、次に実行する予定のfuntion
	 */
	public <R> void run(RunFun<R> runFun, NextFun<R> nextFun) {
		run(runFun, nextFun, null);
	}
	
	/**
	 *  バックで実行</br><br/>
	 *  runFunはほかのスレッドで実行する<br/>
	 *  nextFunとexceptionFunはJavaFX Application Threadで実行する<br/>
	 *  runFunの戻り値はパラメータとしてnextFunに送ります
	 * @param runFun         実行する予定のfuntion
	 * @param nextFun        実行成功の場合、次に実行する予定のfuntion
	 * @param exceptionFun   実行に例外発生の場合、実行する予定のfuntion
	 */
	public <R> void run(RunFun<R> runFun, NextFun<R> nextFun, ExceptionFun excpFun) {
		Task<R> task = new Task<R>() {
			@Override
			protected R call() throws Exception {
				if(comStartFun != null) comStartFun.call();
				try {
					return runFun != null ? runFun.call() : null;
				} finally {
					if(comEndFun != null) comEndFun.call();
				}
			}
			
			@Override
			protected void succeeded() {
				if(nextFun != null) nextFun.call(this.valueProperty().get());
			}
			
			@Override
			protected void failed() {
				if(excpFun != null) excpFun.call(this.exceptionProperty().get());
				else { this.exceptionProperty().get().printStackTrace();  }
			}
		};
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
}
