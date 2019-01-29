package ru.tolsi.blockchain.pow

import monix.execution.Scheduler.Implicits.traced
import scala.concurrent.duration._

object PowTestApp extends App {
  val genesis = PowBlock.Genesis
  val startBlockchain = PowBlockchain(Seq(genesis))
  val miner = new PowMiner(1.second)
  miner.minerTask(startBlockchain).runAsyncAndForget
  miner.blocksSubject.foreach(bc => println(bc))
  Thread.sleep(10.seconds.toMillis)
}
