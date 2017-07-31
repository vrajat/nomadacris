package io.vrajat.nomadacris;

import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rvenkatesh on 7/28/17.
 */
class Message {
  static final Logger log = LoggerFactory.getLogger(Message.class);

  public enum TypeEnum {
    client_ready,
    hatching,
    hatch_complete,
    hatch,
    stop,
    client_stopped,
    quit
  }

  private final String type;
  final TypeEnum typeEnum;
  private final String nodeId;
  final Map<String, Object> data;


  final static Message clientReady = new Message("client_ready","Java Slave", null);
  final static Message hatching = new Message("hatching","Java Slave", null);
  final static Message clientStopped = new Message("client_stopped","Java Slave", null);
  final static Message quit = new Message("quit","Java Slave", null);

  public Message(String type, String nodeId, Map<String, Object> data) {
    this.type = type;
    this.nodeId = nodeId;
    this.data = data;
    this.typeEnum = TypeEnum.valueOf(type);
  }

  Message(byte[] bytes) throws IOException {
    MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(bytes);
    int arrayLen = messageUnpacker.unpackArrayHeader();
    log.info("Got array size of: " + arrayLen);
    this.type = messageUnpacker.unpackString();
    log.info("Message type is:" + this.type);
    this.typeEnum = TypeEnum.valueOf(this.type);

    if (messageUnpacker.getNextFormat() != MessageFormat.NIL) {
      int numMapElems = messageUnpacker.unpackMapHeader();
      log.info("Num Data elems" + numMapElems);
      this.data = new HashMap<String, Object>();
      String key = null;
      while (numMapElems > 0) {
        if (messageUnpacker.getNextFormat() == MessageFormat.NIL) {
          log.info("Key is null");
          messageUnpacker.unpackNil();
        } else {
          key = messageUnpacker.unpackString();
          log.info("Key is " + key);
        }
        MessageFormat messageFormat = messageUnpacker.getNextFormat();

        Object value;

        switch (messageFormat.getValueType()) {
          case BOOLEAN:
            value = messageUnpacker.unpackBoolean();
            break;
          case FLOAT:
            value = messageUnpacker.unpackFloat();
            break;
          case INTEGER:
            value = messageUnpacker.unpackInt();
            break;
          case NIL:
            value = null;
            messageUnpacker.unpackNil();
            break;
          case STRING:
            value = messageUnpacker.unpackString();
            break;
          default:
            throw new IOException("Message received unsupported type: " + messageFormat.getValueType());
        }
        log.info("Value is : " + value);
        this.data.put(key, value);
        numMapElems--;
      }
    } else {
      this.data = null;
    }
    if (messageUnpacker.getNextFormat() != MessageFormat.NIL) {
      this.nodeId = messageUnpacker.unpackString();
    } else {
      messageUnpacker.unpackNil();
      this.nodeId = null;
    }
  }

  void write(MessagePacker packer) throws IOException {
    packer.packArrayHeader(3);
    packer.packString(this.type);
    if (this.data != null) {
      packer.packMapHeader(this.data.size());
      for (Map.Entry<String, Object> entry : this.data.entrySet()) {
        packer.packString(entry.getKey());
        Object value = entry.getValue();
        if (value == null) {
          packer.packNil();
        } else if (value instanceof String) {
          packer.packString((String) value);
        } else if (value instanceof Integer) {
          packer.packInt((Integer) value);
        } else if (value instanceof Boolean) {
          packer.packBoolean((Boolean) value);
        } else if (value instanceof Float) {
          packer.packFloat((Float) value);
        } else {
          throw new IOException("Cannot pack type for" + entry.getKey());
        }
      }
    } else {
      packer.packNil();
    }
    packer.packString(this.nodeId);
    packer.close();

  }
}
