



* http://diethardsteiner.github.io/spark/2017/04/04/Spark-Scala-Retrieve-Data-From-REST-API-and-convert-to-Spark-Dataset.html

```scala
import scala.io.Source._
import org.json4s.jackson.JsonMethods.parse

implicit val formats = org.json4s.DefaultFormats

case class SensorData(
  bulletin_date:String
  , bulletin_date_formatted:String
  , easting:String
  , last_uploaded:String
  , latest_capture:String
  , latest_week:String
  , latitude:String
  , location:Location
  , longitude:String
  , no2_air_quality_band:String
  , no2_air_quality_index:String
  , northing:String
  , site_code:String
  , site_name:String
  , site_type:String
  , spatial_accuracy:String
  , ward_code:String
  , ward_name:String
)


val parsedData = parse(fromURL("https://opendata.camden.gov.uk/resource/83f4-6in2.json?$limit=10").mkString).extract[Array[SensorData]]
parsedData(1).site_code
val mySourceDataset = spark.createDataset(parsedData)

scala> mySourceDataset.printSchema
root
 |-- bulletin_date: string (nullable = true)
 |-- bulletin_date_formatted: string (nullable = true)
 |-- easting: string (nullable = true)
 |-- last_uploaded: string (nullable = true)
 |-- latest_capture: string (nullable = true)
 |-- latest_week: string (nullable = true)
 |-- latitude: string (nullable = true)
 |-- location: struct (nullable = true)
 |    |-- type: string (nullable = true)
 |    |-- coordinates: array (nullable = true)
 |    |    |-- element: string (containsNull = true)
 |-- longitude: string (nullable = true)
 |-- no2_air_quality_band: string (nullable = true)
 |-- no2_air_quality_index: string (nullable = true)
 |-- northing: string (nullable = true)
 |-- site_code: string (nullable = true)
 |-- site_name: string (nullable = true)
 |-- site_type: string (nullable = true)
 |-- spatial_accuracy: string (nullable = true)
 |-- ward_code: string (nullable = true)
 |-- ward_name: string (nullable = true)

scala> mySourceDataset.createOrReplaceTempView("campden_pollution_sensor_data")

scala> spark.sql("SELECT bulletin_date, ward_name, no2_air_quality_index FROM campden_pollution_sensor_data")
res9: org.apache.spark.sql.DataFrame = [bulletin_date: string, ward_name: string ... 1 more field]

scala> spark.sql("SELECT bulletin_date, ward_name, no2_air_quality_index FROM campden_pollution_sensor_data").show



```

