package ru.tolsi.blockchain

trait Block {
  def parentHash: ByteStr
}