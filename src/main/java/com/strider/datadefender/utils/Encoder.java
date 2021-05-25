/*
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
 */
package com.strider.datadefender.utils;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.log4j.Log4j2;

/**
 * This class implements determenistic data anonymization 
 * by encoding the data using "salt" and decoding it.
 * 
 * @author Armenak Grigoryan
 */
@Log4j2
public final class Encoder {
    
    static final String salt = "paXwBqVDN6KJEG7u4JNaEEcMnJMEdqM3";
    
    public String encode(String data) {
        Key key = new SecretKeySpec(salt.getBytes(), "AES");
        byte[] encryptedData = null;
        
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedData = cipher.doFinal(data.getBytes());        
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | 
                 BadPaddingException | NoSuchPaddingException ex) {
            log.error("Problem encrypting data", ex);
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< encryptedData.length; i++) {
            char c = (char) encryptedData[i];
            sb.append(c);
        }
        
        return sb.toString();
        
    }
    
    public String decode(byte[] data) {
        Key key = new SecretKeySpec(salt.getBytes(), "AES");
        
        StringBuilder sb = new StringBuilder();
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] decryptedData = cipher.doFinal(data);        
            for (int i=0; i<decryptedData.length; i++) {
                char c = (char) decryptedData[i];
                sb.append(c);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | 
                 BadPaddingException | NoSuchPaddingException ex) {
            log.error("Problem encrypting data", ex);
        }
        
        return sb.toString();
    }
    
}
