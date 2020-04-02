[![License](http://img.shields.io/:license-apache-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed95fbecb60b4e98973c976f338ab4b5)](https://www.codacy.com/app/agstrider/DataDefender?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=armenak/DataDefender&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/armenak/DataDefender/branch/master/graph/badge.svg)](https://codecov.io/gh/armenak/DataDefender)

Data Discovery and Anonymization toolkit
========================================

Table of content
----------------
- [Purpose](#purpose)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Build from source](#build-from-source)
- [Including JDBC Drivers](#including-jdbc-drivers)
- [Extensions](#extensions)
- [Contributing](#contributing)
- [How to run](#how-to-run-data)
- [Using argument files](#using-argument-files)
- [File Discovery](#file-discovery)
- [Column Discovery](#column-discovery)
- [Data Discovery](#data-discovery)
- [Data Extractor](#data-extractor)
- [Anonymizer](#anonymizer)
- [Upgrading to 2.0](#upgrading-to-20)
- [Features and issues](#features-and-issues)
- [Code quality](#code-quality)

Purpose
-------
While performing application development, testing, or maintenance, it is important to operate in an environment that is as close to the production environment as possible when it comes to the amount of data and close-to-real content. At the same time it is important to ensure that data privacy policies are not violated.

Database, column, and file discovery identify and analyze data risks and report on potentially identifiable and personal information stored. And the database anonymization process anonymizes sensitive data and transfer information between organizations, while reducing the risk of unintended disclosure.

The complete source code is available, so you can inspect it and perform security audits if necessary.

This implementation of Data Discovery program is using [Apache OpenNLP](https://opennlp.apache.org/)

Features
--------
1. Identifies sensitive personal data.
2. Creates plan (XML document) to define what columns and how should be anonymized.
3. Anonymizes the data.
4. Platform-independent.
5. Supports Oracle,MS SQL Server,MySQL, and PostgreSQL. Work in progress for DB2.
6. This tool can help you on GDPR.

Prerequisites
----------------
1. JDK 11+
2. Maven 3+

Build from source
-----------------
1. Download ZIP file and unzip in a directory of your choice, or clone repo
2. cd {dir}/DataDefender/
3. mvn package
4. DataDefender.jar will be located in "target" directory {dir}/DataDefender/target/

Including JDBC Drivers
-----------------
JDBC drivers are included as optional dependencies included in maven profiles that can be activated.  Valid options are:

- mariadb
- mysql
- sqlserver
- postgresql
- oracle (requires configuring the oracle repository, which requires a user account.  See https://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9010)

In addition, a property to activate all but the oracle driver is available as well for convenience:

- jdbc-drivers-all

Example builds:

```
mvn package -P mariadb,mysql
mvn package -Djdbc-drivers-all
mvn package -P oracle -Djdbc-drivers-all
```

Alternatively, the JDBC drivers can be included as jar files in the extensions folder.

Note: sqlite-jdbc is included always for file discovery.

Extensions
------------
Additional jar files/classes can be added under an 'extensions' folder in the current directory.  The default 'datadefender' scripts copied to the target directory adds classes/jar files under 'extensions' to the classpath.

See [sample_projects/anonymizer/]([sample_projects/anonymizer/) for an example.

Contributing
------------
We encourage you to contribute to DataDefender! Please check out the [Contribution guidelines for this project](CONTRIBUTING.md). 

How to run
----------
The toolkit is implemented as a command line program. To run it first build the application as above (mvn package). This will generate an executable jar file in the "target" directory. For your convenience executable 'sh' and 'bat' files are created as well.  You may need to adjust permissions for the executable shell script (chmod +x datadefender). Once this has been done you can get help by running 'datadefender' or 'datadefender.bat' in your shell/command prompt:

``` datadefender help ```

```
Usage: datadefender [-hvV] [--debug] COMMAND
Data detection and anonymization tool
      --debug     enable debug logging
  -h, --help      Show this help message and exit.
  -v, --verbose   enable more verbose output
  -V, --version   Print version information and exit.
Commands:
  help       Displays help information about the specified command
  anonymize  Run anonymization utility
  extract    Run data extraction utility -- generates files out of table
               columns with the name 'table_columnName.txt' for each column
               requested.
  discover   Run data discovery utility
```

The toolkit can be run in anonymizer mode, data extraction mode (extract), and three different discovery modes (file, column, and database discovery).

Using argument files
----------
DataDefender is using [picocli](https://picocli.info) as its framework for processing command-line input.  The framework allows using argument files to set argument values when running the tool.  The argument file contains a list of arguments to pass (more than one can be used), and when invoking DataDefender, the argument file can be specified with an "@".  For example:

File: database.config
```
--url=jdbc:mariadb://localhost:3306/database?zeroDateTimeBehavior=convertToNull
--password
--user=root
```

Running with database.config:
```
datadefender @database.config
```


File Discovery
--------------

``` datadefender discover files ```

```
Usage: datadefender discover files ([-l=<limit>] [-e=<extensions>]
                                   [-e=<extensions>]...
                                   [--model-file=<fileModels>]
                                   [--model-file=<fileModels>]...
                                   [--token-model=<tokenModel>]
                                   [--probability-threshold=<probabilityThreshold>]
                                   [--[no-]score-calculation]
                                   [--threshold-count=<thresholdCount>]
                                   [--threshold-high=<thresholdHighRisk>]
                                   [-m=<models>] [-m=<models>]...) [-hvV]
                                   [--debug] -d=<directories>
                                   [-d=<directories>]... -x=<excludeExtensions>
                                   [-x=<excludeExtensions>]...
Run file discovery utility
  -d, --directory=<directories>
                         Adds a directory to list of directories to be scanned
      --debug            enable debug logging
  -h, --help             Show this help message and exit.
  -v, --verbose          enable more verbose output
  -V, --version          Print version information and exit.
  -x, --exclude-extension=<excludeExtensions>
                         Adds an extension to exclude from data discovery
Model discovery settings
  -e, --extension=<extensions>
                         Adds a call to an extension method (e.g. com.strider.
                           datadefender.specialcase.SinDetector.detectSin)
  -l, --limit=<limit>    Limit discovery to a set number of rows in a table
  -m, --model=<models>   Adds a built-in configured opennlp TokenizerME model
                           for data discovery. Available models are: date,
                           location, money, organization, person, time
      --model-file=<fileModels>
                         Adds a custom made opennlp TokenizerME file for data
                           discovery.
      --[no-]score-calculation
                         If set, includes a column score
      --probability-threshold=<probabilityThreshold>
                         Minimum NLP match score to return results for
      --threshold-count=<thresholdCount>
                         Reports if number of rows found are greater than the
                           defined threshold
      --threshold-high=<thresholdHighRisk>
                         Reports if number of high risk columns found are
                           greater than the defined threshold
      --token-model=<tokenModel>
                         Override the default built-in token model (English
                           tokens, en-token.bin) with a custom token file for
                           use by opennlp's TokenizerModel

```

File discovery will attempt to find sensitive personal information in binary and text files located on the file system.

Sample project can be found here: [sample_projects/file_discovery](sample_projects/file_discovery)

Column Discovery
----------------

``` datadefender discover columns ```

```
Usage: datadefender discover columns [[-u=<username>] [-p[=<password>]]
                                     [--schema=<schema>]
                                     [--[no-]skip-empty-tables-metadata]
                                     [--include-table-pattern-metadata=<includeTablePatterns>]
                                     [--include-table-pattern-metadata=<includeTablePatterns>]...
                                     [--exclude-table-pattern-metadata=<excludeTablePatterns>]
                                     [--exclude-table-pattern-metadata=<excludeTablePatterns>]...
                                     [--vendor=<vendor>]
                                     [--url=<url>]] [-hvV] [--debug]
                                     [-o=<outputFile>]
                                     --column-pattern=<patterns>
                                     [--column-pattern=<patterns>]...
Run column discovery utility
      --column-pattern=<patterns>
                          Regex pattern(s) to match column names
      --debug             enable debug logging
  -h, --help              Show this help message and exit.
  -o, --output=<outputFile>
                          Generate a requirements xml file and write it out to
                            the specified file
  -v, --verbose           enable more verbose output
  -V, --version           Print version information and exit.
Database connection settings
      --exclude-table-pattern-metadata=<excludeTablePatterns>
                          Pattern(s) matching table names to exclude for
                            metadata analysis
      --include-table-pattern-metadata=<includeTablePatterns>
                          Pattern(s) matching table names to include for
                            metadata analysis
  -p, --password[=<password>]
                          The password to connect with
      --schema=<schema>   The schema to connect to
      --[no-]skip-empty-tables-metadata
                          Include generating metadata for empty tables
                            (defaults to skipping)
  -u, --user=<username>   The username to connect with
      --url=<url>         The datasource URL
      --vendor=<vendor>   Database vendor, available options are: h2, mysql,
                            mariadb, postgresql, sqlserver, oracle. If not
                            specified, vendor will attempt to be extracted from
                            the datasource url for a jdbc scheme.
```

In this mode the tool attempts to query your database and identified columns that should be anonymized based on their names.  When -o is provided a sample requirements file (which can be modified and used for the anonymizer stage) will be created based on the columns discovered.

Note that column and data discovery can be combined.  The generated requirements file will combine both results.

Data Discovery
------------------

``` datadefender discover data ```

```
Usage: datadefender discover data ([-l=<limit>] [-e=<extensions>]
                                  [-e=<extensions>]...
                                  [--model-file=<fileModels>]
                                  [--model-file=<fileModels>]...
                                  [--token-model=<tokenModel>]
                                  [--probability-threshold=<probabilityThreshold
                                  >] [--[no-]score-calculation]
                                  [--threshold-count=<thresholdCount>]
                                  [--threshold-high=<thresholdHighRisk>]
                                  [-m=<models>] [-m=<models>]...)
                                  [[-u=<username>] [-p[=<password>]]
                                  [--schema=<schema>]
                                  [--[no-]skip-empty-tables-metadata]
                                  [--include-table-pattern-metadata=<includeTablePatterns>]
                                  [--include-table-pattern-metadata=<includeTablePatterns>]...
                                  [--exclude-table-pattern-metadata=<excludeTablePatterns>]
                                  [--exclude-table-pattern-metadata=<excludeTablePatterns>]...
                                  [--vendor=<vendor>]
                                  [--url=<url>]] [-hvV] [--debug]
                                  [-o=<outputFile>]
Run data discovery utility
      --debug             enable debug logging
  -h, --help              Show this help message and exit.
  -o, --output=<outputFile>
                          Generate a requirements xml file and write it out to
                            the specified file
  -v, --verbose           enable more verbose output
  -V, --version           Print version information and exit.
Model discovery settings
  -e, --extension=<extensions>
                          Adds a call to an extension method (e.g. com.strider.
                            datadefender.specialcase.SinDetector.detectSin)
  -l, --limit=<limit>     Limit discovery to a set number of rows in a table
  -m, --model=<models>    Adds a built-in configured opennlp TokenizerME model
                            for data discovery. Available models are: date,
                            location, money, organization, person, time
      --model-file=<fileModels>
                          Adds a custom made opennlp TokenizerME file for data
                            discovery.
      --[no-]score-calculation
                          If set, includes a column score
      --probability-threshold=<probabilityThreshold>
                          Minimum NLP match score to return results for
      --threshold-count=<thresholdCount>
                          Reports if number of rows found are greater than the
                            defined threshold
      --threshold-high=<thresholdHighRisk>
                          Reports if number of high risk columns found are
                            greater than the defined threshold
      --token-model=<tokenModel>
                          Override the default built-in token model (English
                            tokens, en-token.bin) with a custom token file for
                            use by opennlp's TokenizerModel
Database connection settings
      --exclude-table-pattern-metadata=<excludeTablePatterns>
                          Pattern(s) matching table names to exclude for
                            metadata analysis
      --include-table-pattern-metadata=<includeTablePatterns>
                          Pattern(s) matching table names to include for
                            metadata analysis
  -p, --password[=<password>]
                          The password to connect with
      --schema=<schema>   The schema to connect to
      --[no-]skip-empty-tables-metadata
                          Include generating metadata for empty tables
                            (defaults to skipping)
  -u, --user=<username>   The username to connect with
      --url=<url>         The datasource URL
      --vendor=<vendor>   Database vendor, available options are: h2, mysql,
                            mariadb, postgresql, sqlserver, oracle. If not
                            specified, vendor will attempt to be extracted from
                            the datasource url for a jdbc scheme.
```

In data discovery mode, the tool will perform an NLP scan of data in the database and return columns that have a match score greater than the value of probability-threshold.  When -o is provided a sample requirements file (which can be modified and used the anonymizer stage) will be created based on the columns discovered.

Note that column and data discovery can be combined.  The generated requirements file will combine both results.

Data Extractor
------------------

``` datadefender extract ```

```
Usage: datadefender extract ([-u=<username>] [-p[=<password>]]
                            [--schema=<schema>] [--[no-]skip-empty-tables-metadata]
                            [--include-table-pattern-metadata=<includeTablePatterns>]
                            [--include-table-pattern-metadata=<includeTablePatterns>]...
                            [--exclude-table-pattern-metadata=<excludeTablePatterns>]
                            [--exclude-table-pattern-metadata=<excludeTablePatterns>]...
                            [--vendor=<vendor>] [--url=<url>]) [-hvV]
                            [--debug] [columns...]
Run data extraction utility -- generates files out of table columns with the
name 'table_columnName.txt' for each column requested.
      [columns...]        Generate data for the specified table.columName(s)
      --debug             enable debug logging
  -h, --help              Show this help message and exit.
  -v, --verbose           enable more verbose output
  -V, --version           Print version information and exit.
Database connection settings
      --exclude-table-pattern-metadata=<excludeTablePatterns>
                          Pattern(s) matching table names to exclude for
                            metadata analysis
      --include-table-pattern-metadata=<includeTablePatterns>
                          Pattern(s) matching table names to include for
                            metadata analysis
  -p, --password[=<password>]
                          The password to connect with
      --schema=<schema>   The schema to connect to
      --[no-]skip-empty-tables-metadata
                          Include generating metadata for empty tables
                            (defaults to skipping)
  -u, --user=<username>   The username to connect with
      --url=<url>         The datasource URL
      --vendor=<vendor>   Database vendor, available options are: h2, mysql,
                            mariadb, postgresql, sqlserver, oracle. If not
                            specified, vendor will attempt to be extracted from
                            the datasource url for a jdbc scheme.
```

The Data Extractor is used to load table data into text files.  The text files are useful to modify and then feed into the annoymizer as input data.

Anonymizer
------------------

``` datadefender anonymize ```

```
Usage: datadefender anonymize ([-u=<username>] [-p[=<password>]]
                              [--schema=<schema>]
                              [--[no-]skip-empty-tables-metadata]
                              [--include-table-pattern-metadata=<includeTablePatterns>]
                              [--include-table-pattern-metadata=<includeTablePatterns>]...
                              [--exclude-table-pattern-metadata=<excludeTablePatterns>]
                              [--exclude-table-pattern-metadata=<excludeTablePatterns>]...
                              [--vendor=<vendor>] [--url=<url>])
                              [-hvV] [--debug] [-b=<batchSize>]
                              -r=<requirementFile> [tables...]
Run anonymization utility
      [tables...]         Limit anonymization to specified tables
  -b, --batch-size=<batchSize>
                          Number of update queries to batch together
      --debug             enable debug logging
  -h, --help              Show this help message and exit.
  -r, --requirement-file=<requirementFile>
                          Requirement XML file
  -v, --verbose           enable more verbose output
  -V, --version           Print version information and exit.
Database connection settings
      --exclude-table-pattern-metadata=<excludeTablePatterns>
                          Pattern(s) matching table names to exclude for
                            metadata analysis
      --include-table-pattern-metadata=<includeTablePatterns>
                          Pattern(s) matching table names to include for
                            metadata analysis
  -p, --password[=<password>]
                          The password to connect with
      --schema=<schema>   The schema to connect to
      --[no-]skip-empty-tables-metadata
                          Include generating metadata for empty tables
                            (defaults to skipping)
  -u, --user=<username>   The username to connect with
      --url=<url>         The datasource URL
      --vendor=<vendor>   Database vendor, available options are: h2, mysql,
                            mariadb, postgresql, sqlserver, oracle. If not
                            specified, vendor will attempt to be extracted from
                            the datasource url for a jdbc scheme.
```

In this mode, data anonymization is performed on the database based on the requirements file. The requirements file is an XML-formatted file describing which tables and columns should be anonymized, and how.  For an example, refer to [sample_projects/anonymizer/requirement.xml](sample_projects/anonymizer/requirement.xml).

Upgrading to 2.0
----------------

#### Database configuration
Database configuration has been moved from a properties file to command line arguments.  The command line arguments can be bundled in files for easy configuration, see [Using argument files](#using-argument-files) for more information.

The configuration options remain the same except:

 - 'vendor' is not required, datadefender will try to determine that from the provided jdbc connection url.  It's still provided as an option (which would allow using other jdbc drivers/urls and specifying a vendor to use for datadefender's queries, operations, etc...)
 - There's no need to specify a driver

Example change:

1.0 file:
```
vendor=mysql
driver=com.mysql.jdbc.Driver
username=root
password=mypassword
url=jdbc:mysql://localhost:3306/test
```

2.0 file:
```
--username=root
--password
--url=jdbc:mysql://localhost:3306/test
```

(Leaving password without a value will cause the utility to prompt for a value interactively).

Please review the command line help in the app itself, or in the readme under command headings for database configuration details.

#### Column, data, and file discovery
The main difference is the move from properties files to picocli argument configuration.  The easiest way to do that is use your existing properties file, and rename the property portion of each line to the corresponding argument.  For example:

1.0 properties file for file discovery:
```
probability_threshold=0.5
english_tokens=en-token.bin
person=en-ner-person.bin
location=en-ner-location.bin
models=person,location,organization
directories=/path/to/directory,/path/to/other/directory
exclusions=jar,exe
```

Becomes the following in 2.0:
```
-m location
-m person
--directory=/path/to/directory
--directory=/path/to/other/directory
--exclude-extension=jar
--exclude-extension=exe
```

Please review the help associated with each command as there are small differences (for example, there's no need to specify en-token.bin, it's the default if --token-model isn't provided with a custom file.  It's also not necessary to provide the files, they've been bundled in the jar file.  You can provide custom opennlp models with --model-file.

#### Anonymization
There are a couple of changes affecting anonymization (in addition to [database configuration](#database-configuration)):

A new format and features for the requirements xml file.  See the [sample_project]([sample_projects/anonymizer/) for an example new format, and also the xml schema file [requirement.xsd]([src/main/resources/com/strider/datadefender/requirement/file/requirement.xsd).

CoreFunctions has been split into a few different classes, and its package has been moved.  See the new classes here: [src/main/java/com/strider/datadefender/anonymizer/functions](src/main/java/com/strider/datadefender/anonymizer/functions).  Some functions have been removed entirely, for example randomInt, because apache commons can be used instead with RandomUtils.nextInt.

Referencing functions from the xml file now takes the form ClassName#method rather than ClassName.method to be more in-line with java's standards for naming.

## Features and issues
Please report issues or ask for future requests here: https://github.com/armenak/DataDefender/issues

## Code quality
Two amazing tools - Empear http://empear.com/ and SonarQube http://www.sonarqube.org/ help contributors of DataDefender maintain decent quality of code. Many thanks to their creators!
