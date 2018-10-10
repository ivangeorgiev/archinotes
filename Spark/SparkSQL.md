# SparkSQL Recipes

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