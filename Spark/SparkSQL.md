# SparkSQL Recipes

## Table of Contents

[TOC]



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

In the above dependency `2.11` is the version of Scala and `4.0.0` is the version of the library.

```scala
// Create sample data
val userDF = sql("""
SELECT 1 AS id, "Jon" AS first_name, "Smight" AS last_name, CAST('1984-12-03' AS DATE) AS birth_date
UNION
SELECT 2 AS id, "Janett" AS first_name, "Ganguli" AS last_name, CAST('1993-04-23' AS DATE) AS birth_date
""")
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

## Create a DataFrame from XML Files

You need to add following dependency (check [Maven spark-xml](https://mvnrepository.com/artifact/com.databricks/spark-xml) package for correct version):

```
com.databricks:spark-xml_2.11:0.4.1
```



### Using DataBricks Example

```bash
wget -O /resources/data/books.xml https://github.com/databricks/spark-xml/raw/master/src/test/resources/books.xml
cat /resources/data/books.xml
```



```scala
sql("DROP TABLE IF EXISTS books")
sql("""
CREATE TABLE books
USING com.databricks.spark.xml
OPTIONS (path "/resources/data/books.xml", rowTag "book")
""")
val booksDF = sql("SELECT * FROM books")
booksDF.show()
```



### Using Random User Data



```bash
# Download random data
wget -O /resources/data/users10.xml "https://randomuser.me/api/?format=xml&inc=name,email&results=10&seed=rufus"
cat /resources/data/users10.xml
```



```scala
var userDF = sqlContext.read
  .format("com.databricks.spark.xml")
   .option("rowTag", "user")
   .load("/resources/data/users10.xml")
 userDF.printSchema
```

The result is as follows:

```
userDF: org.apache.spark.sql.DataFrame = [info: struct<page: bigint, results: bigint ... 2 more fields>, results: array<struct<email:string,name:struct<first:string,last:string,title:string>>>]
root
 |-- info: struct (nullable = true)
 |    |-- page: long (nullable = true)
 |    |-- results: long (nullable = true)
 |    |-- seed: string (nullable = true)
 |    |-- version: double (nullable = true)
 |-- results: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- email: string (nullable = true)
 |    |    |-- name: struct (nullable = true)
 |    |    |    |-- first: string (nullable = true)
 |    |    |    |-- last: string (nullable = true)
 |    |    |    |-- title: string (nullable = true)
```

See also:

* [Databricks XML Library](https://github.com/databricks/spark-xml).
* [Databricks SparkSQL CREATE TABLE Statement](https://docs.databricks.com/spark/latest/spark-sql/language-manual/create-table.html)

Same with creating table against the XML file.

```scala
sql("DROP TABLE IF EXISTS users")
sql("""
CREATE TABLE users
USING com.databricks.spark.xml
OPTIONS (path "/resources/data/users10.xml", rowTag "user")
""")
val booksDF = sql("SELECT u.email, u.name.title, u.name.first, u.name.last FROM users LATERAl VIEW explode(results) AS u")
booksDF.show()
```

```
+--------------------+-----+---------+----------+
|               email|title|    first|      last|
+--------------------+-----+---------+----------+
|vanessa.sullivan@...| miss|  vanessa|  sullivan|
|thea.sviggum@exam...| miss|     thea|   sviggum|
|lauren.garza@exam...|   ms|   lauren|     garza|
|eric.daniels@exam...|   mr|     eric|   daniels|
|حسین.سهيليراد@exa...|   mr|     حسین| سهيلي راد|
|kathryn.soto@exam...|  mrs|  kathryn|      soto|
|jusseline.santos@...|  mrs|jusseline|    santos|
|aloïs.roussel@exa...|   mr|    aloïs|   roussel|
|frens.kruidenier@...|   mr|    frens|kruidenier|
|leana.roger@examp...| miss|    leana|     roger|
+--------------------+-----+---------+----------+
```



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



## Query CSV Data As Table

```sql
CREATE TABLE IG_DIM_PARTY
USING CSV
OPTIONS (
    path "/user/ivan/party/",
    delimiter "\t",
    header true
)
```

```sql
DESCRIBE IG_LND_DIM_PARTY
```

```
+----------------------------+---------+-------+
|col_name                    |data_type|comment|
+----------------------------+---------+-------+
|PARTY_ID                    |string   |null   |
|PARTY_CODE                  |string   |null   |
|FAMILY_NAME                 |string   |null   |
|MIDDLE_NAMES                |string   |null   |
|GIVEN_NAME                  |string   |null   |
|GENDER                      |string   |null   |
|DATE_OF_BIRTH               |string   |null   |
|NATIONALITY                 |string   |null   |
|MARITAL_STATUS              |string   |null   |
+----------------------------+---------+-------+
```

See also:

* https://github.com/databricks/spark-csv
* https://docs.databricks.com/spark/latest/spark-sql/language-manual/create-table.html



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
val theDF = userDF


// ==================================

// Gelt a list of fields and types
theDF.createOrReplaceTempView(TempViewName)
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
    |CREATE EXTERNAL TABLE $TargetTable (
    |$dataFieldsString
    |)
    |$partitionByString
    |STORED AS ORC
    |LOCATION '$LocationDir';
""")
```

```
CREATE EXTERNAL TABLE user(
   user_id  int,
   first_name  string,
   last_name  string
)
PARTITIONED BY (title  string)
STORED AS ORC
LOCATION '/data/data_lake/USER';
```



## Use Databricks DBUtils Library

Add dependency:

```
com.databricks:dbutils-api_2.11:0.0.3
```

```scala
dbutils.fs.put("/resources/data/test.json", """
{"string":"string1","int":1,"array":[1,2,3],"dict": {"key": "value1"}}
{"string":"string2","int":2,"array":[2,4,6],"dict": {"key": "value2"}}
{"string":"string3","int":3,"array":[3,6,9],"dict": {"key": "value3", "extra_key": "extra_value3"}}
""", true)
```

```bash
cat /resources/data/test.json
```





# New Topics

* https://stackoverflow.com/questions/31341498/save-spark-dataframe-as-dynamic-partitioned-table-in-hive
* 