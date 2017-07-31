package io.vrajat.nomadacris;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by rvenkatesh on 7/31/17.
 */
public abstract class Task implements Runnable {
  static final Logger log = LoggerFactory.getLogger(Task.class);
  final int weight;
  private final String name;
  private final DateTime startTime;
  private final TimeUnit timeUnit;

  private int numFailures = 0;
  private int numRequests = 0;
  private Long minDuration = null;
  private Long maxDuration = null;
  private long totalDuration = 0;

  private long lastRequestTimestamp = DateTime.now().getMillis()/1000;

  class LongIntMap {
    final Map<Long, Integer> longIntegerMap;

    LongIntMap() {
      longIntegerMap = new HashMap<>();
    }

    void add(Long k) {
      if (longIntegerMap.containsKey(k)) {
        longIntegerMap.put(k, longIntegerMap.get(k) + 1);
      } else {
        longIntegerMap.put(k, 1);
      }
    }
  }

  private final LongIntMap requestsPerSec;
  private final LongIntMap responseTimes;

  private volatile boolean stop = false;

  public Task(String name, int weight, TimeUnit timeUnit) {
    this.name = name;
    this.weight = weight;
    this.timeUnit = timeUnit;
    this.startTime = DateTime.now();
    this.requestsPerSec = new LongIntMap();
    this.responseTimes = new LongIntMap();
  }

  public abstract void execute();

  public void run() {
    while (!stop) {
      try {
        long start = DateTime.now().getMillis();
        this.execute();
        long end = DateTime.now().getMillis();
        this.logTime(end - start);
      } catch (Exception e) {
        numFailures++;
      } finally {
        numRequests++;
      }
    }
  }

  private void logTime(long duration) {
    if (timeUnit == TimeUnit.SECONDS) {
      duration /= 1000;
    }
    log.debug("Duration:" + duration);
    this.totalDuration += duration;

    if (this.minDuration == null) {
      this.minDuration = duration;
    } else {
      this.minDuration = Math.min(this.minDuration, duration);
    }

    if (this.maxDuration == null) {
      this.maxDuration = duration;
    } else {
      this.maxDuration = Math.max(this.maxDuration, duration);
    }

    long roundedDuration = 0;

    if (duration < 100) {
      roundedDuration = duration;
    } else if (duration < 1000) {
      roundedDuration = Math.round(duration/10.0) * 10;
    } else if (duration < 1000) {
      roundedDuration = Math.round(duration/100.0) * 100;
    } else {
      roundedDuration = Math.round(duration/1000.0) * 1000;
    }

    this.responseTimes.add(roundedDuration);
    this.lastRequestTimestamp = DateTime.now().getMillis() / 1000;
    this.requestsPerSec.add(this.lastRequestTimestamp);
  }

  void stop() {
    this.stop = true;
  }

  Map<String, Object> getStatistics() {
    log.info("Capturing Statistics");
    Map<String, Object> statistics = new HashMap<>();

    statistics.put("name", this.name);
    statistics.put("method", "thrift");
    statistics.put("last_request_timestamp", this.lastRequestTimestamp);
    statistics.put("start_time", this.startTime.toString());
    statistics.put("num_requests", this.numRequests);
    statistics.put("num_failures", this.numFailures);
    statistics.put("total_response_time", this.totalDuration);
    statistics.put("max_response_time", this.maxDuration);
    statistics.put("min_response_time", this.minDuration);
    statistics.put("total_content_length", 0);
    statistics.put("response_times", this.responseTimes);
    statistics.put("num_reqs_per_sec", this.requestsPerSec);

    numFailures = 0;
    requestsPerSec.longIntegerMap.clear();

    log.info(this.toString());
    return statistics;
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Name: ").append(this.name)
        .append('~').append("Last Request Timestamp").append(this.lastRequestTimestamp)
        .append('~').append("num_requests").append(this.numRequests)
        .append('~').append("num_failures").append(this.numFailures)
        .append('~').append("total_response_time").append(this.totalDuration)
        .append('~').append("max_response_time").append(this.maxDuration)
        .append('~').append("min_response_time").append(this.minDuration);

    return stringBuilder.toString();


  }
}
