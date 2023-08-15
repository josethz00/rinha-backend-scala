package database

import api_rest.Pessoa

import java.time.LocalDate
import java.util.UUID

object Tables {

  import slick.jdbc.PostgresProfile.api._

  implicit val optionSeqStringColumnType: BaseColumnType[Option[Seq[String]]] = MappedColumnType.base[Option[Seq[String]], String](
    optSeq => optSeq.map(_.mkString(",")).getOrElse(""),
    str => if (str.nonEmpty) Some(str.split(",").toSeq) else None
  )

  class PessoaTable(tag:Tag) extends Table[Pessoa] (tag,"pessoas") {
    def id = column[Option[UUID]]("id",O.PrimaryKey, O.AutoInc)
    def apelido = column[String]("apelido",O.Length(32))
    def nome = column[String]("nome",O.Length(100))
    def nascimento = column[LocalDate]("nascimento")
    def stack = column[Option[Seq[String]]]("stack")

    override def * = (id,apelido,nome,nascimento,stack) <> (Pessoa.tupled, Pessoa.unapply)
  }

  lazy val pessoaTable = TableQuery[PessoaTable]

}