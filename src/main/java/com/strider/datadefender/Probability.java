/*
 * 
 * Copyright 2014-2015, Armenak Grigoryan, and individual contributors as indicated
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

import java.util.Comparator;

/**
 *
 * @author strider
 */
public class Probability {
    private final String sentence;
    private final Double probabilityValue;
    
    public Probability(String sentence, Double probabilityValue) {
        this.sentence = sentence;
        this.probabilityValue = probabilityValue;
    }
    
    public String getSentence() {
        return this.sentence;
    }
    
    public Double getProbabilityValue() {
        return this.probabilityValue;
    }
    
    /**
     * @return comparator used for sorting
     */
//    public static Comparator<Probability> compare() {
//        return Comparator.comparing(Probability::getProbabilityValue)
//                .thenComparing(Probability::getSentence);            
//    }    
    
}
