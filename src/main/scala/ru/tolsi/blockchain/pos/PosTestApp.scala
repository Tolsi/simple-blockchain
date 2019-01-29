package ru.tolsi.blockchain.pos

import com.typesafe.scalalogging.StrictLogging
import ru.tolsi.blockchain.ByteStr
import scorex.crypto.signatures.Curve25519
import monix.execution.Scheduler.Implicits.traced

import scala.concurrent.duration._

object PosTestApp extends App with StrictLogging {
  val genesis = PosBlock.Genesis
  val (alicePk, alicePub) = Curve25519.createKeyPair(Array(1,3,5))
  val (bobPk, bobPub) = Curve25519.createKeyPair(Array(5,2,1))
  val startBlockchain = PosBlockchain(Seq(genesis), Map(ByteStr(alicePub) -> 1000000, ByteStr(bobPub) -> 2000000), 10.seconds)
  logger.info(s"Start from $startBlockchain")
  val miner = new PosMiner(ByteStr(alicePk), ByteStr(alicePub))
  miner.minerTask(startBlockchain).runAsyncAndForget
  miner.blocksSubject.foreach(bc => logger.info(bc.toString))
  Thread.sleep(5.minutes.toMillis)
}
