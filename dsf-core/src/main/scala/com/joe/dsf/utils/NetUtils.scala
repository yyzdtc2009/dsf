package com.joe.dsf.utils

import java.net.InetAddress

/**
 * 网络相关工具类
 * Created by Joe on 15-6-25.
 */
object NetUtils {
  def getLocalIpAddress:String = InetAddress.getLocalHost.getHostAddress
}
