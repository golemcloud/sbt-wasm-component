package example

@cloud.golem.WitExport
object ShoppingCart extends Api { self =>

  def initializeCart(userId: String): WitResult[String, String] = {
    println(s"Initializing cart for user $userId")
    if (math.random() > 0.1) WitResult.ok(userId)
    else WitResult.err("Error while initializing cart")
  }

}
