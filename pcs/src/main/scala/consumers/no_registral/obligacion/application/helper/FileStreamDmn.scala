package consumers.no_registral.obligacion.application.helper

import org.camunda.bpm.model.dmn.Dmn
import org.camunda.dmn.DmnEngine
import org.camunda.dmn.parser.ParsedDmn
import scalaz.\/
import scalaz.concurrent.Task.Try

import java.io.InputStream
import java.io.FileInputStream

object FileStreamDmn {


  val dmnStream= {
    val path: String = Try(System.getenv("PATH_DMN_DECISION_30")).getOrElse("/opt/docker/bin/decision_30_descuento.dmn")
    val file: FileInputStream = Try(new FileInputStream(path)).getOrElse(null)
    if(file == null) {
      val path: String = Try(System.getenv("PATH_DMN_DECISION_30")).getOrElse("/opt/docker/bin/decision_30_descuento.dmn")
      val file1: FileInputStream = Try(new FileInputStream(path)).getOrElse(null)
      val chequeoDmn1: Either[DmnEngine.Failure, ParsedDmn] = new DmnEngine().parse(file1)
      chequeoDmn1
    }else{
      val chequeoDmn1: Either[DmnEngine.Failure, ParsedDmn] = new DmnEngine().parse(file)
      chequeoDmn1
    }
  }
}


