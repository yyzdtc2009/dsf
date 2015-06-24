package com.joe.dsf

import scala.xml._
/**
 * DSF的通用配置对象，与conf下得dsf-env.xml相关联
 * Created by Joe on 15-6-24.
 */
private[dsf] class DSFConf(path:String) {
  var zookeeperAddress:String = null

  load()

  def load(): Unit = {
    val dsfXml = XML.loadFile(path)
    zookeeperAddress = (dsfXml \ "zookeeper").text.trim
  }
}
