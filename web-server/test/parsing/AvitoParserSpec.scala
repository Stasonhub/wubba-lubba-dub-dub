package parsing

import config.ProxyConfig
import org.mockserver.client.proxy.ProxyClient
import org.mockserver.integration.ClientAndProxy
import org.specs2.Specification
import service.provider.avito.AvitoParser
import service.provider.connection.{ProxyServer, WebDriver}

import scala.concurrent.duration._

class AvitoParserSpec extends Specification {

  def is =
    s2"""
      Avito provider must scan:
        at least something $scanAtLeastSomething
      """

  def scanAtLeastSomething = {
    val parser = avitoParser()
    val items = parser.parseAdverts(0, System.currentTimeMillis() - (12.days.toMillis + 12.hours.toMillis))
    println(items)

    items must not beEmpty
  }

  def avitoParser() = {
    val webDriver = new WebDriver(proxy, null)
    new AvitoParser(webDriver)
  }

}
