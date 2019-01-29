package ru.tolsi.blockchain

trait Blockchain[B <: Block, BC <: Blockchain[B, BC]] {
  def blocks: Seq[B]
  def append(block: B): Either[ValidationException, BC]
  def validate(block: B): Either[ValidationException, B]
}