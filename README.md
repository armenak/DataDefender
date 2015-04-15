DataAnonymizer
==============

Purpose
-------
While performing the application development, testing, or maintenance, it is important to operate in the environment that is as close to the production environment as possible when it comes to amount of data and close-to-real content.

Features
--------
1. identifies personal data (column and data discovery utilities).
2. creates custom plan (requirement document) to define what columns are to be anonymized.
3. anonymizes the data based on custom data sources.
4. Platform-independent. The tool supports common databases such as Oracle, MS SQL Server, DB2, MySQL, Postgres.

Prerequisites
----------------
1. JDK 1.8+
2. Maven 3+

How to build DataAnonymizer
------------------------------
1. Download ZIP file and unzip in a directory of your choice, or clone repo
2. cd {dir}/DataAnonymizer/
3. mvn install
4. DataAnonymizer.jar will be located in "target" directory {dir}/DataAnonymizer/target/

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
    
DA can be run in anonymizer mode, and two different discovery modes, column discovery and data discovery. In each of these modes you need to provide
DA with a database property file. This tells DA which database to connect to and how to connect. The location of this property file it passed in to DA using the -P or --data switch.

All modes support an optional list of tables at the end to use for either discover, or anonymization of a specific table or list of tables.

Column Discovery
--------------------
In this mode DA attempts to query your database and identified columns that should be anonymized based on their names. To run in this mode type the following:

    java -jar DataAnonymizer.jar discover -c --data <db.properties> --column-discovery <columndiscovery.properties>
    
Where:
    <db.properties>              - Path and file name of the file containing database connection properties 
                                   (see src/main/resources/db.properties for an example)

    <columndiscovery.properties> - Path and file name of the file containing column discovery properties
                                   (see src/main/resources/columndiscovery.properties for an example)
    

Data Discovery
------------------
To run DA in Data Discovery mode, pass '-d' to discover.  DA will perform an NLP scan of data in the database and return columns that have a match score greater than the value of probability_threshold specified in datadiscovery.properties file.

    java -jar DataAnonymizer.jar discover -d --data <db.properties> --data-discovery <datadiscovery.properties>

Where:
    <db.properties>            - Path and file name of the file containing database connection properties 
                                (see src/main/resources/db.properties for an example)

    <datadiscovery.properties> - Path and file name of the file containing data discovery properties
                                (see src/main/resources/datadiscovery.properties for an example)

Anonymizer
------------------
In this mode, DA will anonymize the data in the database based on the requirements file specified in the anonymizer.properties file.  The requirements file is an XML-formatted file describing which tables and columns should be anonymized, and how.  For an example, refer to src/main/resources/Requirement.xml.

    java -jar DataAnonymizer.jar anonymize --data <db.properties> --anonymizer-properties <anonymizer.properties>

Where:
    <db.properties>         - Path and file name of the file containing database connection properties 
                             (see src/main/resources/db.properties for an example)

    <anonymizer.properties> - Path and file name of the file containing anonymizer properties
                             (see src/main/resources/anonymizer.properties for an example)

To run the anonymizer on specific tables, the table names can be passed at the end:

    java -jar DataAnonymizer.jar anonymize -P db.properties -A anonymizer.properties myTable1 myTable2

This would run the anonymizer for tables defined in the requirements XML file with the name myTable1 or myTable2 only.  Any other tables defined in the requirements would be ignored.


Using 3rd-Party JDBC Drivers with Maven
------------------
Unfortunately, not all JDBC drivers are downloadable via a publicly available maven repostitory and must be downloaded individually.  For example:

- http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html
- http://www.microsoft.com/en-us/download/details.aspx?displaylang=en&id=11774

In order to use these drivers via maven you can add the driver jar to your private maven repository if you have one or install locally:

<ol>
<li>download package</li>
<li>unzip/extract jdbc jar file from package</li>
<li>add driver to your local maven repository by executing:  
<pre>
mvn install:install-file -Dfile=${path to jdbc driver jar file} -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=jar
</pre>
</li> 
<li>add dependency to pom.xml:
<pre>
    &lt;dependency&gt;
        &lt;groupId&gt;${groupId}&lt;/groupId&gt;
        &lt;artifactId&gt;${artifactId}&lt;/artifactId&gt;
        &lt;version&gt;${version}&lt;/version&gt;
    &lt;/dependency&gt;
</pre>
</li>
</ol>


Using Maven to execute Anonymizer
------------------

The pom.xml has also been configured to allow easy execution of the Anonymizer suite of programs from the command line. This option allows you to quickly run any of the programs after you make changes to the code, for example. Most of the configurable options are supported via Maven, but please read on for the details.

<h3>Prerequisites</h3>

Ensure that:
- The required database driver has been installed and configured for Maven (refer to 3rd party JDBC section if necessary).
- 'mvn clean compile' or even better 'mvn clean test' has been run successfully
- Necessary property files (db.properties, and at least one of: columndiscovery|datadiscovery|anonymizer.properties) have been configured and placed under ${execAppDir} (defaults to ${basedir}/exec-app/dir).

<h3>Configuration</h3>

- ${execAppDir} can be overridden via the command line (ie; -DexecAppDir=blah...) to set the Anonymizer working directory (and also the relative directory where the properties files should be placed).  *Note: the log directory will also be created under ${execAppDir}
- Maven doesn't handle dynamic behaviour very well (such as a variable # of tables names given on the command line); therefore, in order to restrict applications to certain tables, table names must be provided via the optional 'tables' property in the appropriate application properties file (columndiscovery|datadiscovery|anonymizer.properites).

<h3>Execution</h3>

A maven profile has been configured for each application, here are the Maven commands to run for the following programs:
- column discovery: mvn exec:exec -P column-discovery
- data discovery: mvn exec:exec -P data-discovery
- anonymizer: mvn exec:exec -P anonymize
