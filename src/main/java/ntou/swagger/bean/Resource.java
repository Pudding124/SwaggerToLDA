package ntou.swagger.bean;

import java.util.ArrayList;

public class Resource {

	private String swaggerTitle;
	private String swaggerDescription;
	private String swaggerXtag;
	private ArrayList<String> LDA;
	private ArrayList<String> wordNet;
	
	public String getSwaggerTitle() {
		return swaggerTitle;
	}

	public void setSwaggerTitle(String swaggerTitle) {
		this.swaggerTitle = swaggerTitle;
	}

	public String getSwaggerDescription() {
		return swaggerDescription;
	}

	public void setSwaggerDescription(String swaggerDescription) {
		this.swaggerDescription = swaggerDescription;
	}

	public String getSwaggerXtag() {
		return swaggerXtag;
	}

	public void setSwaggerXtag(String swaggerXtag) {
		this.swaggerXtag = swaggerXtag;
	}

	public ArrayList<String> getLDA() {
		return LDA;
	}

	public void setLDA(ArrayList<String> lDA) {
		LDA = lDA;
	}

	public ArrayList<String> getWordNet() {
		return wordNet;
	}

	public void setWordNet(ArrayList<String> wordNet) {
		this.wordNet = wordNet;
	}

}
