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
package com.strider.datadefender.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class calculates the risk score for an individual column and for entire data store.
 * 
 * @author Armenak Grigoryan
 */
public class Score {
    
    private static final int SCORE_LOW    = 1;
    private static final int SCORE_MEDIUM = 2;    
    private static final int SCORE_HIGH   = 3;
    
    private static final List<Integer> averageScore = new ArrayList<>();
    
    public String columnScore(final int rowCount) {
        
        int score = 0;
        if (rowCount == 0) {
            score = 0;
        } else if (rowCount > 0 && rowCount <= 100) {
            score = SCORE_LOW;
        } else if ( rowCount > 100 && rowCount <= 1000 ) {
            score = SCORE_MEDIUM;
        } else {
            score = SCORE_HIGH;            
        }
        
        averageScore.add(score);
        
        return getScoreDefinition(score);
    }
    
    public String dataStoreScore() {
        float averageDataStoreScore = 0;
        int tmp = 0;
        
        for (final int score: averageScore) {
            tmp += score;
        }
        
        if (averageScore != null && averageScore.size() > 0) {
            averageDataStoreScore = tmp/averageScore.size();
        }
        
        return getScoreDefinition(Math.round(averageDataStoreScore));
    }
    
    public String getScoreDefinition(final int score) {
        String scoreDefinition = "";
        
        if (score == 1) {
            scoreDefinition = "Low";
        } else if (score == 2) {
            scoreDefinition = "Medium";            
        } else if (score == 3) {
            scoreDefinition = "High";                        
        } else {
            scoreDefinition = "Undefined"; 
        }
        
        return scoreDefinition;
    }
}
