package com.barrybecker4.simulations.traffic.signals

enum TrafficSignalType(val create: Int => TrafficSignal) {
  case DUMB_TRAFFIC_SIGNAL extends TrafficSignalType(numStreets => new DumbTrafficSignal(numStreets))
  case SEMAPHORE_TRAFFIC_SIGNAL extends TrafficSignalType(numStreets => new SemaphoreTrafficSignal(numStreets))
}