package tinkoff.fintech.service.services

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller, ToResponseMarshallable}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import io.circe._
import io.circe.syntax._
import tinkoff.fintech.service.quest.{Fail, Ok, OkCreate, Response}

import scala.collection.immutable.Seq
import scala.concurrent.Future

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  */
object AkkaHttpSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/json`)

  /**
    * `Json` => HTTP entity
    *
    * @return marshaller for JSON value
    */
  implicit final def jsonMarshaller(
                                     implicit printer: Printer = Printer.noSpaces
                                   ): ToEntityMarshaller[Json] =
    Marshaller.oneOf(mediaTypes: _*) { mediaType =>
      Marshaller.withFixedContentType(ContentType(mediaType)) { json =>
        HttpEntity(mediaType, printer.pretty(json))
      }
    }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit final def marshaller[A: Encoder](
                                             implicit printer: Printer = Printer.noSpaces
                                           ): ToEntityMarshaller[A] =
    jsonMarshaller(printer).compose(Encoder[A].apply)

  /**
    * HTTP entity => `Json`
    *
    * @return unmarshaller for `Json`
    */
  implicit final val jsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty => throw Unmarshaller.NoContentException
        case data => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
      }

  implicit def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    def decode(json: Json) = Decoder[A].decodeJson(json).fold(throw _, identity)

    jsonUnmarshaller.map(decode)
  }


}



