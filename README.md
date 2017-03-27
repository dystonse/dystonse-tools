# dystonse-tools
Java-powered tools supporting the dystonse-algorithm. The aim is bridging the gap between Berlins real time data (in the proprietary HAFAS format) and other available date, which is published as GTFS realtime feed, so that software can be built that works on either kind of data stream.

Currently there is only a very simple data logger to get delay information from VBB and write it to a database.

More tools, structure and documentation will follow, see below.

## Compiling
Compile using Maven by running:

    mvn package

## Import tool
This tool makes a request to the [VBB](http://www.vbb.de/de/article/fahrplan/online-fahrplanservices-auf-einen-blick/vbb-livekarte/20046.html) real time  [HAFAS API](https://github.com/derhuerst/vbb-hafas) to get the current position and delay of vehicles and writes them into a SQL table. It is in very early development stage, but anyway it has already collected over four millions of records.

Use it like this:

usage: Import [-h <arg>] [-u <arg>] [-p <arg>] -d <arg> [-help | -c | -s]
 -h,--host <arg>       Hostname or IP of the database server
 -u,--user <arg>       User name for the database server
 -p,--password <arg>   Password for the database server
 -d,--database <arg>   The database name
 -help,--help          Print command line syntax
 -c,--create-table     Executes a CREATE TABLE statement instead of inserting data
 -s,--show-table       Prints out a CREATE TABLE statement instead of inserting data

For each invocation, it makes a single request to the API and another one to the database. For continous data collection, you might set up a cronjob which runs every minute.

## Future tools
Over the course of 2017, the following tools/features are planned:

 * __GtfsRealtimeExport__ - Output a GTFS realtime feed
 * __Analyse__ - Perform several statistical analyses on the collected delay data, focused on delay prediction
 * __MapToShape__ - Map (lat,lon)-Locations to shapes from a GTFS feed