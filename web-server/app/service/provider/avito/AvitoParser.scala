package service.provider.avito

import java.util.regex.Pattern
import java.util.stream.Collectors

import com.typesafe.scalalogging.Logger
import org.openqa.selenium._
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import service.provider.AdvertImporter
import service.provider.api.RawAdvert
import service.provider.common.Util
import service.provider.connection.WebDriver
import util.ScUtils.binarySearchFirstGreaterOrEq
import util.Utils

import scala.collection.JavaConverters._

class AvitoParser(webDriver: WebDriver) {

  val logger = Logger[AvitoParser]

  private val PAGE_MIN = 1
  private val PAGE_MAX = 50

  /**
    * Parses data [fromTs;untilTs)
    *
    * @param fromTs  inclusive
    * @param untilTs exclusive
    */
  def parseAdverts(fromTs: Long, untilTs: Long): List[RawAdvert] = {
    if (fromTs > untilTs) throw new IllegalArgumentException("")

    // define lazy collection of page max timestamps ordered desc by ts
    val pageMaxTimestamps = (PAGE_MIN to PAGE_MAX).view.map(getFirstAdvertTs)

    // binary search the page
    //  start lessest page
    //
    //
    //
    binarySearchFirstGreaterOrEq(pageMaxTimestamps, fromTs)
      .toList
      .flatMap(page => advertsFromStartPage(page)
        .view
        .dropWhile(advert => advert.publicationDate < fromTs)
        .takeWhile(advert => advert.publicationDate < untilTs))
  }

  private def getFirstAdvertTs(pageNumber: Int): Long = {
    openPage(pageNumber)

    Option(PageElements.parsePageAdvertHeaders(webDriver)
      .stream()
      .findFirst()
      .orElse(null))
      .map(PageElements.parseHeaderTimestamp)
    match {
      case Some(value) => {logger.error(s"FIRST ADVERT IS IS $value");value}
      case _ => throw new IllegalStateException("Couldn't find timestamp of first item on the page " + pageNumber)
    }
  }

  /** Get adverts from start page to 0 */
  private def advertsFromStartPage(startPage: Int): Seq[RawAdvert] =
    (startPage to 0)
      .view
      .flatMap(getAdvertsForPage)

  private def openPage(pageNumber: Int) = {
    val mainPage = "https://www.avito.ru/kazan/kvartiry/sdam/na_dlitelnyy_srok"
    val pageIndex = "?p="

    if (pageNumber == 0) webDriver.get.get(mainPage)
    else webDriver.get.get(mainPage + pageIndex + pageNumber)
  }

  /** Adverts for page time asc order */
  private def getAdvertsForPage(pageNumber: Int) = {
    logger.info(s"Opening page $pageNumber")

    openPage(pageNumber)

    PageElements.parsePageAdvertHeaders(webDriver)
      .stream().collect(Collectors.toList())
      .asScala
      .map(item => (PageElements.parseHeaderLink(item), PageElements.parseHeaderTimestamp(item)))
      .reverse
      .view
      .map { case (url, ts) => getAdvert(url, ts) }
  }

  private def getAdvert(url: String, timestamp: Long): RawAdvert = {
    // delete all cookies to emulate new user
    webDriver.get.manage.deleteAllCookies()
    webDriver.get.get(url)

    // click on phone button and wait
    Thread.sleep(3000)
    webDriver.get.asInstanceOf[JavascriptExecutor].executeScript("$(\".js-item-phone-button\").click()")
    new WebDriverWait(webDriver.get, 50)
      .until(Utils.cv(ExpectedConditions.presenceOfNestedElementLocatedBy(By.className("item-phone-big-number"), By.tagName("img"))))

    // parse all data
    RawAdvert(
      timestamp,
      1,
      1,
      PageElements.parseRooms(webDriver),
      PageElements.parseSq(webDriver),
      PageElements.parseFloor(webDriver),
      PageElements.parseMaxFloor(webDriver),
      PageElements.parseAddress(webDriver),
      PageElements.parseDescription(webDriver),
      PageElements.parseCoordinates(webDriver)._1,
      PageElements.parseCoordinates(webDriver)._2,
      PageElements.parsePrice(webDriver),
      PageElements.parsePhotos(webDriver),
      PageElements.parseUsername(webDriver),
      PageElements.parsePhone(webDriver),
      PageElements.parseTrustrate(webDriver),
      PageElements.parseId(webDriver)
    )
  }


  object PageElements {

    private val dateFormatter = new AvitoDateFormatter
    private val avitoPhoneParser = new AvitoPhoneParser
    private val imageUrlPattern = Pattern.compile(".*background-image:[ ]*url[ ]*\\(.*//([a-zA-Z0-9/.]*)[\"]*\\).*")

