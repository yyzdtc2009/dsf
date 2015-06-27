package com.joe.dsf

import com.joe.dsf.deploy.node.DSFNode

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.xml._
/**
 * DSF的通用配置对象，与conf下得dsf-env.xml相关联
 * Created by Joe on 15-6-24.
 */
private[dsf] class DSFConf(path:String) {

  val settings = new mutable.HashMap[String, String]()
  private val nodeList = new ArrayBuffer[String]()
  var zookeeperAddress:String = null

  load()

  def load(): Unit = {
    val dsfXml = XML.loadFile(path)
    zookeeperAddress = (dsfXml \ "zookeeper").text.trim
  }

  def loadNodeList(dsfXml:NodeSeq) = {
    val clusterConfPath = (dsfXml \ "cluster-conf-path").text.trim
    for(line <- Source.fromFile(clusterConfPath)){
      line match {
        case DSFNode.dsfIdRegex(host,port) => nodeList += line
      }
    }
  }

  def getNodeList:Array[String] = nodeList.toArray

  def set(key: String, value: String): DSFConf = {
    if (key == null) {
      throw new NullPointerException("null key")
    }
    if (value == null) {
      throw new NullPointerException("null value")
    }
    settings(key) = value
    this
  }

  /** Set multiple parameters together */
  def setAll(settings: Traversable[(String, String)]) = {
    this.settings ++= settings
    this
  }

  /** Set a parameter if it isn't already configured */
  def setIfMissing(key: String, value: String): DSFConf = {
    if (!settings.contains(key)) {
      settings(key) = value
    }
    this
  }

  /** Remove a parameter from the configuration */
  def remove(key: String): DSFConf = {
    settings.remove(key)
    this
  }

  /** Get a parameter; throws a NoSuchElementException if it's not set */
  def get(key: String): String = {
    settings.getOrElse(key, throw new NoSuchElementException(key))
  }

  /** Get a parameter, falling back to a default if not set */
  def get(key: String, defaultValue: String): String = {
    settings.getOrElse(key, defaultValue)
  }

  /** Get a parameter as an Option */
  def getOption(key: String): Option[String] = {
    settings.get(key)
  }

  /** Get all parameters as a list of pairs */
  def getAll: Array[(String, String)] = settings.clone().toArray

  /** Get a parameter as an integer, falling back to a default if not set */
  def getInt(key: String, defaultValue: Int): Int = {
    getOption(key).map(_.toInt).getOrElse(defaultValue)
  }

  /** Get a parameter as a long, falling back to a default if not set */
  def getLong(key: String, defaultValue: Long): Long = {
    getOption(key).map(_.toLong).getOrElse(defaultValue)
  }

  /** Get a parameter as a double, falling back to a default if not set */
  def getDouble(key: String, defaultValue: Double): Double = {
    getOption(key).map(_.toDouble).getOrElse(defaultValue)
  }

  /** Get a parameter as a boolean, falling back to a default if not set */
  def getBoolean(key: String, defaultValue: Boolean): Boolean = {
    getOption(key).map(_.toBoolean).getOrElse(defaultValue)
  }

  def getAkkaConf: Seq[(String, String)] = getAll.filter { case (k, _) => k.startsWith("akka.") }
}
