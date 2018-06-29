package com.hirecraft.jugunoo.passenger.adapter;

public class States
{
	String code = null;
	String name = null;
	boolean selected = false;
	int checked = 0;

	public States(String name, String code, int checked)
	{
		super();
		this.code = code;
		this.name = name;
		this.checked = checked;

		if (checked == 0)
		{
			selected = false;
		}
		else
		{
			selected = true;
		}
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public int getChecked()
	{
		return checked;
	}

	public void setChecked(int checked)
	{
		this.checked = checked;
	}
}
