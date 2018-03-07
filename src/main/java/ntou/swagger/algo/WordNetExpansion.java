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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.morph.WordnetStemmer;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import ntou.swagger.algo.ExToken.ExpandsionType;

@Component
public class WordNetExpansion {
	private IDictionary dict = null;
	private WordnetStemmer wordNetStemming;
	Logger log = LoggerFactory.getLogger(WordNetExpansion.class); 
	
	private String jwnlPropertiesPath = "./src/main/resources/wordnet_config.xml";
	private String wordNetPath = "/Users/xumingjen/WordNet-3.0/dict"; // "/home/mis101bird/WordNet/dict";

	private String[] stackOverflowWords = {"entity", "check", "rule", "limit", 
			"hold", "control", "restrict", "train", "suppress", "lock", "draw", "thermostat"};
	
	private double threshold = 0.9;
	File jwiFile;
	File jwnlFile;
	FileInputStream propertiesStream;
	
	public WordNetExpansion() {
		
		jwiFile = new File(wordNetPath);
		jwnlFile = new File(jwnlPropertiesPath);
		try {
			dict = new edu.mit.jwi.Dictionary(jwiFile);
			dict.open();
			propertiesStream = new FileInputStream(jwnlFile);
			JWNL.initialize(propertiesStream);
			wordNetStemming = new WordnetStemmer(dict);
			propertiesStream.close();
		}catch (Exception e) {
			log.error("Constructing WordNetExpansion failure!", e);
		}
	}

	public void openDic() throws IOException{
		if( dict != null){
		dict.open();
		}
	}
	
	private boolean isStackOverflowBug(String word){
		for(String cw :stackOverflowWords){
			if(cw.equals(word)){
				return true;
			}
		}
		return false;
	}
	
	public Hashtable<String, Double> getHypernymsByNounOrVerb(String str, edu.mit.jwi.item.POS pos) {

		Hashtable<String, Double> strScoreHT = new Hashtable<String, Double>();
		List<String> strStemmedList = wordNetStemming.findStems(str, pos);

		String strStemmed = "";
		if (strStemmedList.size() > 0) {
			strStemmed = strStemmedList.get(0);
		} else { // 表示在wordNet中查無此字，且沒有辦法stemming
			return strScoreHT;
		}
		if (!isStackOverflowBug(strStemmed)) {
			try {
				
				net.didion.jwnl.data.POS type = null;
				if(pos == edu.mit.jwi.item.POS.VERB){
					type = net.didion.jwnl.data.POS.VERB;
				}else if(pos == edu.mit.jwi.item.POS.NOUN){
					type = net.didion.jwnl.data.POS.NOUN;
				}
				
				IndexWord iWord = net.didion.jwnl.dictionary.Dictionary.getInstance()
						.getIndexWord(type, strStemmed);

				if (iWord != null) {
					for (Synset s : iWord.getSenses()) {
						Synset nowSynset = s; // 同義字集
						List l = PointerUtils.getInstance().getHypernymTree(nowSynset).toList();
						for (Object o : l) {

							PointerTargetNodeList pt = (PointerTargetNodeList) o;
							int depth = pt.size();
							int length = 0;
							for (Object o1 : pt) {
								PointerTargetNode ptn = (PointerTargetNode) o1;

								Synset ps = ptn.getSynset();
								--depth;

								for (Word w : ps.getWords()) {

									double simScore = similarityFormula(length, depth);

									threshold = 0.9;
									if (simScore >= threshold) {
										if (!w.getLemma().equals(strStemmed)) {
											String tmpLemma = w.getLemma();
											String lemma = tmpLemma;
											/*
											 * String[] tmpLemmaSplit =
											 * tmpLemma.split("[_]"); String
											 * lemma ="";
											 * if(tmpLemmaSplit.length>0){
											 * //會有複合字的情況name_and_address，把它合起來!
											 * for(String tmpStr :
											 * tmpLemmaSplit)
											 * lemma+=tmpStr.toLowerCase();
											 * }else{ lemma =
											 * tmpLemmaSplit[0].toLowerCase(); }
											 */

											if (!strScoreHT.containsKey(lemma)) {
												strScoreHT.put(lemma, simScore);
											} else { // 如果有重複的字，則查看simScore是否比原本在hashtable裡面的大，如果比較大則替換掉；如果沒有，則不動。
												double value = strScoreHT.get(lemma);
												if (simScore > value)
													strScoreHT.put(lemma, simScore);
											}
										}
									}
								}
								length++;
							}
						}
					}
				}
				// return strScoreHT;
			} catch (Exception e) {
				log.error("Error from WordNetExpansion.getHypernymsByNoun!!!", e);
			}
		}
		return strScoreHT;
	}

