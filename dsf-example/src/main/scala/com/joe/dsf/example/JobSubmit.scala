package com.joe.dsf.example

/**
 * 一个用户提交应用的例子
 * Created by Joe on 15-6-22.
 */
object JobSubmit {
  /**
   * Test Command:java -cp dsf-core-1.0-SNAPSHOT.jar com.joe.dsf.tools.DSFTools \
   *                    submit \
   *                    ../../dsf-example/target/dsf-example-1.0-SNAPSHOT.jar \
   *                    com.joe.dsf.example.JobSubmit
   */


  def main(args: Array[String]) {
    println("This is a DSF application.")
    print("Arguments:")
    args.foreach(println)
  }
}
