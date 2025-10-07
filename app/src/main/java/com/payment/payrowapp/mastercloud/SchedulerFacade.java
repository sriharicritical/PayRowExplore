package com.payment.payrowapp.mastercloud;

import io.reactivex.rxjava3.core.Scheduler;

public interface SchedulerFacade {
  Scheduler io();

  Scheduler computation();

  Scheduler ui();
}
