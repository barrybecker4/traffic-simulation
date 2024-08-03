package com.barrybecker4.simulations.traffic.demo

import org.graphstream.ui.view.ViewerListener


class ViewerAdapter extends ViewerListener {
  private var looping = true

  def isLooping: Boolean = looping

  // Viewer Listener Interface
  override def viewClosed(id: String): Unit = {
    looping = false
  }

  override def buttonPushed(id: String): Unit = {
  }

  override def buttonReleased(id: String): Unit = {
  }

  override def mouseOver(id: String): Unit = {
  }

  override def mouseLeft(id: String): Unit = {
  }
}
