package ru.tolsi.blockchain.pow

import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import ru.tolsi.blockchain.{Blockchain, ValidationException}

import scala.concurrent.duration.{Duration, FiniteDuration}

object PowBlockchain extends StrictLogging {
  def newDifficulty(oldDifficulty: Int, lastBlockTime: Long, currentTime: Long, targetBlockFrequency: FiniteDuration): Int = {
    val timeDiff = currentTime - lastBlockTime
    val timesRelativelyTarget = timeDiff.toDouble / targetBlockFrequency.toMillis
    val newValue = if (timesRelativelyTarget > 1.2) {
      oldDifficulty - 1
    } else {
      if (timesRelativelyTarget < 0.8) {
        oldDifficulty + 1
      } else {
        oldDifficulty
      }
    }
    logger.trace(s"newDifficulty($oldDifficulty, $lastBlockTime, $currentTime, $targetBlockFrequency), timeDiff = $timeDiff, timesRelativelyTarget = $timesRelativelyTarget = $newValue")
    newValue
  }

  def mine(blockchain: PowBlockchain, nonce: Long = 0, delay: FiniteDuration = Duration.Zero): Task[PowBlock] = {
    Task {
      val lastBlock = blockchain.blocks.last
      val now = System.currentTimeMillis()
      val newBlock = PowBlock(lastBlock.hash, nonce, now, newDifficulty(lastBlock.difficulty, lastBlock.ts, now, blockchain.targetBlockFrequency))
      blockchain.validate(newBlock)
    }.executeWithOptions(_.enableAutoCancelableRunLoops)
      .flatMap {
      case Left(error) =>
        logger.trace(s"validation was failed: ${error.message}, mine($blockchain, ${nonce + 1}, $delay)")
        mine(blockchain, nonce + 1, delay)
      case Right(newBlock) =>
        logger.trace(s"validation was successful, new block: $newBlock")
        Task.now(newBlock)
    }.delayExecution(delay)
  }
}

case class PowBlockchain(override val blocks: Seq[PowBlock], targetBlockFrequency: FiniteDuration) extends Blockchain[PowBlock, PowBlockchain] {
  override def append(block: PowBlock): Either[ValidationException, PowBlockchain] =
    validate(block).map(block => copy(blocks = blocks :+ block))

  override def validate(block: PowBlock): Either[ValidationException, PowBlock] = {
    val lastBlock = blocks.last
    for {
      r1 <- Either.cond(block.hash.arr.takeWhile(_ == 0).length >= block.difficulty, block, ValidationException("Hash not suitable for given complexity"))
      r2 <- Either.cond(PowBlockchain.newDifficulty(lastBlock.difficulty, lastBlock.ts, block.ts, targetBlockFrequency) == block.difficulty, r1, ValidationException("New difficulty is wrong"))
    } yield r2
  }
}
