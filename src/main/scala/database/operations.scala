package database

import api_rest.Pessoa

import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object operations {

  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  def insertPessoa(pessoa: Pessoa): Future[Unit] = {

    val insertQuery = Tables.pessoaTable += pessoa
    val futureId: Future[Int] = connection.db.run(insertQuery)
    futureId.flatMap { _ =>
      Future.successful(())
    }.recoverWith {
      case ex: Throwable =>
        println(ex)
        Future.failed(ex)

    }
  }

  def getPessoasByID(uuid: UUID): Future[Seq[Pessoa]] = {
    val query = Tables.pessoaTable.filter(_.id === uuid)
    connection.db.run(query.result)
  }

  def getContagemPessoas: Future[Int] = {
    val query = Tables.pessoaTable.map(_.id).length
    connection.db.run(query.result)
  }

  def getPessoasPSimilaridade(termoParaPesquisa: String): Seq[Pessoa] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val query =
      sql"""
        SELECT *
        FROM pessoas
        WHERE similarity('#$termoParaPesquisa',search) > 0
          """.as[(String, String, String, String, String)]

    val resultFuture: Future[Seq[(String, String, String, String, String)]] = connection.db.run(query)

    val result = Await.result(resultFuture, 10.seconds)

    val pessoasResults: Seq[Pessoa] = result.map { row =>
      val stackOption: Option[String] = Option(row._5)
      val stackValidation: Option[List[String]] = stackOption.map { value =>
        value.stripPrefix("{").stripSuffix("}").split(",").map(_.trim).toList
      }
    Pessoa(
      id = Some(UUID.fromString(row._1)),
      apelido = row._2,
      nome = row._3,
      nascimento = LocalDate.parse(row._4, formatter),
      stack = stackValidation
    )
  }
  pessoasResults
  }
}