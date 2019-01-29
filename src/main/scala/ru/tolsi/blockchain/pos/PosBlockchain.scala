package ru.tolsi.blockchain.pos

import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import ru.tolsi.blockchain.{Blockchain, ByteStr, ValidationException}
import scorex.crypto.signatures.{Curve25519, PublicKey, Signature}

import scala.concurrent.duration._

object PosBlockchain extends StrictLogging {
  def xor(b1: Array[Byte], b2: Array[Byte]): Array[Byte] = {
    b1.zip(b2).map((x: (Byte, Byte)) => (x._1 ^ x._2).toByte)
  }

  def hit(prevBlock: PosBlock, account: ByteStr): Long =
    math.abs(BigInt(1, xor(prevBlock.hash.arr, account.arr)).longValue())

  def calculateDelay(prevBlock: PosBlock, account: ByteStr, accountBalance: Long): Long =
    hit(prevBlock, account) / (accountBalance * prevBlock.baseTarget)

  def newBaseTarget(delay: Long, baseTarget: Long, targetDelay: FiniteDuration): Long = {
    if (delay > targetDelay.toMillis * 1.5) {
      (baseTarget * 1.05).toLong
    } else {
      if (delay < targetDelay.toMillis * 0.5) {
        (baseTarget * 0.95).toLong
      } else {
        baseTarget
      }
    }
  }

  def mine(blockchain: PosBlockchain, minerPrivateKey: ByteStr, minerPublicKey: ByteStr): Task[PosBlock] = {
    val lastBlock = blockchain.blocks.last
    val accountBalance = blockchain.balances.getOrElse(minerPublicKey, 0L)
    val delay = calculateDelay(lastBlock, minerPublicKey, accountBalance)
    logger.trace(s"Miner delay: ${delay.millis}")
    Task {
      val now = System.currentTimeMillis()
      val timeDiff = now - lastBlock.ts
      val newBlock = PosBlock.sign(lastBlock.hash, hit(lastBlock, minerPublicKey), newBaseTarget(timeDiff, lastBlock.baseTarget, blockchain.targetBlockFrequency), now, minerPublicKey, minerPrivateKey)
      blockchain.validate(newBlock)
    }.executeWithOptions(_.enableAutoCancelableRunLoops)
      .flatMap {
        case Left(error) =>
          logger.trace(s"validation was failed: ${error.message}, mine($blockchain, $minerPublicKey, $accountBalance)")
          mine(blockchain, minerPrivateKey, minerPublicKey)
        case Right(newBlock) =>
          logger.trace(s"validation was successful, new block: $newBlock")
          Task.now(newBlock)
      }.delayExecution(delay.millis)
  }
}

case class PosBlockchain(override val blocks: Seq[PosBlock], balances: Map[ByteStr, Long], targetBlockFrequency: FiniteDuration) extends Blockchain[PosBlock, PosBlockchain] {
  override def append(block: PosBlock): Either[ValidationException, PosBlockchain] = {
    validate(block).map(block => copy(blocks = blocks :+ block))
  }

  override def validate(block: PosBlock): Either[ValidationException, PosBlock] = {
    val lastBlock = blocks.last
    for {
      _ <- Either.cond(block.hit == PosBlockchain.hit(lastBlock, block.minerPublicKey), block, ValidationException("New hit is not valid"))
      _ <- Either.cond(block.ts >= lastBlock.ts + PosBlockchain.calculateDelay(lastBlock, block.minerPublicKey, balances(block.minerPublicKey)), block, ValidationException("Miner can't mine at this moment"))
      res <- Either.cond(Curve25519.verify(Signature @@ block.signature.arr, block.signatureBytes.arr, PublicKey @@ block.minerPublicKey.arr), block, ValidationException("Signature is not valid"))
    } yield block
  }

  override def toString: String = s"PoSBC[Last block: ${blocks.last}, total: ${blocks.size}]"
}
