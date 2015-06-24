package com.joe.dsf.deploy

import com.joe.dsf.DSFConf
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.recipes.leader.{LeaderSelectorListenerAdapter, LeaderSelector, LeaderLatch}
import org.apache.curator.retry.ExponentialBackoffRetry

/**
 * DSFNode 启动入口
 * Created by Joe on 15-6-24.
 */
object DSFNode {
  def main(args: Array[String]) {
    val dsfConf = new DSFConf("conf/dsf-env.xml")
    val retryPolicy = new ExponentialBackoffRetry(1000,3)
    val client = CuratorFrameworkFactory.builder()
      .connectString(dsfConf.zookeeperAddress)
      .sessionTimeoutMs(2000)
      .connectionTimeoutMs(10000)
      .retryPolicy(retryPolicy)
      .namespace("dsf").build()
    client.start()

    val leaderSelector = new LeaderSelector(client,"/dsf/node/leader",new LeaderSelectorListenerAdapter {
      override def takeLeadership(p1: CuratorFramework): Unit = {
        println("I'm on !")
        Thread.sleep(30000)
        println("I'm off !")
      }
    })
    leaderSelector.autoRequeue()
    leaderSelector.start()
  }
}
