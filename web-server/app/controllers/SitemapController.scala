package controllers

import java.io.File
import java.util.Date
import java.util.concurrent.locks.ReentrantLock
import javax.inject._

import com.redfin.sitemapgenerator.{ChangeFreq, WebSitemapGenerator, WebSitemapUrl}
import model.Advert
import org.slf4j.LoggerFactory
import play.api.http.FileMimeTypes
import play.api.mvc._
import repository.interops.AdvertRepositoryJv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

@Singleton
class SitemapController @Inject()(advertRepositoryJv: AdvertRepositoryJv,
                                  @Named("app.domain") domain: String,
                                  implicit val fileMimeTypes: FileMimeTypes) extends Controller {

  val logger = LoggerFactory.getLogger(classOf[SitemapController])
  val lock = new ReentrantLock

  def sitemap = Action { request =>
    lock.lock()
    try {
      val file = new File(System.getProperty("java.io.tmpdir") + File.separator + "/ftr")
      file.mkdirs()

      var sitemap = new File(file, "sitemap.xml")

      if (!sitemap.exists() || sitemap.lastModified() < (System.currentTimeMillis() - 3600000)) {
        val wsg = new WebSitemapGenerator(domain, file)
        wsg.addUrl(new WebSitemapUrl.Options(domain)
          .lastMod(new Date())
          .priority(1.0)
          .changeFreq(ChangeFreq.HOURLY)
          .build())
        wsg.addUrl(new WebSitemapUrl.Options(domain + "/search")
          .lastMod(new Date())
          .priority(1.0)
          .changeFreq(ChangeFreq.HOURLY)
          .build())

        // fill sitemap by batches
        var timestamp = System.currentTimeMillis()
        var adverts: List[Advert] = List.empty
        do {
          adverts = advertRepositoryJv.getNextAdvertsBeforeTime(timestamp, 100).asScala.toList
          for (advert <- adverts) {
            wsg.addUrl(new WebSitemapUrl.Options(domain + "/advert/" + advert.id)
              .lastMod(new Date(advert.publicationDate))
              .priority(0.7)
              .changeFreq(ChangeFreq.DAILY)
              .build())
          }
          if (!adverts.isEmpty)
            timestamp = adverts.last.publicationDate
        } while (!adverts.isEmpty)

        sitemap = wsg.write().get(0)
      }

      Ok.sendFile(sitemap, inline = true)
    } finally {
      lock.unlock()
    }
  }

}
