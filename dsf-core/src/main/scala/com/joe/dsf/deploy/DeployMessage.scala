package com.joe.dsf.deploy

/**
 * 部署相关的通信消息
 * Created by Joe on 15-6-25.
 */
private[deploy] sealed trait DeployMessage extends Serializable

object DeployMessages {
  case class NotifyGetLeader(id:String) extends DeployMessage

  case class RegisterFollower(id:String) extends DeployMessage

  case class RegisteredFollowerSuccess(id:String) extends DeployMessage

  case class RegisteredFollowerFailed(reason:String) extends DeployMessage
}
