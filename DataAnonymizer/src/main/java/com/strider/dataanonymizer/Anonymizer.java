/*
 * 
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.dataanonymizer;

import static com.strider.dataanonymizer.utils.AppProperties.loadProperties;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import static org.apache.log4j.Logger.getLogger;

/**
 * Entry point to Data Anonymizer. 
 *  
 * This class will parse and analyze the parameters and execute appropriate 
 * service.
 *
 */
public class Anonymizer  {
 
    private static final Logger log = getLogger(Anonymizer.class);

    public static void main(String[] args) throws ParseException, AnonymizerException {

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
        
        String cmd = unparsedArgs.get(0);
        Collection<String> tables = getTableArgs(unparsedArgs);
        
        String databasePropertyFile = line.getOptionValue('P', "db.properties");
        Properties props = null;
        
        switch (cmd) {
            case "anonymize":
                try {
                    props = loadProperties(databasePropertyFile);            
                } catch (IOException ioe) {
                    throw new AnonymizerException("ERROR: Unable to load " + databasePropertyFile, ioe);
                }
                
                String anonymizerPropertyFile = line.getOptionValue('A', "anonymizer.properties");
                Properties anonymizerProperties = null;
                try {
                    anonymizerProperties = loadProperties(anonymizerPropertyFile);
                } catch (IOException ioe) {
                    throw new AnonymizerException("ERROR: Unable to load " + anonymizerPropertyFile, ioe);
                }
                IAnonymizer anonymizer = new DatabaseAnonymizer();
                anonymizer.anonymize(props, anonymizerProperties, tables);
                break;
            case "discover":
                try {
                    props = loadProperties(databasePropertyFile);            
                } catch (IOException ioe) {
                    throw new AnonymizerException("ERROR: Unable to load " + databasePropertyFile, ioe);
                }
                
                if (line.hasOption('c')) {
                    
                    String columnPropertyFile = line.getOptionValue('C', "columndiscovery.properties");
                    Properties columnProperties = null;
                    try {
                        columnProperties = loadProperties(columnPropertyFile);
                    } catch (IOException ioe) {
                        throw new AnonymizerException("ERROR: Unable to load " + columnPropertyFile, ioe);
                    }
                    IDiscoverer discoverer = new ColumnDiscoverer();
                    discoverer.discover(props, columnProperties, tables);
                }
                if (line.hasOption('d')) {
                    log.info("Data discovery in process");
                    String datadiscoveryPropertyFile = line.getOptionValue('D', "datadiscovery.properties");
                    Properties dataDiscoveryProperties = null;
                    try {
                        dataDiscoveryProperties = loadProperties(datadiscoveryPropertyFile);
                    }  catch (IOException ioe) {
                        throw new AnonymizerException("ERROR: Unable to load " + datadiscoveryPropertyFile, ioe);
                    }                            
                    IDiscoverer discoverer = new DataDiscoverer();
                    discoverer.discover(props, dataDiscoveryProperties, tables);
                }
                break;
            default:
                help(options);
                break;
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
        options.addOption("h", "help", false, "Display help");        
        options.addOption("A", "anonymizer-properties", true, "define anonymizer property file");
        options.addOption("c", "columns", false, "discover candidate column names for anonymization based on provided patterns");
        options.addOption("C", "column-properties", true, "define column property file");
        options.addOption("d", "data", false, "discover candidate column for anonymization based on semantic algorithms");
        options.addOption("D", "data-properties", true, "discover candidate column for anonymization based on semantic algorithms");
        options.addOption("P", "database properties", true, "define database property file");
        options.addOption("debug", false, "Enable debug output");
        return options;
    }
 
    /**
     * Displays command-line help options
     * 
     * @param Options 
     */
    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataAnonymizer anonymize|discover [options] [table1 [table2 [...]]]", options);
    }
    
    /**
     * Returns the list of unparsed arguments as a list of table names by
     * transforming the strings to lower case.
     * 
     * This guarantees table names to be in lower case, so functions comparing
     * can use contains() with a lower case name.
     * 
     * @param unparsedArgs
     * @return The list of table names
     */
    private static Collection<String> getTableArgs(List<String> unparsedArgs) {
        unparsedArgs = unparsedArgs.subList(1, unparsedArgs.size());
        return unparsedArgs.stream().map(s -> s.toLowerCase()).collect(Collectors.toSet());
    }
}
