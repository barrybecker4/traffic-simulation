package com.barrybecker4.simulations.traffic

import com.barrybecker4.graph.GraphTstUtil
import com.barrybecker4.graph.visualization.{GraphStreamAdapter, GraphViewerFrame}
import com.barrybecker4.simulations.traffic.viewer.TrafficViewerFrame


/**
 * Ideas:
 *  - Avoid gridlock. If cars are stopped in one of the outgoing intersection streets, then we need to turn red.
 *     - add a lastVehicle attribute to streets.
 *     - Use the lastVehicle in the traffic flow calculation.
 */
object TrafficApp extends App {

  val frame = new TrafficViewerFrame()

}
