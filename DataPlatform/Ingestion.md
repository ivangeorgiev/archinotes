---
typora-copy-images-to: .assets
typora-root-url: .assets
---

# Data Ingestion

In this chapter we will discuss how the data is retrieved from the outside world into the Strategic Data Platform. The process of retrieval, referred also as ingestion is implemented by multiple ingestion data flows. Each ingestion flow is responsible for delivering a single source data set.

* *Ingestion Flows* section discusses the ingestion processes in high level.
* At the core of the ingestion is NiFi. *NiFi Ingestion Flows* section discusses outline for building ingestion data flows for SDP in NiFi.
* *Guidelines and Best Practices* provides naming conventions, rules and advices, applicable to the data ingestion.



## Ingestion Flows

Ingestion from a source could be split into a series of activities. Each activity is executed as a series of steps.

![Ingestion Flow Activities and Steps](/DataPlatform-Ingestion-IngestionOverview.png)

### Transport Activity

Data is retrieved from the remote system over the network. It is available from the source systems in variety of transport protocols, formats, velocity etc. 

The focus of the initial version of SDP and AM application is the archive storage of the legacy application. For that reason we will focus on HDFS as a source system type.

### Acquire Activity

The acquisition activity follows the Capture-Distribute-Fetch pattern.

Data is extracted from the remote system incrementally. First changes since the last extract are captured. 

Not all the files received in the list of changes are valid. For that reason quality validation rules are applied to captured changes.  During the validation files are also tagged by attaching some attributes:

* flow identifier
* extract date
* dataset identifier, e.g. country code.

There are many use cases when it is important to be able to extract only changes between two specific moments - `ExtractStartDate` and `ExtractEndDate`. For example correction extracts. 

To avoid duplicates and the need for global synchronization mechanism, all initial steps (Capture Changes, Validate Changelog, Filter Changes in Scope) are performed at the head node. The resulting list presents work log, which can be distributed across the cluster and executed by multiple instances in parallel. 

Workers fetch the data from the changelog and complete the Acquisition activity.

### Manage Activity

The purpose of this activity is to apply sensitivity management rules (anonymization) and make the data ready for further processing by the curation flows. Many steps need to be performed. Not all of these steps are applicable to any source dataset.

* Data needs to be decompressed.
* Archives need to be unpacked.
* Standard storage data formats: XML, JSON, CSV.
* Small data items need to be combined for optimal storage and processing in Hadoop.
* In some cases data needs to be re-identified before proper anonymization is applied.
* Data for archival needs to be packaged and compressed.
* Files for store need to be tagged with proper filename and storage location.

### Store Activity

Data is stored in two ways - raw and archived. Raw data is short-living, it is placed in the Inbox to be consumed by the curation flows. Archived data is stored for extended periods, managed by expiration policies.

## NiFi Ingestion Flows

NiFi is the central component of the Data Gateway. All the data flows into the SDP through NiFi. In this section we will discuss reference NiFi data flow design for SDP.

### Reference Design for NiFi Ingestion Flows

The NiFi ingestion flow for a data source which implements the ingestion process as discussed in the previous section follows the capture-distribute-fetch pattern. It addresses some NiFi limitations, while allowing for greater flexibility, good resource utilization and scalability.

![NiFi Ingestion Flow Outline](/DataPlatform-Ingestion-NiFiIngestion.png)

* Create Changelog activity captures, validates and filters the changes from the source system. Flow files are also tagged with source and stream specific information. 
* The changelog is distributed among the nodes in the cluster, using Remote Process Group.
* Changelog is received by an Input Port. Typically different streams from the same data source, e.g. country streams can be combined and processed by same flow. This is enabled by proper tagging during the Create Changelog. step.
* Data from the list is fetched from the Source System.
* Data is managed, e.g. decompress, unpack, concatenate, anonymize etc.
* To store the data, additional management is needed, e.g. pack, compress, add target folder names, etc.
* Files are stored to the Landing Zone - Inbox and Archive.

For more detailed discussion on each activity, see Data Source Ingestion Implementation Guide. AMS-LOG (AMS Horizon) is a good example to study.



### Capture Changelog

The first activity in the dataset ingestion flow is the  `Capture Changelog` activity. It is executed on the head node. 

![Capture Changelog Ingestion Activity](/DataPlatform-Ingestion-CaptureChangelog.png)

A list of all changes since the last session is requested from the source system. A list of empty FlowFiles is produced, each FlowFile has only attributes, e.g. `filename`. Quality management rules are applied to validate the FlowFiles. Most often `filename` attribute is validated against expected pattern. 

