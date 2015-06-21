package com.joe.dsf.tools

/**
 * 用于提交应用的命令
 * Created by Joe on 15-6-21.
 */
object SubmitCommand extends DSFCommand{

  override def execute(args: Seq[String]): Unit = {
    args.foreach(println)
  }

  override def getUsage: String = "<app jar>"

  override def getCommandName: String = "submit"
}

