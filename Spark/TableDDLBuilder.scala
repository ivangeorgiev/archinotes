// Utilities
import org.apache.spark.sql.DataFrame

object TableDDLBuilder {

    def getFieldsFromDF(df: DataFrame): Array[(String, String)] = df.schema.fields.map(f => (f.name, f.dataType.sql))
    
    /**
     * Build CREATE TABLE DDL from a list of fields.
     */
    def fromFields(fields: Array[(String, String)], tableName: String, dbName: String = "", 
                   fileLocation: String, format: String ="ORC", 
                   partitionFields: List[String] = null): String = {
        val dataFieldsString = fields
               .filter(fld => ! partitionFields.contains(fld._1))
               .map(fld => { "   `" + fld._1 + "`  " + fld._2})
               .mkString(",\n")
        
        val isPartitioned: Boolean = partitionFields != null && partitionFields.size > 0
        val partitionByString = if ( isPartitioned ) {
            // Build a partition field types map, indexed by field name
            val partitionFieldMap = scala.collection.mutable.Map[String,String]()
            
            fields.foreach(fld => {
                if (partitionFields.contains(fld._1)) 
                   partitionFieldMap(fld._1.toString) = fld._2.toString
            })
            
            // Create PARTITIONED BY string
            "PARTITIONED BY (" + partitionFields.map(fld => fld.toString + "  " + partitionFieldMap(fld.toString)).mkString(", ") + ")\n"
        } else {
            ""
        }
        
        val qualifiedTableName = if (dbName == "") tableName else s"$dbName.$tableName" 
        // Build final CREATE TABLE string
        (s"""
            |DROP TABLE IF EXISTS $qualifiedTableName;\n

            |CREATE EXTERNAL TABLE $qualifiedTableName (
            |$dataFieldsString
            |)
            |$partitionByString
            |STORED AS $format
            |LOCATION '$fileLocation';\n

            |MSCK REPAIR TABLE $qualifiedTableName;
        """)
    }

    
    /**
     * Build CREATE TABLE DDL from a DataFrame.
     */
    def fromDF(df: DataFrame, tableName: String, dbName: String = "", fileLocation: String, format: String = "ORC", partitionFields: List[String] = null): String = {
        val allFields = getFieldsFromDF( df )
        fromFields( fields = getFieldsFromDF( df ),
            tableName = tableName,
            dbName = dbName,
            fileLocation = fileLocation,
            format = format,
            partitionFields = partitionFields)
    }
    
    
    
    /**
     * Build CREATE TABLE DDL, infering the schema from the data at location.
     */
    def fromLocation(tableName: String, dbName: String = "", fileLocation: String, format:String = "ORC", partitionFields: List[String] = null): String = {
        fromDF( 
            df = spark.read.format(format).load(fileLocation),
            tableName = tableName,
            dbName = dbName,
            fileLocation = fileLocation,
            format = format,
            partitionFields = partitionFields
        )
    }
}
