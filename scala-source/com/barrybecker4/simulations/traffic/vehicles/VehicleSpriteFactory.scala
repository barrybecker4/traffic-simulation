package com.barrybecker4.simulations.traffic.vehicles

import org.graphstream.ui.graphicGraph.stylesheet.Values
import org.graphstream.ui.spriteManager.{Sprite, SpriteFactory, SpriteManager}
import com.barrybecker4.simulations.traffic.vehicles.VehicleSpriteManager


class VehicleSpriteFactory(initialSpeed: Double) extends SpriteFactory {

  override def newSprite(identifier: String, manager: SpriteManager, position: Values) =
    new VehicleSprite(identifier, initialSpeed, manager.asInstanceOf[VehicleSpriteManager])

}