package com.joe.dsf.utils

import akka.actor.{ExtendedActorSystem, ActorSystem}
import com.joe.dsf.{DSFConf, Logging}
import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import scala.collection.JavaConversions.mapAsJavaMap
/**
 * Akka相关工具类
 * Created by Joe on 15-6-27.
 */
object AkkaUtils extends Logging{
  def createActorSystem(
                         name: String,
                         host: String,
                         port: Int,
                         dsfConf:DSFConf): (ActorSystem, Int) = {
    //TODO 将一切从配置中读出或采取默认值
    val akkaThreads   = dsfConf.getInt("dsf.akka.threads", 4)
    val akkaBatchSize = dsfConf.getInt("dsf.akka.batchSize", 15)
    val akkaTimeout = dsfConf.getInt("dsf.akka.timeout", 100)
    val akkaFrameSize = dsfConf.getInt("dsf.akka.frameSize", 10) * 1024 * 1024
    val akkaLogLifecycleEvents = dsfConf.getBoolean("dsf.akka.logLifecycleEvents", defaultValue = false)
    val lifecycleEvents = if (akkaLogLifecycleEvents) "on" else "off"
    if (!akkaLogLifecycleEvents) {
      Option(Logger.getLogger("akka.remote.EndpointWriter")).map(l => l.setLevel(Level.FATAL))
    }

    val logAkkaConfig = if (dsfConf.getBoolean("dsf.akka.logAkkaConfig", defaultValue = false)) "on" else "off"

    val akkaHeartBeatPauses = dsfConf.getInt("dsf.akka.heartbeat.pauses", 600)
    val akkaFailureDetector =
      dsfConf.getDouble("dsf.akka.failure-detector.threshold", 300.0)
    val akkaHeartBeatInterval = dsfConf.getInt("dsf.akka.heartbeat.interval", 1000)

    val akkaConf = ConfigFactory.parseMap(dsfConf.getAkkaConf.toMap[String, String]).withFallback(
      ConfigFactory.parseString(
        s"""
      |akka.daemonic = on
      |akka.loggers = [""akka.event.slf4j.Slf4jLogger""]
      |akka.stdout-loglevel = "ERROR"
      |akka.jvm-exit-on-fatal-error = off
      |akka.remote.require-cookie = "off"
      |akka.remote.secure-cookie = ""
      |akka.remote.transport-failure-detector.heartbeat-interval = $akkaHeartBeatInterval s
      |akka.remote.transport-failure-detector.acceptable-heartbeat-pause = $akkaHeartBeatPauses s
      |akka.remote.transport-failure-detector.threshold = $akkaFailureDetector
      |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
      |akka.remote.netty.tcp.transport-class = "akka.remote.transport.netty.NettyTransport"
      |akka.remote.netty.tcp.hostname = "$host"
      |akka.remote.netty.tcp.port = $port
      |akka.remote.netty.tcp.tcp-nodelay = on
      |akka.remote.netty.tcp.connection-timeout = $akkaTimeout s
      |akka.remote.netty.tcp.maximum-frame-size = ${akkaFrameSize}B
      |akka.remote.netty.tcp.execution-pool-size = $akkaThreads
      |akka.actor.default-dispatcher.throughput = $akkaBatchSize
      |akka.log-config-on-start = $logAkkaConfig
      |akka.remote.log-remote-lifecycle-events = $lifecycleEvents
      |akka.log-dead-letters = $lifecycleEvents
      |akka.log-dead-letters-during-shutdown = $lifecycleEvents
      """.stripMargin))

    val actorSystem = ActorSystem(name, akkaConf)
    val provider = actorSystem.asInstanceOf[ExtendedActorSystem].provider
    val boundPort = provider.getDefaultAddress.port.get
    (actorSystem, boundPort)
  }

}
