package api_rest

import PessoaActor.ActionPerformed
import PessoaActor.ActionPerformed
import spray.json._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object jsonSerializer extends DefaultJsonProtocol {
  implicit object LocalDateFormat extends JsonFormat[LocalDate] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    def write(date: LocalDate): JsValue = JsString(date.format(formatter))

    def read(json: JsValue): LocalDate = json match {
      case JsString(value) => LocalDate.parse(value, formatter)
      case _ => deserializationError("Esperado um LocalDate como JsString")
    }
  }

  implicit val pessoaJsonFormat: RootJsonFormat[Pessoa] = jsonFormat4(Pessoa.apply)
  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed.apply)
}
