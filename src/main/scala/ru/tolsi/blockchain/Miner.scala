package ru.tolsi.blockchain

import monix.eval.Task
import monix.reactive.subjects.ConcurrentSubject

trait Miner[B <: Block, BC <: Blockchain[B, BC]] {
  def minerTask(blockchain: BC): Task[BC]
  def blocksSubject: ConcurrentSubject[BC, BC]
}
