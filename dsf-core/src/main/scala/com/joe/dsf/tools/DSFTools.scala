package com.joe.dsf.tools

import scala.collection.mutable

/**
 *
 * Created by Joe on 15-6-21.
 */
object DSFTools {

  val commands = new mutable.HashMap[String,DSFCommand]()
  commands.put(SubmitCommand.getCommandName,SubmitCommand)

  def main(args: Array[String]) {
    commands.get(args(0)) match {
      case Some(command) => command.execute(args.tail)
      case None => printUsageAndExit(1)
    }
  }


  def printUsageAndExit(exitCode: Int) {
    var usage = "Usage: DSFTools <command> \n" +
      "\n" +
      "\n" +
      "Command:\n"
    commands.foreach(commandData => {
      val command = commandData._2
      usage += "%s\t%s\n".format(commandData._1,command.getUsage)
    })
    System.err.println(usage)
    System.exit(exitCode)
  }
}
