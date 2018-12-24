---
typora-copy-images-to: SeshionizationWithSpark
typora-root-url: ./
---



## Monday, 18 September 2017



### Sessionization with Spark



I recently worked on a project where we needed to process large number of user events and collate these into "sessions". Specifically, we had a large number of events from each user that was accessing an application, but in this case we didn't care about the details of each event, we just wanted to know how often users used the app in question, how long they used it for and how many clicks each session consisted of. We would consider successive events to be in different sessions when they were more than 30 minutes apart, i.e. we would consider a gap of half an hour to end the previous session, and a new session to start with the next event.

Put simply, we wanted to take a table of event data like the following:

| user ID | URL        | timestamp |
| ------- | ---------- | --------- |
| x       | http://foo | 101       |
| x       | http://foo | 150       |
| x       | http://foo | 201       |
| ...     | ...        | ...       |

and turn it into a table like this:

| user ID | URL        | start time | end time | number of clicks |
| ------- | ---------- | ---------- | -------- | ---------------- |
| x       | http://foo | 101        | 201      | 3                |
| ...     | ...        | ...        | ...      | ...              |

We used Spark for all our big data jobs, so we turned to it for a solution to this problem as well. It turns out that the solutions to this problem are useful to illustrate various different ways you can write Spark code, highlighting the distinct pros and cons of each approach.

