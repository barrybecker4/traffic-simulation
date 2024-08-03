package com.barrybecker4.simulations.traffic.vehicles

import scala.collection.immutable.Set

case class VehicleStatistics(vehicles: Set[VehicleSprite]) {

  private var totalDistance: Double = _
  private var incrementalDistance: Double = _
  initialize()

  def getTotalDistance: Double = totalDistance
  def getIncrementalDistance: Double = incrementalDistance
  def resetIncrementalDistance(): Unit = {
    for (vehicle <- vehicles) {
      vehicle.resetIncrementalDistance()
    }
  }

  private def initialize(): Unit = {
    for (vehicle <- vehicles) {
      totalDistance += vehicle.getTotalDistance
      incrementalDistance += vehicle.getIncrementalDistance
    }
  }
}
