package com.barrybecker4.simulations.traffic.signals

enum SignalState(val color: String) {
  case GREEN extends SignalState("#22FF2255;")
  case YELLOW extends SignalState("#FFFF2266;")
  case RED extends SignalState("#FF222244;")
}