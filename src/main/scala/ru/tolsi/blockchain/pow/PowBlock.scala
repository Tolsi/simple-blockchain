package ru.tolsi.blockchain.pow

import com.google.common.primitives.Longs
import ru.tolsi.blockchain.{Block, ByteStr}
import scorex.crypto.hash.Blake2b256

object PowBlock {
  val Genesis = PowBlock(ByteStr(Array.fill(32)(0)), Long.MinValue)
}
case class PowBlock(override val parentHash: ByteStr, nonce: Long) extends Block {
  def hash: ByteStr = ByteStr(Blake2b256.hash(innerBytes.arr))
  override def innerBytes: ByteStr = ByteStr(parentHash.arr ++ Longs.toByteArray(nonce))
}
