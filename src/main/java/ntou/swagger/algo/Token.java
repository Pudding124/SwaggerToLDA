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


public class Token {
	public Token(String token, int tf) {
		super();
		this.token = token;
		this.tf = tf;
	}
	private String token;
	private int tf;
	private double length;
	
	
	public double getUnitizationTf() {
		return tf/length;
	}
	public void setUnitizationTfLenth(double length) {
		this.length = length;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
}