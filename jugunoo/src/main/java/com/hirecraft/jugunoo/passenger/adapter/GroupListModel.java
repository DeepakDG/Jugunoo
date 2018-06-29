package com.hirecraft.jugunoo.passenger.adapter;

public class GroupListModel
{
	private String GroupId;
	private String GroupName;

	private String ManagingUserId;
	private String ManagerName;

	private String Count;

	public GroupListModel(String GroupId, String GroupName,
			String ManagingUserId, String ManagerName, String Count)
	{
		this.GroupId = GroupId;
		this.GroupName = GroupName;

		this.ManagingUserId = ManagingUserId;
		this.ManagerName = ManagerName;

		this.Count = Count;

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

	public String getManagingUserId()
	{
		return ManagingUserId;
	}

	public void setManagingUserId(String managingUserId)
	{
		ManagingUserId = managingUserId;
	}

	public String getManagerName()
	{
		return ManagerName;
	}

	public void setManagerName(String managerName)
	{
		ManagerName = managerName;
	}

	public String getCount()
	{
		return Count;
	}

	public void setCount(String count)
	{
		Count = count;
	}

}
