package com.joe.dsf

/**
 * DSF的公用异常类
 * Created by Joe on 15-6-21.
 */
class DSFException(message:String,cause:Throwable) extends Exception(message,cause){
  def this(message:String) = this(message,null)
}
