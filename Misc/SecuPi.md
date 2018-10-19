# SecuPi

2018-16-10

* Use Case 1: Data is stored encrypted (field/record level) with re-identification (decription) to authorized users.
* Use Case 2: Data is stored un-encrypted, but is tokenized/encrypted/masked to unathorized users.

FPE - Format Preserving Encryption-->

LDPA/AD/Kerberos is supported.

NiFi - JSON processor, CSV processor

* How about the collisions on the encrypted values?
  * No collisions. AES encryption.
* Define policy, e.g. "Drivers Policy" - comma separated list of policies.
* Rules can be defined for stages of access. (e.g. can alter SQL, re-identify)



**Scalability** (query, encrypt, NiFi)

Horizontal scaling , 500 transactions per server (metadata exchange - audit, config)

**Requirements for Setup**

Min 3 servers for prod. Use docker. No internet connection required. Initial install is better connected to Internet. Use Linux (RedHat - Docker Enterprise recommended, CentOS - Docker CE).

Worst case - works without docker.

What we need to audit.

**Story:** Unprivileged user sees encrypted key and asks privileged user for support. How the privileged user can query it. - *only through workarounds*



Deleted list is maintained through UI or REST API. (SmartLists) Uses Bloom Filters, distributed across cluster (agents) to determine non-presence (not deleted).

Adding noise is possible - makes data masking irreversible.



Partial column encryption? - can trim parts of the field (left or right)

If we query with SparkSQL?



