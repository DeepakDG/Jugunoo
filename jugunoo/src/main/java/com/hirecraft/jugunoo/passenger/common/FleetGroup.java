package com.hirecraft.jugunoo.passenger.common;


public class FleetGroup {

	private Integer RID;
	private String GroupName;

	public FleetGroup(Integer rID, String groupName) {

		RID = rID;
		GroupName = groupName;
	}

	public FleetGroup() {
	}

	public Integer getRID() {
		return RID;
	}

	public void setRID(Integer rID) {
		RID = rID;
	}

	public String getGroupName() {
		return GroupName;
	}

	public void setGroupName(String groupName) {
		GroupName = groupName;
	}

	@Override
	public String toString() {
		return this.getGroupName();
	}

	@Override
	public boolean equals(Object object) {
		boolean sameSame = false;

		if (object != null && object instanceof FleetGroup) {

			sameSame = this.RID.equals(((FleetGroup) object)
					.getRID());
		}

		return sameSame;
	}

}
