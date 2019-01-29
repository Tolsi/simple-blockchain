package ru.tolsi.blockchain

trait Block {
  def parentHash: ByteStr
  def innerBytes: ByteStr
}