package com.joe.dsf.client

import scala.collection.mutable.ArrayBuffer

/**
 * DSF的基本实体，每个DSF应用以Factory的方式存在。
 * Created by Joe on 15-6-29.
 */
class Factory(nodeAddress:String) {

  val pipeLines = new ArrayBuffer[PipeLine]()

  def addPipeLine(newPipeLine:PipeLine): Unit ={
    pipeLines += newPipeLine
  }

  def submit():Boolean = {
    true
  }
}
