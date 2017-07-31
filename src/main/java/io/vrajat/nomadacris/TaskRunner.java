package io.vrajat.nomadacris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by rvenkatesh on 7/31/17.
 */
public class TaskRunner {
  static final Logger log = LoggerFactory.getLogger(TaskRunner.class);

  final Class taskSetClass;
  final int numClients;
  final Client client;

  final TaskList taskList;
  final ExecutorService executorService;
  final ScheduledExecutorService statsReporter;

  TaskRunner(String className, int numClients, Client client) throws
      ClassNotFoundException, InstantiationException, IllegalAccessException {
    this.taskSetClass = Class.forName(className);
    this.taskList = (TaskList) taskSetClass.newInstance();
    this.numClients = numClients;
    this.executorService = Executors.newFixedThreadPool(numClients);
    this.statsReporter = Executors.newSingleThreadScheduledExecutor();
    this.client = client;
  }

  void start() {
    List<Task> tasks = this.taskList.getTasks();
    int totalWeight = 0;

    for (Task task : tasks) {
      totalWeight += task.weight;
    }

    for (Task task  : tasks) {
      float percent = (float)(task.weight) / totalWeight;
      int amount = Math.round((float)this.numClients * percent);

      if (totalWeight == 0) {
        amount = (int) ((float)this.numClients / tasks.size());
      }

      for (int count = 0; count < amount; count++) {
        this.executorService.submit(task);
      }
    }
    statsReporter.scheduleAtFixedRate(new Reporter(this.client, this.taskList),
        0, 10, TimeUnit.SECONDS);
  }

  void shutdown() {
    for (Task task : this.taskList.getTasks()) {
      task.stop();
    }
    executorService.shutdownNow();
    statsReporter.shutdown();
    try {
      statsReporter.awaitTermination(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.warn("Interrupted", e);
    }
    statsReporter.shutdownNow();
  }
}
