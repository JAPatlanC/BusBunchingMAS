package model;

public class Passenger {
	public int stopWaitingTime;
	public int onboardWaitingTime;
	public int stopDestinationId;
	public int busOnboardId;
	public boolean active;
	
	public int totalWaitingTime() {
		return stopWaitingTime+onboardWaitingTime;
	}
}
