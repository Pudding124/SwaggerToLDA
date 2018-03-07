package ntou.swagger.algo;

/* 
 * Author: Hsuan-Ju Lin
 * Email: mis101bird@gmail.com
 * Github: https://github.com/mis101bird
 * 
 * Author: Ming-jen Xu 
 * Email: surprised128@gmail.com 
 * Github: https://github.com/Pudding124
 *  
 * Licensed under the MIT license. 
 * http://www.opensource.org/licenses/mit-license.php 
 * 
 */


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ntou.swagger.calculate.LDAReduction;

@Component
public class TokenizationAndStemming {

	Logger log = LoggerFactory.getLogger(TokenizationAndStemming.class);
	
	public LDAReduction LDAreduction = new LDAReduction(); //將 Token前後的結果放入裡面
	
	final List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
			"in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
			"these", "they", "this", "to", "was", "will", "with", "part", "http", "put", "swagger", "set",
			"up", "url", "url2", "get", "api", "api2", "long", "understand", "app", "add", "people", "provide", "provid",
			"start", "restful", "tful", "make", "www", "run", "html", "servic", "endpoint", "user");

	public String replaceTagsToNone(String input) {
		return input.replaceAll("<.*?>", " ").trim();
	}

	public ArrayList<String> changeCamelCaseToTerms(String[] container) {
		ArrayList<String> terms = new ArrayList<String>();
		for (String input : container) {
			for (String w : input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
				w = w.toLowerCase();
				if (!isStopWordOrNumber(w)) {
					terms.add(w);
				}
			}
		}
		return terms;
	}

	public String changeDotsToSeperateTerms(String input) {
		return input.replaceAll("\\.", " ").trim();
	}

	public String change_ToSeperateTerms(String input) {
		return input.replaceAll("_", " ").trim();
	}

	private static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public boolean isStopWordOrNumber(String input) {
		if (isNumeric(input)) {
			return true;
		}
		for (String s : stopWords) {
			if (s.equals(input)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<String> applyTokenization(String input) {
		if (input != null) {
			String preProccessInput = this.changeDotsToSeperateTerms(this.replaceTagsToNone(input));
			log.info("input before split: {}", preProccessInput);
			String[] tokens = preProccessInput.split(" +");
			ArrayList<String> finishCamels = this.changeCamelCaseToTerms(tokens);
			return finishCamels;
		} else {
			return null;
		}
	}

	public ArrayList<String> applyTokenizationAndStemming(String input) {

		HashMap<String, Boolean> repeated = new HashMap<String, Boolean>();
		ArrayList<String> tokens = new ArrayList<String>();
		// Define your attribute factory (or use the default) - same between 4.x
		// and 5.x
		Analyzer analyzer = new StopAnalyzer();
		TokenStream tokenStream;
		try {
			tokenStream = analyzer.tokenStream("contents", new StringReader(replaceTagsToNone(input)));
			tokenStream.reset();
			// Then process tokens - same between 4.x and 5.x
			TokenStream stemTerm = new PorterStemFilter(tokenStream);
			CharTermAttribute attr = stemTerm.addAttribute(CharTermAttribute.class);

			while (stemTerm.incrementToken()) {
				// Grab the term
				String term = new String(attr.buffer(), 0, attr.length());
				if (!repeated.containsKey(term)) {
					tokens.add(term);
					repeated.put(term, true);
				}
			}
			tokenStream.end();
			stemTerm.end();
		} catch (IOException e) {
			log.error("Error on parsing Tokenization And Stemming", e);
		}
		return tokens;

	}

	public String stemTermsAndSaveOriginalTerm(String term, HashMap<String, String> table) throws IOException {
		log.info("- stemming for LDA");
		StringBuilder builder = new StringBuilder();
		EnglishAnalyzer analyzer = new EnglishAnalyzer();
		TokenStream tokenStream = null;
		boolean x = true;
		for (String w : term.split(" +")) {
			
			w = w.replaceAll("-", ""); // 這邊是先避免掉有類似符號出現 例如:real-time
			
			tokenStream = analyzer.tokenStream("content", new StringReader(w));
			tokenStream.reset();
			CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()) {
				x = true; //必須重設, 不然只要有一個false, 其他就無法進入
				String t = new String(attr.buffer(), 0, attr.length()); //抓取斷詞完結果 不過是lucene分析後的斷詞
				log.info("-- {} --> {}", t, w);
				table.put(t, w);
				for(String s : stopWords) { //這邊是我自己寫的 比對stopword
					
					if(s.equals(w) || s.equals(t)) {
						x = false;
						log.info("丟棄:{}", w);
						break;
					}
				}
				
				if(x == true) {
					builder.append(t);
					builder.append(" ");
				}
			}
			tokenStream.close();
		}
		LDAreduction.setBeforeToken(table); //將token前後的詞都存入到這裡
		analyzer.close();
		return builder.toString().trim();
	}

	public ArrayList<String> applyArrayListOnStemming(ArrayList<String> inputs) {

		HashMap<String, Boolean> repeated = new HashMap<String, Boolean>();
		ArrayList<String> tokens = new ArrayList<String>();
		// Define your attribute factory (or use the default) - same between 4.x
		// and 5.x
		Analyzer analyzer = new StopAnalyzer();
		TokenStream tokenStream;
		
		ArrayList<String> delBottomLine = new ArrayList<String>(); //這邊是將底線換成空白
		for(String data : inputs) {
			data = data.replaceAll("_", " ");
			log.info("WordNet原始字詞:{}", data);
			delBottomLine.add(data);
		}
		LDAreduction.setWordNetReduction(delBottomLine);
		
		try {
			tokenStream = analyzer.tokenStream("contents",
					new StringReader(this.changeDotsToSeperateTerms(this.replaceTagsToNone(String.join(" ", inputs)))));
			tokenStream.reset();

			// Then process tokens - same between 4.x and 5.x
			TokenStream stemTerm = new PorterStemFilter(tokenStream);
			CharTermAttribute attr = stemTerm.addAttribute(CharTermAttribute.class);

			while (stemTerm.incrementToken()) {
				// Grab the term
				String term = new String(attr.buffer(), 0, attr.length());
				if (!repeated.containsKey(term)) {
					tokens.add(term);
					repeated.put(term, true);
				}
			}
			tokenStream.end();
			stemTerm.end();
		} catch (IOException e) {
			log.error("Error on parsing Tokenization And Stemming", e);
		}
		return tokens;

	}

}