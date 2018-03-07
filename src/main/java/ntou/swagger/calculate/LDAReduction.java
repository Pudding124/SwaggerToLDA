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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ntou.swagger.bean.Operations;
import ntou.swagger.bean.Resource;
import ntou.swagger.guruapi.GuruAPI;

public class LDAReduction {

	private ArrayList<String> reduction;
	
	private ArrayList<String> wordNetReduction;
		
	private HashMap<String, String> beforeToken;
	
	public HashMap<String, Integer> Record = new HashMap<String, Integer>();
			
	public HashMap<String, String> getBeforeToken() {
		return beforeToken;
	}

	public void setBeforeToken(HashMap<String, String> beforeToken) {
		this.beforeToken = null; //清記憶體空間
		this.beforeToken = beforeToken;
	}

	public void setWordNetReduction(ArrayList<String> wordNetReduction) {
		this.wordNetReduction = null;
		this.wordNetReduction = wordNetReduction;
	}

	public HashMap<String, Integer> getRecord() {
		return Record;
	}

	public void compareAndStoreRCLDA(Resource resource, ArrayList<String> needReduction) {
		reduction = new ArrayList<String>();
		for(String data : needReduction) {
			
			for (String key : beforeToken.keySet()) {
				
				if(data.equals(key)) {
					String str = beforeToken.get(key).replaceAll("[\\pP\\p{Punct}]"," "); //去除所有標點符號 剩下字母 數字 中文
					reduction.add(str);
					//----------------------------------紀錄Guru文件每個api字詞分析結果的頻率
					if(Record.isEmpty()) {
						Record.put(str, 1);
					}else if(Record.isEmpty() == false) {
						boolean y = true;
						for(String storeTest : Record.keySet()) {
							if(storeTest.equals(str)) {
								Record.put(str, Record.get(storeTest)+1);
								y = false;
								break;
							}
						}
						
						if(y) {
							Record.put(str, 1);
						}
					}
					//----------------------------------------
				}
	        }
			
		}
		
		resource.setLDA(reduction);
	}
	
	public void saveRCWordNet(Resource resource) {
		resource.setWordNet(wordNetReduction);
	}
	
	public void compareAndStoreOPLDA(Operations operations, ArrayList<String> needReduction) {
		reduction = new ArrayList<String>();
		for(String data : needReduction) {
			
			for (String key : beforeToken.keySet()) {
				
				if(data.equals(key)) {
					reduction.add(beforeToken.get(key));
				}
	        }
			
		}
		
		operations.setLDA(reduction);
	}
	
	public void saveOPWordNet(Operations operations) {
		operations.setWordNet(wordNetReduction);
	}
	
}
