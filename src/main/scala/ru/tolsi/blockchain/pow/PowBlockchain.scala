package ru.tolsi.blockchain.pow

import monix.eval.Task
import ru.tolsi.blockchain.{Blockchain, ValidationException}
import scala.concurrent.duration.{Duration, FiniteDuration}

object PowBlockchain {
  def mine(blockchain: PowBlockchain, nonce: Long = Long.MinValue, delay: FiniteDuration = Duration.Zero): Task[PowBlock] = {
    Task {
      val lastBlock = blockchain.blocks.last
      val newBlock = PowBlock(lastBlock.hash, nonce)
      blockchain.validate(newBlock)
    }.executeWithOptions(_.enableAutoCancelableRunLoops)
      .flatMap {
      case Left(error) => mine(blockchain, nonce + 1, delay)
      case Right(newBlock) => Task.now(newBlock)
    }.delayExecution(delay)
  }
}

case class PowBlockchain(override val blocks: Seq[PowBlock]) extends Blockchain[PowBlock, PowBlockchain] {
  override def append(block: PowBlock): Either[ValidationException, PowBlockchain] =
    validate(block).map(block => PowBlockchain(blocks :+ block))

  override def validate(block: PowBlock): Either[ValidationException, PowBlock] = {
    Right(block)
  }
}
