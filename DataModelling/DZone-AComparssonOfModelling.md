---
typora-copy-images-to: DZone-AComparssonOfModelling
typora-root-url: ./
---

Source: https://dzone.com/articles/a-comparison-of-data-modeling-methods-for-big-data



# A Comparison of Data Modeling Methods for Big Data

### The explosive growth of the Internet, smart devices, and other forms of information technology in the DT era has seen data growing at an equally impressive rate.



The explosive growth of the Internet, smart devices, and other forms of information technology in the DT era has seen data growing at an equally impressive rate. The challenge of the era, it seems, is how to classify, organize, and store all of this data.

## Why Is Data Modeling Necessary?

In a library, we need to classify all books and arrange them on shelves to make sure we can easily access every book. Similarly, if we have massive amounts of data, we need a system or a method to keep everything in order. The process of sorting and storing data is called "data modeling."

A data model is a method by which we can organize and store data. Just as the Dewey Decimal System organizes the books in a library, a data model helps us arrange data according to service, access, and use. Torvalds, the founder of Linux, alluded to the importance of data modeling when he wrote an article on “what makes an excellent programmer”: “Poor programmers care about code, and good programmers care about the data structure and the relationships between data.” Appropriate models and storage environments offer the following benefits to big data:

• **Performance:** Good data models can help us quickly query the required data and reduce I/O throughput.
• **Cost:** Good data models can significantly reduce unnecessary data redundancy, reuse computing results, and reduce the storage and computing costs for the big data system.
• **Efficiency:** Good data models can greatly improve user experience and increase the efficiency of data utilization.
• **Quality:** Good data models make data statistics more consistent and reduce the possibility of computing errors.

Therefore, it is without question that a big data system requires high-quality data modeling methods for organizing and storing data, allowing us to reach the optimal balance of performance, cost, efficiency, and quality.

## Relational Database Systems and Data Warehouse

E. F. Codd is the originator of relational databases. He first proposed the relational model of database systems and began researching relational methodology and relational data theories. Almost every modern company has come to use relational databases to store and process data. This comes as a result of the rise of an entire generation of data software like Oracle, Informix, and DB2. The data warehouse system is hardly an exception. Many data warehouse systems store and process data by leveraging the strengths of relational databases, and even employ data models that employ the same theory.

Despite the recent rapid growth of storage and computing infrastructure for Big Data as well as the growing popularity of NoSQL technology, Hadoop, Spark, and Alibaba Cloud’s MaxCompute still use SQL for large-scale data processing. Data gets stored in tables, and relational theory is used to describe the relationships between data. However, there are different options in the form of relational data models based on how you access the data.

## Modeling Methodology for OLTP and OLAP Systems

The main data operation in the OLTP system is random read/write. The OLTP system mainly employs entity-relationship models that meet 3NF to store data to solve data redundancy problems and inconsistency in transaction processing. The main data operation in the OLAP system is batch read/write. The OLAP system focuses on data integration and performance of one-off, complex big data queries, and processing instead of inconsistency in transaction processing. Therefore, the OLAP system needs to use different data modeling methods.

## Typical Data Warehouse Modeling Methodology

### ER Model

The modeling method proposed by Bill Inmon, father of data warehousing, is to design a 3NF model encompassing the whole company and describe enterprise business through an entity-relationship (ER) model. It complies with 3NF in the normal form. The difference between 3NF in the data warehouse and 3NF in the OLTP system is that 3NF is abstraction targeting themes from the perspective of the company rather than abstractly targeting entity objects in a specific business process. 3NF has the following features:

• We need to understand the company's business and data comprehensively.
• Implementation can be quite slow.
• It requires significant skill on the part of the modeler.

To create data warehouse models by using ER modeling, we first need to integrate and combine the data in various systems thematically and from the perspective of the entire enterprise. We then need to process the data for consistency to enable analysis and decision making based on the data. The data itself, however, cannot be used directly for analysis or decision making.

