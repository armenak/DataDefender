/*
 * 
 * Copyright 2014-2016, Armenak Grigoryan, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.strider.datadefender;

import com.strider.datadefender.database.IDBFactory;
import static com.strider.datadefender.utils.AppProperties.loadProperties;
import com.strider.datadefender.utils.ApplicationLock;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;


/**
 * Entry point to Data Defender. 
 *  
 * This class will parse and analyze the parameters and execute appropriate 
 * service.
 *
 */
public class DataDefender  {
 
    private static final Logger log = getLogger(DataDefender.class);

    public static void main(final String[] args) throws ParseException, AnonymizerException, IOException, SAXException, TikaException {
        
        // Ensure we are not trying to run second instance of the same program
        ApplicationLock al = new ApplicationLock("DataDefender");

        if (al.isAppActive()) {
            log.error("Another instance of this program is already active");
            System.exit(1);    
        }        
        
        log.info("Command-line arguments: " + Arrays.toString(args));

        final Options options = createOptions();
        final CommandLine line = getCommandLine(options, args);
        
        @SuppressWarnings("unchecked")
        List<String> unparsedArgs = line.getArgList();
        
        if (line.hasOption("help") || args.length == 0 || unparsedArgs.size() < 1) {
            help(options);
            return;
        }
        if (line.hasOption("debug")) {
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        } else {
            LogManager.getRootLogger().setLevel(Level.INFO);
        }

        final String cmd = unparsedArgs.get(0); // get & remove command arg
        unparsedArgs = unparsedArgs.subList(1, unparsedArgs.size());
        
        if ("file-discover".equals(cmd)) {
            final String fileDiscoveryPropertyFile = line.getOptionValue('F', "filediscovery.properties");
            final Properties fileDiscoveryProperties = loadProperties(fileDiscoveryPropertyFile);
            final FileDiscoverer discoverer = new FileDiscoverer();
            discoverer.discover(fileDiscoveryProperties);  
            return;
        }
        
        final String databasePropertyFile = line.getOptionValue('P', "db.properties");
        final Properties props = loadProperties(databasePropertyFile);
        try (final IDBFactory dbFactory = IDBFactory.get(props);) {
            switch (cmd) {
                case "anonymize":
                    final String anonymizerPropertyFile = line.getOptionValue('A', "anonymizer.properties");
                    final Properties anonymizerProperties = loadProperties(anonymizerPropertyFile);
                    final IAnonymizer anonymizer = new DatabaseAnonymizer();
                    anonymizer.anonymize(dbFactory, anonymizerProperties, getTableNames(unparsedArgs, anonymizerProperties));
                    break;
                case "generate":
                    final IGenerator generator = new DataGenerator();
                    final String generatorPropertyFile = line.getOptionValue('A', "anonymizer.properties");
                    final Properties generatorProperties = loadProperties(generatorPropertyFile);
                    generator.generate(dbFactory, generatorProperties);
                    break;
                case "database-discover":
                    if (line.hasOption('c')) {
                        final String columnPropertyFile = line.getOptionValue('C', "columndiscovery.properties");
                        final Properties columnProperties = loadProperties(columnPropertyFile);
                        final ColumnDiscoverer discoverer = new ColumnDiscoverer();
                        discoverer.discover(dbFactory, columnProperties, getTableNames(unparsedArgs, columnProperties));
                        if (line.hasOption('r')) {
                            discoverer.createRequirement(line.getOptionValue('R', "Sample-Requirement.xml"));
                        }                        
                    } else if (line.hasOption('d')) {
                        final String datadiscoveryPropertyFile = line.getOptionValue('D', "datadiscovery.properties");
                        final Properties dataDiscoveryProperties = loadProperties(datadiscoveryPropertyFile);
                        final DatabaseDiscoverer discoverer = new DatabaseDiscoverer();
                        discoverer.discover(dbFactory, dataDiscoveryProperties, getTableNames(unparsedArgs, dataDiscoveryProperties));
                        if (line.hasOption('r')) {
                            discoverer.createRequirement(line.getOptionValue('R', "Sample-Requirement.xml"));
                        }                         
                    }
                    break;
                default:
                    help(options);
                    break;
            }
        }
    }
    
    /**
     * Parses command line arguments
     * 
     * @param options
     * @param args
     * @return CommandLine
     */
    private static CommandLine getCommandLine(final Options options, final String[] args) {
        final CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args, false);
        } catch (ParseException e) {
            help(options);
        }
        return line;
    }    
    
    /**
     * Creates options for the command line
     * 
     * @return Options
     */
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption("A", "anonymizer-properties", true, "define anonymizer property file");
        options.addOption("c", "columns", false, "discover candidate column names for anonymization based on provided patterns");
        options.addOption("C", "column-properties", true, "define column property file");
        options.addOption("d", "data", false, "discover candidate column for anonymization based on semantic algorithms");
        options.addOption("D", "data-properties", true, "discover candidate column for anonymization based on semantic algorithms");
        options.addOption("r", "requirement", false, "create discover and create requirement file");
        options.addOption("R", "requirement-file", false, "define requirement file name");
        options.addOption("P", "database properties", true, "define database property file");
        options.addOption("F", "file discovery properties", true, "define file discovery property file");
        options.addOption("debug", false, "enable debug output");
        return options;
    }
 
    /**
     * Displays command-line help options
     * 
     * @param Options 
     */
    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataAnonymizer anonymize|discover|generate [options] [table1 [table2 [...]]]", options);
    }
    
    /**
     * Returns the list of unparsed arguments as a list of table names by
     * transforming the strings to lower case.
     * 
     * This guarantees table names to be in lower case, so functions comparing
     * can use contains() with a lower case name.
     * 
     * If tables names are not supplied via command line, then will search the property file
     * for space separated list of table names.
     * 
     * @param tableNames
     * @param props application property file
     * @return The list of table names
     */
    public static Set<String> getTableNames(List<String> tableNames, final Properties props) {
        if (tableNames.isEmpty()) {
            final String tableStr = props.getProperty("tables");
            if (tableStr == null) {
                return Collections.emptySet();
            }
            tableNames = Arrays.asList(tableStr.split(" "));
            log.info("Adding tables from property file.");
        }
        final Set<String> tables = tableNames.stream().map(s -> s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());
        log.info("Tables: " + Arrays.toString(tables.toArray()));
        return tables;
    }
}
