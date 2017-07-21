package parsing

import config.ProxyConfig
import org.specs2.Specification
import service.provider.connection.{OkHttpClient, ProxyServer}
import service.provider.totook.{TotookAdvertsProvider, TotookDateFormatter}

class TotookParsingSpec extends Specification {

  def is =
    s2"""
      Totook provider must scan:
        at least something scanAtLeastSomething
      """

  def scanAtLeastSomething  = {
    val items = totookAdvertsProvider().getHeaders
    while(items.hasNext) {
      println(items.next())
    }
    items must not beNull
  }

  def totookAdvertsProvider() = {
    val proxy = new ProxyServer(ProxyConfig("149.255.104.247", 29842, "abarie", "bX4BP9jM"))
    val okHttpClient = new OkHttpClient(proxy)
    new TotookAdvertsProvider(okHttpClient, new TotookDateFormatter(), 5)
  }

}
