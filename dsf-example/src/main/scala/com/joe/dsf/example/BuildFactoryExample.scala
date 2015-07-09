package com.joe.dsf.example

import com.joe.dsf.client.{PipeLine, Factory}

/**
 * 一个建立Factory的例子
 * Created by Joe on 15-7-9.
 */
object BuildFactoryExample {
  def main(args: Array[String]) {
    val factory = new Factory("192.168.0.103:9001,192.168.0.103:9002,192.168.0.103:9003")
    val pipeLine = new PipeLine("test-pipeline")

  }
}
