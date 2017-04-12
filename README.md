# dystonse-tools
Java-powered tools supporting the [dystonse](https://github.com/lenaschimmel/dystonse)-algorithm. The aim is bridging the gap between Berlins real time data (in the proprietary HAFAS format) and other available date, which is published as GTFS realtime feed, so that software can be built that works on either kind of data stream.

Currently there is only a very simple data logger to get delay information from VBB and write it to a database.

More tools, structure and documentation will follow, see below.

## Compiling
Compile using Maven by running:

    mvn package

## Import tool
This tool makes a request to the [VBB](http://www.vbb.de/de/article/fahrplan/online-fahrplanservices-auf-einen-blick/vbb-livekarte/20046.html) real time  HAFAS API (See explanations [here](https://github.com/derhuerst/vbb-hafas)) to get the current position and delay of vehicles and writes them into a **MySQL** table. It is in very early development stage, but anyway it has already collected over four million records.

    usage: Import [-h <arg>] [-u <arg>] [-p <arg>] -d <arg> [-r <arg>] [-help | -c | -s]
    -h,--host <arg>       Hostname or IP of the database server
    -u,--user <arg>       User name for the database server
    -p,--password <arg>   Password for the database server
    -d,--database <arg>   The database name
    -r,--rect <arg>       Use this bounding box to limit queries. Provide four values separated by
                          semicolons
    -help,--help          Print command line syntax
    -c,--create-table     Executes a CREATE TABLE statement instead of inserting data
    -s,--show-table       Prints out a CREATE TABLE statement instead of inserting data

You should use `-c` or `-s` on the first run to initialize the table schema.

For each invocation without `-c` or `-s`, it performs a single request to the API and another one to the database. For continous data collection, you might set up a cronjob which runs every minute.

## Geocode tool
This tool is in very early developtment and does not do what it's supposed to do. It can currently be used to view small subset of the collected data on a map. See the code for details.

    usage: Geocode [-help] -r <arg> [-h <arg>] [-u <arg>] [-p <arg>] -d <arg>
    -help,--help          Print command line syntax
    -r,--route <arg>      Name of the route which shall be shown.
    -h,--host <arg>       Hostname or IP of the database server
    -u,--user <arg>       User name for the database server
    -p,--password <arg>   Password for the database server
    -d,--database <arg>   The database name


<img src="https://github.com/lenaschimmel/dystonse-tools/blob/master/doc/first-test.png?raw=true" alt="Screenshots of a very early version, showing the route S42."/>

## Database setup
In the near future, _dystonse-tools_ will support the database schemas defined by the popular python tools [gtfsdb](https://github.com/OpenTransitTools/gtfsdb) and [gtfsrdb](https://github.com/mattwigway/gtfsrdb), as well as the data they create. By then, _dystonse-tools_ should be able to create the needed tables automatically.

If you want to have those schemas now, you can either download, install and run them to create the needed tables (which maybe quite a hassle) or have a look at the SQL scripts from the directory [schema](https://github.com/lenaschimmel/dystonse-tools/tree/master/schema).

## Future tools
Over the course of 2017, the following tools/features are planned:

 * __GtfsRealtimeExport__ - Output a GTFS realtime feed like [bullrunner-gtfs-realtime-generator](https://github.com/CUTR-at-USF/bullrunner-gtfs-realtime-generator) does
 * __Analyse__ - Perform several statistical analyses on the collected delay data, focused on delay prediction
 * __MapToShape__ - Map (lat,lon)-Locations to shapes from a GTFS feed
