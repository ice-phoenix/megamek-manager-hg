package info.icephoenix.mmm

import akka.actor._

package object util {

//  def onActor(path: String, onSome: (ActorRef) => Unit, onNone: () => Unit)(implicit self: ActorRef, context: ActorContext) = {
//    val Id = math.random
//    context.actorSelection(path) ! Identify(Id)
//    context.become({
//      case ActorIdentity(Id, Some(ref)) => {
//        onSome(ref)
//        context.unbecome()
//      }
//      case ActorIdentity(Id, None) => {
//        onNone()
//        context.unbecome()
//      }
//      case msg => {
//        self ! msg
//      }
//    })
//  }

}
