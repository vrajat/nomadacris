package com.github.vrajat.nomadacris;


import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by rvenkatesh on 7/28/17.
 */
public class SocketClient implements Client {
  static final Logger log = LoggerFactory.getLogger(SocketClient.class);
  public final String hostname;
  public final int port;
  public final Socket socket;
  public final DataInputStream dataInputStream;
  public final DataOutputStream dataOutputStream;

  public SocketClient(String hostname, int port) throws IOException {
    this.hostname = hostname;
    this.port = port;
    this.socket = new Socket(hostname, port);
    this.dataInputStream = new DataInputStream(this.socket.getInputStream());
    this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
  }

  public Message read() throws IOException {
    int length = this.dataInputStream.readInt();
    log.info("Message size is " + length);
    int rlen = 0;
    byte[] array = new byte[length];

    rlen = this.dataInputStream.read(array, rlen, length);
    if (rlen < length) {
      throw new IOException("Read only " + rlen + ". Expected " + length);
    }

    return new Message(array);
  }

  public void write(Message message) throws IOException{
    MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
    log.info("Packing will begin");
    message.write(packer);
    byte [] bytes = packer.toByteArray();
    log.info("Packer has packed message: " + bytes.length);
    dataOutputStream.writeInt(bytes.length);
    dataOutputStream.write(bytes);
    dataOutputStream.flush();
  }
}
