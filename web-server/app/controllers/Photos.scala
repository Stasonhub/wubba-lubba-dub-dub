package controllers

import java.io.File
import javax.inject.{Inject, Singleton}

import play.api.http.FileMimeTypes
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class Photos @Inject()(implicit val fileMimeTypes: FileMimeTypes) extends Controller {

  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>
    val fileToServe = new File(rootPath, file)
    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      NotFound
    }
  }

}
