/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package com.strider.datadefender.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import com.strider.datadefender.DataDefenderException;
import lombok.extern.log4j.Log4j2;

/**
 * Most of the code is copied from this page: http://www.rgagnon.com/javadetails/java-0288.html
 *
 * Modified by Armenak Grigoryan
 */
@Log4j2
public class ApplicationLock {

    private final String        appName;
    private File                file;
    private FileChannel         channel;
    private FileLock            lock;

    /**
     * Constructor
     *
     * @param appName application name
     */
    public ApplicationLock(final String appName) {
        this.appName = appName;
    }

    private void closeLock() throws DataDefenderException {
        try {
            lock.release();
        } catch (IOException e) {
            throw new DataDefenderException("Problem releasing file lock", e);
        }

        try {
            channel.close();
        } catch (IOException e) {
            throw new DataDefenderException("Problem closing channel", e);
        }
    }

    private void deleteFile() {
        file.delete();
    }

    /**
     * Returns true if there is another instance of the application is running.
     * Otherwise returns false.
     *
     * @return boolean
     * @throws com.strider.datadefender.DataDefenderException
     */
    public boolean isAppActive() throws DataDefenderException {
        try {
            file    = new File(System.getProperty("user.home"), appName + ".tmp");
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
                                            } catch (DataDefenderException ae) {
                                                log.error("Problem closing file lock");
                                            }
                                        }
                                    });

            return false;
        } catch (FileNotFoundException fnfe) {
            closeLock();

            return true;
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
