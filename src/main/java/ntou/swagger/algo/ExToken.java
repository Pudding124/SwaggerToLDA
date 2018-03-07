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

public class ExToken extends Token {
	public ExToken(String term, int tf, Double maxWeight, ExpandsionType exType) {
		super(term, tf);
		this.maxWeight = maxWeight;
		//this.exType = exType;
		this.setMaxWeightType(exType.toString());
	}

	public ExToken(String term, int tf, Double maxWeight) {
		super(term, tf);
		this.maxWeight = maxWeight;		
	}
	
	public enum ExpandsionType{classSuper,classSub,propRange,propDomain,original,wordnet};
	
	Double maxWeight;

	String maxWeightType;
	
	public double getMaxWeight() {
		return maxWeight;
	}
	public void setMaxWeight(Double maxWeight) {
		this.maxWeight = maxWeight;
	}

	public String getMaxWeightType() {
		return maxWeightType;
	}
	public void setMaxWeightType(String maxWeightType) {
		this.maxWeightType = maxWeightType;
	}
	public String toString(){
		
		return "term:"+super.getToken()+" exType:"+maxWeightType+" tf:"+super.getTf()+" maxWeight:"+ maxWeight;
		
	}
}
