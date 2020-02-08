/*
 *
 * Copyright 2014-2020, Armenak Grigoryan, and individual contributors as indicated
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
 * This class represents the probability of sentence in database column
 * 
 * @author Armenak Grigoryan
 */
public class Probability {
    private final String sentence;
    private final Double probabilityValue;

    public Probability(final String sentence, final Double probabilityValue) {
        this.sentence         = sentence;
        this.probabilityValue = probabilityValue;
    }

    public Double getProbabilityValue() {
        return this.probabilityValue;
    }

    public String getSentence() {
        return this.sentence;
    }

    /**
     * @return comparator used for sorting
     */

  public static Comparator<Probability> compare() {
      return Comparator.comparing(Probability::getSentence)
              .thenComparing(Probability::getProbabilityValue);            
  }    
}