Valid FlowFiles are tagged with additional attributes and are passed to next ingestion activities.



#### List Changed Files

To capture the changes on the remote system, ListHDFS processor following attributes must be set.

| Property                       | Setting Description                                          |
| ------------------------------ | ------------------------------------------------------------ |
| Hadoop Configuration Resources | Use expression language to define the configuration files, e.g. `${BDAHadoopConfig}` |
| Kerberos Principal             | `${BDAKerberosKeytab}`                                       |
| Kerberos Keytab                | `${BDAKerberosKeytab}`                                       |
| Directory                      | Use expression language to define the directory using combination of variables, e.g. `${BDARoot}/${dirToList}`. Variable `dirToList` is specific to the dataset and is defined within local scope. |







#### Validate Filenames



#### Tag FlowFiles

Following tagging attributes are required:

| Attribute Name | Description                                                  |
| -------------- | ------------------------------------------------------------ |
| ex_dt          | Extract date. Used to limit the extract against a date range, group files for archive, etc. |
| fl_id          | Flow ID. Follows the convention `IN<sourceId><datasetId>`. Used for flow dispatching, logging, grouping, etc. |

Some ingestion flows might require additional attributes.



### Data Acquisition Activity

Data acquisition is specific to the type of the source system, e.g. SFTP, RDBMS, HDFS, Kafka etc.

* Capture Source Changes - monitor the source system for changes and capture the changes.
* Filter Relevant Changes - usually not all the changes captured at the previous step are relevant to the particular data flow. Changes are validated and filtered for relevance, before the actual data retrieval is initiated.
* Retrieve data - data is transmitted from the source system into NiFi for further processing by the ingestion flow.

### Manage FlowFiles Activity

In the real world, some minor data management might be required before data is stored inside the Data Platform. Examples of FlowFile management:

* Unzip, untar, etc.
* Anonymize
* Convert format, e.g. CSV to Avro
* Merge small files
* Package and compress files for archive
* Set target folder for landing and archive

### Store FlowFiles Activity

To store data into the Data Platform, two centralized services are provided. Data which is a subject of further processing is stored into the Landing Zone by the StoreIngestedFiles service (Process Group). The archive data is directed to the ArchiveIngestedFiles service. is a centralized service. 

#### StoreIngestedFiles PG

This service can store the FlowFiles into two targets - Azure Blob and HDFS. Any of these targets can be switched on or off via NiFi variables `${IsStoreToAzureBlob}` and `${IsStoreToHDFS}`.

Target location for Azure Blob is `${LandingZoneAzure}/${StoreToDirectory}/${filename}` 

Target location for HDFS is `${LandingZoneHDFS}/${StoreToDirectory}/${filename}`

Existing files are being replaced.

| Parameter          | Type               | Description                                                  |
| ------------------ | ------------------ | ------------------------------------------------------------ |
| IsStoreToAzureBlob | Variable           | If set to `true` files are stored to the target Azure Blob.  |
| IsStoreToHDFS      | Variable           | If set to `true` files are stored to the target Data Platform's HDFS. |
| LandingZoneAzure   | Variable           | Root folder for storing files on Data Platform's Azure Blob. |
| LandingZoneHDFS    | Variable           | Root folder for storing files on the Data Platform's HDFS.   |
| StoreToDirectory   | FlowFile Atrribute | Defines the target folder for storing the file. Set during the Manage activity. |
| filename           | FlowFile Attribute | This is a standard NiFi FlowFile attribute. Defines the file name. |

 

#### ArchiveIngestedFiles PG

This service can store the FlowFiles into two targets - Azure Blob and HDFS. Any of these targets can be switched on or off via NiFi variables `${IsStoreToAzureBlob}` and `${IsStoreToHDFS}`.

Target location for Azure Blob is `${ArchiveAzure}/${ArchiveToDirectory}/${filename}` 

Target location for HDFS is `${ArchiveHDFS}/${ArchiveToDirectory}/${filename}`

Existing files are being replaced.

| Parameter            | Type               | Description                                                  |
| -------------------- | ------------------ | ------------------------------------------------------------ |
| IsArchiveToAzureBlob | Variable           | If set to `true` files are stored to the target Azure Blob.  |
| IsArchiveToHDFS      | Variable           | If set to `true` files are stored to the target Data Platform's HDFS. |
| ArchiveAzure         | Variable           | Root folder for archiving files on Data Platform's Azure Blob. |
| ArchiveHDFS          | Variable           | Root folder for archiving files on Data Platform's HDFS.     |
| ArchiveToDirectory   | FlowFile Atrribute | Defines the target folder for archiving the file. Set during the Manage activity. |
| filename             | FlowFile Attribute | This is a standard NiFi FlowFile attribute. Defines the file name. |

 

