package com.strider.dataanonymizer.requirement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class Demo {

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        findLocation();
    }
    
//    private void loadFile() throws IOException {
//        InputStream modelIn = new FileInputStream("/Users/sdi/work/strider/DataAnonymizer/DataAnonymizer/src/main/resources/en-ner-person.bin");
//        TokenNameFinderModel model = new TokenNameFinderModel(modelIn);        
//        NameFinderME nameFinder = new NameFinderME(model);
//        
//        String sentences[] = sentenceDetector.sentDetect(documentStr);
//        for (String sentence : sentences) {
//            String tokens[] = tokenizer.tokenize(sentence);
//            Span nameSpans[] = nameFinder.find(tokens);
//            // do something with the names
//            System.out.println("Found entity: " + Arrays.toString(Span.spansToStrings(nameSpans, tokens)));
//        }        
//        
//        for (String document[][] : documents) {
//            for (String[] sentence : document) {
//                Span nameSpans[] = nameFinder.find(sentence);
//                // do something with the names
//            }
//
//            nameFinder.clearAdaptiveData();
//        }
//	        
//    }
    
    public static void findLocation() {
        	 String sentence = "Jack London is the author of what novel?";
InputStream modelInToken = null;
InputStream modelIn = null;
try {
//1. convert sentence into tokens
modelInToken = new FileInputStream("/Users/sdi/work/strider/DataAnonymizer/DataAnonymizer/src/main/resources/en-token.bin");
TokenizerModel modelToken = new TokenizerModel(modelInToken);
Tokenizer tokenizer = new TokenizerME(modelToken);
String tokens[] = tokenizer.tokenize(sentence);
 
//2. find names
modelIn = new FileInputStream("/Users/sdi/work/strider/DataAnonymizer/DataAnonymizer/src/main/resources/en-ner-person.bin");
TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
NameFinderME nameFinder = new NameFinderME(model);
Span nameSpans[] = nameFinder.find(tokens);
//find probabilities for names
double[] spanProbs = nameFinder.probs(nameSpans);
//3. print names
for( int i = 0; i<nameSpans.length; i++) {
System.out.println("Span: "+nameSpans[i].toString());
System.out.println("Covered text is: "+tokens[nameSpans[i].getStart()] + " " + tokens[nameSpans[i].getStart()+1]);
System.out.println("Probability is: "+spanProbs[i]);
}
//Span: [0..2) person
//Covered text is: Jack London
//Probability is: 0.7081556539712883
}
catch (Exception ex) {}
finally {
try { if (modelInToken != null) modelInToken.close(); } catch (IOException e){};
try { if (modelIn != null) modelIn.close(); } catch (IOException e){};
}
}           
}