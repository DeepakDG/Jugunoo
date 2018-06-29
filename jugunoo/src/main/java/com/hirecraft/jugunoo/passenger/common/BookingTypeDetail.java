package com.hirecraft.jugunoo.passenger.common;

public class BookingTypeDetail {

	private Integer RID;
	private String BookingType;

	public BookingTypeDetail(Integer rID, String bookingType) {

		RID = rID;
		BookingType = bookingType;
	}

	public BookingTypeDetail() {

	}

	public Integer getRID() {
		return RID;
	}

	public void setRID(Integer rID) {
		RID = rID;
	}

	public String getBookingType() {
		return BookingType;
	}

	public void setBookingType(String bookingType) {
		BookingType = bookingType;
	}

	@Override
	public String toString() {
		return this.getBookingType();
	}

	@Override
	public boolean equals(Object object) {
		boolean sameSame = false;

		if (object != null && object instanceof BookingTypeDetail) {

			sameSame = this.BookingType.equalsIgnoreCase(((BookingTypeDetail) object)
					.getBookingType());
		}

		return sameSame;
	}
	
	
}
