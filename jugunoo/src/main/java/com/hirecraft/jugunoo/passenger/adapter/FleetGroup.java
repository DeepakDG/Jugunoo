package com.hirecraft.jugunoo.passenger.adapter;

public class FleetGroup
{
	private String name;
	private String status;
	public boolean isChecked;
	private int id;
	private boolean active_id;

	public FleetGroup(String name, String status, int id, boolean active_id)
	{
		this.name = name;
		this.status = status;
		this.id = id;
		this.active_id = active_id;

		if (active_id)
		{
			isChecked = true;
		}
		else
		{
			isChecked = false;
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public boolean isChecked()
	{
		return isChecked;
	}

	public void setChecked(boolean isChecked)
	{
		this.isChecked = isChecked;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public boolean getactive_id()
	{
		return active_id;
	}

	public void setactive_id(boolean active_id)
	{
		this.active_id = active_id;
	}

}
