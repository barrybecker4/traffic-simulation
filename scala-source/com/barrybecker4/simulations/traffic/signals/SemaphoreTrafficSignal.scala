package com.barrybecker4.simulations.traffic.signals

import com.barrybecker4.simulations.traffic.signals.{SignalState, TrafficSignal}
import com.barrybecker4.simulations.traffic.signals.SignalState.*

import java.util.concurrent.{Executors, ScheduledFuture, Semaphore, TimeUnit}
import concurrent.duration.DurationInt
import com.barrybecker4.simulations.traffic.signals.SemaphoreTrafficSignal.*
import com.barrybecker4.simulations.traffic.vehicles.VehicleSprite
import com.barrybecker4.simulations.traffic.viewer.TrafficGraphUtil.sleep
import org.graphstream.graph.Node

import scala.annotation.tailrec

/**
 * A more intelligent traffic light system that uses a semaphore to control the traffic lights.
 *  - If a car is within double yellow distance, then try to take semaphore and become green.
 *    Others remain red.
 *  - A light stays green until no cars within double yellow empty or time exceeded.
 *    If time exceeded, then turn yellow, else straight to red.
 *  - The next street (i.e. intersection node port) with cars waiting gets the semaphore and turns green.
 *  - If no cars coming, all will be red, and semaphore up for grabs.
 *
 * @param numStreets the number of streets leading into the intersection
 */
class SemaphoreTrafficSignal(numStreets: Int) extends TrafficSignal(numStreets) {
  private val lightStates: Array[SignalState] = Array.fill(numStreets)(RED)
  private var currentSchedule: ScheduledFuture[?] = _
  private var streetWithSemaphore: Int = AVAILABLE
  private var lastToBecomeRed: Int = -1

  override def getGreenDurationSecs: Int = 8
  override def getLightState(street: Int): SignalState = lightStates(street)

  def shutdown(): Unit = scheduler.shutdown()

  def handleTraffic(sortedVehicles: IndexedSeq[VehicleSprite],
                    portId: Int, edgeLen: Double, deltaTime: Double): Unit = {
    handleTrafficBasedOnLightState(sortedVehicles, portId, edgeLen, deltaTime)
    updateSemaphore(sortedVehicles, portId, edgeLen)
  }

  private def updateSemaphore(sortedVehicles: IndexedSeq[VehicleSprite],
                              portId: Int, edgeLen: Double): Unit = {
    val lightState = getLightState(portId)
    streetWithSemaphore match {
      case AVAILABLE =>
        assert(lightState == RED, "The light state was unexpectedly " + lightState)
        trySwitchingToGreen(portId, sortedVehicles, edgeLen)
      case `portId` =>
        assert(lightState != RED, "The light state was unexpectedly " + lightState)
        if (areCarsComing(sortedVehicles, edgeLen)) {
          // already have it, stay yellow or green, unless we are headed for a jammed street
          // check if the last car is headed for a jammed street. If so, turn yellow.
          val lastCar = sortedVehicles.last
          val isJammed = isNextStreetJammed(lastCar)
          if (isJammed && lightState == GREEN) {
            println("Next street is jammed, so switching to yellow")
            switchToYellow(portId, sortedVehicles, edgeLen)
          }
        } else if (currentSchedule != null && lightState == GREEN) {
          // No cars are coming, so give up the semaphore
          println("No cars coming on street " + portId + " so canceling schedule and switching to red")
          currentSchedule.cancel(true)
          currentSchedule = null
          switchToRed(portId)
        }
      case _ =>
        // do nothing. Some other street has the semaphore
        //println("different street has the semaphore = " + streetWithSemaphore + " port=" + portId + " lightState=" + lightState)
    }
  }

  private def isNextStreetJammed(lastCar: VehicleSprite): Boolean = {
    // If any car on the intersection is stopped, then the next street is jammed.
    val lastIntersectionCar = lastCar.getNextEdge.getAttribute("lastVehicle", classOf[Option[VehicleSprite]])
    if (lastIntersectionCar.nonEmpty && lastIntersectionCar.get.getSpeed < 0.1) {
      true
    } else {
      val nextNode = lastCar.getNextEdge.getTargetNode
      assert(nextNode.getOutDegree == 1)
      val nextStreet = nextNode.getLeavingEdge(0)
      val len = nextStreet.getAttribute("length", classOf[Object]).asInstanceOf[Double]
      val lastVehicleOnNextStreet =
        nextStreet.getAttribute("lastVehicle", classOf[Option[VehicleSprite]])
      if (lastVehicleOnNextStreet.isEmpty) false
      else lastVehicleOnNextStreet.get.getSpeed < 0.1 && lastVehicleOnNextStreet.get.getPosition * len < getFarDistance
    }
  }

  private def trySwitchingToGreen(street: Int, sortedVehicles: IndexedSeq[VehicleSprite],
                            edgeLen: Double): Unit = {
    assert(lightStates(street) == RED)
    assert(streetWithSemaphore == AVAILABLE, "semaphore was not available. It was " + streetWithSemaphore)
    if (lastToBecomeRed != street) {
      if (areCarsComing(sortedVehicles, edgeLen) && !isNextStreetJammed(sortedVehicles.last)) {
        lightStates(street) = GREEN
        streetWithSemaphore = street
        sortedVehicles.last.accelerate(0.1)
        currentSchedule = scheduler.schedule(new Runnable {
          def run(): Unit = switchToYellow(street, sortedVehicles, edgeLen)
        }, getGreenDurationSecs, TimeUnit.SECONDS)
      } else {
        lastToBecomeRed = -1
      }
    }
  }

  private def switchToYellow(street: Int, sortedVehicles: IndexedSeq[VehicleSprite],
                             edgeLen: Double): Unit = {
    assert(lightStates(street) == GREEN)
    lightStates(street) = YELLOW
    yellowStartTime = System.currentTimeMillis()
    assert(streetWithSemaphore == street)
    println("switched to yellow and scheduling switch to red for street " + street + " schedule=" + currentSchedule)
    currentSchedule.cancel(true)
    currentSchedule = scheduler.schedule(new Runnable {
      def run(): Unit = switchToRed(street)
    }, getYellowDurationSecs, TimeUnit.SECONDS)
  }

  private def switchToRed(street: Int): Unit = {
    if (lightStates(street) == YELLOW) {
      println("switching to red from yellow on street " + street)
    }
    lightStates(street) = RED
    assert(streetWithSemaphore == street)
    streetWithSemaphore = AVAILABLE
    lastToBecomeRed = street
  }

  private def areCarsComing(sortedVehicles: IndexedSeq[VehicleSprite], edgeLen: Double): Boolean =
    sortedVehicles.nonEmpty
}


object SemaphoreTrafficSignal {

  private val AVAILABLE = -1
  private val JAM_FACTOR = 2.0

  def main(args: Array[String]): Unit = {
    val numStreets = 5
    val trafficLight = new SemaphoreTrafficSignal(numStreets)
    val checkInterval = 1.second

    val executor = Executors.newScheduledThreadPool(1)
    executor.scheduleAtFixedRate(new Runnable {
      def run(): Unit = trafficLight.printLightStates()
    }, 0, checkInterval.toMillis, TimeUnit.MILLISECONDS)

    Thread.sleep(30000)
    executor.shutdown()
    trafficLight.shutdown()
  }
}