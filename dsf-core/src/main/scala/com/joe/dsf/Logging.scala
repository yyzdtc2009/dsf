package com.joe.dsf

import com.joe.dsf.utils.CommonUtils
import org.apache.log4j.{PropertyConfigurator, LogManager}
import org.slf4j.impl.StaticLoggerBinder
import org.slf4j.{LoggerFactory, Logger}

/**
 * 通用日志输出接口
 * Created by Joe on 15-6-21.
 */
trait Logging {
  //添加transient标签，使有Logging这个trait的对象能够跨节点序列化传输。
  @transient private var log_ : Logger = null

  protected def logName = {
    this.getClass.getName.stripSuffix("$")
  }

  protected def log: Logger = {
    if (log_ == null) {
      initializeIfNecessary()
      log_ = LoggerFactory.getLogger(logName)
    }
    log_
  }

  protected def logInfo(msg: => String) {
    if (log.isInfoEnabled) log.info(msg)
  }

  protected def logDebug(msg: => String) {
    if (log.isDebugEnabled) log.debug(msg)
  }

  protected def logTrace(msg: => String) {
    if (log.isTraceEnabled) log.trace(msg)
  }

  protected def logWarning(msg: => String) {
    if (log.isWarnEnabled) log.warn(msg)
  }

  protected def logError(msg: => String) {
    if (log.isErrorEnabled) log.error(msg)
  }

  protected def logInfo(msg: => String, throwable: Throwable) {
    if (log.isInfoEnabled) log.info(msg, throwable)
  }

  protected def logDebug(msg: => String, throwable: Throwable) {
    if (log.isDebugEnabled) log.debug(msg, throwable)
  }

  protected def logTrace(msg: => String, throwable: Throwable) {
    if (log.isTraceEnabled) log.trace(msg, throwable)
  }

  protected def logWarning(msg: => String, throwable: Throwable) {
    if (log.isWarnEnabled) log.warn(msg, throwable)
  }

  protected def logError(msg: => String, throwable: Throwable) {
    if (log.isErrorEnabled) log.error(msg, throwable)
  }

  protected def isTraceEnabled: Boolean = {
    log.isTraceEnabled
  }

  private def initializeIfNecessary() {
    if (!Logging.initialized) {
      Logging.initLock.synchronized {
        if (!Logging.initialized) {
          initializeLogging()
        }
      }
    }
  }

  private def initializeLogging() {
    val binderClass = StaticLoggerBinder.getSingleton.getLoggerFactoryClassStr
    val usingLog4j12 = "org.slf4j.impl.Log4jLoggerFactory".equals(binderClass)
    val log4j12Initialized = LogManager.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4j12Initialized && usingLog4j12) {
      val defaultLogProps = "com/joe/dsf/log4j-defaults.properties"
      Option(CommonUtils.getDSFClassLoader.getResource(defaultLogProps)) match {
        case Some(url) =>
          PropertyConfigurator.configure(url)
          System.err.println(s"Using DSF's default log4j profile: $defaultLogProps")
        case None =>
          System.err.println(s"DSF was unable to load $defaultLogProps")
      }
    }
    Logging.initialized = true

    log
  }
}

private object Logging {
  @volatile private var initialized = false
  val initLock = new Object()
  try {
    val bridgeClass = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler")
    bridgeClass.getMethod("removeHandlersForRootLogger").invoke(null)
    val installed = bridgeClass.getMethod("isInstalled").invoke(null).asInstanceOf[Boolean]
    if (!installed) {
      bridgeClass.getMethod("install").invoke(null)
    }
  } catch {
    case e: ClassNotFoundException => // can't log anything yet so just fail silently
  }
}