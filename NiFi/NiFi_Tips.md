# NiFi Tips



## Avoid Merging and Splitting Huge Amount of Files

Processors like mergeContent, SplitContent, SplitText, etc can use a lot of heap depending on the incoming FlowFile(s) and configuration. For example a mergeContent configured to merge 100,000 FlowFiles is going to use a lot of heap bining that many FlowFiles. A better approach is to use to mergeContent processor in a row with the first merging 10,000 and the second merging bundles of 10 again to create the 100,000 desired end result. Same goes for SplitText. If your source FlowFile results in excess of 10,000 splits, try using two SplitText processors (First splitting by every 10,000 lines and the second splitting those by every line.) With either example above you are reducing he amount of FlowFiles held in heap memory at any given time.



## Performance Tuning

### Increase Parallelism

For computation intensive processors, you might increase the number of processes. This will improve the throughput. However, using too high number processes might have huge negative impact.



