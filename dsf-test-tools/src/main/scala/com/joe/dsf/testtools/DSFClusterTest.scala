package com.joe.dsf.testtools

import com.joe.dsf.deploy.node.DSFNode

/**
 * DSF集群测试工具，用于自动在本地启动多个DSFNode进程，检验是否能够成功组件集群。
 * 在之后的测试中，也用作启动本地DSF集群，协助其他功能测试。
 * Created by yang_yongzhou on 15-6-28.
 */
object DSFClusterTest {
  def main(args: Array[String]) {
    val node1 = new Thread(new Runnable {
      override def run(): Unit = {
        DSFNode.main(Array[String]("-P","dsf.node.port=9001"))
      }
    })

    val node2 = new Thread(new Runnable {
      override def run(): Unit = {
        DSFNode.main(Array[String]("-P","dsf.node.port=9002"))
      }
    })

    val node3 = new Thread(new Runnable {
      override def run(): Unit = {
        DSFNode.main(Array[String]("-P","dsf.node.port=9003"))
      }
    })

    node1.start()
    node2.start()
    node3.start()

    node1.join()
  }
}
