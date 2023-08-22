package api_rest

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import java.time.LocalDate
import database.operations._

import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


final case class Pessoa(
                        var id:Option[UUID] = None,
                        apelido: String,
                        nome: String,
                        nascimento: LocalDate,
                        stack:Option[List[String]]
                       )

object PessoaActor {

  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  sealed trait Command

  final case class CreatePessoa(pessoa: Pessoa, replyTo: ActorRef[PessoaCreated]) extends Command

  final case class GetPessoa(uuidPessoa: String, replyTo: ActorRef[GetPessoaResponse]) extends Command


  final case class GetPessoaResponse(maybeUser: Option[Pessoa])

  final case class PessoaCreated(id: String)

  final case class ActionPerformed(description: String)


  def apply(): Behavior[Command] = registry(Set.empty)


  private def registry(pessoas: Set[Pessoa]): Behavior[Command] =
    Behaviors.receiveMessage {

      case CreatePessoa(pessoa, replyTo) =>
        val uuid = UUID.randomUUID()
        pessoa.id = Some(uuid)
        replyTo ! PessoaCreated(pessoa.id.get.toString)
        insertPessoa(pessoa)
        registry(pessoas + pessoa)

      case GetPessoa(uuidPessoa, replyTo) =>
        val optionPerson = getPessoasByID(UUID.fromString(uuidPessoa))
        optionPerson.onComplete {
          case Success(pessoasEncontradas) =>
            if (pessoasEncontradas.nonEmpty)
            replyTo ! GetPessoaResponse(Some(pessoasEncontradas.head))
            else {
              replyTo ! GetPessoaResponse(None)
            }
        }
        Behaviors.same
    }
}