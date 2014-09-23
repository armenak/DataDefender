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
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
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

    public static void main( String[] args ) throws ParseException, AnonymizerException {

        if (args.length == 0 ) {
            log.info("To display usage info please type");
            log.info("    java -jar DataAnonymizer.jar com.strider.DataAnonymyzer --help");
            return;
        }        

        final Options options = createOptions();
        final CommandLine line = getCommandLine(options, args);
        if (line.hasOption("help")) {
            help(options);
            return;
        }
        
        String databasePropertyFile = "db.properties";
        Properties props = null;
        if (line.hasOption("P")) {
            databasePropertyFile = line.getOptionValues("P")[0];
            try {
                props = loadProperties(databasePropertyFile);            
            } catch (IOException ioe) {
                throw new AnonymizerException("ERROR: Unable to load " + databasePropertyFile, ioe);
            }
        }
        if (props == null) {
            throw new AnonymizerException("ERROR: Database property file is not defined.");
        }
        
        String anonymizerPropertyFile = "anonymizer.properties";
        if (line.hasOption("A")) {
            anonymizerPropertyFile = line.getOptionValue("A");
        } 
        
        Properties anonymizerProperties = null;
        try {
            anonymizerProperties = loadProperties(anonymizerPropertyFile);
        } catch (IOException ioe) {
            throw new AnonymizerException("ERROR: Unable to load " + anonymizerPropertyFile, ioe);
        }
        if (anonymizerProperties == null) {
            throw new AnonymizerException("ERROR: Database property file is not defined.");
        }                    
        
        if (line.hasOption("a")) {
            IAnonymizer anonymizer = new DatabaseAnonymizer();
            anonymizer.anonymize(props, anonymizerProperties);
        }
    }
    
    /**
     * Parses command line arguments
     * @param options
     * @param args
     * @return CommandLine
     * @throws AnonymizerException 
     */
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
    
    /**
     * Creates options for the command line
     * 
     * @return Options
     */
    @SuppressWarnings("static-access")
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption( "h", "help", false, "Display help");        
        options.addOption( "a", "anonymize", false, "anonymize database" );
        options.addOption( "A", "anonymizer properties", true, "define anonymizer property file" );
        options.addOption( "P", "database properties", true, "define database property file" );
        return options;
    }
 
    /**
     * Displays help
     * 
     * @param Options 
     */
    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataAnonymizer", options);
    }    
}