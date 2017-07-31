package io.vrajat.nomadacris.examples;

import com.google.common.collect.ImmutableList;
import io.vrajat.nomadacris.Task;
import io.vrajat.nomadacris.TaskList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rvenkatesh on 7/31/17.
 */
public class Prime implements TaskList {
  final List<Task> tasks;

  public Prime() {
    Task task = new PrimeTask((long)Math.pow(2, 16));
    this.tasks = new ImmutableList.Builder<Task>().add(task).build();
  }

  public List<Task> getTasks() {
    return this.tasks;
  }

  class PrimeTask extends Task {
    final long maxLong;
    List<Long> primes = new ArrayList<>();
    PrimeTask(long maxLong) {
      super("Prime: " +  maxLong, 1, TimeUnit.SECONDS);
      this.maxLong = maxLong;
    }

    @Override
    public void execute() {
      for(long i = 1; i <= maxLong; ++i) {
        // checks if the number is a prime or not
        boolean isPrime = true;
        for(int check = 2; check < i; ++check) {
          if(i % check == 0) {
            isPrime = false;
          }
        }
        if(isPrime) {
          primes.add(i);
        }
      }
    }
  }
}
