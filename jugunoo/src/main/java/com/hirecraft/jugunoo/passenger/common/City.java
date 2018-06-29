package com.hirecraft.jugunoo.passenger.common;


public class City {

	private Integer RID;
	private String City;

	public City(Integer rID, String city) {
		RID = rID;
		City = city;
	}

	public City() {
	
	}

	public Integer getRID() {
		return RID;
	}

	public void setRID(Integer rID) {
		RID = rID;
	}

	public String getCity() {
		return City;
	}

	public void setCity(String city) {
		City = city;
	}

	@Override
	public String toString() {
		return this.getCity();
	}

	@Override
	public boolean equals(Object object) {
		boolean sameSame = false;

		if (object != null && object instanceof City) {

			sameSame = this.City.equalsIgnoreCase(((City) object).getCity());
		}

		return sameSame;
	}
	
	
	
}
