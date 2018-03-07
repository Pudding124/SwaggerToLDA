package ntou.swagger.bean;

import java.util.ArrayList;

public class UserResponseBean {
	
	Resource Resource;
	public ArrayList<Operations> Operations;
	
	public Resource getResource() {
		return Resource;
	}
	public void setResource(Resource resource) {
		Resource = resource;
	}
	public ArrayList<Operations> getOperations() {
		return Operations;
	}
	public void setOperation(ArrayList<Operations> operations) {
		Operations = operations;
	}
	
}
