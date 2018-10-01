package models.data
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.JdbcProfile

/**
  * Created by Kos on 28.07.2017.
  */
case class SimpleDate(id:Long,value:Long)

object DatasRepo{

	final val day = 24*60*60*1000

	final val LAST_UPDATE_TIME = 1L
	final val none = SimpleDate(LAST_UPDATE_TIME,0)
}

class DatasRepo  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
	val dbConfig = dbConfigProvider.get[JdbcProfile]
	val db = dbConfig.db
	import dbConfig.profile.api._
	private val dates = TableQuery[DateTable]


	def updateTime() = {
		db.run(updateTimeDbio())
	}

	def updateTimeDbio() = {
		val time=SimpleDate(DatasRepo.LAST_UPDATE_TIME, System.currentTimeMillis())
		val r=dates.insertOrUpdate(time)
		r
	}

	def lastTime ={
		db.run(dates.filter(_.id === DatasRepo.LAST_UPDATE_TIME).result.headOption)
	}



	private class DateTable(tag: Tag) extends Table[SimpleDate](tag, "DATES_TABLE") {

		def id = column[Long]("id", O.PrimaryKey)
		def value = column[Long]("date")

		def * = (id, value) <> (SimpleDate.tupled, SimpleDate.unapply)
	}
}
