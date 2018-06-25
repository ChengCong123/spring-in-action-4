package com.inspur.learning.springinaction.springidol_aop;

public class Instrumentalist implements Performer {
  private Instrument instrument;

  public void perform() throws PerformanceException {
    instrument.play();
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public Instrument getInstrument() {
    return instrument;
  }
}
