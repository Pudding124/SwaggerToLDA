package ntou.swagger.bean;

import java.util.ArrayList;

public class Operations {

	private String swaggerPath = null;
	private String swaggerOperationId = null;
	private String swaggerDescription = null;
	private String swaggerSummary = null;
	private ArrayList<String> LDA = null;
	private ArrayList<String> wordNet = null;
	
	public String getSwaggerPath() {
		return swaggerPath;
	}

	public void setSwaggerPath(String swaggerPath) {
		this.swaggerPath = swaggerPath;
	}

	public String getSwaggerOperationId() {
		return swaggerOperationId;
	}

	public void setSwaggerOperationId(String swaggerOperationId) {
		this.swaggerOperationId = swaggerOperationId;
	}

	public String getSwaggerDescription() {
		return swaggerDescription;
	}

	public void setSwaggerDescription(String swaggerDescription) {
		this.swaggerDescription = swaggerDescription;
	}

	public String getSwaggerSummary() {
		return swaggerSummary;
	}

	public void setSwaggerSummary(String swaggerSummary) {
		this.swaggerSummary = swaggerSummary;
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
