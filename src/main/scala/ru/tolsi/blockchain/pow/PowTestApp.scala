package ru.tolsi.blockchain.pow

import com.typesafe.scalalogging.StrictLogging
import monix.execution.Scheduler.Implicits.traced

import scala.concurrent.duration._

object PowTestApp extends App with StrictLogging {
  val genesis = PowBlock.Genesis
  val startBlockchain = PowBlockchain(Seq(genesis), 10.seconds)
  val miner = new PowMiner
  miner.minerTask(startBlockchain).runAsyncAndForget
  miner.blocksSubject.foreach(bc => logger.info(bc.toString))
  Thread.sleep(5.minutes.toMillis)
}
