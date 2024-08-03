package com.barrybecker4.simulations.traffic.viewer

import com.barrybecker4.simulations.traffic.viewer.adapter.IntersectionSubGraphBuilder.INTERSECTION_TYPE
import com.barrybecker4.simulations.traffic.viewer.adapter.StreetSubGraph.STREET_TYPE
import org.graphstream.graph.{Edge, Graph, Node}

object TrafficGraphUtil {

  def showNodeLabels(graph: Graph): Unit = {
    graph.nodes.forEach((node: Node) => node.setAttribute("ui.label", node.getId))
  }

  def addEdgeLengths(graph: Graph): Unit = {
    graph.edges.forEach((edge: Edge) => edge.setAttribute("length", computeEdgeLength(edge)))
  }
  
  def isStreet(edge: Edge): Boolean = edge.getAttribute("type") == STREET_TYPE
  def isIntersection(edge: Edge): Boolean = edge.getAttribute("type") == INTERSECTION_TYPE

  private def computeEdgeLength(edge: Edge) = {
    val source = edge.getSourceNode
    val target = edge.getTargetNode
    //System.out.println("source = "+ Arrays.toString(source.getAttribute("xyz", Object[].class)) + " target = " + target.getAttribute("xyz", Object[].class));
    val sourceXYZ = source.getAttribute("xyz", classOf[Array[AnyRef]])
    val targetXYZ = target.getAttribute("xyz", classOf[Array[AnyRef]])
    Math.sqrt(Math.pow(targetXYZ(0).asInstanceOf[Double] - sourceXYZ(0).asInstanceOf[Double], 2) + Math.pow(targetXYZ(1).asInstanceOf[Double] - sourceXYZ(1).asInstanceOf[Double], 2))
  }

  def sleep(ms: Long): Unit = {
    try Thread.sleep(ms)
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
  }
}
