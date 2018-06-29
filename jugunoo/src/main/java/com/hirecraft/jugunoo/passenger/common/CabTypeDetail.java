package com.hirecraft.jugunoo.passenger.common;

public class CabTypeDetail {

	private Integer RID;
	private String CabType;

	public CabTypeDetail() {
		// TODO Auto-generated constructor stub
	}

	public CabTypeDetail(Integer rID, String cabType) {
		RID = rID;
		CabType = cabType;
	}

	public Integer getRID() {
		return RID;
	}

	public void setRID(Integer rID) {
		RID = rID;
	}

	public String getCabType() {
		return CabType;
	}

	public void setCabType(String cabType) {
		CabType = cabType;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getCabType();
	}

	@Override
	public boolean equals(Object object) {
		boolean sameSame = false;

		if (object != null && object instanceof CabTypeDetail) {

			sameSame = this.CabType.equalsIgnoreCase(((CabTypeDetail) object)
					.getCabType());
		}

		return sameSame;
	}

}
