package controllers

import java.io.File
import javax.inject.Inject

import models.data._
import play.api.libs.json.{Json, Writes}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import java.nio.file.Paths
import java.util.UUID

import play.api.{Configuration, Logger}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Request, _}

import scala.util.hashing.MurmurHash3
/**
  * Created by Kos on 24.07.2017.
  */
class EventController @Inject()(implicit executionContext: ExecutionContext,
								components: ControllerComponents,
								eventRepo:EventDataRepo,
								dateRepo:DatasRepo,
								config: Configuration
								) extends AbstractController(components) {



	implicit def eventWrites(implicit request: Request[AnyContent]) =
		new Writes[EventData] {
	//class eventWriter =
		def writes(x: EventData) = Json.obj(
			"name" → x.name,
			"kind" → x.kind	,
			"color" → x.color,
			"date" → x.date,
			"duration" → x.duration,
			"image" → {
				if (x.image.startsWith("*"))
					routes.ScalaFileUploadController.at(x.image.drop(2)).absoluteURL()
				else
					x.image
			},
			"uri" → x.uri	,
			"info" → x.info	,
			"shortInfo" → x.shortInfo,
			"id" → x.id,
			"author" → x.author
		)
	}

	implicit def eventListWriter(implicit request: Request[AnyContent]) = new Writes[EventsInfo] {
		def writes(x: EventsInfo) = Json.obj(
			"time" → x.time,
			"list" → x.list
			//"image" → x.image
		)
	}

	def deleteEvent(eventId:Long) = Action.async { implicit request:Request[AnyContent] =>
		eventRepo.delete(eventId).map(res ⇒ Ok(Json.toJson(res)))
	}

	def updateEvent(eventId:Long) = Action.async { implicit request:Request[AnyContent] =>
		request.body.asFormUrlEncoded match {
			case Some(items) ⇒
				val eventData=readEvent(items,items("image").head)
				eventRepo.update(eventId,eventData).map(res ⇒ Ok(Json.toJson(res)))
			//DataStore.add(event)
			//Ok("Event created")
			case _ ⇒
				Future(Ok("Don't update"))
		}

	}

	def putEventWithImage() = Action(parse.multipartFormData).async{ implicit request =>
		request.body.file("picture") match{
			case Some(picture) ⇒
				//Logger.debug(s"Start move file ${picture.getClass.toString} ${picture.ref.getClass.toString}")
				val filename = AppConstant.generteUniqueFileName(picture.filename)
				val imagePath=s"/picture/$filename"
				val contentType = picture.contentType
				val file= new File(AppConstant.UPLOAD_IMAGE_PATH(config)+imagePath).getParentFile
				file.mkdirs

				val ref :play.api.libs.Files.TemporaryFile = picture.ref
				picture.ref.moveTo(Paths.get(AppConstant.UPLOAD_IMAGE_PATH(config)+imagePath), replace = true)


				Logger.debug("Move file "+filename)
				val eventData=readEvent(request.body.asFormUrlEncoded,"*"+imagePath)
				for {res ← eventRepo.add(eventData)
				} yield Ok(Json.toJson(res))
				//Future(Ok("File uploaded"))
			case _ ⇒ Future(Ok("Don't create"))
		}
	}

	def putEvent() = Action.async { implicit request:Request[AnyContent] =>

		request.body.asFormUrlEncoded match {
			case Some(items) ⇒
				val eventData=readEvent(items,items("image").head)
				for {res ← eventRepo.add(eventData)
					 } yield Ok(Json.toJson(res))

				//DataStore.add(event)
				//Ok("Event created")
			case _ ⇒
				Future(Ok("Don't create"))
		}
	}

//	def addEvent(
//					name		: String,
//					kind		: String,
//					color		: String,
//					date		: Long,
//					duration	: Long,
//					image		: String,
//					uri			: String,
//					info		: String,
//					shortInfo	: String) = Action { implicit rs =>
//
//		DataStore.add(EventData(DataStore.generateId, name, kind, color, date,duration, image, uri, info, shortInfo))
//		Ok("Event created")
//
//	}

	private def readEvent(items: Map[String,Seq[String]],image:String):EventData = {
		val date = items("date").head.toLong
		val duration = items("duration").head.toLong
		val name = items("name").head
		val kind = items("kind").head
		val color = items("color").head
		//val image = items("image").head
		val info = items("info").head
		val uri = items("uri").head
		val shortInfo = items("shortInfo").head

		val eventData = EventData(0, name, kind, color, date, duration, image, uri, info, shortInfo)
		eventData
//		for {res ← eventRepo.add(eventData)
//		} yield Ok(Json.toJson(res))

	}

	def showEvent(id:Long) = Action.async { implicit request:Request[AnyContent] =>

		eventRepo.get(id).map(res ⇒ Ok(Json.toJson(res)))
	}

	def checkLastTime(time:Long) = Action.async {
		dateRepo.lastTime.map(date ⇒ if (date.getOrElse(DatasRepo.none).value==time) NoContent else Ok)
	}


	def listLastTime(time:Long) = Action.async { implicit request:Request[AnyContent] ⇒

		for {dateTh ← dateRepo.lastTime
			 date = dateTh.getOrElse(DatasRepo.none).value
			 res ← eventRepo.nowOrNull(date==time)
		}yield {
			if (res==null)
				NoContent
			else
				Ok(Json.toJson(EventsInfo(date,res)))
		}
//		dateRepo.lastTime.map(date ⇒
//			if (date==time)
//				NoContent
//			else
//				eventRepo.now.map(res ⇒ Ok(Json.toJson(EventsInfo(date.getOrElse(DatasRepo.none).value,res))))
//		)
//		if (DataStore.checkDate(time))
//			NoContent
//		else{
//			//val res=EventsInfo(DataStore.updateTime,DataStore.all)
//			Ok(Json.toJson(res))
//		}
	}

	def list() =  Action.async {implicit request:Request[AnyContent] ⇒
	//	val res=EventsInfo(DataStore.updateTime,DataStore.all)
	//	Ok(Json.toJson(res))
		for {
			res ← eventRepo.now
			date ← dateRepo.lastTime
		} yield Ok(//executionContext.getClass.getCanonicalName)
			Json.toJson(EventsInfo(date.getOrElse(DatasRepo.none).value,res)))
	}

}
