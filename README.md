# java4eurostat
Java4eurostat is a Java library for statistical data manipulation. It provides a number of functions to load statistical data into an 'hypercube' structure and index it for fast in-memory computations. A number of specific functions are provided to easily access [Eurostat](http://ec.europa.eu/eurostat/) data.

## Usage example

Let's start with a simple example on this dataset:

|country|gender|year|population|
|:--:|:--:|:--:| --:|
|Brasil|Male|2013|45.1|
|Brasil|Female|2013|48.3|
|Brasil|Total|2013|93.4|
|Brasil|Male|2014|46.2|
|Brasil|Female|2014|47.7|
|Brasil|Total|2014|93.9|
|Japan|Male|2013|145.1|
|Japan|Female|2013|148.3|
|Japan|Total|2013|293.4|
|Japan|Male|2014|146.2|
|Japan|Female|2014|147.7|
|Japan|Total|2014|293.9|

stored as a CSV file `example.csv`:

```
country,gender,year,population
Brasil,Male,2013,45.1
Brasil,Female,2013,48.3
Brasil,Total,2013,93.4
Brasil,Male,2014,46.2
Brasil,Female,2014,47.7
Brasil,Total,2014,93.9
Japan,Male,2013,145.1
Japan,Female,2013,148.3
Japan,Total,2013,293.4
Japan,Male,2014,146.2
Japan,Female,2014,147.7
Japan,Total,2014,293.9
```

This file can be loaded into an hypercube structure with:

```java
StatsHypercube hc = CSV.load("example.csv", "population");
```

Information on the hypercube structure is shown with:

```java
hc.printInfo();
```

which returns:

```
Information: 12 value(s) with 3 dimension(s).
   Dimension: gender (3 dimension values)
      Female
      Male
      Total
   Dimension: year (2 dimension values)
      2013
      2014
   Dimension: country (2 dimension values)
      Brasil
      Japan
```

A number of input formats are supported to load data. It is for example possible to retrieve [Eurostat](http://ec.europa.eu/eurostat/) data directly from the web. For that, only the database code is required. For example, the database on *HICP - Country weights* (code *prc_hicp_cow*) can be retrieved with:

```java
StatsHypercube hc2 = EurobaseIO.getDataFromDBCode("prc_hicp_cow");
```

The structure returned with a ```hc2.printInfo();``` is:

```
Information: 2001 value(s) with 3 dimension(s).
   Dimension: time (21 dimension values)
      1996
      1997
      1998
      ...
   Dimension: geo (35 dimension values)
      AT
      BE
      BG
      ...
   Dimension: statinfo (6 dimension values)
      COWEA
      COWEA18
      COWEA19
      ...
```

TODO: Describe short example on selection.

## Input formats
TODO describe CSV, TSV, JSONStat

## Data manipulation
### Selection/filtering/slicing/dicing
TODO describe
### Indexing
TODO describe
## Time series analysis
TODO describe gap and outlier detection