	public Hashtable<String, Double> getHyponymsByNounOrVerb(String str, edu.mit.jwi.item.POS pos) {
		Hashtable<String, Double> strScoreHT = new Hashtable<String, Double>();
		List<String> strStemmedList = wordNetStemming.findStems(str, pos);
		String strStemmed = "";
		if (strStemmedList.size() > 0) {
			strStemmed = strStemmedList.get(0);
		} else { // 表示在wordNet中查無此字，且沒有辦法stemming
			return strScoreHT;
		}
		if (!isStackOverflowBug(strStemmed)) {
			try {
				net.didion.jwnl.data.POS type = null;
				if(pos == edu.mit.jwi.item.POS.VERB){
					type = net.didion.jwnl.data.POS.VERB;
				}else if(pos == edu.mit.jwi.item.POS.NOUN){
					type = net.didion.jwnl.data.POS.NOUN;
				}
				
				IndexWord iWord = net.didion.jwnl.dictionary.Dictionary.getInstance()
						.getIndexWord(type, strStemmed);
				// System.out.println(iWord.getSenseCount());
				// 先算各個Synset到root(entity)的深度
				if (iWord != null) {
					ArrayList<Integer> synsetDepthList = new ArrayList<Integer>();
					for (Synset s : iWord.getSenses()) {
						Synset sourceSynset = s;

						List l = PointerUtils.getInstance().getHypernymTree(sourceSynset).toList();
						for (Object o : l) {
							PointerTargetNodeList pt = (PointerTargetNodeList) o;
							int depth = 0;
							// int length = 0;
							for (Object o1 : pt) {
								PointerTargetNode ptn = (PointerTargetNode) o1;
								Synset ps = ptn.getSynset();
								depth++;

							}
							synsetDepthList.add((--depth));
						}
					}

					// 再開始探索hyponyms

					// IndexWord iWord =
					// net.didion.jwnl.dictionary.Dictionary.getInstance().getIndexWord(net.didion.jwnl.data.POS.NOUN,
					// strStemmed);
					// System.out.println(iWord.getSenseCount());
					for (Synset s : iWord.getSenses()) {
						Synset sourceSynset = s; // 同義字集

						// IndexWord tIWord =
						// Dictionary.getInstance().getIndexWord(POS.NOUN,
						// "abstract entity");

						// Relationship r =
						// RelationshipFinder.getInstance().findRelationships(sourceSynset,
						// tIWord.getSense(1) ,
						// PointerType.HYPERNYM).getShallowest();
						List l = PointerUtils.getInstance().getHyponymTree(sourceSynset).toList();
						int depth2RootIndex = 0;
						for (Object o : l) {
							PointerTargetNodeList pt = (PointerTargetNodeList) o;
							int depth = synsetDepthList.get(depth2RootIndex);
							// depth2RootIndex++;
							int length = 0;
							for (Object o1 : pt) {
								PointerTargetNode ptn = (PointerTargetNode) o1;
								// System.out.println(ptn);
								Synset ps = ptn.getSynset();
								// System.out.print("Depth "+ depth +"\t,");
								for (Word w : ps.getWords()) {
									// System.out.print("length from
									// \""+strStemmed+"\" to
									// \""+w.getLemma()+"\"="+length+", ");
									double simScore = similarityFormula(length, depth);

									// double threshold =
									// Double.parseDouble((String)appProperties.get("similirtyThreshold"));
									if (simScore >= threshold) {
										if (!w.getLemma().equals(strStemmed)) {
											String tmpLemma = w.getLemma();
											String lemma = tmpLemma;
											/*
											 * String[] tmpLemmaSplit =
											 * tmpLemma.split("[_]"); String
											 * lemma ="";
											 * if(tmpLemmaSplit.length>0){
											 * //會有複合字的情況name_and_address，把它合起來!
											 * for(String tmpStr :
											 * tmpLemmaSplit)
											 * lemma+=tmpStr.toLowerCase();
											 * }else{ lemma =
											 * tmpLemmaSplit[0].toLowerCase(); }
											 */

											if (!strScoreHT.containsKey(lemma)) {
												strScoreHT.put(lemma, simScore);
											} else { // 如果有重複的字，則查看simScore是否比原本在hashtable裡面的大，如果比較大則替換掉；如果沒有，則不動。
												double value = strScoreHT.get(lemma);
												if (simScore > value)
													strScoreHT.put(lemma, simScore);
											}
										}
									}
								}
								depth++;
								length++;
								// System.out.println();
							}
						}
						// System.out.println();
					}
				}
				// return strScoreHT;
			} catch (Exception e) {
				log.error("Error from WordNetExpansion.getHyponymsByNoun OR Verb!!!", e);
			}
		}
		return strScoreHT;

	}

