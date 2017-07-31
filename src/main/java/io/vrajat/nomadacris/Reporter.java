package io.vrajat.nomadacris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by rvenkatesh on 7/31/17.
 */
class Reporter implements Runnable {
  static final Logger log = LoggerFactory.getLogger(Reporter.class);

  final Client client;
  final TaskList taskList;

  public Reporter(Client client, TaskList taskList) {
    this.client = client;
    this.taskList = taskList;
  }

  @Override
  public void run() {
    try {
      Map<String, Object> data = new HashMap<>();
      List<Map<String, Object>> statisticsList = new ArrayList<>();

      for (Task task : taskList.getTasks()) {
        statisticsList.add(task.getStatistics());
      }

      data.put("stats", statisticsList);
      data.put("errors", new HashMap<String, String>());
      data.put("user_count", taskList.getTasks().size());

      this.client.write(new Message("stats", "Java Slave", data));
    } catch (IOException e) {
      TaskRunner.log.error("Failed to send statistics", e);
    }
  }
}
