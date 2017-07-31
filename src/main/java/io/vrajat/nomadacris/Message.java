package io.vrajat.nomadacris;

import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rvenkatesh on 7/28/17.
 */
class Message {
  static final Logger log = LoggerFactory.getLogger(Message.class);

  public enum TypeEnum {
    client_ready,
    client_stopped,
    hatching,
    hatch_complete,
    hatch,
    stats,
    stop,
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
    Visitor visitor = new Visitor(packer);
    packer.packArrayHeader(3);
    packer.packString(this.type);
    if (this.data != null) {
      packer.packMapHeader(this.data.size());
      for (Map.Entry<String, Object> entry : this.data.entrySet()) {
        packer.packString(entry.getKey());
        visitor.visit(entry.getValue());
      }
    } else {
      packer.packNil();
    }
    packer.packString(this.nodeId);
    packer.close();

  }

  class Visitor {
    final MessagePacker messagePacker;

    Visitor(MessagePacker messagePacker) {
      this.messagePacker = messagePacker;
    }

    void visit(Object value) throws IOException {
      if (value == null) {
        visitNull(value);
      } else if (value instanceof String) {
        visitString(value);
      } else if (value instanceof Integer) {
        visitInt(value);
      } else if (value instanceof Long) {
        visitLong(value);
      } else if (value instanceof Boolean) {
        visitBool(value);
      } else if (value instanceof Float) {
        visitFloat(value);
      } else if (value instanceof Double) {
        visitDouble(value);
      } else if (value instanceof Map) {
        visitMap(value);
      } else if (value instanceof List) {
        visitList(value);
      } else if (value instanceof Task.LongIntMap) {
        visitRps(value);
      } else {
        throw new IOException("Cannot pack type unknown type:" + value.getClass().getSimpleName());
      }
    }

    void visitNull(Object value) throws IOException {
      this.messagePacker.packNil();
    }

    void visitString(Object value) throws IOException {
      messagePacker.packString((String) value);
    }

    void visitInt(Object value) throws IOException {
      messagePacker.packInt((Integer) value);
    }

    void visitLong(Object value) throws IOException {
      messagePacker.packInt(((Long)value).intValue());
    }

    void visitBool(Object value) throws IOException {
      messagePacker.packBoolean((Boolean) value);
    }

    void visitFloat(Object value) throws IOException {
      messagePacker.packFloat((Float) value);
    }

    void visitDouble(Object value) throws IOException {
      messagePacker.packFloat(((Double) value).floatValue());
    }

    void visitMap(Object value) throws IOException {
      Map<String, Object> map = (Map<String, Object>) value;
      messagePacker.packMapHeader(map.size());

      for (Map.Entry entry : map.entrySet()) {
        this.visitString(entry.getKey());
        this.visit(entry.getValue());
      }
    }

    void visitList(Object value) throws IOException {
      List<Object> list = (List<Object>) value;
      messagePacker.packArrayHeader(list.size());

      for (Object object : list) {
        this.visit(object);
      }
    }

    void visitRps(Object value) throws IOException {
      Task.LongIntMap longIntMap = (Task.LongIntMap) value;
      messagePacker.packMapHeader(longIntMap.longIntegerMap.size());

      for (Map.Entry entry : longIntMap.longIntegerMap.entrySet()) {
        messagePacker.packLong((long)entry.getKey());
        messagePacker.packInt((int)entry.getValue());
      }
    }
  }
}
