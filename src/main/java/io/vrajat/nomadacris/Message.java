package io.vrajat.nomadacris;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rvenkatesh on 7/28/17.
 */
public class Message {
  public enum TypeEnum {
    client_ready,
    hatching,
    hatch,
    stop,
    client_stopped,
    quit
  }

  public final String type;
  public final TypeEnum typeEnum;
  public final String nodeId;
  public final Map<String, String> data;


  public final static Message clientReady = new Message("client_ready","Java Slave", null);
  public final static Message hatching = new Message("hatching","Java Slave", null);
  public final static Message clientStopped = new Message("client_stopped","Java Slave", null);

  public Message(String type, String nodeId, Map<String, String> data) {
    this.type = type;
    this.nodeId = nodeId;
    this.data = data;
    this.typeEnum = TypeEnum.valueOf(type);
  }

  public Message(byte[] bytes) throws IOException {
    MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(bytes);
    this.type = messageUnpacker.unpackString();
    this.typeEnum = TypeEnum.valueOf(this.type);
    int numMapElems = messageUnpacker.unpackMapHeader();
    this.data = new HashMap<String, String>();
    while (numMapElems > 0) {
      this.data.put(messageUnpacker.unpackString(), messageUnpacker.unpackString());
      numMapElems--;
    }
    this.nodeId = messageUnpacker.unpackString();
  }

}
