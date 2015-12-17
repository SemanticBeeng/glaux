package glaux.persistence.mongodb

import glaux.interfaces.akka.api.domain.{ AdvancedAgentSettings, AgentSettings, SessionId }
import reactivemongo.bson.Macros
import reactivemongo.bson.Macros.Options.AllImplementations
import GlauxHandlers._

object ShifuHandlers {
  implicit val sessionIdH = Macros.handler[SessionId]
  implicit val aashandler = Macros.handler[AdvancedAgentSettings]
  implicit val agshandler = Macros.handlerOpts[AgentSettings, AllImplementations]
}