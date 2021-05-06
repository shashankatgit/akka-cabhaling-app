package pods.cabs.models;

import pods.cabs.values.CabStates;

public class CabStatus {
	public String cabId;
	public String majorState;
	public String minorState;
	public long initialPos;
	
	public CabStatus(String cabId, String majorState, String minorState, long initialPos) {
		super();
		this.cabId = cabId;
		this.majorState = majorState;
		this.minorState = minorState;
		this.initialPos = initialPos;
	}
	
	public CabStatus(String cabId) {
		super();
		this.cabId = cabId;
		this.majorState = CabStates.MajorStates.SIGNED_OUT;
		this.minorState = CabStates.MinorStates.NONE;
		this.initialPos = -1;
	}	
	
}
