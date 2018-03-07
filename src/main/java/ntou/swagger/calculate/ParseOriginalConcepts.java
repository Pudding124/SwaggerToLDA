package ntou.swagger.calculate;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ntou.swagger.algo.ExToken;
import ntou.swagger.algo.LDA;
import ntou.swagger.algo.TokenizationAndStemming;
import ntou.swagger.algo.WordNetExpansion;
import ntou.swagger.bean.Operations;
import ntou.swagger.bean.Resource;
import ntou.swagger.web.SwaggerEntry;
import ntou.swagger.web.WriteTxt;
import ntou.swagger.bean.UserResponseBean;
@Component
public class ParseOriginalConcepts {
	
	@Autowired
	WordNetExpansion wordNet;
		
	public TokenizationAndStemming tokenizationAndStemming = new TokenizationAndStemming();
	
	Logger log = LoggerFactory.getLogger(ParseOriginalConcepts.class);
		
	public void RC_Calculate_OriginalConcepts(Resource resource, String[] ResourceConcept, edu.mit.jwi.item.POS type) throws IOException {
		
		LDA lda = new LDA();
		// For saving key: stemming term --> value: original term
		HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();
		// For filtering out repeated concept in original concept list and wordnet concept list
		HashMap<String, Boolean> filterRepeatedTerm = new HashMap<String, Boolean>();
		
		// pre-processing for inputs on LDA
		log.info("pre-processing for inputs on LDA");
		
		for (int i = 0; i < ResourceConcept.length; i++) {
			String terms = change_ToSeperateTerms(
				changeDotsToSeperateTerms(changeCamelWordsToSeperateTerms(replaceTagsToNone(ResourceConcept[i]))));
			ResourceConcept[i] = tokenizationAndStemming.stemTermsAndSaveOriginalTerm(terms, stemmingAndTermsTable);
			log.info(" -- {}", ResourceConcept[i]);
		}
		// Apply LDA and get ArrayList
		ArrayList<String> map = new ArrayList<String>(); // original concepts
														 // from LDA
		
		List<Map<String, Integer>> l = lda.apply(ResourceConcept, 3);
		for(Map<String, Integer> temp :l){
			int index = 1;
			log.info("---- LDA Topic response ----");
			for (String key : temp.keySet()) {
				if (!filterRepeatedTerm.containsKey(key)) {
					log.info(key);
					map.add(key);
					filterRepeatedTerm.put(key, new Boolean(true));
					if (index < 2) {
						index++;
					} else {
						break;
					}
				}
			}
		}
		
		//resource.setLDA(map);
		tokenizationAndStemming.LDAreduction.compareAndStoreRCLDA(resource, map); //還原 LDA被token前的結果
		log.info("RC LDA 結果:{}", map); //RC LDA結果
		
		parseExTokenToString(wordNet.expandWithHashMapTable(map, type, stemmingAndTermsTable), filterRepeatedTerm);
		tokenizationAndStemming.LDAreduction.saveRCWordNet(resource);
		//resource.setWordNet(parseExTokenToString(wordNet.expandWithHashMapTable(map, type, stemmingAndTermsTable), filterRepeatedTerm));
		log.info("RC WordNet 結果:{}", resource.getWordNet()); //RC WordNet回傳結果
	}
	
public void OC_Calculate_OriginalConcepts(Operations operations, String[] OperationConcept, edu.mit.jwi.item.POS type) throws IOException {
		
		LDA lda = new LDA();
		// For saving key: stemming term --> value: original term
		HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();
		// For filtering out repeated concept in original concept list and wordnet concept list
		HashMap<String, Boolean> filterRepeatedTerm = new HashMap<String, Boolean>();
		
		// pre-processing for inputs on LDA
		log.info("pre-processing for inputs on LDA");
		for (int i = 0; i < OperationConcept.length; i++) {
			String terms = change_ToSeperateTerms(
				changeDotsToSeperateTerms(changeCamelWordsToSeperateTerms(replaceTagsToNone(OperationConcept[i]))));
			OperationConcept[i] = tokenizationAndStemming.stemTermsAndSaveOriginalTerm(terms, stemmingAndTermsTable);
			log.info(" -- {}", OperationConcept[i]);
		}
		// Apply LDA and get ArrayList
		ArrayList<String> map = new ArrayList<String>(); // original concepts
														 // from LDA
		
		List<Map<String, Integer>> l = lda.apply(OperationConcept, 3);
		for(Map<String, Integer> temp :l){
			int index = 1;
			log.info("---- LDA Topic response ----");
			for (String key : temp.keySet()) {
				if (!filterRepeatedTerm.containsKey(key)) {
					log.info(key);
					map.add(key);
					filterRepeatedTerm.put(key, new Boolean(true));
					if (index < 2) {
						index++;
					} else {
						break;
					}
				}
			}
		}
		
		//operation.setLDA(map); 這邊是提取詞幹後的輸出
		tokenizationAndStemming.LDAreduction.compareAndStoreOPLDA(operations, map);
		log.info("OC LDA 結果:{}", map);
		
		parseExTokenToString(wordNet.expandWithHashMapTable(map, type, stemmingAndTermsTable), filterRepeatedTerm);
		tokenizationAndStemming.LDAreduction.saveOPWordNet(operations);
		//operation.setWordNet(parseExTokenToString(wordNet.expandWithHashMapTable(map, type, stemmingAndTermsTable), filterRepeatedTerm));
		log.info("OC WordNet 結果:{}", operations.getWordNet());
	}

	//--------------------------------------------------------------------------------LDA 跟 斷詞等
	
	private String replaceTagsToNone(String input) {
		return input.replaceAll("<.*?>", " ").trim();
	}
	
	private String changeCamelWordsToSeperateTerms(String input) {
		String[] data = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		StringBuilder builder = new StringBuilder();
		for (String w : data) {
			builder.append(w.toLowerCase());
			builder.append(" ");
		}
		return builder.toString().trim();
	}
	
	private String changeDotsToSeperateTerms(String input) {
		return input.replaceAll("\\.", " ").trim();
	}
	
	private String change_ToSeperateTerms(String input) {
		return input.replaceAll("_", " ").trim();
	}
	
	//--------------------------------------------------------------------------------WordNet
		
	private ArrayList<String> parseExTokenToString(ArrayList<ExToken> extokens, HashMap<String, Boolean> repeatedMap) {
		ArrayList<String> temp = new ArrayList<String>();
		for (ExToken ex : extokens) {
			temp.add(ex.getToken());
		}
		ArrayList<String> result = tokenizationAndStemming.applyArrayListOnStemming(temp);
		
		// filter repeated terms and original concept's terms
		for(String term :new ArrayList<String>(result)){
			if(repeatedMap.containsKey(term)){
				result.remove(term);
			}else{
				repeatedMap.put(term, new Boolean(true));
			}
		}
		return result;
	}
}
