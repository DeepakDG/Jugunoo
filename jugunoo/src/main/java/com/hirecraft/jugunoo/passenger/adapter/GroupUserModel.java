package com.hirecraft.jugunoo.passenger.adapter;

public class GroupUserModel
{
	private String GroupId;
	private String UserId;
	private String UserName;
	private String Count;
	private String KMS;
	private String Amount;

	public GroupUserModel(String GroupId, String UserId, String UserName,
			String Count, String Kms, String Amount)
	{

		this.GroupId = GroupId;
		this.UserId = UserId;
		this.UserName = UserName;
		this.Count = Count;
		this.KMS = Kms;
		this.Amount = Amount;
	}

	public String getGroupId()
	{
		return GroupId;
	}

	public void setGroupId(String groupId)
	{
		GroupId = groupId;
	}

	public String getUserId()
	{
		return UserId;
	}

	public void setUserId(String userId)
	{
		UserId = userId;
	}

	public String getUserName()
	{
		return UserName;
	}

	public void setUserName(String userName)
	{
		UserName = userName;
	}

	public String getCount()
	{
		return Count;
	}

	public void setCount(String count)
	{
		Count = count;
	}

	public String getKMS()
	{
		return KMS;
	}

	public void setKMS(String kMS)
	{
		KMS = kMS;
	}

	public String getAmount()
	{
		return Amount;
	}

	public void setAmount(String amount)
	{
		Amount = amount;
	}

}