This type of modeling has three phases:

• **High-level modeling:** A highly abstract model, which mainly describes the relationship between themes. We can use this model to describe an overview of the company.
• **Middle-level modeling:** Details data and themes based on the high-level model.
• **Physical modeling (also called the underlying model):** Operates by the middle-level model to design physical characteristics according to physical storage availability, performance, and platform characteristics. It can also involve combining tables or designing partitions.

In practice, the most typical representation of ER models is the Financial Services Logical Data Model (FS-LDM) released by Teradata based on financial services. Through high abstraction and summarization, the FS-LDM divides financial services into ten themes to design financial warehouse models. With the FS-LDM, companies can adjust and expand for quick modeling implementation.

### Dimensional Modeling

Ralph Kimball, a master in data warehousing, introduced Dimensional Modeling to the world. His book ‘The Data Warehouse Toolkit — The Complete Guide to Dimensional Modeling" is a classic of data warehouse modeling in the data warehouse engineering field. Dimensional Modeling tackles the issue of analytical decision making and requirement analysis. Therefore, it focuses primarily on allowing the user to quickly complete requirement analysis while maintaining high performance when handling large and complex queries. Typical examples of Dimensional Modeling are the star model and the snowflake model. The design of the models includes following steps:
• **Select an application involving analysis and decision-making.** The application could be a single business event, for example, issuing a refund, the state of an event, for example, the balance of an account or a process containing a series of inter-related events. This depends mainly on if we're looking at a series of events, a current state, or the efficiency of a flow of events.

• **Select a granularity.** In event analysis, we need to determine the degree of sub-division for all analysis in advance to decide the granularity. Granularity is a combination of dimensions.

• **Identify a dimension table.** After selecting a granularity, design the dimension table based on the granularity, including dimension attributes. This table serves grouping and screening during analysis.

• **Select facts.** Determine which indicators you have to measure during the analysis.

### Data Vault Model

Dan Linstedt used ER model as a base to create the Data Vault Model. The model’s design is useful for integrating data, but one cannot use it directly for data analysis and decision making.

The model emphasizes establishment of an auditable basic data layer focusing on data history, traceability, and atomicity. It does not require excessive consistency processing or integration. Meanwhile, it organizes company data in a thematic, structured manner and introduces further normal form processing to optimize the model to properly scale with changes to the source system. The Data Vault Model consists of the following:

• **Hub:** A hub is the company's core business entity. It consists of entity keys, data warehouse serial surrogate keys, loading time, and data sources.

• **Link:** Links indicate the relationship between hubs. The greatest difference between the Data Vault Model and an ER Model is that a relationship is abstracted as an independent unit, which improves the scalability of the model. A link can directly describe the relationship between 1:1, 1:n and n:n without making any changes. It consists of hub surrogate keys, loading time, and data sources.

• **Satellite:** A satellite includes detailed descriptions of hubs. One hub may have multiple satellites. A satellite consists of hub surrogate keys, loading time, source types, and detailed hub descriptions.

It is easier to design and create a Data Vault Model than an ER Model, and ETL processing of the Data Vault Model is configurable. Dan Linstedt gave us the following analogy to better understand the concept of a Data Vault: Imagine a hub as the skeleton of a human adult, links as ligaments that connect the skeleton, and satellites as flesh and blood.

### Anchor Model

The Anchor Model further normalizes the data vault model. The initial intention of Lars Rönnbäck was to design a highly scalable model. His core concept is that all expansion involves adding rather than modifying. Therefore, he normalized the model to 6NF, and it becomes a K-V structural model. The Anchor Model consists of the following:

