package database

import api_rest.Pessoa
import java.time.LocalDate
import java.util.UUID
import com.github.tminglei.slickpg._


trait MyPostgresProfile extends ExPostgresProfile with PgArraySupport {
  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits
}

object Tables {
  object MyPostgresProfile extends MyPostgresProfile

  import MyPostgresProfile.api._

  class PessoaTable(tag:Tag) extends Table[Pessoa] (tag,"pessoas") {
    def id = column[Option[UUID]]("id",O.PrimaryKey)
    def apelido = column[String]("apelido",O.Length(32))
    def nome = column[String]("nome",O.Length(100))
    def nascimento = column[LocalDate]("nascimento")
    def stack = column[Option[List[String]]]("stack",O.SqlType("VARCHAR[]"))

    override def * = (id,apelido,nome,nascimento,stack) <> (Pessoa.tupled, Pessoa.unapply)
  }

  val pessoaTable = TableQuery[PessoaTable]

}