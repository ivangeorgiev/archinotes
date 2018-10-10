# Spark and Scala Recipes

## How to Check Spark and Scala versions in Zeppelin

```scala
// Get Spark Version
sc.version

// Get Scala Version
util.Properties.versionString
```



## Parse CSV String into a DataSet or DataFrame

Spark 2.2.0+

```scala
val csvDataString = 
    """id, date, timedump
      |1, "2014/01/01 23:00:01",1499959917383
      |2, "2014/11/31 12:40:32",1198138008843"""
```



```scala
// Convert CSV string into a DataSet of String
val csvStringDataSet = csvDataString.stripMargin.lines.toList.toDS()
csvStringDataSet.show(false)
```

```
+--------------------------------------+
|value                                 |
+--------------------------------------+
|id, date, timedump                    |
|1, "2014/01/01 23:00:01",1499959917383|
|2, "2014/11/31 12:40:32",1198138008843|
+--------------------------------------+
```



com.databricks:spark-avro_2.11:1.5.0

