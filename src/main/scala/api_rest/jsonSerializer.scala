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

  implicit val pessoaJsonFormat: RootJsonFormat[Pessoa] = jsonFormat5(Pessoa.apply)
  implicit val actionPerformedJsonFormat: RootJsonFormat[CreatePessoaResponse] = jsonFormat1(CreatePessoaResponse.apply)
}