package io.vrajat.nomadacris;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;

/**
 * Created by rvenkatesh on 7/28/17.
 */
public class Slave {
  private static final Logger log = LoggerFactory.getLogger(Slave.class);

  private final Client client;

  public enum State {
    STATE_INIT,
    STATE_HATCHING,
    STATE_RUNNING,
    STATE_STOPPED
  }

  private class Quitter extends Thread {
    @Override
    public void run() {
      try {
        client.write(Message.quit);
      } catch (IOException e) {
        log.error("Failed to send quit message:" + e);
      }
    }
  }

  private State state;
  private TaskRunner taskRunner;
  private String className;

  Slave(Client client, String className) {
    this.client = client;
    this.state = State.STATE_INIT;
    this.className = className;
  }

  void run() throws
      IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
    Runtime.getRuntime().addShutdownHook(new Quitter());

    this.client.write(Message.clientReady);
    log.info("I am ready!");
    while (true) {
      Message message;
      try {
        message = this.client.read();
      } catch (EOFException e) {
        log.warn("Connection closed by server");
        return;
      }
      log.info("Received Message:" + message);
      switch (message.typeEnum) {
        case hatch:
          this.client.write(Message.hatching);
          this.hatch(message);
          break;
        case stop:
          this.stop();
          this.client.write(Message.clientStopped);
          this.client.write(Message.clientReady);
          break;
        case quit:
          this.stop();
          return;
      }
    }
  }

  private void hatch(Message hatch) throws
      IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
    this.state = State.STATE_HATCHING;
    this.client.write(Message.hatching);

    Integer numClients = (Integer) hatch.data.get("num_clients");
    taskRunner = new TaskRunner(this.className, numClients, this.client);

    this.client.write(new Message("hatch_complete", "Java Slave",
        ImmutableMap.of("count", (Object) numClients)));

    taskRunner.start();
  }

  private void stop() throws IOException {
    if (this.state == State.STATE_RUNNING) {
      taskRunner.shutdown();
      this.state =State.STATE_STOPPED;
      this.client.write(Message.clientStopped);
    }
  }

  public State getState() {
    return state;
  }
}
