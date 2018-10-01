package executions

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import scala.concurrent.ExecutionContext
/**
  * Created by Kos on 04.08.2017.
  */
trait NotifityExecutionContext extends ExecutionContext

class NotifityExecutionContextImpl @Inject()(system: ActorSystem)
	extends CustomExecutionContext(system, "notifity.executor") with NotifityExecutionContext{


}
