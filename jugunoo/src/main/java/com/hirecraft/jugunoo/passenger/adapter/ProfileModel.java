package com.hirecraft.jugunoo.passenger.adapter;

public class ProfileModel {
	public int icon;
	public String groupName;
	public String mgrName;
	public String status;
	public String count;

	public ProfileModel() {
		super();
	}

	public ProfileModel(int icon, String groupName, String mgrName,
			String status, String count) {
		super();
		this.icon = icon;
		this.groupName = groupName;
		this.mgrName = mgrName;
		this.status = status;
		this.count = count;
	}
}
