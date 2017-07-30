package io.vrajat.nomadacris;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by rvenkatesh on 7/28/17.
 */
public class Nomadacris {
  public static void main(String[] args) {
    setupLogger();
    ArgumentParser parser = ArgumentParsers.newArgumentParser("Nomadacris")
        .defaultHelp(true)
        .description("Start a slave for Locust.io");
    parser.addArgument("-m", "--master")
        .required(true)
        .help("Hostname or IP of master");

    parser.addArgument("-p", "--port")
        .required(true)
        .type(Integer.class)
        .help("Port of Master");

    Namespace ns;
    try {
      ns = parser.parseArgs(args);
      Slave slave = new Slave(new SocketClient(ns.getString("master"), ns.getInt("port")));
      slave.run();
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  private static void setupLogger() {
    LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();

    PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
    logEncoder.setContext(logCtx);
    logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
    logEncoder.start();

    ConsoleAppender logConsoleAppender = new ConsoleAppender();
    logConsoleAppender.setContext(logCtx);
    logConsoleAppender.setName("console");
    logConsoleAppender.setEncoder(logEncoder);
    logConsoleAppender.start();
  }
}
