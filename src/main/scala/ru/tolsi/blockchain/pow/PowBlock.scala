package ru.tolsi.blockchain.pow

import java.text.SimpleDateFormat
import java.util.Date

import com.google.common.primitives.{Ints, Longs}
import ru.tolsi.blockchain.{Block, ByteStr}
import scorex.crypto.hash.Blake2b256

object PowBlock {
  val Genesis = PowBlock(ByteStr(Array.fill(32)(0)), 0, System.currentTimeMillis(), 0)
  private val timePrintFormat = new SimpleDateFormat("HH:mm:ss")
}
case class PowBlock(override val parentHash: ByteStr, nonce: Long, ts: Long, difficulty: Int) extends Block {
  import PowBlock._
  def hash: ByteStr = ByteStr(Blake2b256.hash(hashBytes.arr))
  def hashBytes: ByteStr = ByteStr(parentHash.arr ++ Longs.toByteArray(nonce) ++ Longs.toByteArray(ts) ++ Ints.toByteArray(difficulty))
  override def toString: String = s"POWBlock[hash: ${hash.toString.take(6)}..., parent: ${parentHash.toString.take(6)}..., nonce: $nonce, time: ${timePrintFormat.format(new Date(ts))}, diff: $difficulty]"
}
