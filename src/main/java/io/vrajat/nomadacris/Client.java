package io.vrajat.nomadacris;

import java.io.IOException;

/**
 * Created by rvenkatesh on 7/28/17.
 */
public interface Client {
  Message get() throws IOException;
  void write(Message message) throws IOException;
}
