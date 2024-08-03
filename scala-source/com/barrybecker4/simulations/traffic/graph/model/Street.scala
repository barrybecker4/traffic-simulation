package com.barrybecker4.simulations.traffic.graph.model

import com.barrybecker4.graph.Edge


case class Street(intersectionIdx1: Int, 
                  portIdx1: Int, 
                  intersectionIdx2: Int, 
                  portIdx2: Int) {

  override def toString: String =
    s"intersectionIdx1=$intersectionIdx1, portIdx1=$portIdx1; intersectionIdx2=$intersectionIdx2, portIdx2=$portIdx2"
}