## Examples

### Acquire files from HDFS

![](/1540349863078.png)

Change capture is performed by the ListHDFS processor, named ListCustomer. For proper load balancing, the list of captured files is passed to a Report Process group, connected to the ListCustomer input port.

The actual file retrieval is handled by the FetchHDFS processor, named FetchCustomer.

### Manage Archived (tar) and Compressed Files

[TODO]



## NiFi Ingestion Flow Design Principles

### Prevent Data Loss

Common design error is to ignore error conditions and terminate the error links. This causes data loss and produces data flows which are very difficult to debug and maintain. To prevent such situations, all the errors need to be handled. One approach is to direct error links to a disabled terminal processor, e.g. DebugFlow processor. The  terminal processor name follows naming convention which allows for automated monitoring and management:

`DPEH_<severity>_<step identifier>`

Where severity follows the Log4j logging levels: `ERROR`, `WARN`, `INFO` or `DEBUG` . Severity of `INFO` or `DEBUG` should be used in rear, very specific situations.

The prefix `DPEH_` is used to easily search for the terminal processors by monitoring automation.

### Distribute Processing

Many NiFi processors which interact with the outside world (boundary processors) are executed on the head node. If you do not take special care the downstream processors will be executed on the same node. The NiFi way for load balancing is to use the Remote Process Group. This principle is one of the fundamentals of the capture-distribute-fetch pattern, described in further chapters.

### Keep Remote Process Groups to a Minimum

Each Remote Process Group (RPG) consumes resources, e.g. runs in a process on the head node. Having a dedicated RPG per dataset or datasource ingestion flow looks good as a drawing, but brings some issues. First is the high resource utilization. 

Second issue is that flow designers are tend to place RPGs inside Process Groups for better modularity. This makes the overall NiFi flow very difficult to maintain. RPGs contain environment specific information hardcoded so they should be considered environment specific components and treated as such.

Having in mind above concerns proper consideration needs to take place when deciding on RPG use.



## Guidelines and Best Practices

### Version Control

All flows are stored in Bitbucket:

* Repository: `nifi-templates`
* Project: `SDP Gateway`

You can clone the repository using `git`:

```bash
git clone ssh://git@localhost:7999/sg/nifi-templates.git
```



### Handle Errors in NiFi Flows

A general principle in data processing is that no data should be discarded without justified reason. To handle errors in NiFi, we attach LogMessage and DebugFlow processors to the failure relationship of the all processors that present failure relationship. In the following example, we handle errors during file uncompress.

![Handle Errors in NiFi Flows](/DataPlatform-Ingestion-HandleErrors.png)

The LogMessage processor properties:

| Property Name | Value                                                        | Description                                |
| ------------- | ------------------------------------------------------------ | ------------------------------------------ |
| Log prefix    | #SDP#>                                                       | Used to identify log lines in the log.     |
| Log message   | `{"ev":"${fl_id}-UNCOMPRESS", "fn":"${filename}","path":"${path}"}` | Json, containing all relevant information. |

Set expiration time on the LogMessage processor connection.

The DebugFlow is disabled. It's purpose is to allow for corrective actions on the FlowFiles queued in the processor's connection. The debug connection also must have expiration time set.

**WARNING!** There is a bug in NiFi 1.7 LogMessage processor which might cause data loss if you connect the debug processor after the LogMessage processor.



### Connection Settings

Following are the typical settings for debug and log connections. Depending on the actual data flow characteristics, you might want to adjust more appropriate values.

* Log links are short living.
* Debug links provide  enough time for issue investigation, but they should put back pressure and block further processing in case of high error events frequency.

| Connection             | Setting                           | Value  |
| ---------------------- | --------------------------------- | ------ |
| Debug for errors       | FlowFile Expiration               | 1 week |
|                        | Back Pressure Object Threshold    | 100    |
|                        | Back Pressure Data Size Threshold | 200 MB |
| Debug for warning      | FlowFile Expiration               | 1 day  |
|                        | Back Pressure Object Threshold    | 100    |
|                        | Back Pressure Data Size Threshold | 200 MB |
| Log for errors/warning | FlowFile Expiration               | 2 min  |
|                        | Back Pressure Object Threshold    | 100    |
|                        | Back Pressure Data Size Threshold | 200 MB |
| Log for debug          | FlowFile Expiration               | 2 sec  |
|                        | Back Pressure Object Threshold    | 10000  |
|                        | Back Pressure Data Size Threshold | 2 GB   |



