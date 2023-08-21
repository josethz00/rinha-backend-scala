package com.api_rest

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.database.{DatabaseConnection, DatabaseQueries, Pessoa, Pessoas}
import doobie.hikari.HikariTransactor
import doobie.implicits._

import scala.util.matching.Regex

object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class CreatePessoa(user: Pessoa, replyTo: ActorRef[ApiResponse]) extends Command
  final case class GetPessoa(id: String, replyTo: ActorRef[GetPessoaResponse]) extends Command
  final case class GetPessoas(t: String, replyTo: ActorRef[GetPessoasResponse]) extends Command
  final case class GetContagemPessoas(replyTo: ActorRef[GetContagemPessoasResponse]) extends Command
  final case class GetPessoaResponse(maybePessoa: Option[Pessoa])
  final case class GetPessoasResponse(pessoas: Pessoas)
  final case class GetContagemPessoasResponse(n: Int)
  final case class ApiResponse(status: StatusCode, description: String, maybeId: Option[String] = None)
  private val yyyymmddPattern: Regex = """\d{4}-\d{2}-\d{2}""".r

  def apply(xa: HikariTransactor[IO]): Behavior[Command] = registry(Set.empty)

  private def registry(pessoas: Set[Pessoa]): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreatePessoa(pessoa, replyTo) =>

        val isValidDate: Boolean = yyyymmddPattern.findFirstMatchIn(pessoa.nascimento).isDefined
        if (!isValidDate) {
          replyTo ! ApiResponse(StatusCodes.BadRequest, "Invalid date. Please send a date in the format YYYY-MM-DD.")
          return Behaviors.same
        }

        if (pessoa.stack.exists(_.length > 32)) {
          replyTo ! ApiResponse(StatusCodes.BadRequest, "Invalid stack. Please send a stack with no more than 32 characters.")
          return Behaviors.same
        }

        DatabaseConnection.transactor.use { xa =>
          DatabaseQueries.insertPessoa(pessoa).transact(xa)
        }.unsafeRunAsync {
          case Right(insertedId) =>
            replyTo ! ApiResponse(StatusCodes.Created, "Pessoa created successfully.", Some(insertedId))
          case Left(e) =>
            replyTo ! ApiResponse(StatusCodes.InternalServerError, e.getMessage)
        }

        Behaviors.same

      case GetPessoa(id, replyTo) =>
        // query the database using DatabaseConnection to get `pessoa` by ID and return it
        // throw a 404 - Not Found if the `pessoa` is not found
        DatabaseConnection.transactor.use { xa =>
          DatabaseQueries.findPessoaByID(id).transact(xa)
        }.unsafeRunAsync {
          case Right(maybePessoa) =>
            replyTo ! GetPessoaResponse(maybePessoa)
          case Left(_) =>
            replyTo ! GetPessoaResponse(None)
        }

        Behaviors.same

      case GetContagemPessoas(replyTo) =>
        // query the database using DatabaseConnection to get the number of `pessoas` and return it
        DatabaseConnection.transactor.use { xa =>
          DatabaseQueries.contagemPessoas().transact(xa)
        }.unsafeRunAsync {
          case Right(n) =>
            replyTo ! GetContagemPessoasResponse(n)
          case Left(_) =>
            replyTo ! GetContagemPessoasResponse(0)
        }

        Behaviors.same

      case GetPessoas(t, replyTo) =>
        // query the database using DatabaseConnection to get `pessoas` by `t` and return it
        DatabaseConnection.transactor.use { xa =>
          DatabaseQueries.listPessoas(t).transact(xa)
        }.unsafeRunAsync {
          case Right(pessoas) =>
            replyTo ! GetPessoasResponse(pessoas)
          case Left(_) =>
            replyTo ! GetPessoasResponse(Pessoas(Seq.empty))
        }

        Behaviors.same
    }
}
//#user-registry-actor
