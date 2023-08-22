package api_rest

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import java.time.LocalDate
import database.operations._

import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.Success


final case class Pessoa(
                        var id:Option[UUID] = None,
                        apelido: String,
                        nome: String,
                        nascimento: LocalDate,
                        stack:Option[List[String]] = None
                       )

object PessoaActor {

  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  sealed trait Command

  final case class CreatePessoa(pessoa: Pessoa, replyTo: ActorRef[CreatePessoaResponse]) extends Command

  final case class GetPessoa(uuidPessoa: String, replyTo: ActorRef[GetPessoaResponse]) extends Command

  final case class GetPessoasPorTermo(termo:String, replyTo: ActorRef[GetPessoasPorTermoResponse]) extends Command

  final case class GetContagemPessoa(replyTo:ActorRef[GetContagemPessoaResponse]) extends Command


  final case class GetPessoaResponse(maybePessoa: Option[Pessoa])

  final case class GetPessoasPorTermoResponse(maybePessoas:Option[Seq[Pessoa]])

  final case class GetContagemPessoaResponse(numeroDePessoas: Int)

  final case class CreatePessoaResponse(id: String)

  def apply(): Behavior[Command] = registry(Set.empty)


  private def registry(pessoas: Set[Pessoa]): Behavior[Command] =
    Behaviors.receiveMessage {

      case CreatePessoa(pessoa, replyTo) =>
        val uuid = UUID.randomUUID()
        pessoa.id = Some(uuid)
        insertPessoa(pessoa)
        replyTo ! CreatePessoaResponse(pessoa.id.get.toString)
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

      case GetContagemPessoa(replyTo) =>
        val pessoasTotal = getContagemPessoas
        pessoasTotal.onComplete( contagem =>
          replyTo ! GetContagemPessoaResponse(contagem.get)
        )
        Behaviors.same

      case GetPessoasPorTermo(termo, replyTo) =>
        val maybePessoas = getPessoasPSimilaridade(termo)
        maybePessoas match {
          case pessoas =>
            replyTo ! GetPessoasPorTermoResponse(Some(pessoas))
        }
        Behaviors.same
    }

}