### Naming Conventions

Connections

* `con_<relationship_name>_<upstreap_processor_name>_<flow_id>`

  Applied only for error handling connections.
  Examples: 

  * `con_failure_UntarSourceArchive_AMSLOG`
  * `con_success_UntarSourceArchive_AMSLOG` 



Processors

* ` Log_<relationship_name>_<upstream_processor_name>_<flow_id>`
  For logging processors
* ` Debug_<relationship_name>_<upstream_processor_name>_<flow_id>`
  For debug processors, e.g. used for error handling.





### Single Responsibility

Keep ingestion for distinct datasets separate. As a general rule, avoid the temptation to combine multiple flows into a single flow.

### NiFi Process Group for Processing Activity

NiFi uses a single root flow, known as NiFi Flow. Placing all the processors for all the data flows at root level is not a good idea. Over time the process flow might become cluttered very difficult to maintain and monitor.

To organize NiFi Flow better, use Process Group. In addition, it enables proper version control through NiFi Registry. Process Groups can be nested.

Unfortunately there are some NiFi limitations that need to be taken in account:

*  Input ports for Remote Process Group must be at root level.

### Use Variables for Configuration

Instead of hard-coding configuration, use variables. Source-system specific variables can be defined a the enclosing Process Group. Global configuration is defined in global variables.

Unfortunately not all NiFi processor properties accept Expression Language.

### Load Balancing with Remote Process Group

Many operators are executed on the cluster's head without automatic load balancing. In these cases load balancing should be considered during the flow design.

Typical approach for load balancing is the List-Distribute-Fetch pattern:

* Capture changes on the remote system using a ListXXXX processor (e.g. ListHDFS).
* Distribute the list, using Remote Process Group
* Connect the Input Port which receives the list to a FetchXXXX processor (e.g. FetchHDFS)

Here is an example data flow, using this pattern:

![1540349863078](/1540349863078.png)



### Data Flow Processors Schedule Optimization

By default processors are scheduled to execute at 0 seconds.

For sources with scheduled delivery, e.g. daily, it is appropriate to use cron-based schedule.

For real-time or near-real time sources, more appropriate is to use time-based schedule. Tune the schedule interval so that it provides optimal balance between, NiFi resource utilization, throughput, latency and source system pressure.

### Keep Transformations To Minimum

NiFi is a data flow management tool, not a data processing framework. To maintain high throughput and avoid resource contention, push the processing to downstream processing. Downstream processing is supported by a variety of frameworks with Spark, Storm etc. Standard data processing tool for the Data Platform is Spark, managed by Yarn.

### Unit Test Custom Processors

All custom processors must be unit tested. The execution of the unit tests is a required step during the build of the processor package (nar-file). Usually, NiFi context is mocked up, using unit the capabilities, available in most unit test frameworks. Recommended language for custom processors is Java, but Scala is also acceptable.

### Unit Test Scripting

NiFi provides scripting capabilities through the ExecuteScript processor. Although a wide variety of languages are supported, preferred scripting language is ECMAScript (aka JavaScript) due to its popularity.

In the context of the NiFi Flow each ExecuteScript processor is considered unit. To enable testability the scripts must be placed in separate files, not embedded into the processor. For each script unit tests must be created.

[Framework for testing NiFi ExecuteScripts](https://github.com/mattyb149/nifi-script-tester) scripts for JavaScript was evaluated. Although the evaluation was done only by reviewing the repository and corresponding documentation, seems promising and provides enough features to enable unit testing. 

Additionally a [blog post](http://funnifi.blogspot.com/2016/06/testing-executescript-processor-scripts.html)  about the above mentioned framework was found.

### Document Data Flows

* Add description to processors and process groups - the descriptions are made available on mouse over - improves readability.
* Define appropriate expressive names for processors, process groups and ports.
* Names are defined using `CamelCase`. No spaces and special characters.
* Processor group names typically follow the pattern `<Verb><Noun>`
* Port names describe the FlowFiles which are passing through the port, e.g. `CustomerList` , `CustomerFiles`, `ZippedCustomer`, etc.

## Case Study: Outline for a CRM Ingestion Flow

In our example the data source is the CRM system. It contains three datasets:

* Customer
  * Location: `/public/crm/customer_<yyyy-mm-dd>.csv`
  * Format: CSV
* Vendor
  * Location: `/public/crm/vendor_yyyymmdd.json.gz`
  * Format: Gzip compressed JSON
* VendorContract
  * Location: `/public/crm/contract_vendorid_<yyyy-mm-dd>.json`
  * Format JSON



