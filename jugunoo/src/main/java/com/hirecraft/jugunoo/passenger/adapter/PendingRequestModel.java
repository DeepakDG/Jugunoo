package com.hirecraft.jugunoo.passenger.adapter;

public class PendingRequestModel {
	
	private String RID;
	private String UserName;
	private String EmailId;
	private String GroupName;
	
	public PendingRequestModel(String RID,String UserName,String EmailId,String GroupName){
		  super();
		this.RID=RID;
		this.UserName=UserName;
		this.EmailId=EmailId;
		this.GroupName=GroupName;
	}

	public String getRID() {
		return RID;
	}

	public void setRID(String rID) {
		RID = rID;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getEmailId() {
		return EmailId;
	}

	public void setEmailId(String emailId) {
		EmailId = emailId;
	}

	public String getGroupName() {
		return GroupName;
	}

	public void setGroupName(String groupName) {
		GroupName = groupName;
	}	

}
