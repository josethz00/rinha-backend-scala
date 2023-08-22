package api_rest

import PessoaActor.CreatePessoaResponse
import spray.json._
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object jsonSerializer extends DefaultJsonProtocol {
  implicit object LocalDateFormat extends JsonFormat[LocalDate] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    def write(date: LocalDate): JsValue = JsString(date.format(formatter))

    def read(json: JsValue): LocalDate = json match {
      case JsString(value) => LocalDate.parse(value, formatter)
      case _ => deserializationError("Esperado um LocalDate como JsString")
    }
  }

  implicit object UuidFormat extends JsonFormat[UUID] {
    def write(uuid: UUID): JsValue = JsString(uuid.toString)

    def read(json: JsValue): UUID = json match {
      case JsString(value) => UUID.fromString(value)
      case _ => deserializationError("Esperado um UUID como JsString")
    }
  }

  implicit val nomeValidFormatLength: JsonFormat[String] = new JsonFormat[String] {
    def write(nome: String): JsValue = JsString(nome)

    def read(json: JsValue): String = json match {
      case JsString(value) if value.length > 100 => deserializationError("Esperado um nome com menos de 100 caracteres")
      case JsString(value) => value
    }
  }

  implicit val apelidoValidFormatLength: JsonFormat[String] = new JsonFormat[String] {
    def write(apelido: String): JsValue = JsString(apelido)

    def read(json: JsValue): String = json match {
      case JsString(value) if value.length > 32 => deserializationError("Esperado um apelido com menos de 32 caracteres")
      case JsString(value) => value
    }
  }

  // validar se stack é uma lista de strings e se algum elemento possui mais de 32 caracteres
  implicit val stackValidFormat: JsonFormat[Option[List[String]]] = new JsonFormat[Option[List[String]]] {
    def write(stack: Option[List[String]]): JsValue = JsArray(stack.get.map(JsString(_)).toVector)

    def read(json: JsValue): Option[List[String]] = json match {
      case JsArray(value) if value.nonEmpty =>
        val stack = value.map {
          case JsString(value) if value.length > 32 => deserializationError("Esperado um stack com menos de 32 caracteres")
          case JsString(value) if value.getClass != "".getClass => deserializationError("Esperado um stack com strings")
          case JsString(value) => value
        }
        Some(stack.toList)
      case _ => None
    }
  }

  implicit val pessoaJsonFormat: RootJsonFormat[Pessoa] = jsonFormat5(Pessoa.apply)
  implicit val actionPerformedJsonFormat: RootJsonFormat[CreatePessoaResponse] = jsonFormat1(CreatePessoaResponse.apply)
}