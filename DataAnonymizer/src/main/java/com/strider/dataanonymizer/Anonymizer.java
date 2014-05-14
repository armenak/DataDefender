package com.strider.dataanonymizer;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

/**
 * Entry point to Data Anonymizer. 
 * 
 * This class will parse and analyze the parameters and execute appropriate 
 * service.
 *
 */
public class DataAnonymizer 
{
    public static void main( String[] args )
    throws Exception {

        if (args.length == 0 ) {
            System.out.println("To display usage info please type");
            System.out.println("    java -jar DataAnonymizer.jar com.strider.DataAnonymyzer help");
            return;
        }        

        final Options options = createOptions();
        final CommandLine line = getCommandLine(options, args);
        if (line.hasOption("help")) {
            help(options);
            return;
        } else {
            //
        }          
        
    }
    
    private static CommandLine getCommandLine(final Options options, final String[] args)
    throws Exception {
        final CommandLineParser parser = new GnuParser();
        final CommandLine line;
 
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            help(options);
            throw new Exception("Unable to process command line arguments");
        }
 
        return line;
    }    
    
    @SuppressWarnings("static-access")
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption("help", false, "Display help");
        options.addOption( "a", "anonymize", false, "anonymize database" );
        options.addOption( "d", "discover", false, "discover candidate columns for anonymization" );
        options.addOption( "s", "semantic", false, "discover candidate columns for anonymization based on semantic algorithm" );
        options.addOption( "D", "property", true, "define property file" );
        return options;
    }
 
    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DataAnonymizer", options);
    }    
}