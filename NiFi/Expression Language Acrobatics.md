Expression Language Acrobatics



| Scope     | Name | Value              |
| --------- | ---- | ------------------ |
| Variable  | Attr | path               |
| Variable  | Expr | ${path}            |
| Attribute | path | /path/to/somewhere |
|           |      |                    |



|                                                              | Expression                                                   | Value                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ | -------------------------- |
|                                                              | ${${Attr}}                                                   | /path/to/somewhere         |
|                                                              | ${${Attr}:substring(0,2)}                                    | /p                         |
| Expr=${path}                                                 | ${${Expr}}                                                   | <!--\<Empty string set>--> |
| Expr=${path}                                                 | ${literal(${Expr})}                                          | ${path}                    |
| Expr=${path}                                                 | ${${Expr}:substring(0,2)}                                    | <!--\<Empty string set>--> |
| Expr=path:substring(0,2)                                     | ${${Expr}}                                                   | <!--\<Empty string set>--> |
| ExtractDateAttr=filename<br />ExtractDateMatch=CAS_SUB([0-9]{8}).*<br />ExtractDateReplace=$1<br />filename=CAS_SUB20180103010002.xml | ${${ExtractDateAttr}:replaceAll(${ExtractDateMatch}, ${ExtractDateReplace})} | 20180103                   |
| ExtractDateMatch=`^[^0-9]*([0-9]{8})`                        |                                                              |                            |
|                                                              |                                                              |                            |
|                                                              | ${${AttrLandingZoneAzure:replaceEmpty("LandingZoneAzure")}}/${${AttrStoreAzureDir:replaceEmpty("StoreToDirectory")}}/${${AttrStoreAzureFilename:replaceEmpty("filename")}} |                            |



### How to Get Extract Date from filename, path or another attribute

In case the file name is encoded in the path or filename, we could use regular expression replace or substring to extract this information. To improve maintainability and create reusable components, we might define generic components, which are controlled through configuration. Configuration is specified through variables.

When using regular expressions, you can use online [testing tool](https://www.freeformatter.com/java-regex-tester.html).

Here are some examples.

**Example 1: Date encoded in the path**

````
# Extract date is encoded in the path using ddMMyyyy format.
path=/datawarehouse/crm/edge/ora_21032019_raw
filename=customer.csv
````

Using NiFi Expression Language extracting the date might be done in different ways:

```
>>> ${path:substringAfterLast('/')}
ora_21032019_raw
>>> ${path:substringAfterLast('/'):substring(4,12)}
21032019
>>> ${path:substringAfterLast('/'):substring(4,12):toDate("ddMMyyyy")}
Thu Mar 21 00:00:00 UTC 2019
# To yyyyMMdd string:
>>> ${path:substringAfterLast('/')
   :substring(4,12)
   :toDate("ddMMyyyy")
   :format("yyyyMMdd")}
```



```
# Extract as is (ddMMyyyy):
>>> ${path:replaceAll('^.*(\d{8})_raw$', '$1')}
21032019
# To yyyyMMdd string:
>>> ${path:replaceAll('^.*(\d\d)(\d\d)(\d\d\d\d)_raw$', '$3$2$1')}
20190321
```

How we can parameterize above operation? As it is seen the first approach needs:

* Attribute name.
* Last separator - could use just substring, but will reduce flexibility in case of system change. Increases coupling.
* Start and end character for substring. - also presents tight coupling, assuming that the date is in a continuous substring.
* Source date format
* Target date format.

For the second, regular expression replace approach:

* Attribute name.
* Search string
* Replace string

The coupling of the second approach is significantly lower. I would go for it.

Let's start with regular expression parameters first. We define variables:

```
ExtractDateReSearch=^.*(\d\d)(\d\d)(\d\d\d\d)_raw$
ExtractDateReReplace=$3$2$1
```

Additional benefit of this approach is that we can use the regular expression also for quality purposes - validate filenames and/or path to filter out incorrect data.

Even more flexibility - configuration settings might be FlowFile attributes, defined by upstream processing.

Now the full extract expression looks like:

```
>>> ${path:replaceAll(${ExtractDateReSearch}, ${ExtractDateReReplace})}
20190321
```

Now you can have a reusable component which extracts the date from the path. What if my files are tagged in the filename?

**Example 2: Date in the filename**

Consider following example:

```
# Extract date is encoded in the path using ddMMyyyy format.
path=/datawarehouse/crm/edge/ora_raw
filename=customer_21032019_delta.csv
```

We can create another component which extracts the date from the filename instead of the path attribute.

```
ExtractDateReSearch=^customer_(\d\d)(\d\d)(\d\d\d\d)_delta\.csv$
ExtractDateReReplace=$3$2$1

>>> ${filename:replaceAll(${ExtractDateReSearch}, ${ExtractDateReReplace})}
20190321
```

Looks great. But now we have two components to maintain. What if we need to switch from `filename` to `path`? We need to alter the flow.

Let's do a little refactoring first. Let's define a new variable

```
ExtractDateAttr=filename
```

And modify the expression:

```
>>> ${${ExtractDateAttr}:replaceAll(${ExtractDateReSearch}, ${ExtractDateReReplace})}
20190321
```

We can define now the attribute.

What if we have mix of the two?

```
# Extract year is encoded in the path as yyyy.
# Extract date is in the filename as ddMM.
path=/datawarehouse/crm/edge/ora_2021_raw
filename=customer_2103_delta.csv
```

Again we could create a new component:

```
ExtractDateRePathSearch=^.*(\d\d\d\d)_raw$
ExtractDateRePathReplace=$1
ExtractDateReFnSearch=^customer_(\d\d)(\d\d)_delta\.csv$
ExtractDateReFnReplace=$2$1

>>> ${path:replaceAll(${ExtractDateRePathSearch}, ${ExtractDateRePathReplace})}
    ${filename:replaceAll(${ExtractDateReFnSearch}, ${ExtractDateReFnReplace})}
20210321

```

Again we have a working solution, but ended with an extra component. We also have to define four regular expressions. Plus the fixed order in the concatenation.

What if we concatenate path and filename?

```
>>> ${path:append('/'):append(${filename})}
/datawarehouse/crm/edge/ora_2021_raw/customer_2103_delta.csv
```

And now we have the flexibility that we need:

```
ExtractDateReSearch=^.*ora_(\d\d\d\d)_raw.*(\d\d)(\d\d).*$
ExtractDateReReplace=$1$3$2

>>> ${path:append('/'):append(${filename})
     :replaceAll(${ExtractDateReSearch},${ExtractDateReReplace})}
20210321
```

#### Putting all together

If we want to have the flexibility to use fields other than path and fieldname:

```
ExtractDateReSearch=^.*ora_(\d\d\d\d)_raw.*(\d\d)(\d\d).*$
ExtractDateReReplace=$1$3$2
ExtractDateAttrFilename=filename
ExtractDateAttrPath=path

>>> ${${ExtractDateAttrPath}:append('/'):append(${${ExtractDateAttrFilename}})
     :replaceAll(${ExtractDateReSearch},${ExtractDateReReplace})}
20210321
```

