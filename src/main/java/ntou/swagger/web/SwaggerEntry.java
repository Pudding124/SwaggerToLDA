package ntou.swagger.web;

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
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import ntou.swagger.algo.LDA;
import ntou.swagger.algo.TokenizationAndStemming;
import ntou.swagger.bean.Operations;
import ntou.swagger.bean.Resource;
import ntou.swagger.bean.UserResponseBean;
import ntou.swagger.calculate.ParseOriginalConcepts;

@RestController
@RequestMapping("/send")
public class SwaggerEntry {
	
	@Autowired
	ParseOriginalConcepts parseOriginalConcepts;
	
	TokenizationAndStemming tokenizationAndStemming = new TokenizationAndStemming();
	Logger log = LoggerFactory.getLogger(SwaggerEntry.class);

	@RequestMapping(value = "/swagger", method = RequestMethod.POST)
	public String parseSwagger(@RequestBody String swaggerDoc) throws IOException {
		
		Resource resource = new Resource(); //放置Resource的計算結果
		
		ArrayList<Operations> AllOperation = new ArrayList<Operations>(); //放置每個Operation計算後的所有結果
		
		UserResponseBean userResponseBean = new UserResponseBean();
		
		Gson gson = new Gson();
		Swagger swagger = new SwaggerParser().parse(swaggerDoc); //解析Swagger文件
		Map<String, Object> info = swagger.getInfo().getVendorExtensions();
		ArrayList<String> tags = new ArrayList<String>();
		
		//String[] ResourceConcept = new String[3];
		//ResourceConcept[0] = "Accu Weather API";
		//ResourceConcept[1] = "Accu Weather is one of the leading digital weather information providers. According to its website, AccuWeather provides weather forecasts for nearly 3 million locations worldwide, and over a billion people worldwide rely on AccuWeather every day.";
		
		// For resource concept
		String title = null;
		String description = null;
		
		// get title and description
		title = swagger.getInfo().getTitle();
		description = swagger.getInfo().getDescription();
		log.info("title:{}", title);
		log.info("description:{}", description);
		
		// get x-tags
		if (info.get("x-tags") != null) {
			Object infoTags = info.get("x-tags");
			if (infoTags instanceof ArrayList) {
				ArrayList<String> infoTagsNode = (ArrayList) infoTags;
				// assertEquals(infoTagsNode.get(0), "Azure");
				for (String tag : infoTagsNode) {
					tags.add(tag);
				}
			}
		}
		log.info("tags:{}", tags);
		
		ArrayList<String> RC_inputs = new ArrayList<String>();

		if (title != null) {
			RC_inputs.add(title);
			resource.setSwaggerTitle(title);
		}

		if (description != null) {
			RC_inputs.add(description);
			resource.setSwaggerDescription(description);
		}

		if (tags != null && tags.size() != 0) {
			String temp = "";
			for (String tag : tags) {
				temp += tag + " ";
			}
			RC_inputs.add(temp);
			resource.setSwaggerXtag(temp);
		}
		
		parseOriginalConcepts.RC_Calculate_OriginalConcepts(resource, RC_inputs.toArray(new String[0]), edu.mit.jwi.item.POS.NOUN); //將RC 相關資訊 丟入計算
		
		//------------------------------------------------------------------------------------------------
		//------------------------------------------------------------------------------------------------
		
		for (String p : swagger.getPaths().keySet()) {
			if (swagger.getPaths().get(p).getDelete() != null) {
				Operations operations = new Operations();  //每個不同的操作 都會自創一個新的Operation
				log.info("--- operation:DELETE on {}", p);

				io.swagger.models.Operation swaggerOperation = swagger.getPaths().get(p).getDelete();
				OC_Calculate(operations, swaggerOperation.getOperationId(), swaggerOperation.getDescription(), swaggerOperation.getSummary(), "delete");
				operations.setSwaggerPath("delete");
				operations.setSwaggerOperationId(swaggerOperation.getOperationId());
				operations.setSwaggerDescription(swaggerOperation.getDescription());
				operations.setSwaggerSummary(swaggerOperation.getSummary());
				AllOperation.add(operations); //在統一存入到Operation的ArrayList裡面
			}
			if (swagger.getPaths().get(p).getGet() != null) {
				Operations operations = new Operations();
				log.info("--- operation:GET on {}", p);

				io.swagger.models.Operation swaggerOperation = swagger.getPaths().get(p).getGet();
				OC_Calculate(operations, swaggerOperation.getOperationId(), swaggerOperation.getDescription(), swaggerOperation.getSummary(), "get");
				operations.setSwaggerPath("get");
				operations.setSwaggerOperationId(swaggerOperation.getOperationId());
				operations.setSwaggerDescription(swaggerOperation.getDescription());
				operations.setSwaggerSummary(swaggerOperation.getSummary());
				AllOperation.add(operations);
			}
			if (swagger.getPaths().get(p).getPatch() != null) {
				Operations operations = new Operations();
				log.info("--- operation:PATCH on {}", p);
				
				io.swagger.models.Operation swaggerOperation = swagger.getPaths().get(p).getPatch();
				OC_Calculate(operations, swaggerOperation.getOperationId(), swaggerOperation.getDescription(), swaggerOperation.getSummary(), "patch");
				operations.setSwaggerPath("patch");
				operations.setSwaggerOperationId(swaggerOperation.getOperationId());
				operations.setSwaggerDescription(swaggerOperation.getDescription());
				operations.setSwaggerSummary(swaggerOperation.getSummary());
				AllOperation.add(operations);
			}
			if (swagger.getPaths().get(p).getPost() != null) {
				Operations operations = new Operations();
				log.info("--- operation:POST on {}", p);

				io.swagger.models.Operation swaggerOperation = swagger.getPaths().get(p).getPost();
				OC_Calculate(operations, swaggerOperation.getOperationId(), swaggerOperation.getDescription(), swaggerOperation.getSummary(), "post");
				operations.setSwaggerPath("post");
				operations.setSwaggerOperationId(swaggerOperation.getOperationId());
				operations.setSwaggerDescription(swaggerOperation.getDescription());
				operations.setSwaggerSummary(swaggerOperation.getSummary());
				AllOperation.add(operations);
			}
			if (swagger.getPaths().get(p).getPut() != null) {
				Operations operations = new Operations();
				log.info("--- operation:PUT on {}", p);

				io.swagger.models.Operation swaggerOperation = swagger.getPaths().get(p).getPut();
				OC_Calculate(operations, swaggerOperation.getOperationId(), swaggerOperation.getDescription(), swaggerOperation.getSummary(), "put");
				operations.setSwaggerPath("put");
				operations.setSwaggerOperationId(swaggerOperation.getOperationId());
				operations.setSwaggerDescription(swaggerOperation.getDescription());
				operations.setSwaggerSummary(swaggerOperation.getSummary());
				AllOperation.add(operations);
			}
		}
		
		userResponseBean.setResource(resource);  //將所有結果丟塑並以Gson傳出
		userResponseBean.setOperation(AllOperation); 
		String result = gson.toJson(userResponseBean);
		return result;
	}
	
