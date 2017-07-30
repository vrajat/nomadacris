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
  static final Logger log = LoggerFactory.getLogger(Slave.class);

  public final Client client;

  public enum State {
    STATE_INIT,
    STATE_HATCHING,
    STATE_RUNNING,
    STATE_STOPPED
  }

  protected State state;

  public Slave(Client client) {
    this.client = client;
    this.state = State.STATE_INIT;
  }

  public void run() throws IOException {
    this.client.write(Message.clientReady);
    while (true) {
      Message message;
      try {
        message = this.client.get();
      } catch (EOFException e) {
        log.warn("Connection closed by server");
        return;
      }
      log.info("Received Message:" + message);
      switch (message.typeEnum) {
        case hatch:
          this.client.write(Message.hatching);
          int rate = Integer.parseInt(message.data.get("hatch_rate"));
          int numWorkers = Integer.parseInt(message.data.get("num_clients"));
          this.hatch(rate, numWorkers);
          break;
        case stop:
          this.stop();
          this.client.write(Message.clientStopped);
          this.client.write(Message.clientReady);
          break;
        case quit:
          return;
      }
    }
  }

  private void hatch(int rate, int numClients) throws IOException {

    this.client.write(new Message("hatch_complete", "Java Slave",
        ImmutableMap.of("count", new Integer(numClients).toString())));
  }

  private void stop() {
    if (this.state == State.STATE_RUNNING) {
      //TODO stop thread pool
      this.state =State.STATE_STOPPED;
    }
  }

  public State getState() {
    return state;
  }
}
