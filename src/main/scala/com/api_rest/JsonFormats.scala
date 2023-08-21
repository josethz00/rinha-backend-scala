package com.api_rest

import com.api_rest.UserRegistry.ApiResponse
import com.database.{Pessoa, Pessoas}
import spray.json.{JsNumber, JsValue, RootJsonFormat, deserializationError}
import akka.http.scaladsl.model.StatusCode

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val pessoaJsonFormat: RootJsonFormat[Pessoa] = jsonFormat5(Pessoa.apply)
  implicit val pessoasJsonFormat: RootJsonFormat[Pessoas] = jsonFormat1(Pessoas.apply)

  implicit val statusCodeJsonFormat: RootJsonFormat[StatusCode] = new RootJsonFormat[StatusCode] {
    override def write(status: StatusCode): JsValue = JsNumber(status.intValue())

    override def read(json: JsValue): StatusCode = json match {
      case JsNumber(value) => StatusCode.int2StatusCode(value.toInt)
      case _ => deserializationError("StatusCode expected")
    }
  }

  implicit val actionPerformedJsonFormat: RootJsonFormat[ApiResponse] = jsonFormat3(ApiResponse.apply)
}
//#json-formats
