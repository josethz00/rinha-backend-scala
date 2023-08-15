package rest.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import java.time.LocalDate

final case class Pessoa(id:Option[String],
                        apelido: String,
                        nome: String,
                        nascimento: LocalDate
                       )

object PessoaActor {

  sealed trait Command

  final case class CreateUser(pessoa: Pessoa, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUser(nome: String, replyTo: ActorRef[GetUserResponse]) extends Command

  final case class DeleteUser(nome: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[Pessoa])

  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(pessoas: Set[Pessoa]): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateUser(pessoa, replyTo) =>
        replyTo ! ActionPerformed(s"User ${pessoa.nome} created.")
        registry(pessoas + pessoa)
      case GetUser(nomeFilter, replyTo) =>
        replyTo ! GetUserResponse(pessoas.find(_.nome == nomeFilter))
        Behaviors.same
      case DeleteUser(nomeFilter, replyTo) =>
        replyTo ! ActionPerformed(s"User $nomeFilter deleted.")
        registry(pessoas.filterNot(_.nome == nomeFilter))
    }
}
//#user-registry-actor
