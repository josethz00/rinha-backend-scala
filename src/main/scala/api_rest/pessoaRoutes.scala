package api_rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import scala.concurrent.Future
import PessoaActor._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import PessoaActor.{CreatePessoa, GetPessoa, GetPessoaResponse}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import jsonSerializer._
import org.json4s._
import scala.util.{Failure, Success}
import org.json4s.native.{Json, Serialization}


class pessoaRoutes(pessoaManageActor: ActorRef[PessoaActor.Command])(implicit val system: ActorSystem[_]) {

  implicit val formats = Serialization.formats(NoTypeHints)

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getPessoa(uuid: String): Future[GetPessoaResponse] =
    pessoaManageActor.ask(GetPessoa(uuid, _))

  def createPessoa(pessoa: Pessoa): Future[CreatePessoaResponse] =
    pessoaManageActor.ask(CreatePessoa(pessoa, _))

  def getContagemPessoa: Future[GetContagemPessoaResponse] =
    pessoaManageActor.ask(GetContagemPessoa)

  def getPessoasPorTermo(termo:String): Future[GetPessoasPorTermoResponse] =
    pessoaManageActor.ask(GetPessoasPorTermo(termo, _))


  val userRoutes: Route = {
    concat(
      pathPrefix("pessoas") {
        concat(
          post {
            entity(as[Pessoa]) { pessoa =>
              onComplete(createPessoa(pessoa)) {
                case Success(performed) =>
                  complete((StatusCodes.Created,performed))
                case Failure(ex) =>
                  complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            }
          },
          get {
            parameter(Symbol("t").?) {
              case Some(t) =>
                onSuccess(getPessoasPorTermo(t)) { response =>
                  complete(response.maybePessoas)
                }
              case None =>
                complete(StatusCodes.BadRequest, "Parametro t não especificado.")
            }
          },

          path(Segment) { uuid =>
            concat(
              get {
                onComplete(getPessoa(uuid)) {
                  case Success(response) =>
                    response.maybePessoa match {
                      case Some(pessoa) =>
                        if (pessoa.stack.isEmpty) {
                          val stack = null
                          val pessoaToReturn = Map(
                            "id" -> pessoa.id.get.toString,
                            "nome" -> pessoa.nome,
                            "apelido" -> pessoa.apelido,
                            "nascimento" -> pessoa.nascimento.toString,
                            "stack" -> stack
                          )
                          complete(Json(DefaultFormats).write(pessoaToReturn))
                        } else {
                          val stack = pessoa.stack
                          val pessoaToReturn = Map(
                            "id" -> pessoa.id.get.toString,
                            "nome" -> pessoa.nome,
                            "apelido" -> pessoa.apelido,
                            "nascimento" -> pessoa.nascimento.toString,
                            "stack" -> stack
                          )
                          complete(Json(DefaultFormats).write(pessoaToReturn))
                        }

                      case None =>
                        val message = Map("Message" -> s"UUID $uuid não está associado a uma pessoa.")
                        complete(StatusCodes.NotFound, Json(DefaultFormats).write(message))
                    }
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                }
              })
          }
        )
      },
      pathPrefix("contagem-pessoas") {
        get {
          onComplete(getContagemPessoa) {
            case Success(contagem) =>
              val message = Map("Total pessoas" -> contagem.numeroDePessoas)
              complete(Json(DefaultFormats).write(message))
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    )
  }
}
