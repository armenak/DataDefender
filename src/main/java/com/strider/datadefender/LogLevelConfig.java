/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

import picocli.CommandLine.Option;

import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Zaahid Bateson <zaahid.bateson@ubc.ca>
 */
@Log4j2
public class LogLevelConfig {

    private boolean isDebug = false;
    private boolean isVerbose = false;

    @Option(names = "--debug", description = "enable debug logging")
    public void setDebug(boolean debug) {
        if (!isDebug) {
            System.out.println("DEBUG logging turned on. DEBUG level messages only "
                + "appear in the log file by default.");
            Configurator.setRootLevel(Level.DEBUG);
            log.warn("Private/sensitive data that should be anonymized will be "
                + "logged to configured debug output streams.");
            isDebug = true;
        }
    }

    @Option(names = { "-v", "--verbose" }, description = "enable more verbose output")
    public void setVerbose(boolean verbose) {
        if (!isVerbose) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            LoggerConfig conf = ctx.getLogger("com.strider.datadefender").get();
            Map<String, Appender> appenders = conf.getAppenders();
            List<AppenderRef> refs = conf.getAppenderRefs();
            for (AppenderRef ref : refs) {
                if (ref.getLevel().isMoreSpecificThan(Level.INFO)) {
                    conf.removeAppender(ref.getRef());
                    conf.addAppender(appenders.get(ref.getRef()), Level.INFO, null);
                }
            }
            isVerbose = true;
        }
    }

}
