package example

@cloud.golem.Worker
object ShoppingCart { self =>

  def initializeCart(userId: String): String = {
    println(s"Initializing cart for user $userId")
    if (math.random() > 0.1) userId
    else "Error while initializing cart"
  }

}
