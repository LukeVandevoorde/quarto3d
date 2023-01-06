package com.lukevandevoorde.interfaces;

public interface TimeKeeper {

    public interface Job {
        public void execute();
    }

    public void scheduleJob(int millisUntilExecution, Job job);

    public int millis();

}
