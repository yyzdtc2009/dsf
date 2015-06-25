package com.joe.dsf.deploy.node

import akka.actor.{ActorSelection, Actor}
import com.joe.dsf.deploy.DeployMessages.{RegisteredFollowerFailed, RegisteredFollowerSuccess, RegisterFollower, NotifyGetLeader}
import com.joe.dsf.utils.NetUtils
import com.joe.dsf.{DSFException, DSFConf, Logging}
import org.apache.curator.framework.recipes.leader.{LeaderSelector, LeaderSelectorListenerAdapter}
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * DSFNode 启动入口
 * Created by Joe on 15-6-24.
 */
private[dsf] class DSFNode(client:CuratorFramework,nodeId:String,nodes:Array[String/* address:port */]) extends Actor with Logging {

  var isOccupyLeader = true
  var leaderId:String = null
  var isLeader = false
  val followerMap = new mutable.HashMap[String,ActorSelection]()

  override def preStart(): Unit = {
    client.start()
    val leaderSelector = new LeaderSelector(client,"/dsf/node/leader",new LeaderSelectorListenerAdapter {
      override def takeLeadership(client: CuratorFramework): Unit = {
        /**
         * 添加当选leader后的逻辑
         */
        isLeader = true
        leaderId = nodeId
        broadcastGetLeader()
        while (isOccupyLeader)/*只要标志位不释放，常年占用leader的地位*/{
          Thread.sleep(5000)
        }
      }
    })

    leaderSelector.setId(nodeId)

    if(leaderSelector.hasLeadership)/*如果已有leader,直接配置leader*/{
      leaderId = leaderSelector.getLeader.getId
      registerWithLeader()
    }

  }

  override def receive: Receive = {
    case NotifyGetLeader(id) =>
      leaderId = id
      registerWithLeader()

    case RegisterFollower(id) =>
      if(!followerMap.contains(id)) {
        val follower = context.actorSelection(DSFNode.toAkkaUrl(id))
        followerMap.put(id,follower)
      }
      sender ! RegisteredFollowerSuccess(this.nodeId)

    case RegisteredFollowerSuccess(id) =>
      logInfo("Register success to leader:" + id)

    case RegisteredFollowerFailed(reason) =>
      logError("Register failed to leader:%s,reason:%s".format(leaderId,reason) )
  }

  /**
   * 向Leader注册自己
   */
  def registerWithLeader(): Unit ={
    val actor = context.actorSelection(DSFNode.toAkkaUrl(leaderId))
    actor ! RegisterFollower(this.nodeId)
  }

  /**
   * 向集群所有其他节点广播自己当选为leader
   */
  def broadcastGetLeader():Unit = {
    nodes.foreach(id => {
      val actor = context.actorSelection(DSFNode.toAkkaUrl(id))
      actor ! NotifyGetLeader(this.nodeId)
    })
  }
}

object DSFNode {
  val dsfIdRegex = "([^:]+):([0-9]+)".r
  val systemName = "dsfNode"
  private val actorName = "Node"
  def main(args: Array[String]) {
    val dsfConf = new DSFConf("conf/dsf-env.xml")
    val port = 9003 /*为了方便开发阶段测试，这里直接写死，正式版在xml中配置*/
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
        Thread.sleep(300000)
        println("I'm off !")
      }
    })
    leaderSelector.setId(NetUtils.getLocalIpAddress + ":" + port)
    println(leaderSelector.getLeader)
    leaderSelector.autoRequeue()
    leaderSelector.start()
    while(true){
      println("loop")
      Thread.sleep(3000)
    }
  }

  def toAkkaUrl(dsfId: String): String = {
    dsfId match {
      case dsfIdRegex(host, port) =>
        "akka.tcp://%s@%s:%s/user/%s".format(systemName, host, port, actorName)
      case _ =>
        throw new DSFException("Invalid node id: " + dsfId)
    }
  }
}
