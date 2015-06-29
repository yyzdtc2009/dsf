package com.joe.dsf.deploy.node

import akka.actor.{Props, ActorSystem, ActorSelection, Actor}
import com.joe.dsf.deploy.DeployMessages.{RegisteredFollowerFailed, RegisteredFollowerSuccess, RegisterFollower, NotifyGetLeader}
import com.joe.dsf.utils.{AkkaUtils, NetUtils}
import com.joe.dsf.{DSFException, DSFConf, Logging}
import org.apache.curator.framework.recipes.leader.{LeaderSelector, LeaderSelectorListenerAdapter}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.collection.mutable

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


  override def postStop(): Unit = {

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

private[dsf] object DSFNode extends Logging{
  val dsfIdRegex = "([^:]+):([0-9]+)".r
  val systemName = "dsfNode"
  private val actorName = "Node"
  def main(args: Array[String]) {
    logInfo("DSFNode start.")
    val arguments = new DSFNodeArguments(args)        //解析命令行输入参数
    val dsfConf = new DSFConf(arguments.envConfPath)  //读取配置文件

    /**
     * 如果命令行参数中用“-P”指定参数，则用该参数覆盖默认值
     */
    arguments.inputProperties.foreach(property => {
      dsfConf.set(property._1,property._2)
    })

    val host = NetUtils.getLocalIpAddress
    val port = dsfConf.getInt("dsf.node.port",9000)
    val nodeId = host + ":" + port
    logInfo("DSFNode Id:" + nodeId)
    val client = createAndStartCuratorClient(dsfConf)
    val actorSystem = startSystemAndActor(host,port,client,nodeId,dsfConf.getNodeList,dsfConf)
    actorSystem.awaitTermination()
  }

  def toAkkaUrl(dsfId: String): String = {
    dsfId match {
      case dsfIdRegex(host, port) =>
        "akka.tcp://%s@%s:%s/user/%s".format(systemName, host, port, actorName)
      case _ =>
        throw new DSFException("Invalid node id: " + dsfId)
    }
  }

  def createAndStartCuratorClient(dsfConf:DSFConf):CuratorFramework = {
    //TODO 真实运行时配置应从配置文件中读取，或选用一个默认值
    val retryPolicy = new ExponentialBackoffRetry(1000,3)
    val client = CuratorFrameworkFactory.builder()
      .connectString(dsfConf.zookeeperAddress)
      .sessionTimeoutMs(2000)
      .connectionTimeoutMs(10000)
      .retryPolicy(retryPolicy)
      .namespace("dsf").build()
    client.start()
    client
  }

  def startSystemAndActor(host:String,
                          port:Int,
                          curatorClient:CuratorFramework,
                          nodeId:String,
                          nodes:Array[String],
                          dsfConf:DSFConf):ActorSystem = {
    val actorName = "Worker"
    val (actorSystem, boundPort) = AkkaUtils.createActorSystem(systemName, host, port,dsfConf)
    actorSystem.actorOf(Props(classOf[DSFNode], curatorClient,nodeId,nodes), name = actorName)
    actorSystem
  }
}
