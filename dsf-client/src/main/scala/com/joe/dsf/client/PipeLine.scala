package com.joe.dsf.client

import com.joe.dsf.client.procedure.Procedure
import scala.collection.mutable

/**
 * 代表工厂中的一个生产线
 * Created by Joe on 15-7-9.
 */
class PipeLine(name:String) {

  val procedureMap = new mutable.HashMap[String,Procedure]()

  def addProcedure(procedure:Procedure): Unit = {
    if(procedureMap.contains(procedure)){

    }else{
      procedureMap.put(procedure.getName,procedure)
    }
  }
}
