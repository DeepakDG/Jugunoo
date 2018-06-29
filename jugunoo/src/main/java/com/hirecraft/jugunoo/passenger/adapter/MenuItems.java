package com.hirecraft.jugunoo.passenger.adapter;

public class MenuItems {
	private String member_name;
	private int profile_pic_id;
	private String count;

	public MenuItems(String member_name, int profile_pic_id,String cnt) {

		this.member_name = member_name;
		this.profile_pic_id = profile_pic_id;
		this.count=cnt;
	}

	public String getCount()
	{
		return count;
	}

	public void setCount(String count)
	{
		this.count = count;
	}

	public String getMember_name() {
		return member_name;
	}

	public void setMember_name(String member_name) {
		this.member_name = member_name;
	}

	public int getProfile_pic_id() {
		return profile_pic_id;
	}

	public void setProfile_pic_id(int profile_pic_id) {
		this.profile_pic_id = profile_pic_id;
	}

}
