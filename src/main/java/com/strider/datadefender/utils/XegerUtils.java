/**
 * Copyright 2009 Wilfred Springer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modified by Armenak Grigoryan
 */

package com.strider.datadefender.utils;

import java.util.Random;

public class XegerUtils {
    /**
     * Generates a random number within the given bounds.
     *
     * @param min The minimum number (inclusive).
     * @param max The maximum number (inclusive).
     * @param random The object used as the randomizer.
     * @return A random number in the given range.
     */
    public final static int getRandomInt(final int min, final int max, final Random random) {
        final int dif = max - min;
        final float number = random.nextFloat();              // 0 <= number < 1
        return min + Math.round(number * dif);
    }    
}