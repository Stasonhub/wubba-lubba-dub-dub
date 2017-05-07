package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Controller}
import service.UserService

@Singleton
class UserController @Inject()(userService: UserService) extends Controller {

  def userForAdvert(advertId: Int) = Action { request =>
    val user = userService.getUserForAdvert(advertId)
    Ok(views.html.fragment.userInfo(user))
  }

}
