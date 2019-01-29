package ru.tolsi.blockchain.pos

import monix.eval.Task
import monix.execution.Scheduler.Implicits.traced
import monix.reactive.subjects.ConcurrentSubject
import ru.tolsi.blockchain.{ByteStr, Miner}

class PosMiner(minerPrivateKey: ByteStr, minerPublicKey: ByteStr) extends Miner[PosBlock, PosBlockchain] {
  override def minerTask(startBlockchain: PosBlockchain): Task[PosBlockchain] = {
    PosBlockchain.mine(startBlockchain, minerPrivateKey, minerPublicKey)
      .executeWithOptions(_.enableAutoCancelableRunLoops)
      .flatMap(newBlock => {
          val newBlockchain = startBlockchain.append(newBlock).right.get
          blocksSubject.onNext(newBlockchain)
          minerTask(newBlockchain)
      })
  }
  override val blocksSubject: ConcurrentSubject[PosBlockchain, PosBlockchain] = ConcurrentSubject.publish[PosBlockchain]
}
