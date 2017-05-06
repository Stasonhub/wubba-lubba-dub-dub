package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.Controller
import service.UserService

@Singleton
class UserController @Inject()(userService: UserService) extends Controller {

  def userForAdvert(advertId: Int) = {
    val user = userService.getUserForAdvert(advertId)
    Ok(views.fragment.html.userinfo)
  }

}
