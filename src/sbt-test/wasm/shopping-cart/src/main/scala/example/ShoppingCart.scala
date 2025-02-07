package example

import example.bindings.wit._
import example.bindings.export.pack.name.api._

final case class State(userId: String, items: List[ProductItem]) {
  self =>
  def withUserId(userId: String): State = self.copy(userId = userId)

  def addItem(item: ProductItem): State = self.copy(items = self.items :+ item)

  def removeItem(productId: String): State = self.copy(items = self.items.filterNot(_.productId == productId))

  def updateItemQuantity(productId: String, quantity: Integer): State =
    self.copy(items = self.items.map { item =>
      if (item.productId == productId) ProductItem(item.productId, item.name, item.price, quantity)
      else item
    })

  def clear: State = self.copy(items = List.empty)
}

object State {
  val empty = State(userId = "", items = List.empty)
}
