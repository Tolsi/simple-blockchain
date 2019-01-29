package ru.tolsi.blockchain.pow

import monix.eval.Task
import monix.reactive.subjects.ConcurrentSubject
import monix.execution.Scheduler.Implicits.traced
import ru.tolsi.blockchain.Miner

import scala.concurrent.duration.{Duration, FiniteDuration}

class PowMiner(delay: FiniteDuration = Duration.Zero) extends Miner[PowBlock, PowBlockchain] {
  override def minerTask(startBlockchain: PowBlockchain): Task[PowBlockchain] = {
    PowBlockchain.mine(startBlockchain, delay = delay)
      .executeWithOptions(_.enableAutoCancelableRunLoops)
      .flatMap(newBlock => {
          val newBlockchain = startBlockchain.append(newBlock).right.get
          blocksSubject.onNext(newBlockchain)
          minerTask(newBlockchain)
      })
  }
  override val blocksSubject: ConcurrentSubject[PowBlockchain, PowBlockchain] = ConcurrentSubject.publish[PowBlockchain]
}
