# java4eurostat
Java4eurostat is a Java library for statistical data manipulation. It provides a number of functions to load statistical data into an 'hypercube' structure and index it for fast in-memory computations. A number of specific functions are provided to easily access [Eurostat](http://ec.europa.eu/eurostat/) data.

## Usage example

Let's start with a simple dataset:

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

Information on the hypercube structure is shown with ```hc.printInfo();```, which returns:

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

Several input formats are supported. For example, [Eurostat](http://ec.europa.eu/eurostat/) data can be loaded directly from the web. For that, only the database code given in [Eurostat databases catalog](http://ec.europa.eu/eurostat/data/database) is required. For example, the database on *HICP - Country weights* (code *prc_hicp_cow*) can be downloaded and loaded simply with:

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

Once loaded, data can be filtered/selected. For example, ```hc.selectDimValueEqualTo("country","Brasil")``` selects data for Brasil and ```hc.selectValueGreaterThan(147)``` selects data with values greater than 147. Selection criteria can be combined in cascade like ```hc.selectDimValueEqualTo("country","Brasil").selectDimValueGreaterThan("year",2012)``` for the selection of Brasil data after 2012. Logical operators 'AND', 'OR' and 'NOT' can also be used to build more complex selection criteria. Totally generic selection criteria can be specified such as:

```java
hc.select(new Criteria(){
	@Override
	public boolean keep(Stat stat) {
		return stat.dims.get("country").contains("r") && Math.sqrt(stat.value)>7;
	}
});
```

which selects all statistics with country names containing a "r" character, and whose root square value is greater than 7.

A single value can be retrieved with for example ```hc.selectDimValueEqualTo("country", "Japan", "gender", "Total", "year", "2014").stats.iterator().next().value``` but the fastest way to retrieve a value and scan a dataset is to use an index such as:

```java
TODO example
```

Scanning a full dataset across its dimensions is very fast with:

```java
TODO example
```


## Input formats
### CSV
```java
//load
StatsHypercube hc = CSV.load("example.csv", "population");
//save
TODO example
```
### JSON-stat https://json-stat.org/
```java
//load
TODO example
//save
TODO example
```
### Eurostat TSV
```java
//load
TODO example
//save
TODO example
```
### Web scraping
TODO describe
### Eurostat web service
TODO describe
### Filtering on loading
TODO describe

## Data manipulation
### Selection/filtering/slicing/dicing
TODO describe
### Indexing
TODO describe

## Time series analysis
TODO describe gap and outlier detection
