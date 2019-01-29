package ru.tolsi.blockchain

import ru.tolsi.blockchain.pow.PowBlock

trait Blockchain[B <: Block, BC <: Blockchain[B, BC]] {
  def blocks: Seq[B]
  def append(block: PowBlock): Either[ValidationException, BC]
  def validate(block: PowBlock): Either[ValidationException, B]
}