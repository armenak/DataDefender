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
package com.strider.datadefender.anonymizer.functions;

import com.strider.datadefender.functions.NamedParameter;
import com.strider.datadefender.requirement.registry.RequirementFunction;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class Lipsum extends RequirementFunction {

    private Random rand = new Random();
    private static final List<String> lipsumParagraphs = new ArrayList<>();

    static {
        final BufferedReader br = new BufferedReader(new InputStreamReader(Lipsum.class.getResourceAsStream("lipsum.txt")));
        final StringBuilder sb = new StringBuilder();
        try {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.trim().length() == 0) {
                    lipsumParagraphs.add(sb.toString());
                    sb.setLength(0);
                    continue;
                }
                sb.append(line);
            }
        } catch (IOException ex) {
            log.fatal("Error initializing lipsum.txt", ex);
            System.exit(1);
        }
    }

    /**
     * Generates between min and max (inclusive) lorem ipsum sentences.
     *
     * The sentences are generated from the beginning of paragraphs, although
     * the first paragraph chosen is random.  Paragraphs are joined together to
     * form sentences without line breaks.
     *
     * @param min Minimum number of sentences to generate
     * @param max Maximum number of sentences to generate
     * @return the generated sentences.
     * @throws IOException if an error occurs reading from the lipsum text file
     */
    public String sentences(
        @NamedParameter("pattern") int min,
        @NamedParameter("pattern") int max
    ) throws IOException {

        final List<String> lp = lipsumParagraphs;
        final StringBuilder sb = new StringBuilder();

        final int nSentences = max - rand.nextInt((max + 1) - min);
        String separator = "";
        mainLoop:
        for (int i = 0, start = rand.nextInt(lp.size()); i < nSentences; ++start) {
            final String para = lp.get(start % lp.size());
            final String[] sentences = para.split("\\.");
            for (String s : sentences) {
                s = s.trim().replaceAll("\\s+", " ");
                if (s.isEmpty()) {
                    sb.append('.');
                    continue;
                }

                sb.append(separator).append(s).append('.');
                separator = " ";
                ++i;

                if (i == nSentences) {
                    break mainLoop;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Generates the specified number of paragraphs.
     *
     * The paragraphs are generated from the loaded lorem ipsum text.  The start
     * position of the text is randomized, however the following paragraphs
     * appear in sequence - restarting at the beginning if all paragraphs have
     * been used.
     *
     * @param paragraphs the number of paragraphs to generate
     * @return the paragraphs
     * @throws IOException if an error occurs reading from the lipsum text file
     */
    public String paragraphs(@NamedParameter("paragraphs") int paragraphs) throws IOException {
        final List<String> lp = lipsumParagraphs;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, start = rand.nextInt(lp.size()); i < paragraphs; ++i, ++start) {
            sb.append(lp.get(start % lp.size())).append("\r\n\r\n");
        }
        return sb.toString().trim();
    }

    /**
     * Generates an amount of text similar to the text passed as a parameter.
     *
     * The method counts the number of paragraphs in the text, generating the
     * same number of "lorem ipsum" paragraphs.  If the text doesn't contain
     * paragraphs, the method counts the number of sentences and generates a
     * similar amount of sentences (+/- 1 sentence).
     *
     * @param text the text to use as a basis for generation
     * @return the generated lorem ipsum text
     * @throws IOException if an error occurs reading from the lipsum text file
     */
    public String similar(@NamedParameter("text") String text) throws IOException {
        final String sParas = text.replaceAll("\r\n", "\n");
        final int nParas = StringUtils.countMatches(sParas, "\n");
        if (nParas > 0) {
            final StringBuilder sb = new StringBuilder();
            for (final String para : sParas.split("\n")) {
                if (para.trim().isEmpty()) {
                    sb.append("\r\n");
                    continue;
                }
                sb.append(similar(para)).append("\r\n");
            }
            return sb.toString().trim();
        }
        final int nSent = StringUtils.countMatches(text.replaceAll("\\.{2,}", "."), ".");
        return sentences(Math.max(1, nSent - 1), Math.max(1, nSent + 1));
    }
}
