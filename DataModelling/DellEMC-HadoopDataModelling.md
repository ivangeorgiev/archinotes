---
typora-copy-images-to: DellEMC-HadoopDataModelling
typora-root-url: ./
---

Source: https://infocus.dellemc.com/william_schmarzo/hadoop-data-modeling-lessons-vin-diesel/



# Hadoop Data Modeling Lessons – by Vin Diesel



By Bill Schmarzo

September 23, 2014



As my friends know…okay, my nerdy friends… there are 3 big data topics that give me pause.  These topics are critical from a standpoint of operationalizing big data, but I still have not gotten my head completely around them:

1. What skills, training, capabilities and attitudes does someone with a Business Intelligence/SQL/Statistics background need to learn in order to become a data scientist?
2. What is the economic value of data; that is, how do I price data that I might want to buy and/or sell?
3. In a world of “schema on query”, what tools, skills and design techniques does one need to simplify and standardize the ad hoc schema definition process in order to operationalize the resulting schema and analytic results?

Today’s blog is about that third topic. Which brings us to Xander Cage?  Who is Xander Cage?  One of the original Hadoop developers?  The code name for a super-secret big data project happening within EMC Global Services?  Nah, Xander Cage is a character played by Vin Diesel in the movie “[XXX](http://www.imdb.com/title/tt0295701/?ref_=nv_sr_1).”  He’s a bad-ass that’s out to save the world from a deadly virus.  A great B-rated shoot ’em up, blow ’em up movie.



Source: Columbia Pictures

There is a scene in the movie where one of the bad guys has the Prague police pinned down from a sniper’s position in a large industrial storage room.  Xander comes into the scene, surveys the situation, grabs a bazooka and utters:

*“*[*You have a bazooka! Dude, stop thinking Prague Police and start thinking Playstation. Blow [schtuff\] up*](https://www.youtube.com/watch?v=5fVtIY4rOlQ)*!!!”*

This quote is the ultimate in “**think differently**” messaging, which is exactly what we need to do when we think about data modeling in a big data world.

# History of Data Modeling By Bill Schmarzo

The world of data modeling (at least as it pertained to Bill Schmarzo) started with third normal form (3NF) and E. F. Codd.  E. F. Codd defined third normal form data modeling in 1971 as a database normalization technique to improve data base processing while minimizing storage costs. 3NF data modeling was ideal for online transaction processing (OLTP) applications with heavy order entry type of needs.

When I was coding in the early 1980’s, disk space and processing power were extremely expensive. 3NF was designed to minimize the amount of data that we needed to store by ensuring that we eliminated data redundancy in storage. Heck, the entire “Y2K” panic was caused by programmers like me hardcoding “19” into the date (year) field so that we could save two bytes each time that the “year” field was used in calculations or reporting. When we were writing programs in the 1980’s, no one dreamed that our programs would still be running 20+ years later (I wonder if anyone ever found all the Easter eggs that I buried in my code, he-he-he).

As a result, we ended up with OLTP data models that looked like Figure 1.

![img](/DellEMC-HadoopDataModelling/9-11-14-Bill-Figure-2.png)

**Figure 1: Third Normal Form Sample Data Model**

# Data Modeling For Business Users

While 3NF was ideal for machine processing, the spaghetti nature of the data model was uninterpretable by a human user.  The world of analysis (a.k.a. query, reporting, dashboards) required a different type of data model that supported analysis such as trend lines, period-to-date calculations (month-to-date, quarter-to-date, year-to-date), cumulative calculations, basic statistics (average, standard deviation, moving averages) and previous period comparisons (year ago, month ago, week ago).

Ralph Kimball, while at Metaphor Computer Systems, pioneered dimensional modeling, or star schemas, in the early 1980s (see Figure 2).  The dimensional model was designed to accommodate the analysis approach of the business users via two important design concepts:

- Fact tables (populated with metrics, measures or scores) that correspond to transactional systems such as orders, shipments, sales, returns, premiums, claims, accounts receivable, and accounts payable.  Facts are typically numeric values that can be aggregated (e.g., averaged, counted, summed).
- Dimension tables (populated with attributes about that dimension) that represent the “nouns” of that particular transactional system such as products, markets, stores, employees, customers, and different variations of time.  Dimensions are groups of hierarchies and descriptors that describe the facts.  It is these dimensional attributes that enable analytic exploration, attributes such as size, weight, location (street, city, state, zip), age, gender, tenure, etc.

![Dimensional Model (/DellEMC-HadoopDataModelling/9-11-14-Bill-Figure-3.png)](https://infocus.dellemc.com/wp-content/uploads/2014/09/9-11-14-Bill-Figure-3.png)

**Figure 2: Dimensional Model (Star Schema)**

Dimensional modeling was ideal for business users because it was designed with their analytic thinking in mind.  What do I mean by that?  For example, “**By”** analysis is a common way for capturing the reporting and analysis needs of the business users during the design phase of an analytic project.  The “By” analysis is consistent with the way the business users tend to frame their data requests such as:

- I want to see sales and returns **by** month, store, geography, product category, product type and customer segment
- I want to compare this month and last month’s claims **by** policy type, policy rider, zip code, city, state, region, customer tenure and customer age
- I want to see social sentiment scores trended **by** social media source, subject, and day of week

Today, all Business Intelligence (BI) tools use dimensional modeling as the standard way for interacting with the data warehouse.

# Data Modeling For Data Science

In the world of data science using Hadoop, we again need to *think differently* about how we do data modeling.  Hadoop was originally designed by Google and Yahoo to deal with very long, flat web logs (see Figure 3).  Heck, Google called it “Big Table[1]” since it was an uber large table, not a series of smaller tables tied together with joins – it was just designed differently.

![Sample Log file](/DellEMC-HadoopDataModelling/9-11-14-Bill-Figure-4.png)

**Figure 3: Sample Log File**

For example, Hadoop accesses data in very large blocks – the default block size is 64MB to 128MB versus relational database accesses block sizes are typically 32Kb or less.  To optimize this block size advantage, the data science team wants very long, flat records.

For example, some of our data scientists prefer to “flatten” a star schema by collapsing or integrating the dimensional tables that surround the fact table into a single, flat record in order to construct and execute more complex data queries without having to use joins (see Figure 4).

![Flattening the Star Schema for Data Science work on  Hadoop](/DellEMC-HadoopDataModelling/9-11-14-Bill-Figure-5.png)

**Figure 4: Flattening the Star Schema for Data Science Work on Hadoop**

Taking this to the next level, the data science team will create an uber long record for each different business functions that can be more easily analyzed using Hadoop (see Figure 5).

![Data Science Data Modeling on Hadoop](/DellEMC-HadoopDataModelling/9-11-14-Bill-Figure-6.png)

**Figure 5: Using Large Flat Files To Eliminate/Reduce Joins On Hadoop**

For example, we could have the following massively long records for an individual for whom we want to analyze:

- Customer demographics (age, gender, current and previous home addresses, value of current and previous home, history of marital status, kids and their ages and genders, current and previous income, etc.)
- Customer purchase history (annual purchases including items purchased, returns, prices paid, discounts, coupons, location, day of week, time of day, weather condition, temperatures)
- Customer social activities (entire history of social media posts, likes, shares, tweets, favorites, retweets, etc.)

One technique that Dr. Pedro Desouza, the head of our data science practice at EMC Global Services, uses in order to avoid too many and frequent joins is to replicate just the key dimensional attributes into the fact table. In this way, he keeps the clear representation of the star schema but eliminates the joins by performing the heavy lifting analysis just on the flat file. The tradeoff is a little bit of data redundancy to keep clarity, but it takes advantage of the Hadoop performance characteristics.

# What Does This Mean?

It means is that the way that we designed data models for OLTP applications (using third normal form) and for data warehousing (using dimensional modeling) needs to change to take advantage of the inherent architecture and processing advantages offered by Hadoop.  Data scientists will create flat data models that take advantage of the “big table” nature of Hadoop to handle massive volumes of raw, as-is data.

![9 11 14 Bill Image 2](/DellEMC-HadoopDataModelling/9-11-14-Bill-Image-2.png)

My friend (and data scientist) Wei Lin calls this data modeling transition from relational (third normal form) to dimensional (star schema) to universal “The Good, The Bad and The Ugly” (I always had a preference for “Angel Eyes” in that movie, if you know which character that is).  But maybe that’s the wrong title? Maybe the name should be “The Complex, The Elegant and The Simple” that reflects the changing nature of data modeling.

As Xander Cage says, we need to **think differently** about how we approach data modeling in a big data world using Hadoop.  Old data modeling techniques that may have been optimal in a world of constrained storage and processing power, are no longer the optimal data modeling techniques in a big data world.

We need to embrace the “**think differently**” mentality as we continue to explore and learn from this brave new world.  It may be a painful transition, but we made the successful transition from third normal form to dimensional modeling…eventually.

 

 

------

 

[1] In 2004 a Google in-house team of scientists built a new type of database called “Big Table”.  Big Table successfully broke through the barriers presented with a traditional RDBMS to handle massive volumes of semi-structured data (log files). Google scientists published two scholarly research papers on their project, the first describing the Google File System (GFS) and Google MapReduce.  Doug Cutting and Mike Cafarella, two independent developers, convinced Yahoo that this new structure was the solution to their search and indexing challenges, discovered that work.  By 2006 Yahoo had the first prototype called ‘Hadoop’ and in 2008 Yahoo announced the first commercial implementation of Hadoop.  Facebook was a fast follower and soon Twitter, eBay, and other major players were also adopting the technology. (<http://data-magnum.com/a-brief-history-of-big-data-technologies-from-sql-to-nosql-to-hadoop-and-beyond/>)