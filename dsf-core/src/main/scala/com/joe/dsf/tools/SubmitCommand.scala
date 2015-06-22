package com.joe.dsf.tools

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URL

import com.joe.dsf.Logging
import com.joe.dsf.deploy.DSFClassLoader

/**
 * 用于提交应用的命令
 * Created by Joe on 15-6-21.
 */
object SubmitCommand extends DSFCommand with Logging{

  override def execute(args: Seq[String]): Unit = {
    args.foreach(println)
    val classLoader = new DSFClassLoader(new Array[URL](0),Thread.currentThread().getContextClassLoader)
    Thread.currentThread().setContextClassLoader(classLoader)

    val jarFile = new File(args(0))

    classLoader.addURL(jarFile.toURI.toURL)
    val mainClassName = args(1)
    var mainClass:Class[_] = null
    try {
      mainClass = Class.forName(mainClassName, true, classLoader)
    }catch {
      case e:ClassNotFoundException =>
        logError(e.getMessage,e)
        System.exit(1)
    }

    val mainMethod = mainClass.getMethod("main",new Array[String](0).getClass)
    val testChildArgs:Array[String] = Array[String]("hello","class")
    try {
      mainMethod.invoke(null, testChildArgs)
    }catch {
      case e:InvocationTargetException => throw e.getCause
    }
  }

  override def getUsage: String = "<app jar> <main class>"

  override def getCommandName: String = "submit"
}

