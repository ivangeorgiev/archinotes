# Data Source Ingestion Manual

This document focuses on the data sources ingested into the SDP and the flow implementation specifics.

## Source Systems

### BDA HDFS

| Characteristic                     | Value / Description                              |
| ---------------------------------- | ------------------------------------------------ |
| Root data folder<br />`<src_root>` | `/user/group/videorep/audience_measurement/data` |



### BDA Hive

TODO: Is this in scope?



## AMS Horizon (AMS-LOG)

### Introduction

The AMS source files from Horizon boxes are acquired and ingested from a staging server. AMS Horizon is curated daily. Actual log files are archived by packaging using TAR format and further compressed using GZIP format. Each archive contains XML files. XML file contains log of events, e.g. channel change, from a single set top box for a time interval.

**From Legacy Handover:** Daily 95 (covering 15 minute intervals) or 48 (covering 30 minute intervals) files are delivered, depending on the country (15 for NL and CH, 30 min for IE). For example, the file amslogs-1945.gz should contain all of the event files created between 19:30 and 19:45. The true event times, recorded in the XML files, do not match up with the times on the file names. For instance, events contained in the 19:45 file may be from well before 19:30.

The information which follows is based on **Legacy BDA System** as a source for AMS Horizon. In BDA multiple XML messages are packaged (tar) and compressed (gzip) into a single archive with a near real time frequency.

### Source Characteristics (BDA HDFS)

The characteristics of the source are as follows:

| Characteristic                         | Description                                                  |
| -------------------------------------- | ------------------------------------------------------------ |
| Data Format                            | Messages in XML format                                       |
| Packaging                              | Tar                                                          |
| Compression                            | Gzip                                                         |
| Streams<br />`<stream_id>`             | By country: CH, IE, NL                                       |
| Source Location                        | `<bda_root>/ams/raw/<stream_id>/<extract_dt_dir>`<br />Example: `ams/raw/IE/21052018` |
| Date Partition<br />`<extract_dt_dir>` | Each extract date is stored into a separate directory: `DDMMYY`<br />Message date can be found in three places: directory, archive name and file name. They not necessarily match, e.g. directory for 20180818 contains archives  with 20180817 (after 21:44 on 20180817), archives for 20180818 might contain files with 20180817 (very close to midnight 20180817) |
| Date Partition Sample Statistics       | For 20180818:<br />Compressed size: 370 MB<br />Uncompressed size: 6.2 GB<br />Compression: 0.059 (370 MB / 6.2 GB)<br />Number of archives: 2,706 archives<br />Archive size, compressed: 136 KB (370 MB / 2700)<br />Number of XML files: 4,982,159<br />XML files per archive: 1840<br />XML file size: 2.5 KB |
| Monthly Estimate                       | Number of days: 25<br />Number of archives: 70,000 (25 x 2800)<br />Number of XML files: 126,000,000 (70,000 x 1,800)<br />Total compressed size: 9.8 GB (70,000 x 140 KB)<br />Total uncompressed size: 166 GB (9.8 / 0.059) |
| Total Number of Archives               | November 7, 2018: 2,400,000                                  |
| Archive file naming:                   | `AMS_F1_20180101153901259278000.tar.gz`                      |
| XML file name:                         | `AMS_20180101153801173_2180065008_269653856.xml`<br />`AMS_20180101153801252_1715854536_269653857.xml` |
| PII Data Management                    | CardId -> Hash(CardId)<br />For up to date requirements, refer to [Confluence](https://globe.upc.biz/confluence/display/PEDP/PII+Data). |



### Create Ingest List (Change Capture)

#### List Changed Files

List files in the stream's root directory.

#### Validate Change List

| Attribute     | Rule                                |
| ------------- | ----------------------------------- |
| `${filename}` | Regex match: `AMS_F1_\d{23}.tar.gz` |
| `${path}`     | Regex match: `.*/\d{8}`             |



#### Tag Flow Files

| Attribute | Value                                                      | Description                                                  |
| --------- | ---------------------------------------------------------- | ------------------------------------------------------------ |
| `an_dt`   | `substring(filename, 7,15)`                                | `archivename_date`- Date from the archive name.              |
| `c_c`     | CountryCode                                                | `country_code`- Country code, identifying the stream. Process Group variable. |
| `ex_dt`   | `regexReplace(path, '.*(\d\d)(\d\d)(\d\d\d\d)', '$3$2$1')` | `extract_date` - Date from the directory name.               |
| `ex_ux`   | `ex_dt.toUnix()`                                           | `extract_unixtimestamp` - Date from the directory name as unix timestamp. |
| `fl_id`   | SdpFlowId                                                  | `flow_id` - Flow identifier. Process Group variable, e.g. AMSLOGIE. |



#### Filter Files in Scope

| Condition      | Expression                                              | Description                                                  |
| -------------- | ------------------------------------------------------- | ------------------------------------------------------------ |
| `FilesInScope` | `ex_ux BETWEEN StartDate.toUnix() AND EndDate.toUnix()` | Extract date must be in scope as defined by global variables. |



### Data Management

To improve efficiency, small XML files, received by the source system are combined into bigger XML files of maximum size 100 MB.  XML files are grouped using:

* flow ID (`fl_id` attribute)
* extract date (`ex_dt` attribute) - from directory name.
* archivename date (`an_dt`  attribute) - from source archive name.
* filename date (`fn_dt` - new attribute attached during management) - from source XML file name.

**Inbox:**

* Filename for concatenated XML file for Inbox:
  `AMS_<ex_dt>_<an_dt>_<fn_dt>_<uuid>.xml`



**Archive:**

* XML files are packaged as tar archive with maximum size of 1 GB
* Tar archive is compressed using Gzip
* Filename for compressed archive:
  `AMS_<c_c>_<ex_dt>_<an_dt>_<fn_dt>_<uuid>.tar.gz`




