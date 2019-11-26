package com.legendapl.lightning.adhoc.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolService {
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static ThreadPoolService instance;

	public static ThreadPoolService getInstance() {
		//スレッドは安全ではありません
		if(instance == null) {
			instance = new ThreadPoolService();
		}
		return instance;
	}

	public void execute(Runnable command) {
		fixedThreadPool.execute(command);
	}
}
