package com.joe.dsf.client.procedure

/**
 * 代表流水线上一个工序
 * Created by yang_yongzhou on 15-7-13.
 */
class Procedure(name:String,inputIDs:Array[String],outputIDs:Array[String]) {
  def getName:String = name
  def getInputIDs:Array[String] = inputIDs
  def getOutputIDs:Array[String] = outputIDs


}
