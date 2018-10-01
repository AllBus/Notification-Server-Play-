package controllers

import java.nio.file.Paths
import javax.inject.Inject

import models.{ProjectRepo, TaskRepo}
import play.api.mvc._

import scala.concurrent.ExecutionContext
import AppConstant._
import play.api.{Configuration, Logger}

/**
  * Created by Kos on 14.08.2017.
  */
class ScalaFileUploadController @Inject()(implicit executionContext: ExecutionContext,
										  components: ControllerComponents,
										  config: Configuration
										 )
	extends AbstractController(components)  with play.api.i18n.I18nSupport{

	import play.api.data.Form
	import play.api.data.Forms._

	def upload = Action { request:Request[AnyContent] =>
//		request.body.file("picture").map { picture =>
//			println("Start move file ")
//			val filename = picture.filename
//			val contentType = picture.contentType
//			picture.ref.moveTo(Paths.get(s"/tmp/picture/$filename"), replace = true)
//			println("Move file "+filename)
//			Ok("File uploaded")
//		}.getOrElse {
//			Redirect(routes.ScalaFileUploadController.index).flashing(
//				"error" -> "Missing file")
//		}
		Ok("Da")
	}

	def index = Action{
		Ok(views.html.imageloader())
	}

//	def notFound = errorHandler.onClientError(request, NOT_FOUND, "Resource not found by Assets controller")
	def at(fileName:String) = Action {
		if (fileName.contains(".."))
			Forbidden
		else {
			val f = new java.io.File(s"${UPLOAD_IMAGE_PATH(config)}/$fileName").getAbsoluteFile
			//	Logger(f.getAbsolutePath)
			if (f.exists())
				Ok.sendFile(f)
			else
				Redirect(routes.Assets.versioned("images/none.png"))
		}
	}
}
