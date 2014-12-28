DataAnonymizer
==============

Purpose
-------
While performing the application development, testing, or maintenance, it is important to operate in the environment that is as close to the production environment as possible when it comes to amount of data and close-to-real content.

The tool allows:
• to identify personal data (column and data discovery utilities);
• create custom plan (requirement document) to define what columns are to be anonymized
* anonymize the data based on custom data sources

Prerequisites
----------------
1. JDK 1.7+
2. Maven 3+

How to build DataAnonymizer
------------------------------
1. Download ZIP file
2. Unzip in directory of your choice
3. cd DataAnonymizer-master
4. cd DataAnonymizer
5. mvn install
6. DataAnonymizer.jar will be located in "target" directory

Contributing
-------------------------

1. Fork it
2. Create your feature branch (git checkout -b new-feature)
3. Commit your changes (git commit -am 'Add new feature')
4. Push to the branch (git push origin new-feature)
5. Create new Pull Request


How to run DataAnonymizer
----------------------------
DataAnaonymizer (DA) is primarily a command line tool. To run the DA first build the application as above (mvn install). This
will generate an executeable jar file in the "target" directory. Once this has been done you can get help by typing:

    java -jar DataAnonymizer.jar --help
    
DA can be run in two different discovery modes, column discovery and data discovery. In each of these modes you need to provide
DA with a database property file. This tells DA which database to connect to and how to connect. The location of this property file it passed in to DA using the -P or --database-properties switch.

Column Discovery
--------------------
In this mode DA attempts to query your database and identified columns that should be anonymized based on their names. To run in this mode type the following:

    java -jar DataAnonymizer.jar --database-properties <db.properties> --column-discovery <columndiscovery.properties>
    
Where:
    <db.properties>              - Path and file name of the file containing database connection properties 
                                   (see src/main/resources/db.properties for an example)

    <columndiscovery.properties> - Path and file name of the file containing column discovery properties
                                   (see src/main/resources/columndiscovery.properties for an example)
    

Data Discovery
------------------
