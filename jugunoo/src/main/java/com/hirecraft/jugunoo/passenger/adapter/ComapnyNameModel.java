package com.hirecraft.jugunoo.passenger.adapter;

public class ComapnyNameModel
{
	private String CompanyId;
	private String CompanyName;

	public ComapnyNameModel(String CompanyId, String CompanyName)
	{
		this.CompanyId = CompanyId;
		this.CompanyName = CompanyName;
	}

	public String getCompanyId()
	{
		return CompanyId;
	}

	public void setCompanyId(String companyId)
	{
		CompanyId = companyId;
	}

	public String getCompanyName()
	{
		return CompanyName;
	}

	public void setCompanyName(String companyName)
	{
		CompanyName = companyName;
	}

}
