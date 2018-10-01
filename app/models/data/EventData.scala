package models.data

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * Created by Kos on 24.07.2017.
  */
case class EventData(id:Long,
				   	name:String,
				   	kind:String,
				   	color:String,
				   	date:Long,
				   	duration:Long,
				   	image:String,
				   	uri:String,
				   	info:String,
				   	shortInfo:String,
					author:Long=0
					//dropDate:Long=Long.MaxValue
					//parent:Long=0,
					//accessGroup:Int=0
					) {

}

class EventDataRepo  @Inject()(datesRepo: DatasRepo)(protected val dbConfigProvider: DatabaseConfigProvider){
	val dbConfig = dbConfigProvider.get[JdbcProfile]
	val db = dbConfig.db
	import dbConfig.profile.api._
	private val EventDatas = TableQuery[EventDataTable]



	def add(item: EventData): Future[Long] = {
		val a= for{
			resId ← EventDatas returning EventDatas.map(_.id) += item
			ns ← datesRepo.updateTimeDbio()
		} yield resId
		db.run(a.transactionally)
	}

	def get(itemId:Long): Future[Option[EventData]] ={
		db.run(EventDatas.filter(_.id === itemId).result.headOption)
	}

	def all: Future[Seq[EventData]] =
		db.run(EventDatas.to[Seq].result)

	def now: Future[Seq[EventData]] = {
		val showDate = System.currentTimeMillis()
		val nowDate = showDate - 4 * DatasRepo.day

		db.run(EventDatas.filter(x ⇒ x.date >= nowDate && x.dropDate>showDate).sortBy(_.date).to[Seq].result)
	}

	def nowOrNull(nullResult:Boolean): Future[Seq[EventData]]={
		if (nullResult)
			Future(null)
		else
			now
	}

	def update(itemId:Long,item: EventData): Future[Long] = {
		val deleteDate = System.currentTimeMillis()
		val a= for{
			deleteResId ← EventDatas.filter(_.id === itemId).map(_.dropDate).update(deleteDate)
			resId ← EventDatas returning EventDatas.map(_.id) += item
			_ ← EventDatas.filter(_.id === resId).map(_.parent).update(deleteResId)
			ns ← datesRepo.updateTimeDbio()
		} yield resId
		db.run(a.transactionally)
	}

	def delete(itemId:Long):  Future[Int] ={
		val deleteDate = System.currentTimeMillis()
		val interaction = for {
			resId ← EventDatas.filter(_.id === itemId).map(_.dropDate).update(deleteDate)
			ns ← datesRepo.updateTimeDbio()
		} yield resId

		db.run(interaction.transactionally)
	}

	private class EventDataTable(tag: Tag) extends Table[EventData](tag, "EVENTS_TABLE") {

		def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
		def name = column[String]("name")
		def kind= column[String]("kind")
		def color= column[String]("color")
		def date= column[Long]("date")
		def duration= column[Long]("duration")
		def image= column[String]("image")
		def uri= column[String]("uri")
		def info= column[String]("info")
		def shortInfo= column[String]("shortInfo")
		def author = column[Long]("author")
		def parent = column[Long]("parent")
		def dropDate = column[Long]("dropDate")
		def accessGroup = column[Int]("accessGroup")

		def * = (id, name,kind,color,date,duration,image,uri,info,shortInfo,author) <> (EventData.tupled, EventData.unapply)
	}
}