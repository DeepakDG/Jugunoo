package com.hirecraft.jugunoo.passenger.adapter;

public class FavoriteDriverModel
{
	private String RID;
	private String DriverName;

	public FavoriteDriverModel(String RID,String DriverName){
		  super();
		this.RID=RID;
		this.DriverName=DriverName;
		
	}

	public String getRID()
	{
		return RID;
	}

	public void setRID(String rID)
	{
		RID = rID;
	}

	public String getDriverName()
	{
		return DriverName;
	}

	public void setDriverName(String driverName)
	{
		DriverName = driverName;
	}
}