    def parsePageAdvertHeaders(webDriver: WebDriver) =
      webDriver.get()
        .findElement(By.className("js-catalog_after-ads"))
        .findElements(By.className("item"))

    def parseHeaderTimestamp(webElement: WebElement): Long =
      dateFormatter.getTimestamp(webElement.findElement(By.className("date")).getAttribute("innerText"))


    def parseHeaderLink(webElement: WebElement): String =
      webElement.findElement(By.className("item-description-title-link")).getAttribute("href")


    def parseAddress(webDriver: WebDriver): String =
      webDriver.get()
        .findElement(By.className("item-view-main"))
        .findElement(By.cssSelector(".item-map-address [itemprop=streetAddress]"))
        .getAttribute("innerText")
        .trim()

    def parsePhone(webDriver: WebDriver): Long =
      avitoPhoneParser.parseNumbersFromImage(webDriver.get()
        .findElement(By.cssSelector(".item-phone-big-number img"))
        .getAttribute("src"))

    def parseId(webDriver: WebDriver): Int =
      Integer.valueOf(webDriver.get()
        .findElement(By.className("b-search-map"))
        .getAttribute("data-item-id"))

    def parsePrice(webDriver: WebDriver): Int =
      Util.getNumberInsideOf(webDriver.get()
        .findElement(By.className("item-price"))
        .findElement(By.className("price-value-string"))
        .getAttribute("innerText"))

    def parseRooms(webDriver: WebDriver): Int = {
      val rooms = Util.getNumberInsideOf(webDriver.get()
        .findElement(By.className("item-view-main"))
        .findElement(By.className("item-params"))
        .findElements(By.className("item-params-list-item")).get(0).getAttribute("innerText"))
      if (rooms == null) 1 else rooms
    }

    def parseFloor(webDriver: WebDriver): Int =
      Util.getNumberInsideOf(webDriver.get()
        .findElement(By.className("item-view-main"))
        .findElement(By.className("item-params"))
        .findElements(By.className("item-params-list-item")).get(1).getAttribute("innerText"))

    def parseMaxFloor(webDriver: WebDriver): Int =
      Util.getNumberInsideOf(webDriver.get()
        .findElement(By.className("item-view-main"))
        .findElement(By.className("item-params"))
        .findElements(By.className("item-params-list-item")).get(2).getAttribute("innerText"))

    def parseSq(webDriver: WebDriver): Int =
      Util.getNumberInsideOf(webDriver.get()
        .findElement(By.className("item-view-main"))
        .findElement(By.className("item-params"))
        .findElements(By.className("item-params-list-item")).get(4).getAttribute("innerText"))

    def parseDescription(webDriver: WebDriver): String = {
      val itemViewMain = webDriver.get.findElement(By.className("item-view-main"))
      try
        itemViewMain.findElement(By.className("item-description-text")).findElement(By.tagName("p")).getAttribute("innerText")
      catch {
        case _: NoSuchElementException =>
          try
            itemViewMain.findElement(By.className("item-description-html")).findElement(By.tagName("p")).getAttribute("innerText")
          catch {
            case _: NoSuchElementException =>
              itemViewMain.findElement(By.className("item-description-html")).getAttribute("innerText")
          }
      }
    }

    def parseCoordinates(webDriver: WebDriver): (Double, Double) = {
      val searchMap = webDriver.get.findElement(By.className("b-search-map"))
      (searchMap.getAttribute("data-map-lat").toDouble, searchMap.getAttribute("data-map-lon").toDouble)
    }

    def parseUsername(webDriver: WebDriver): String =
      webDriver.get()
        .findElement(By.className("item-view-contacts"))
        .findElement(By.className("seller-info-name"))
        .getAttribute("innerText").trim()


    def parseTrustrate(webDriver: WebDriver): Int =
      if ("агентство" == webDriver.get
        .findElement(By.className("seller-info"))
        .findElement(By.className("seller-info-label"))
        .getAttribute("innerText").trim().toLowerCase()
      ) 1 else 5000

    def parsePhotos(webDriver: WebDriver): List[String] = {
      def getImageUrl(fullImageUrl: String): String = {
        val matcher = imageUrlPattern.matcher(fullImageUrl)
        if (!matcher.matches) throw new IllegalStateException(s"Failed to retrieve image from $fullImageUrl")
        matcher.group(1)
      }

      try {
        webDriver.get
          .findElement(By.className("item-view-gallery"))
          .findElements(By.className("gallery-list-item-link"))
          .stream().collect(Collectors.toList()).asScala
          .map(item => item.getAttribute("style"))
          .map(getImageUrl)
          .map(url => s"http://${url.replace("80x60", "640x480")}")
          .toList
      } catch {
        case e: NoSuchElementException =>
          null
      }
    }

  }

}
