package com.github.unchama.seichiassist.subsystems.donate

trait DonateWriteAPI[F[_]] {



}

object DonateWriteAPI {

  def apply[F[_]](implicit ev: DonateWriteAPI[F]): DonateWriteAPI[F] = ev

}

trait DonateAPI[F[_]] extends DonateWriteAPI[F]
