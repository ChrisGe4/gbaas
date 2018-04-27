package org.cg.service;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chrisge on 11/9/17.
 */
public class CommandRunner {

  private static final Logger log = LoggerFactory.getLogger(CommandRunner.class);


  public static List<String> runCommand(String dir, String cmd)
      throws IOException, InterruptedException {
    List<String> out = new ArrayList<>();
    ProcessBuilder pb = new ProcessBuilder();
    pb.command(Arrays.asList(cmd.replaceAll(";", "").split(" ")));
    //pb.command(Arrays.asList(cmd));
    if (!Strings.isNullOrEmpty(dir)) {
      pb.directory(new File(dir));
    }
    Process process = pb.start();
    System.out.println("Running command " + cmd);

    BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()));
    // String output = bri.readLine();
    String line;
    while ((line = bri.readLine()) != null) {
      out.add(line);
    }
    int exitCode = process.waitFor();
    System.out.println("exitCode = " + exitCode);
    return out;
  }

  public static void runCommand(List<String> cmd, Logger log)
      throws IOException, InterruptedException {
    //List<String> out = new ArrayList<>();
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(true);
    // File log = new File("log");
    // pb.redirectOutput(Redirect.to(log));

    Process process = pb.inheritIO().start();
    log.info("Running command " + cmd);
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      log.error("exitCode = " + exitCode);
      throw new RuntimeException("Command failed. Deployment aborted");
    }
    // BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    // BufferedReader info = new BufferedReader(new InputStreamReader(process.getInputStream()));
    //
    // // String output = err.readLine();
    // String line;
    // while ((line = info.readLine()) != null) {
    //   // out.add(line);
    //   log.info(line);
    // }
    // while ((line = err.readLine()) != null) {
    //   // out.add(line);
    //   log.error(line);
    // }

  }


  private static class StreamGobbler implements Runnable {

    private final InputStream inputStream;
    private final Consumer<String> consumer;

    private StreamGobbler(InputStream inputStream,
        Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    public static StreamGobbler of(InputStream inputStream,
        Consumer<String> consumer) {
      return new StreamGobbler(inputStream, consumer);
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
  }
}
