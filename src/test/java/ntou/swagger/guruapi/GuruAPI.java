package ntou.swagger.guruapi;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ntou.swagger.algo.TokenizationAndStemming;
import ntou.swagger.calculate.LDAReduction;
import ntou.swagger.calculate.ParseOriginalConcepts;
import ntou.swagger.web.SwaggerEntry;
import ntou.swagger.web.WriteTxt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GuruAPI {
	
	Logger log = LoggerFactory.getLogger(GuruAPI.class);
	private String url = "https://api.apis.guru/v2/list.json";
		
	@Autowired
	ParseOriginalConcepts parseOriginalConcepts = new ParseOriginalConcepts();
	
	@Autowired
	WriteTxt writeTxt;
	
	@Autowired
	SwaggerEntry swaggerEntry;
	
	@Test
	public void requestOnlineSwaggers() {
		try {
			String response = requestGuruAPI();
			if(response != null){
				parseGuruAPIResponse(response);
			}else{
				log.error("request Guru API get response code error (no 200).");
			}
		} catch (Exception e) {
			log.error("request Guru API internet error.");
			e.printStackTrace();
		}
		
	}
	
	public String requestGuruAPI() throws Exception{  //獲取 Guru api swagger list 
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		//con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		StringBuffer response = new StringBuffer();
		if(responseCode == 200){
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		}else{
			return null;
		}
	}
	
	private void parseGuruAPIResponse(String response) throws JSONException{ //分析 Guru api swagger list 抓取每一個swagger url 
		
	    JSONObject JSONresponse = new JSONObject(response);
	    Iterator<String> keys = JSONresponse.keys();
	    while(keys.hasNext()){
	    	String key = (String) keys.next();
	    	log.info("In API: {}",key);
	    	
	    	String versionNew = JSONresponse.getJSONObject(key).getString("preferred");
	    	log.info("- API prefer version: {}",versionNew);
	    	
	    	JSONObject object = JSONresponse.getJSONObject(key).getJSONObject("versions");
	    	Iterator<String> versionkeys = object.keys();
	    	while(versionkeys.hasNext()){
	    		String vkey = (String) versionkeys.next();
		    	String swaggerUrl = object.getJSONObject(vkey).getString("swaggerUrl");
		    	log.info("-- API swagger url: {}", swaggerUrl);
		    	
		    	// run swagger parser.
		        transmitLatestVersionAndSwaggerUrlToParser(swaggerUrl, versionNew);
		    	
	    	}
	    }
	    
	    //------------------------------------------------------------------以下皆是將字詞出現頻率放入txt檔 和 排序
	    String[] storeLDAtext = new String[parseOriginalConcepts.tokenizationAndStemming.LDAreduction.getRecord().size()];
	    int[] storeLDACount = new int[parseOriginalConcepts.tokenizationAndStemming.LDAreduction.getRecord().size()];
	    int i = 0;
	    for(String data : parseOriginalConcepts.tokenizationAndStemming.LDAreduction.getRecord().keySet()) {
	    	storeLDAtext[i] = data;
	    	storeLDACount[i] = parseOriginalConcepts.tokenizationAndStemming.LDAreduction.getRecord().get(data);
			//writeTxt.inputTxt(data+" "+String.valueOf(parseOriginalConcepts.tokenizationAndStemming.LDAreduction.getRecord().get(data))+"\\r\n");
	    	i++;
		}
		
	    for(int x = storeLDACount.length;x>=0;x--) {
	    	for(int y = 0;y<x-1;y++) {
	    		if(storeLDACount[y]>storeLDACount[y+1]) {
	    			int tmpA = storeLDACount[y];
	    			String tmpB = storeLDAtext[y];
	    			storeLDACount[y] = storeLDACount[y+1];
	    			storeLDAtext[y] = storeLDAtext[y+1];
	    			storeLDACount[y+1] = tmpA;
	    			storeLDAtext[y+1] = tmpB;
	    		}
	    	}
	    }
	    
	    for(int z = 0;z<storeLDACount.length;z++) {
	    	writeTxt.inputTxt(storeLDAtext[z]+" "+storeLDACount[z]+"\r\n");
	    }
	    //---------------------------------------------------------------------------
	}
	
	protected void transmitLatestVersionAndSwaggerUrlToParser(String swaggerUrl, String latestVersion) {
		// 傳遞資料給Swagger to LDA parser

		for(int i=0;i<3;i++){
			try {
				String swaggerResponse = callForSwaggerStringByUrl(swaggerUrl);
				swaggerEntry.parseSwagger(swaggerResponse);
				System.gc();
				break;
				   
			} catch (Exception e) {
				log.error("Graph Database IO Error on {} times: {}", i,swaggerUrl,e);
			}
		
	} 
	}
	
	public String callForSwaggerStringByUrl(String urlString){ //抓取每個swagger url 的 資料回來分析
		
		URL url;
		String swaggerResponse = null;
		try {
			url = new URL(urlString);
			URLConnection connection;
			connection = url.openConnection();
			connection.setConnectTimeout(6000);  
			connection.setReadTimeout(6000);
			
			InputStream is = connection.getInputStream();
			swaggerResponse = getStringFromInputStream(is);
			log.info("Done {} to string response", urlString);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.error("MalformedURLException on swagger {}", urlString, e);
			return null;
		} catch (java.net.SocketTimeoutException e) {
			log.error("Socket Timeout Exception on swagger {}", urlString, e);
			   return null;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("IOException on swagger {}", urlString, e);
			return null;
		}
	
	return swaggerResponse;
	}
	
	// convert InputStream to String
			private String getStringFromInputStream(InputStream is) {

				BufferedReader br = null;
				StringBuilder sb = new StringBuilder();

				String line;
				try {

					br = new BufferedReader(new InputStreamReader(is));
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

				} catch (IOException e) {
					log.error("swagger request on InputStream Exception", e);
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							log.error("swagger request InputStream close Error", e);
						}
					}
				}

				return sb.toString();

			}

}
