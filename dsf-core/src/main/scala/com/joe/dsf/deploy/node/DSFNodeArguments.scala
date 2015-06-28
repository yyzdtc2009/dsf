package com.joe.dsf.deploy.node

import java.io.File
import com.joe.dsf.Logging
import scala.collection.mutable

/**
 * DSFNode启动参数解析器
 * Created by yang_yongzhou on 15-6-28.
 */
private[node] class DSFNodeArguments(args:Array[String]) {
  var envConfPath:String = "conf/dsf-env.xml"
  val inputProperties = new mutable.HashMap[String,String]()
  def parse(args:List[String]):Unit = args match {
    case ("--env-conf-path")::FileParam(value)::tail =>
      this.envConfPath = value
      parse(tail)
    case ("-P" | "--property")::PropertyParam(value)::tail =>
      val prop = value.toString.split("=")
      inputProperties.put(prop(0),prop(1))
      parse(tail)
    case Nil =>
    case _ =>
      printUsageAndExit(1)

  }

  def printUsageAndExit(exitCode: Int) {
    System.err.println(
      "Usage: DSFNode [options]\n" +
        "\n" +
        "\n" +
        "Options:\n" +
        "  --env-conf-path  PATH          Environment config file path(default:conf/dsf-env.xml)\n" +
        "  --property,-P    property      Property used in DSF")
    System.exit(exitCode)
  }
}

private[dsf] object FileParam extends Logging{
  def unapply(s: String): Option[Int] = {
    val file = new File(s)
    if(file.exists() && file.isFile){
      Some(s)
    }else{
      logWarning("Find an wrong file path param:%s,use default.".format(s))
      None
    }
  }
}

private[dsf] object PropertyParam extends Logging{
  def unapply(s: String): Option[Int] = {
    val prop = s.split("=")
    if(prop.length == 2){
      logInfo("Set a property:key=%s,value=%s".format(prop(0),prop(1)))
      Some(s)
    }else{
      logWarning("Input property error:%s".format(s))
      None
    }
  }
}