package com.joe.dsf.deploy

import java.net.{URLClassLoader, URL}


/**
 * DSF自定义的ClassLoader，独立出来方便在以后进行扩展
 * Created by Joe on 15-6-22.
 */
private[dsf] class DSFClassLoader(urls:Array[URL],parent:ClassLoader) extends URLClassLoader(urls,parent) with MutableURLClassLoader{
  override def addURL(url: URL): Unit = super.addURL(url)
}

private[dsf] trait MutableURLClassLoader extends ClassLoader {
  def addURL(url: URL)
  def getURLs: Array[URL]
}