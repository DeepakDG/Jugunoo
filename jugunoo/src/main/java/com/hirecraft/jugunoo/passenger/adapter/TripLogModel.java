package com.hirecraft.jugunoo.passenger.adapter;

public class TripLogModel
{
	private String BookingId;
	private String StartDate;
	private String CompanyName;
	private String PickPoint;
	private String DropPoint;
	private String VehicleNo;
	private String DriverName;
	private String Behalf;
	private String BookedBy;
	private String Editable;
	private String EndDate;
	private String Pref;
	private String Own;

	public TripLogModel(String BookingId, String StartDate, String CompanyName,
			String PickPoint, String DropPoint, String VehicleNo,
			String DriverName, String Behalf, String BookedBy, String EndDate,
			String Pref, String Own)
	{
		this.BookingId = BookingId;
		this.StartDate = StartDate;
		this.CompanyName = CompanyName;
		this.PickPoint = PickPoint;
		this.DropPoint = DropPoint;
		this.VehicleNo = VehicleNo;
		this.DriverName = DriverName;
		this.Behalf = Behalf;
		this.BookedBy = BookedBy;
		this.EndDate = EndDate;
		this.Pref = Pref;
		this.Own = Own;

	}

	public String getOwn()
	{
		return Own;
	}

	public void setOwn(String own)
	{
		Own = own;
	}

	public String getPref()
	{
		return Pref;
	}

	public void setPref(String pref)
	{
		Pref = pref;
	}

	public String getBookingId()
	{
		return BookingId;
	}

	public void setBookingId(String BookingId)
	{
		this.BookingId = BookingId;
	}

	public String getDate()
	{
		return StartDate;
	}

	public void setDate(String StartDate)
	{
		this.StartDate = StartDate;
	}

	public String getCompanyName()
	{
		return CompanyName;
	}

	public void setCompanyName(String CompanyName)
	{
		this.CompanyName = CompanyName;
	}

	public String getStartPoint()
	{
		return PickPoint;
	}

	public void setStartPoint(String PickPoint)
	{
		this.PickPoint = PickPoint;
	}

	public String getEndPoint()
	{
		return DropPoint;
	}

	public void setEndPoint(String DropPoint)
	{
		this.DropPoint = DropPoint;
	}

	public String getCabNo()
	{
		return VehicleNo;
	}

	public void setCabNo(String VehicleNo)
	{
		this.VehicleNo = VehicleNo;
	}

	public String getName()
	{
		return DriverName;
	}

	public void setName(String DriverName)
	{
		this.DriverName = DriverName;
	}

	public String getbookedName()
	{
		return Behalf;
	}

	public void setbookedName(String Behalf)
	{
		this.Behalf = Behalf;
	}

	public String getbookedNumber()
	{
		return BookedBy;
	}

	public void setbookedNumber(String BookedBy)
	{
		this.BookedBy = BookedBy;
	}

	public String getEditable()
	{
		return Editable;
	}

	public void setEditable(String editable)
	{
		Editable = editable;
	}

	public String getEndDate()
	{
		return EndDate;
	}

	public void setEndDate(String EndDate)
	{
		this.EndDate = EndDate;
	}

}