The following sections discuss some possible solutions, providing example implementations that run on a publicly available dataset - the ["Outbrain Click Prediction" dataset from Kaggle](https://www.kaggle.com/c/outbrain-click-prediction), which contains around 2 billion clicks. The example implementations ([available in Github](https://github.com/stettix/spark-sessions)) are in Scala, but the points raised should be valid for PySpark as well.

## Solution 1: RDDs and groupBy

To anyone who's worked with the RDD API of Spark, there's a simple solution to this problem: simply group each event in the input dataset by the session key (for example user ID or user ID + page), then write a Scala function that processes the events for each grouping to produce sessions. Implementing this function is a straightforward exercise that doesn't involve any Spark concepts, it's purely a Scala function. Our example solution for this is in the [GroupBySessions](https://github.com/stettix/spark-sessions/blob/master/src/main/scala/net/janvsmachine/sparksessions/GroupBySessions.scala#L17) class.

Experienced Spark developers will know that this solution comes with a caveat: when we perform `groupBy` on an RDD, all the items for each grouping key are pulled into the memory of a Spark worker. This means that if the input contains a very large number of events for a given user, we risk seeing the job fail with an `OutOfMemoryError`, if the worker node doesn't have enough heap space for all the items for a key. Whether this is a problem or not depends on your dataset. If you are sure that the number of items for each grouping key will never be that big then you may be OK. In any case, it's not particularly satisfying to know that your job may blow up if it gets data that looks different from what you expect.

## Solution 2: custom partitioners with sorted partitions

To be sure to avoid running out of memory while processing events, we need some way to iterate over all the events for a grouping key in time order, emitting sessions incrementally as we go. One way to do this is to first of all make use of Spark's facility for providing a custom partitioner. This lets us partition the data by the grouping key, so that all the data for a given grouping key (e.g. user ID) lives on the same one node in the cluster.

In [our example implementation](https://github.com/stettix/spark-sessions/blob/master/src/main/scala/net/janvsmachine/sparksessions/SortWithinPartitionsSessions.scala) we just build browsing sessions using clicks across all web sites, so we simply call:

```scala
  clicks.repartition('userId')
```

Next, Spark lets us specify a sort order for the items in a partition. In our case, we call:

```scala
  .sortWithinPartition('userId', 'timestamp')
```

Now we have partitions where the events for a given user live on a single node, appear together in sequences, sorted by the timestamp.

The final piece of the puzzle is Spark's  `mapPartitions` method, which lets us iterate over these events in order. The type signature of the method on `Dataset[T]` is:

```scala
def mapPartitions[U](func: (Iterator[T]) ⇒ Iterator[U])(implicit arg0: Encoder[U]): Dataset[U]
```

Basically, we just have to provide a function that takes Iterator[T] where T is our event type, and returns Iterator[U] where U is our session type. Our example implementation does this with the function:

```
  def aggregateClicks(maxSessionDuration: Long)(clicks: Iterator[Click]): Iterator[Session]
```

Now, we're back in pure Scala code again, we just need to implement this in the desired manner. But note! The point of going to all the trouble of creating our custom sorted partitions was to avoid pulling all the events for a grouping key into memory. So we have to be careful that our implementation of this method doesn't do this either, i.e. it has to produce an output iterator that iterates incrementally over the input iterator when it itself is iterated over.

It's actually very easy to get this wrong. Scala's type system doesn't help us in this case, it'll make sure we provide an output of the right type but doesn't restrict us from doing silly things in the implementation (like calling

 

```
toList
```

 

on the input!). You can easily make more subtle mistakes too. The only way to really make sure is to write some unit tests that exercise corner cases of this and check they don't blow up. We provide

 

two different tests related to this

: one that reads sessions with a very large number of events, and one that reads a very large number of sessions for a user.

Getting the implementation to pass our tests isn't quite as straight-forward as it seems! After some deliberation and a couple of false starts, the solution I came up implements its own iterator, using mutable state in the process (😱!).

See

 

Advanced Analytics with Spark

 

and

 

High Performance Spark

 

for detailed discussions on the partion-based approach.

## Solution 3: Spark SQL and Window Functions

The above solutions used the Scala APIs for RDDs and Datasets to perform the logic of grouping events into session. While both got the job done,

 

```
groupBy
```

 

has the mentioned scalability issues, and the solution involving custom partitioners involved dealing with some fairly low level concepts and writing some error-prone code.

Spark users will know that Spark also provides a SQL interface. Is there a way we could do the sessionization using declarative SQL instead of writing low-level Spark code? It turns out that there is! This may be surprising - how can you write SQL that processes sequences of rows, doing calculation such as the difference in timestamp between rows? The answer, as it turns out, is

 

window functions

.

The topic of window functions is far too extensive to explain in this blog post, but the Spark documentation provides a

 

nice introduction

. It's also worth noting the window functions are a fairly standard SQL feature (introduced in SQL:2003) and commonly available in relational databases (but not in MySQL, duh...).

The implementation we came up with for this problem (see the WindowsFunctionsSession class in the source code) was inspired by the excellent blog post

 

Finding User Sessions with SQL

 

by Benn Stancil. We could have expressed this as a SQL query string, but we wrote it using Spark's typed DSL instead, thereby slightly increasing the chances of getting it right (though queries built this way still have a lot of scope for failing at runtime!). The resulting code, found in

 

the WindowFunctionSessions class

, looks as follows:

```
    val clicksWithSessionIds = clicks
      .select('userId, 'timestamp,
        lag('timestamp, 1)
          .over(Window.partitionBy('userId).orderBy('timestamp))
          .as('prevTimestamp))
      .select('userId, 'timestamp,
        when('timestamp.minus('prevTimestamp) < lit(maxSessionDuration), lit(0)).otherwise(lit(1))
          .as('isNewSession))
      .select('userId, 'timestamp,
        sum('isNewSession)
          .over(Window.partitionBy('userId)
                      .orderBy('userId, 'timestamp))
          .as('sessionId))

    clicksWithSessionIds
      .groupBy("userId", "sessionId")
      .agg(min("timestamp").as("startTime"), 
           max("timestamp").as("endTime"), 
           count("*").as("count"))
      .as[Session]
```

The query basically uses a sliding window of size 2 over the rows partitioned by user ID and sorted by timestamp, using the

 

```
lag('timestamp, 1)
```

 

window function. The timestamp of the previous row is turned into a new column

 

```
prevTimestamp
```

. We next create a new column which contains a 0 or 1 depending on whether the row is in a different session to the previous row. Finally, we add a column with the running total of these 0's and 1's, which becomes a sequential ID for each session. In the final step, we simply group by this session ID, and perform the aggregation we want, in this case min and max on the timestamp, and a count.

## Performance

Leaving aside other considerations for a moment, what is the performance seen using the different approaches above? To find this out, we ran the various algorithms on an example dataset of click data. As mentioned, we used the Kaggle

 

Outbrain Click Prediction dataset, which has around 2 billion rows of clicks associated with a user ID, a page ID, and a timestamp. We ran the tests on an AWS EMR cluster (EMR version 5.8.0, with Spark 2.2.0) with 4 m4.4xlarge worker nodes, reading data from and writing results to AWS S3.



Disclaimer:

 

As all benchmarks, the results are affected by many parameteters and may not provide an accurate prediction of how this will perform for you. YMMV, take all numbers with a large pinch of salt!

The time to perform sessionization with each of the three methods is as follows:

| Method                           | Time       |
| -------------------------------- | ---------- |
| partition + sort + mapPartitions | 7 minutes  |
| SQL window functions             | 10 minutes |
| RDD + groupBy                    | 15 minutes |

## Discussion

We have seen three different ways to solving the same problem. Which one solved the problem best, what approach would we stick with the next time we want to do something similar?

The most important thing when implementing any bit of software is that it works correctly, and keeps working correctly. That means we need to be confident the code doesn't have any bugs, but we also want to have a reasonable level of confidence that the code will cope with data it comes across in the future. Ideally, we should only need to add more hardware to our cluster as data sizes grow, we don't want to end up in a situation where we suddenly have to rewrite the code when the data reaches a certain size, or if we come across outliers in our data.

Judging our solutions by these criteria, we first look at the RDD + groupBy approach. This has a lot going for it in that it is easy to implement, there's little scope for getting the implementation wrong. But, it has the drawback we've discussed where it can cause your job to fail with out-of-memory errors if you have too many records for a given grouping key. With our example click data that wasn't a problem so the code worked fine. Performance-wise, it's OK but a bit slower than the others.

Next, we have the "sorted partitions" solution. This runs fastest of all and will happily cope whatever the number of records you have per session. However, I found it really tricky to come up with a correct solution for this, it's easy to accidentally implement it so that it's functionally correct but that blows up for large sessions. Such subtle bugs that are difficult to catch even in tests are not something I'd like lurking in my code base.

Finally, we have the solution using Spark SQL and window functions. This ticks all the boxes: it's quick and simple to implement, is fairly simple to get right, and performs well. While it was slightly slower than mapping over sorted partitions, I don't think the differences are that significant.

So for me, this is another area where Spark SQL shines. The fact that you can write your code as plain SQL or use Spark 2.0's Dataset API to write the queries is an additional bonus.