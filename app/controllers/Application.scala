package controllers

import java.io.File
import java.nio.file.Paths
import java.util.UUID
import javax.inject.Inject

import models.{ProjectRepo, TaskRepo}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.hashing.MurmurHash3
//import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.data._
import play.api.mvc._
//
//trait NotifityExecutionContext extends ExecutionContext
//
//class NotifityExecutionContextImpl @Inject()(system: ActorSystem)
//	extends CustomExecutionContext(system, "notifity.executor") with NotifityExecutionContext{
//
//
//}

object AppConstant{
	def UPLOAD_IMAGE_PATH(config: Configuration) = {
		config.get[String]("notificator.images.upload")
		//"/var/kos/notifity/play/public/upload/"
	}

	val supportedExts =Seq[String](".jpg", ".png", ".gif", ".jpeg", ".bmp", ".svg" ,".txt")

	def generteUniqueFileName(filename: String):String ={
		val	uuid = UUID.randomUUID

		val p=uuid.toString
		val c:Int= MurmurHash3.stringHash(p)

		val lp=filename.lastIndexOf('.')
		val p2:String=
		if (lp>0){
			val ext=filename.drop(lp).toLowerCase
			if (supportedExts.contains(ext)) ext else ""
		}else
			""

		f"${c&0xFF}%02x/${(c>>2)&0xFF}%02x/${System.currentTimeMillis()}%08x$p$p2"
	}
}

class Application @Inject()(implicit executionContext: ExecutionContext,
							messagesAction: MessagesActionBuilder,
							components: ControllerComponents,
							projectRepo: ProjectRepo,
							taskRepo: TaskRepo,
							config: Configuration
						   )
	extends AbstractController(components) with play.api.i18n.I18nSupport {

	import play.api.data.Form
	import play.api.data.Forms._

	def addTaskToProject(color: String, projectId: Long) = Action.async { implicit rs: Request[AnyContent] =>
		projectRepo.addTask(color, projectId)
			.map { _ => Redirect(routes.Application.projects(projectId)) }
	}

	def modifyTask(taskId: Long, color: Option[String]) = Action.async { implicit rs: Request[AnyContent] =>
		taskRepo.partialUpdate(taskId, color, None, None).map(i =>
			Ok(s"Rows affected : $i"))
	}

	def createProject(name: String) = Action.async { implicit rs: Request[AnyContent] =>
		projectRepo.create(name)
			.map(id => Ok(s"project $id created"))
	}

	def listProjects = Action.async { implicit rs: Request[AnyContent] =>
		projectRepo.all
			.map(projects => Ok(views.html.projects(projects)))
	}

	def projects(id: Long) = Action.async { implicit rs: Request[AnyContent] =>
		for {
			Some(project) <- projectRepo.findById(id)
			tasks <- taskRepo.findByProjectId(id)
		} yield Ok(views.html.project(project, tasks))
	}

	def delete(name: String) = Action.async { implicit rs: Request[AnyContent] =>
		projectRepo.delete(name).map(num => Ok(s"$num projects deleted"))
	}

//	val eventForm: Form[EventData] = Form {
//		mapping(
//			"id" → longNumber,
//			"name" -> nonEmptyText,
//			"kind" -> nonEmptyText,
//			"color" → nonEmptyText,
//			"date" -> longNumber,
//			"duration" -> longNumber,
//			"image" -> nonEmptyText,
//			"uri" -> nonEmptyText,
//			"info" -> nonEmptyText,
//			"shortInfo" -> nonEmptyText
//
//		)(EventData.apply)(EventData.unapply)
//	}

//	def eventConstructor = messagesAction { implicit rs => Ok(views.html.eventconstructor(eventForm))
//	}


	def upload = Action(parse.multipartFormData) { request =>

		println(request.getClass.toString)
		request.body.file("picture").map { picture =>
			Logger.debug("Start move file ")
			val filename = AppConstant.generteUniqueFileName(picture.filename)
			val contentType = picture.contentType
			val f = new File(AppConstant.UPLOAD_IMAGE_PATH(config)+"/tmp/picture/")
			f.mkdirs()

			picture.ref.moveTo(Paths.get(AppConstant.UPLOAD_IMAGE_PATH(config)+s"/tmp/picture/$filename"), replace = true)
			Logger.debug("Move file " + filename)
			Ok("File uploaded")
		}.getOrElse {
			Redirect(routes.Application.index).flashing(
				"error" -> "Missing file")
		}
	}

	def index = Action {
		Ok(views.html.imageloader())
	}
}