	public void OC_Calculate (Operations operations, String operationId, String description, String summary, String path) throws IOException { 
		ArrayList<String> OC_inputs = new ArrayList<String>();

		if (operationId != null) {
			OC_inputs.add(operationId);
		}
		if (description != null) {
			OC_inputs.add(description);
		}
		if (summary != null) {
			OC_inputs.add(summary);
		}

		if (path != null) {
			StringBuilder builder = new StringBuilder();
			String[] array = path.split("/");
			for (String s : array) {
				s = s.replaceAll("\\{", "");
				s = s.replaceAll("\\}", "");
				builder.append(s);
				builder.append(" ");
			}
			OC_inputs.add(builder.toString().trim());
		}
		
		parseOriginalConcepts.OC_Calculate_OriginalConcepts(operations, OC_inputs.toArray(new String[0]), edu.mit.jwi.item.POS.VERB); //將OC丟入計算

	}
	
	// ------------------------------------------------------------------------------------------------ 以下為SwaggerURL 的 access point
	
	@RequestMapping(value = "/swaggerURL", method = RequestMethod.POST)
	public String parseSwaggerURL(@RequestBody String swaggerURL) throws IOException {
		// 傳遞資料給Swagger to LDA parser

		String swaggerResponse = callForSwaggerStringByUrl(swaggerURL);
		System.gc();
		return parseSwagger(swaggerResponse);

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
