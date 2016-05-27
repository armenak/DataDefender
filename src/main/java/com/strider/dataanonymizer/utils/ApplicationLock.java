/*
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

package com.strider.dataanonymizer.utils;

import com.strider.dataanonymizer.AnonymizerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/**
 * Most of the code is copied from this page: http://www.rgagnon.com/javadetails/java-0288.html 
 * 
 * Modified by Armenak Grigoryan
 */
public class ApplicationLock {
    private final String appName;
    private File file;
    private FileChannel channel;
    private FileLock lock;
    
    private static final Logger log = getLogger(ApplicationLock.class);

    /**
     * Constructor
     * 
     * @param appName application name 
     */
    public ApplicationLock(String appName) {
        this.appName = appName;
    }

    /**
     * Returns true if there is another instance of the application is running.
     * Otherwise returns false.
     * 
     * @return boolean
     * @throws AnonymizerException 
     */
    public boolean isAppActive() throws AnonymizerException {
        try {
            file = new File
                 (System.getProperty("user.home"), appName + ".tmp");
            channel = new RandomAccessFile(file, "rw").getChannel();
            log.debug("Creating lock file " + file.getName());
            
            try {
                lock = channel.tryLock();
                log.debug("Locking file ...");
            } catch (OverlappingFileLockException | IOException e) {
                // already locked
                log.error("File  " + file.getName() + " already locket");
                closeLock();
                return true;
            }

            if (lock == null) {
                closeLock();
                return true;
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                    // destroy the lock when the JVM is closing
                    @Override
                    public void run() {
                        try {
                            log.debug("Closing lock file");
                            closeLock();
                            deleteFile();
                        } catch (AnonymizerException ae) {
                            log.error("Problem closing file lock");
                        }
                    }
                });
            return false;
        }
        catch (FileNotFoundException | AnonymizerException e) {
            try {
                closeLock();
                return true;
             } catch (AnonymizerException ae) {  
                throw new AnonymizerException("Problem releasing file lock", ae);
            }
        }
    }

    private void closeLock() throws AnonymizerException {
        try { 
            lock.release();  
        } catch (IOException e) {  
            throw new AnonymizerException("Problem releasing file lock", e);
        }
        
        try { 
            channel.close(); 
        } catch (IOException e) {  
            throw new AnonymizerException("Problem closing channel", e);
        }
    }

    private void deleteFile() throws AnonymizerException {
        try { 
            file.delete(); 
        } catch (Exception e) { 
            throw new AnonymizerException("Problem deleting lock file", e);
        }
    }    
    
}
