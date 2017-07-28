package io.vrajat.nomadacris;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Created by rvenkatesh on 7/28/17.
 */
public class Nomadacris {
  public static void main(String[] args) {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("Nomadacris")
        .defaultHelp(true)
        .description("Start a slave for Locust.io");
    parser.addArgument("-m", "--master")
        .required(true)
        .help("Hostname or IP of master");

    parser.addArgument("-p", "--port")
        .required(true)
        .help("Port of Master");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

    System.out.println(ns.getString("master"));
    System.out.println(ns.getString("port"));
  }
}