	private double similarityFormula(double length, double depth) {
		// double lw =
		// Double.parseDouble((String)appProperties.getProperty("lengthWeight"));
		// double dw =
		// Double.parseDouble((String)appProperties.getProperty("depthWeight"));
		double lw = 0.2;
		double dw = 0.6;

		// length的分數公式
		double lFunc = Math.exp((0 - lw) * length);
		// System.out.println("lFunc:"+lFunc);
		double posDExp = Math.exp(dw * depth);
		// System.out.println("posDExp:"+posDExp);
		double negDExp = Math.exp((0 - dw) * depth);
		// System.out.println("negDExp:"+negDExp);
		// depth的分數公式
		double dFunc = (posDExp - negDExp) / (posDExp + negDExp);
		// System.out.println("dFunc:"+dFunc);
		// s = f(l)*f(d)
		double score = lFunc * dFunc;
		// System.out.println("f(l) = "+lFunc);
		// System.out.println("f(d) = "+dFunc);
		// System.out.println("score = "+score);
		BigDecimal b = new BigDecimal(score);
		double afterRound = b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
		return afterRound;
	}

	private ArrayList<ExToken> getDeduplicationExansion(Hashtable<String, Double> exHypernymsHT,
			Hashtable<String, Double> exHyponymsHT) {// By 小貝
		// 取得擴充字詞
		Hashtable<String, ExToken> wExTokens = new Hashtable<String, ExToken>();
		ArrayList<ExToken> exTokens = new ArrayList<ExToken>();
		// ArrayList<String> expansionList = new ArrayList<String>();
		// Hashtable<String, Double> exHypernymsHT =
		// wordNetEx.getHypernymsByNoun(tpStr);
		// Hashtable<String, Double> exHyponymsHT =
		// wordNetEx.getHyponymsByNoun(tpStr);

		// FileWriter fw1 = new FileWriter(new
		// File("C:\\Users\\phenol\\Desktop\\Code\\demo\\wordNetExpansion\\"+queryTerm+"-exHypernyms.txt"));
		// FileWriter fw2 = new FileWriter(new
		// File("C:\\Users\\phenol\\Desktop\\Code\\demo\\wordNetExpansion\\"+queryTerm+"-exHyponyms.txt"));
		// exTermsHT.putAll(exHypernymsHT.entrySet());
		// 把exHypernymsHT與exHyponymsHT放進exTermsScoreHT
		Iterator hyperIt = exHypernymsHT.keySet().iterator(); // exHypernymsHT
		while (hyperIt.hasNext()) {
			String key = (String) hyperIt.next();
			double exWeight = exHypernymsHT.get(key);

			BigDecimal b = new BigDecimal(exWeight);
			exWeight = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			// System.out.println("docName:"+key+" score:"+afterRound);

			// fw1.write(key+":"+exWeight+"\n");
			if (!wExTokens.contains(key)) { // 不包含
				ExToken exToken = new ExToken(key, 1, exWeight, ExpandsionType.wordnet);

				wExTokens.put(key, exToken);
			}
		}

		Iterator hyponIt = exHyponymsHT.keySet().iterator(); // exHyponymsHT
		while (hyponIt.hasNext()) {
			String key = (String) hyponIt.next();
			double exWeight = exHyponymsHT.get(key);
			BigDecimal b = new BigDecimal(exWeight);
			exWeight = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

			if (!wExTokens.contains(key)) { // 不包含
				ExToken exToken = new ExToken(key, 1, exWeight, ExpandsionType.wordnet);

				wExTokens.put(key, exToken);
			}
		}
		// 整理HashTable 為ArrayList
		Iterator wExTIt = wExTokens.keySet().iterator(); // exHyponymsHT
		while (wExTIt.hasNext()) {
			String key = (String) wExTIt.next();
			ExToken exT = wExTokens.get(key);
			exTokens.add(exT);
		}

		return exTokens;
	}

