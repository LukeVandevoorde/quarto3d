package com.lukevandevoorde.interfaces;

public interface TimeKeeper {

    public void scheduleJob(int millisUntilExecution, Runnable job);

    public int millis();

}
