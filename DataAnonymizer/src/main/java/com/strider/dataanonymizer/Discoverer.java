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

import java.util.Properties;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import static com.strider.dataanonymizer.utils.AppProperties.loadPropertiesFromClassPath;

/**
 * Entry point to Data Discoverer utility. 
 * @author Armenak Grigoryan
 */
public class Discoverer {
    
    private static Logger log = getLogger(Discoverer.class);
    
    public static void main( String[] args ) throws AnonymizerException {

        // Define command line options  
        final Options options = createOptions();
        
        // 
        CommandLine line = null;
        try {
            line = getCommandLine(options, args);
        } catch (ParseException pe) {
            log.error(pe.toString());
        }
        
        
        if (line == null ||  line.hasOption("help")) {
            help(options); 
        } else if (line.hasOption("c")) {
            log.info("Column discovery in process");
            String databasePropertyFile = "db.properties";
            if (line.hasOption("P")) {
                databasePropertyFile = line.getOptionValue("P");
            } 
            Properties dbProperties = null;
            dbProperties = loadPropertiesFromClassPath(databasePropertyFile);
            if (dbProperties == null) {
                throw new AnonymizerException("ERROR: Database property file is not defined.");
            }            

            String columnPropertyFile = "columndiscovery.properties";
            if (line.hasOption("C")) {
                columnPropertyFile = line.getOptionValue("C");
            }
            Properties columnProperties = null;
            columnProperties = loadPropertiesFromClassPath(columnPropertyFile);
            if (columnProperties == null) {
                throw new AnonymizerException("ERROR: Column property file is not defined.");
            }                
            
            IDiscoverer discoverer = new ColumnDiscoverer();
            discoverer.discover(dbProperties, columnProperties);
        } else if (line.hasOption("d")) {
            log.info("Data discovery in process");
            
            String databasePropertyFile = "db.properties";
            if (line.hasOption("P")) {
                databasePropertyFile = line.getOptionValue("P");
            } 
            Properties dbProperties = null;
            dbProperties = loadPropertiesFromClassPath(databasePropertyFile);
            if (dbProperties == null) {
                throw new AnonymizerException("ERROR: Database property file is not defined.");
            }            
            
            String datadiscoveryPropertyFile = "datadiscovery.properties";
            if (line.hasOption("D")) {
                datadiscoveryPropertyFile = line.getOptionValue("D");
            }
            Properties dataDiscoveryProperties = null;
            dataDiscoveryProperties = loadPropertiesFromClassPath(datadiscoveryPropertyFile);
            if (dataDiscoveryProperties == null) {
                throw new AnonymizerException("ERROR: Data discovery property file is not defined.");
            }                            
            
            IDiscoverer discoverer = new DataDiscoverer();
            discoverer.discover(dbProperties, dataDiscoveryProperties);            
        } else {
            help(options);
        }
   }
   
  private static CommandLine getCommandLine(final Options options, final String[] args)
  throws ParseException {
        final CommandLineParser parser = new GnuParser();
        CommandLine line = null;
 
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            help(options);
        }
 
        return line;
    }    
    
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption( "h", "help", false, "Display help");
        options.addOption( "c", "column-discovery", false, "discover candidate column names for anonymization based on provided patterns" );
        options.addOption( "C", "column-properties", true, "define column property file" );                
        options.addOption( "d", "data-discovery", false, "discover candidate column for anonymization based on semantic algorithms" );        
        options.addOption( "D", "data-properties", true, "discover candidate column for anonymization based on semantic algorithms" );
        options.addOption( "P", "database-properties", true, "define database property file" );

        return options;
    }
 
    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataAnonymizer", options);
    }       
}
