package ru.tolsi.blockchain.pos

import java.text.SimpleDateFormat
import java.util.Date

import com.google.common.primitives.Longs
import ru.tolsi.blockchain.{Block, ByteStr}
import scorex.crypto.hash.Blake2b256
import scorex.crypto.signatures.{Curve25519, PrivateKey}

object PosBlock {
  val Genesis = PosBlock(ByteStr(Array.fill(32)(0)), 0, 100000000, System.currentTimeMillis(), ByteStr(Array.fill(32)(0)), ByteStr(Array.fill(32)(0)))

  def signatureBytes(parentHash: ByteStr,
                     hit: Long,
                     baseTarget: Long,
                     ts: Long,
                     minerPublicKey: ByteStr): ByteStr =
    ByteStr(parentHash.arr ++ Longs.toByteArray(hit) ++ Longs.toByteArray(baseTarget) ++ Longs.toByteArray(ts) ++ minerPublicKey.arr)

  def sign(parentHash: ByteStr,
           hit: Long,
           baseTarget: Long,
           ts: Long,
           minerPublicKey: ByteStr,
           privateKey: ByteStr): PosBlock = {
    val signature = ByteStr(Curve25519.sign(PrivateKey @@ privateKey.arr, signatureBytes(parentHash, hit, baseTarget, ts, minerPublicKey).arr))
    PosBlock(parentHash, hit, baseTarget, ts, minerPublicKey, signature)
  }

  private val timePrintFormat = new SimpleDateFormat("HH:mm:ss")
}

case class PosBlock private(parentHash: ByteStr, hit: Long, baseTarget: Long, ts: Long, minerPublicKey: ByteStr, signature: ByteStr) extends Block {
  import PosBlock.timePrintFormat
  def hash: ByteStr = ByteStr(Blake2b256.hash(signatureBytes.arr))
  def signatureBytes: ByteStr = PosBlock.signatureBytes(parentHash, hit, baseTarget, ts, minerPublicKey)
  override def toString: String = s"PoSBlock[hash: ${hash.toString.take(6)}..., parent: ${parentHash.toString.take(6)}..., hit: $hit, baseTarget: $baseTarget,  time: ${timePrintFormat.format(new Date(ts))}]"
}
