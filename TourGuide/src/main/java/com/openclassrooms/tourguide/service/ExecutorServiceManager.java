package com.openclassrooms.tourguide.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExecutorServiceManager {

	private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceManager.class);
	private final ExecutorService executorService;
	private final int optimalThreadCount;

	public ExecutorServiceManager() {
		this.optimalThreadCount = calculateOptimalThreadCount();
		this.executorService = Executors.newFixedThreadPool(optimalThreadCount);
		logger.info("[ExecutorServiceManager] ExecutorService initialized with {} threads", optimalThreadCount);
	}

	private int calculateOptimalThreadCount() {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		double cpuUsageRatio = 0.75;
		int usableProcessors = Math.max(1, (int) (availableProcessors * cpuUsageRatio));

		// For I/O bound tasks (GPS calls, network), we can use more threads than CPU
		// cores
		// Formula: threads = CPU cores * (1 + wait time / compute time)
		// For heavy I/O operations, ratio is typically 10-50x
		int ioMultiplier = 50;
		int calculatedThreads = usableProcessors * ioMultiplier;

		logger.info("[ExecutorServiceManager] Available processors: {}", availableProcessors);
		logger.info("[ExecutorServiceManager] Usable processors ({}%): {}", (int) (cpuUsageRatio * 100),
				usableProcessors);
		logger.info("[ExecutorServiceManager] Calculated optimal threads: {}", calculatedThreads);

		return calculatedThreads;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public int getOptimalThreadCount() {
		return optimalThreadCount;
	}

	public void shutdown() {
		executorService.shutdown();
		logger.info("[ExecutorServiceManager] ExecutorService shut down");
	}
}
