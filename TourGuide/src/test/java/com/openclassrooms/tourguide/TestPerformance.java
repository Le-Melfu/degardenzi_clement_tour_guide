package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.ExecutorServiceManager;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	private static final int USER_NUMBER = 100000;

	private void printProgressBar(String testName, int current, int total) {
		int percent = (current * 100) / total;
		int barLength = 50;
		int filled = (percent * barLength) / 100;
		String bar = "=".repeat(filled) + (filled < barLength ? ">" : "") + " ".repeat(Math.max(0, barLength - filled - 1));
		System.out.print("\r[TestPerformance - " + testName + "] [" + bar + "] " + percent + "% (" + current + "/" + total + ")");
		if (current == total) {
			System.out.println();
		}
	}

	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		ExecutorServiceManager executorServiceManager = new ExecutorServiceManager();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral(), executorServiceManager);
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(USER_NUMBER);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, executorServiceManager);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		AtomicInteger counter = new AtomicInteger(0);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<CompletableFuture<VisitedLocation>> futures = allUsers.stream()
				.map(user -> tourGuideService.trackUserLocation(user)
						.thenApply(result -> {
							int count = counter.incrementAndGet();
							if (count % 1000 == 0 || count == USER_NUMBER) {
								printProgressBar("highVolumeTrackLocation", count, USER_NUMBER);
							}
							return result;
						}))
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("[TestPerformance - highVolumeTrackLocation] Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		ExecutorServiceManager executorServiceManager = new ExecutorServiceManager();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral(), executorServiceManager);

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(USER_NUMBER);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, executorServiceManager);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		AtomicInteger counter = new AtomicInteger(0);
		List<CompletableFuture<Void>> futures = allUsers.stream()
				.map(u -> rewardsService.calculateRewards(u)
						.thenRun(() -> {
							int count = counter.incrementAndGet();
							if (count % 1000 == 0 || count == USER_NUMBER) {
								printProgressBar("highVolumeGetRewards", count, USER_NUMBER);
							}
						}))
				.collect(Collectors.toList());
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("[TestPerformance - highVolumeGetRewards] Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
