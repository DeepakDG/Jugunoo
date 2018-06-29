package com.hirecraft.jugunoo.passenger.adapter;

public class GroupNameModel
{
	private String GroupId;
	private String GroupName;

	public GroupNameModel(String GroupId, String GroupName)
	{
		this.GroupId = GroupId;
		this.GroupName = GroupName;
	}

	public String getGroupId()
	{
		return GroupId;
	}

	public void setGroupId(String groupId)
	{
		GroupId = groupId;
	}

	public String getGroupName()
	{
		return GroupName;
	}

	public void setGroupName(String groupName)
	{
		GroupName = groupName;
	}
}