• **Anchors:** Anchors are similar to Hubs in the Data Vault Model. They stand for business entities and have only primary keys.
• **Attributes:** Attributes are similar to satellites in the Data Vault Model but are more normalized. They are in the K-V structure. Each table describes attributes of only one anchor.
• **Ties:** Ties indicate the relationship between Anchors and get described using a table. Ties are similar to links in the Data Vault Model and can improve the general model expansion capability.
• **Knots:** Knots stand for the attributes that may be shared by multiple anchors, for example, enumerated and public attributes such as gender and state.
We can further sub-divide these four basic objects into historical and non-historical objects, where historical objects record changes in the data using timestamps and keeping multiple records.

This division allowed the author of the Anchor Model to achieve high scalability. However, this model also increases the number of join query operations. The creator believes that analysis and query in a data warehouse are performed only based on a small section of fields. This is similar to the array storage structure, which can significantly reduce data scanning and reduce the impact on query performance. Some databases with the table elimination feature, for example, MariaDB, can greatly reduce the number of join operations. However, this is still open to discussion.

## Overview of Alibaba Group's Big Data Modeling

Alibaba Group has always considered Big Data as its strategic goal since early on in its development. In fact, Alibaba's business relies heavily on Big Data for operational support, so what method does Alibaba use to create data warehouse models? Alibaba's data warehouse modeling has undergone several stages of development.

**Stage 1:** Back when business was completely application-driven, Alibaba built its first data warehouse system on top of Oracle. The purpose of the data at the time was to generate reports. Administrators synchronized data to the Oracle database (also called the ODS layer) in a model identical to the source structure. Based on this ODS data, data engineers where then able to collect and process statistical data. There was essentially no method of systematic modeling. The general practice was to store and process data entirely through the Oracle database. However, for processing some of the historical data, administrators used modeling methods as it would change slowly based on a few dimensions. In this phase, the data architecture had only two layers, namely ODS and DSS.

**Stage 2:** As Alibaba's business grew, the amount of data also increased sharply. At this point, performance became a major consideration. In response, Alibaba introduced Greenplum to the MPP architecture system. Meanwhile, the Alibaba data team also began to optimize the data architecture to a certain extent, hoping to alter the "chimney" development model by changing techniques, eliminating redundancies, and improving data consistency. Data warehousing engineers in traditional industries attempted to apply ER models and dimensional modeling popular in the engineering field to Alibaba Group and construct a four-layer model architecture. These layers included the Operation Data Layer (ODL), Basic Data Layer (BDL), Interface Data Layer (IDL), and Application Data Layer (ADL).

The ODL was consistent with the source system, while the BDL expected to introduce ER models, strengthen data integration, and create consistent basic data models. The IDL, in turn, constituted the mart layer based on the dimensional modeling method, and the ADL completed personalized data assembly based on presentation requirements.

During this period, we were met with a great number of difficulties and challenges as we attempted to create effective ER models. Because internet businesses develop rapidly, staff turnover rates tend to be high, and professional know-how is rarely comprehensive - we were unable to complete our ER models. This, however, was a learning process. We came to understand that creating ER models is risky and inappropriate for an immature, rapidly changing company.

**Stage 3:** Alibaba Group's business, and consequently data, continue to grow, and distributed storage computing platforms like Hadoop are also developing at a break-neck pace. Meanwhile, the development of Alibaba Group's own distributed computing platform, MaxCompute, is also ongoing. While developing our distributed computing platform, we also began to construct our third-generation model architecture. We needed to find a data modeling method that would be sustainable as Alibaba's business grows, and would be able to take full advantage of a distributed computing platform. We decided on a modeling method that uses Kimball's dimensional method as a base, expands on the core methodology, and established a public layer modeling data architecture system.

The purpose of constructing a public data layer is to solve the problem of sharing data storage and computing. Alibaba Group has now grown to have multiple business units (BU), which generate a huge amount of data that grows nearly 2.5 times every year. This rate of expansion far exceeds the growth of the business itself and brings with it worrying increases in costs and expenditure.

The construction of Alibaba's public layer is guided by a unified system of data management and integration methods (internally called "OneData"), which includes a system of consistent indicator definition, a model design system, and auxiliary tools.


  