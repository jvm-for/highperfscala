package highperfscala.orderbook

import org.scalacheck.Gen

/**
  * Please doc ...
  */
case class Price(value: BigDecimal)
object Price {
  implicit val genPrice: Gen[Price] = Gen.posNum[Double].map( d => Price(BigDecimal(d)))

  implicit val ordering: Ordering[Price] = new Ordering[Price] {
    override def compare(x: Price, y: Price): Int =
      Ordering.BigDecimal.compare(x.value, y.value)
  }
}

class Order {

}
