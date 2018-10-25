val SDPJson = """
{
    "environments": [
       { "name": "UAT"},
       { "name": "SIT" },
       { "name": "DEV" },
       { "name": "xxx", "dbNamePrefix": "", "dataRoot": "/tmp" }
    ],

    "databases": [
        {
            "name": "BUSINESS_REPOSITORY",
            "tables": [
                {
                    "name": "RAW_SESSIONS",
                    "partitionFields": ["country_code", "session_date_at_utc"]
                },
                {
                    "name": "SCHEDULE360",
                    "format": "ORC",
                    "partitionFields": ["country_code", "program_schedule_start_date"]
                },
                {
                    "name": "OVERNIGHT_VIEWERSHIP360",
                    "format": "ORC",
                    "partitionFields": ["country_code", "session_date_at"]
                },
                {
                    "name": "IG",
                    "partitionFields": null,
                    "dataRoot": "/path/to/table"
                }
            ]
        },
        {
            "name": "IvAn",
            "dataRoot": "/path/to/database",
            "tables": [
                {
                    "name": "georgiev"
                }
            ]
        }
    ]
}
""";


import scala.util.parsing.json._

case class SDP_Environment(name:String, dataRoot: String = null, dbNamePrefix: String = null)
case class SDP_Table(name: String, format:String = null, location:String = null, partitionFields:List[String] = null, envName: String = null, dbName: String = null)
case class SDP_Database(name: String, tables: Map[String, SDP_Table], dataRoot: String = null, envName: String = null)

class SDP_Helper(SDPJson: String) {
    val j = JSON.parseFull(SDPJson).get.asInstanceOf[Map[String,Any]]
    val dataRoot = "/SDP"
    val defaultFormat = "orc"
    val envList = j("environments").asInstanceOf[List[Map[String,Any]]]
    val dbList = j("databases").asInstanceOf[List[Map[String,Any]]]
    
    val envMap = envList.foldLeft(Map[String,SDP_Environment]()) {(m, e) => 
       val envName = e("name").asInstanceOf[String].toUpperCase()
       val env = SDP_Environment(envName,
              if (e.contains("dataRoot")) e("dataRoot").asInstanceOf[String] else s"$dataRoot/$envName/DATA_LAKE",
              if (e.contains("dbNamePrefix")) e("dbNamePrefix").asInstanceOf[String] else s"${envName}_")
       m + (env.name.toUpperCase() -> env)}
       
    val dbMap = dbList.foldLeft(Map[String, SDP_Database]()){ (m,d) =>
       val dbName = d("name").asInstanceOf[String].toUpperCase()
       val tbList = d("tables").asInstanceOf[List[Map[String,Any]]]
       val tbMap = tbList.foldLeft(Map[String, SDP_Table]()) { (tm, t) =>
          val tbLocation = if (t.contains("location")) t("location").asInstanceOf[String] else null
          val format = if (t.contains("format")) t("format").asInstanceOf[String] else null
          val tbl = SDP_Table(name = t("name").asInstanceOf[String].toUpperCase(), format = format, 
                    location = tbLocation, 
                    partitionFields = if ( t.contains("partitionFields") ) t("partitionFields").asInstanceOf[List[String]] else null
                    )
          tm + (tbl.name.toUpperCase -> tbl)
       }
       val dataRoot = if (d.contains("dataRoot")) d("dataRoot").asInstanceOf[String] else null
       val db = SDP_Database(name = dbName, tables = tbMap, dataRoot = dataRoot)
       m + (db.name.toUpperCase() -> db)
    }

    def getEnv(envName: String) = {
        envMap(envName.toUpperCase())
    }
    
    def getDB(dbName: String, envName: String) = {
        val env = envMap(envName.toUpperCase())
        val db = dbMap(dbName.toUpperCase)
        val dbDataRoot = ( if (db.dataRoot==null || db.dataRoot=="") s"${env.dataRoot}/${db.name}" else db.dataRoot)
        SDP_Database(
            name = db.name,
            tables = db.tables.map(tbEl => {
                val tb = tbEl._2
                (tbEl._1, SDP_Table(
                    name=tb.name,
                    format=( if (tb.format==null || tb.format=="") defaultFormat else tb.format),
                    location=( if (tb.location==null || tb.location=="") s"$dbDataRoot/${tb.name}" else tb.location),
                    partitionFields = tb.partitionFields,
                    envName=env.name,
                    dbName=env.dbNamePrefix + db.name
                ))
                }),
            dataRoot = dbDataRoot,
            envName = env.name)
    }
    
    def getTable(tbName:String, dbName:String, envName: String) = {
         val db = getDB(dbName=dbName, envName=envName)
         db.tables(tbName.toUpperCase())
    }    
}

val SDP = new SDP_Helper(SDPJson)


val tb = SDP.getTable("OVERNIGHT_VIEWERSHIP360", "BUSINESS_REPOSITORY", "UAT")
//     def fromLocation(tableName: String, dbName: String = "", fileLocation: String, format:String = "ORC", partitionFields: List[String] = null): String = {

print(TableDDLBuilder.fromLocation(tb.name+"", tb.dbName, tb.location, tb.format, tb.partitionFields))

