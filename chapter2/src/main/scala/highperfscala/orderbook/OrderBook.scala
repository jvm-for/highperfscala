package highperfscala.orderbook

import scala.collection.immutable.{Queue, TreeMap}

/**
  * Please doc ...
  */
object Commands {
  sealed trait Command
  case class AddLimitOrder(o: LimitOrder) extends Command
  case class CancelOrder(id: OrderId) extends Command
}

object Events {
  sealed trait Event
  object Event {
    val orderCancelRejected: Event = OrderCancelRejected
  }
  case class OrderExecuted(buy: Execution, sell: Execution) extends Event
  case object LimitOrderAdded extends Event
  case object OrderCancelRejected extends Event
  case object OrderCanceled extends Event
}

case class OrderBook(
                    bids: TreeMap[Price, Queue[BuyLimitOrder]],
                    offers: TreeMap[Price, Queue[SellLimitOrder]]
                    ) {
  def bestBid: Option[BuyLimitOrder] = bids.lastOption.flatMap(_._2.headOption)

  def bestOffer: Option[SellLimitOrder] = offers.headOption.flatMap(_._2.headOption)
}

object OrderBook {
  import Commands._
  import Events._
  val noEvent: Option[Event] = None
  val empty: OrderBook = OrderBook(
    TreeMap.empty[Price, Queue[BuyLimitOrder]],
    TreeMap.empty[Price, Queue[SellLimitOrder]]
  )

  // Could make sense to handle with State Monad
  def handle(ob: OrderBook, c: Command): (OrderBook, Event) = c match {
    case AddLimitOrder(o) => handleAddLimitOrder(ob, o)
    case CancelOrder(id) => handleCancelOrder(ob, id)
  }

  private def handleAddLimitOrder(ob: OrderBook, o: LimitOrder): (OrderBook, Event) = o match {
    case oo: BuyLimitOrder =>
      ob.bestOffer.exists(oo.price.value >= _.price.value) match {
        case true => crossBookBuy(ob, oo)
        case false =>
          val orders = ob.bids.getOrElse(oo.price, Queue.empty)
          ob.copy(bids = ob.bids + (oo.price -> orders.enqueue(oo))) -> LimitOrderAdded
      }

    case oo: SellLimitOrder =>
      ob.bestBid.exists(oo.price.value <= _.price.value) match {
        case true => crossBookSell(ob, oo)
        case false =>
          val orders = ob.offers.getOrElse(oo.price, Queue.empty)
          ob.copy(offers = ob.offers + (oo.price -> orders.enqueue(oo))) -> LimitOrderAdded
      }

  }

  private def crossBookBuy(ob: OrderBook, b: BuyLimitOrder): (OrderBook, Event) =
    ob.offers.headOption.fold(handleAddLimitOrder(ob, b)) { case  (_, xs) =>
        val (o, qq) = xs.dequeue
      (ob.copy(offers = qq.isEmpty match {
        case true => ob.offers - o.price
        case false => ob.offers + (o.price -> qq)
      }), OrderExecuted(Execution(b.id, o.price), Execution(o.id, o.price)))
    }
}
