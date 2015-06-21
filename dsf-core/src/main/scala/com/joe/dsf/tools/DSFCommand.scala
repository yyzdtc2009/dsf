package com.joe.dsf.tools

/**
 * DSF命令抽象父类
 * Created by Joe on 15-6-21.
 */
abstract class DSFCommand {
  def execute(args:Seq[String])

  def getUsage:String

  def getCommandName:String
}

