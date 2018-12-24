



* [Using Apache Pig to Sessionize Web Server Log Data](https://www.oreilly.com/library/view/hadoop-real-world-solutions/9781849519120/ch03s05.html) from Hadoop Real-World Solutions Cookbook
* [Analyzing Apache Access Logs with Spark and Scala](https://alvinalexander.com/scala/analyzing-apache-access-logs-files-spark-scala) 
* [Analyzing Apache Access Logs with Databricks](https://databricks.com/blog/2015/04/21/analyzing-apache-access-logs-with-databricks-cloud.html) 
* https://modeanalytics.com/editor/devo/reports/f779eee0283d
* https://www.kaggle.com/c/outbrain-click-prediction/data









```sql
SELECT
   user_id
   ,event_type
   ,event_name
   ,occurred_at
   ,LAG(occurred_at, 1) OVER(PARTITION BY user_id ORDER BY occurred_at)   prev_event_at
FROM tutorial.playbook_events e
```



```sql
SELECT
  s.*
  , CASE WHEN EXTRACT('EPOCH' FROM occurred_at)
              - EXTRACT('EPOCH' FROM prev_event_at) >= (60 * 10)
         OR prev_event_at IS NULL
       THEN 1 ELSE 0 END AS is_new_session
FROM (
  SELECT
     user_id
     ,event_type
     ,event_name
     ,occurred_at
     ,LAG(occurred_at, 1) OVER(PARTITION BY user_id ORDER BY occurred_at)   prev_event_at
  FROM tutorial.playbook_events e
) s
```



```sql
SELECT user_id,
   occurred_at,
   SUM(is_new_session) OVER (ORDER BY user_id, occurred_at) AS global_session_id,
   SUM(is_new_session) OVER (PARTITION BY user_id ORDER BY occurred_at) AS user_session_id
  FROM (
    SELECT *,
          CASE WHEN EXTRACT('EPOCH' FROM occurred_at) - EXTRACT('EPOCH' FROM last_event) >= (60 * 10) 
                 OR last_event IS NULL 
               THEN 1 ELSE 0 END AS is_new_session
     FROM (
          SELECT user_id,
                 occurred_at,
                 LAG(occurred_at,1) OVER (PARTITION BY user_id ORDER BY occurred_at) AS last_event
            FROM tutorial.playbook_events
          ) last
   ) final
LIMIT 100
```



Let's add session start time (`session_start_at`):

```sql
SELECT
   user_id,
   occurred_at,
   MIN(occurred_at) OVER(PARTITION BY user_id, user_session_id ORDER BY occurred_at) AS session_start_at,
   global_session_id,
   user_session_id
FROM(
    SELECT user_id,
       occurred_at,
       SUM(is_new_session) OVER (ORDER BY user_id, occurred_at) AS global_session_id,
       SUM(is_new_session) OVER (PARTITION BY user_id ORDER BY occurred_at) AS user_session_id
      FROM (
        SELECT *,
              CASE WHEN EXTRACT('EPOCH' FROM occurred_at) - EXTRACT('EPOCH' FROM last_event) >= (60 * 10) 
                     OR last_event IS NULL 
                   THEN 1 ELSE 0 END AS is_new_session
         FROM (
              SELECT user_id,
                     occurred_at,
                     LAG(occurred_at,1) OVER (PARTITION BY user_id ORDER BY occurred_at) AS last_event
                FROM playbook_events
              ) last
       ) final
) with_start
    LIMIT 100
```



## SparkSQL

```sql
SELECT user_id,
       occurred_at,
       unix_timestamp(occurred_at, "yyyy/MM/dd HH:mm:ss") occured_at_epoch,
       unix_timestamp(LAG(occurred_at,1) 
              OVER(PARTITION BY user_id ORDER BY occurred_at),
              "yyyy/MM/dd HH:mm:ss") AS prev_event_ts
  FROM playbook_events
```



Marking beginning of sessions:

```sql
SELECT *,
      CASE WHEN event_ts - prev_event_ts >= (60 * 10) 
             OR prev_event_ts IS NULL 
           THEN 1 ELSE 0 END AS is_new_session
FROM (
    SELECT user_id,
           occurred_at,
           unix_timestamp(occurred_at, "yyyy/MM/dd HH:mm:ss") event_ts,
           unix_timestamp(LAG(occurred_at,1) 
                  OVER(PARTITION BY user_id ORDER BY occurred_at),
                  "yyyy/MM/dd HH:mm:ss") AS prev_event_ts
      FROM playbook_events
) last
```



Assigning global and user session id:



