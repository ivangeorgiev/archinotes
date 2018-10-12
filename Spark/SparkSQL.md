# SparkSQL Recipes

## Save a DataFrame to Partitioned ORC

```scala
// Import
import org.apache.spark.sql.SaveMode
// Create sample data
val userDF = sql("""SELECT 1 AS id, "Jon" AS first_name, "Smight" AS last_name, CAST('1984-1203' AS DATE) AS birth_date""")

// Save sample data as ORC
userDF.write.format("orc")
  .mode(SaveMode.Overwrite)
  .partitionBy("birth_date")
  .save("/resources/data/user")
```



```bash
ls -al /resources/data/user/birth_date=1984-12-03
```



```
-rw-r--r-- 1 root resources  458 Oct 12 18:55 part-00000-c3468a56-8a22-4fb0-8e62-8b40d5d4f947.snappy.orc
-rw-r--r-- 1 root resources   12 Oct 12 18:55 .part-00000-c3468a56-8a22-4fb0-8e62-8b40d5d4f947.snappy.orc.crc
```



## Create a DataFrame from ORC Data

```scala
val savedDF = spark.read.format("orc").load("/resources/data/user")
savedDF.show()
```



## Save a DataFrame to a Partitioned Avro

First add following Maven dependency (check [spark-avro](https://mvnrepository.com/artifact/com.databricks/spark-avro) package in Maven for the correct version):

```sclala
com.databricks:spark-avro_2.11:4.0.0
```



```spark
// Create sample data
val userDF = sql("""SELECT 1 AS id, "Jon" AS first_name, "Smight" AS last_name, CAST('1984-12-03' AS DATE) AS birth_date""")
```



```scala
// Save sample data as Avro
import org.apache.spark.sql.SaveMode
userDF.write.format("com.databricks.spark.avro")
  .mode(SaveMode.Overwrite)
  .partitionBy("birth_date")
  .save("/resources/data/user_avro")
```

Checking the result.

```bash
ls -al /resources/data/user_avro/birth_date=1984-12-03
```

```
-rw-r--r-- 1 root resources  244 Oct 12 19:20 part-00000-3f9d597b-b78f-4295-b224-7b827fb1c22d.avro
-rw-r--r-- 1 root resources   12 Oct 12 19:20 .part-00000-3f9d597b-b78f-4295-b224-7b827fb1c22d.avro.crc
```



## Create a DataFrame from Avro File(s)

```scala
val savedDF = spark.read.format("com.databricks.spark.avro")
   .load("/resources/data/user_avro")
savedDF.show()
```

And the result:

```
+---+----------+---------+----------+
| id|first_name|last_name|birth_date|
+---+----------+---------+----------+
|  1|       Jon|   Smight|1984-12-03|
+---+----------+---------+----------+
```



See also [DataBricks::Read Avro](https://docs.databricks.com/spark/latest/data-sources/read-avro.html)

## What is the Schema for a DataFrame?

```scala
// Create a Spark DataFrame with some user data.
val userDF = spark.createDataFrame(sc.parallelize(
        Seq(
           (1, "mr", "javier", "barrett"),
           (2, "ms", "aafje", "kattenberg")
        )
    ))
    .toDF("user_id", "title", "first_name", "last_name")
```



```scala
// Print DataFrame's schema as pretty tree
userDF.printSchema
```

```
root
 |-- user_id: integer (nullable = true)
 |-- title: string (nullable = true)
 |-- first_name: string (nullable = true)
 |-- last_name: string (nullable = true)
```



```scala
// Print DataFrame's schema as JSON string
print(userDF.schema.json)
```

```
{"type":"struct","fields":[{"name":"user_id","type":"integer","nullable":true,"metadata":{}},{"name":"title","type":"string","nullable":true,"metadata":{}},{"name":"first_name","type":"string","nullable":true,"metadata":{}},{"name":"last_name","type":"string","nullable":true,"metadata":{}}]}
```



```scala
// Print DataFrame's schema as pretty JSON string
print(userDF.schema.prettyJson)
```



## Build CREATE TABLE Statement

Here is a sample DataFrame.

```scala
// Create a Spark DataFrame with some user data.
val userDF = spark.createDataFrame(sc.parallelize(
        Seq(
           (1, "mr", "javier", "barrett"),
           (2, "ms", "aafje", "kattenberg")
        )
    ))
    .toDF("user_id", "title", "first_name", "last_name")
```



Very basic way to build `CREATE TABLE` statement:

```scala
// Build CREATE TABLE statement
userDF.createOrReplaceTempView("DUMMY_VIEW")

val fieldList = spark.sql(s"DESCRIBE DUMMY_VIEW")
       .collect()
       .map(fld => { "   " + fld(0) + "  " + fld(1)} )
       .mkString(",\n")
spark.catalog.dropTempView("DUMMY_VIEW")
```



More advanced `CREATE TABLE`  statement, supporting table partitioning:

```scala
// ==== Configuration ===
val LocationDir = "/data/data_lake/USER"
val TargetDatabase = "APP_DATA"
val TargetTable = "USER"
val partitionBy = List("title")
val TempViewName = "_TMP_CREATE_TABLE_DATA_"


// ==================================

// Gelt a list of fields and types
userDF.createOrReplaceTempView(TempViewName)
val schemaFields = spark.sql(s"DESCRIBE $TempViewName")
       .collect()
spark.catalog.dropTempView(TempViewName)

// Filter out partition fields and make a comma separated string
val dataFieldsString = schemaFields
       .filter(fld => ! partitionBy.contains(fld(0)))
       .map(fld => { "   " + fld(0) + "  " + fld(1)})
       .mkString(",\n")
       
// Build a partition field types map, indexed by field name
val partitionFieldMap = scala.collection.mutable.Map[String,String]()

schemaFields.foreach(fld => {
    if (partitionBy.contains(fld(0))) 
       partitionFieldMap(fld(0).toString) = fld(1).toString
})

// Create PARTITIONED BY string
val partitionByString = if (partitionBy.size > 0)  "PARTITIONED BY (" + partitionBy.map(fld => fld.toString + "  " + partitionFieldMap(fld.toString)).mkString(", ") + ")" else ""

// Build final CREATE TABLE string
print(s"""
    |CREATE TABLE user(
    |$dataFieldsString
    |)
    |$partitionByString
    |STORED AS ORC
    |LOCATION '$LocationDir';
""")
```

```
CREATE TABLE user(
   user_id  int,
   first_name  string,
   last_name  string
)
PARTITIONED BY (title  string)
STORED AS ORC
LOCATION '/data/data_lake/USER';
```





# New Topics

* https://stackoverflow.com/questions/31341498/save-spark-dataframe-as-dynamic-partitioned-table-in-hive
* 