# dystonse-tools
Java-powered tools supporting the dystonse-algorithm.

## Compiling
Compile using Maven by running:

    mvn package

## Importer
Currently there is only a very simple data logger to get delay information from VBB and write it to a database. More structure and documentation will follow. Use it like this:

    java -cp <...> Import -h <arg> [-help] -p <arg> -u <arg>
        -h,--host <arg>       Hostname or IP of the database server
        -help,--help          Print command line syntax
        -p,--password <arg>   Password for the database server
        -u,--user <arg>       User name for the database server