	public ArrayList<ExToken> expand(String word, edu.mit.jwi.item.POS pos) {
		//WordNetExpansion wne = new WordNetExpansion();

		// System.out.println("getHypernymsByNoun");
		Hashtable<String, Double> ht = this.getHypernymsByNounOrVerb(word, pos);
		Set<String> ss = ht.keySet();

		for (Iterator<String> it = ss.iterator(); it.hasNext();) {
			String str = it.next();
			double score = ht.get(str);
			// System.out.println("Hypernyms: "+str+": "+score);
		}

		Hashtable<String, Double> hyponymsHT = this.getHyponymsByNounOrVerb(word, pos);
		Set<String> s = hyponymsHT.keySet();

		for (Iterator<String> it = s.iterator(); it.hasNext();) {
			String str = it.next();
			double score = hyponymsHT.get(str);
			// System.out.println("hyponyms: "+str+": "+score);
		}

		ArrayList<ExToken> exTokens = this.getDeduplicationExansion(ht, hyponymsHT);

		for (ExToken expasionTerm : exTokens) {

			log.info("wordnet expansion word: {} -- weight: {}", expasionTerm.getToken(), expasionTerm.getMaxWeight());
		}
		// System.out.println(wne.similarityFormula(0, 7));
		return exTokens;
	}

	public ArrayList<ExToken> expand(ArrayList<String> words, edu.mit.jwi.item.POS pos) {
		ArrayList<ExToken> wordsAllExT = new ArrayList<ExToken>();
		for (int index=0;index<words.size();index++) {
			ArrayList<ExToken> exTokens = new ArrayList<ExToken>();

			try {
					exTokens = expand(words.get(index), pos);
					wordsAllExT.addAll(exTokens);
			}catch (IllegalArgumentException e) {
				System.out.println("WordNet 無法擴充");
				System.out.println(e.getMessage());
			}
		}
		return wordsAllExT;
	}
	
	// The hashMap table is for "stemming term --> original term" mapping
	public ArrayList<ExToken> expandWithHashMapTable(ArrayList<String> words, edu.mit.jwi.item.POS pos, HashMap<String, String> table) {
		ArrayList<ExToken> wordsAllExT = new ArrayList<ExToken>();
		for (int index=0;index<words.size();index++) {
			ArrayList<ExToken> exTokens = new ArrayList<ExToken>();

			try {
					exTokens = expand(words.get(index), pos);
					wordsAllExT.addAll(exTokens);
			}catch (IllegalArgumentException e) {
				String originalTerm = table.get(words.get(index));
				if(originalTerm != null){
					log.info("{} 無法擴充 --> 尋找original term {} 來擴充", words.get(index), originalTerm);
					exTokens = expand(originalTerm, pos);
					wordsAllExT.addAll(exTokens);
				}else{
					log.info("{} 無法擴充，也找不到original term!", words.get(index));
				}
			}
		}
		return wordsAllExT;
	}
	
	public void closeDic(){
		dict.close();
	}
}
