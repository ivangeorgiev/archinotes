# Data Source Ingestion Manual

This document focuses on the data sources ingested into the SDP and the flow implementation specifics.

## AMS Horizon (AMS-LOG)

We will use AMS Horizon (also referred as AMS-LOG) source as an example to illustrate the Reference NiFi Ingestion Flow.

The characteristics of the source are as follows:

| Characteristic        | Description                                                  |
| --------------------- | ------------------------------------------------------------ |
| Data Format           | XML                                                          |
| Archive               | Tar                                                          |
| Compression           | Gzip                                                         |
| Source Location       | `<root dir>/<stream>/<date partition>`<br />Example: `ams/raw/IE/21052018` |
| Streams               | CH<br />IE<br />NL                                           |
| Date Partition        | `DDMMYY`                                                     |
| Example archive name: | `AMS_F1_20180101153901259278000.tar.gz`                      |
| Example file name:    | `AMS_20180101153801173_2180065008_269653856.xml`<br />`AMS_20180101153801252_1715854536_269653857.xml` |
| PII Data Management   | CardId -> Hash(CardId)                                       |

The NiFi ingestion flow for AMSLOG. All the Ingestion happens inside a Process Group AMSLOG_Ingest. The output 

![AMS Horizon (AMSLOG) ingestion flow in NiFi context](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_01_Context.PNG)

The AMSLOG flow is connected to global "services" - `StoreFilesToInbox`, `StoreFilesForArchive` and `NiFi Flow` Remote Process Group. These services are shared with other sources. The funnels are used for better visual organization. All ingestion activities for AMSLOG are grouped into a process group `AMSLOG_Ingest`.

The input port `in_AMSLOG_IngestList` is an endpoint for the `NiFi Flow`  Remote Process Group. It receives a distributed list of files to ingest.

The AMSLOG top level activities follow the Reference NiFi Ingestion Flow Design for SDP.

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_02_Ingest.PNG)

The flow has one input and three output ports:

* `out_IngestList` - the list of files to be ingested. This port is connected to the Remote Process Group. The list is than distributed and delivered back to the input port `in_IngestList`.
* `in_IngestList` - a list of files to be ingested. The input is connected to a root level input port, receiving the list from the Remote Process Group.
* `out_FilesToInbox` - ingested flow files to be stored in the Inbox area. Connects to global Inbox area store service.
* `out_FilesToArchive` - ingested flow files to be stored in the Archive area. Connects to global Archive area store service.

`AMSLOG_CreateIngestList`  Process Group is responsible for capturing, validating and filtering changed files. It produces a list of files to be distributed and ingested. The Process Group is versioned and has multiple instances - one for each country. All the instances are connected to the `out_IngestList`  output port.

`AMSLOG_FetchFiles` Process Group is responsible for acquiring files from the change list, received by the `in_IngestList`  input. It is versioned. The flow files are tagged by the `AMSLOG_CreateIngestList`  and downstream activities are parameterized to use the flow file attributes. This makes it possible to use a single downstream instance, serving all the country streams.

`AMSLOG_ManageData` Process Group is decompresses, unarchives, anonymizes and further combines, archives and compresses flow files for storing into Archive or Inbox.

### AMSLOG_CreateIngestList

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_03_CreateIngestLIst.PNG)





![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_03_02_ValidateAndFilter.PNG)



### AMSLOG_FetchFiles

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_04_FetchFiles.PNG)





### AMSLOG_ManageData

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_05_ManageData.PNG)



#### AMSLOG_Anonymize

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_05_02_Anonymize.PNG)





#### AMSLOG_ManageForStore

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_05_03_ManageForStore.PNG)



##### ConcatenateXMLFiles

![](C:\Sandbox\docs\archinotes\DataPlatform\.assets\DataSourceIngestionManual_AMSLOG_05_04_ConcatenateXMLFiles.PNG)



Warning: #ffa500

Error: #ff4500

Info: #7fffd4

Debug: #add8e6



