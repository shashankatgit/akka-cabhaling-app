package pods.cabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.Logger;

public class FulfillRide extends AbstractBehavior<FulfillRide.Command> {
	
	long rideId;
	String custId;
	long sourceLoc;
	long destinationLoc;
	HashMap<String,CabStatus> cabsMap;
	ActorRef<RideService.RideServiceGenericCommand> replyTo;
	
	public FulfillRide(ActorContext<Command> context, long rideId, String custId, long sourceLoc, 
			long destinationLoc, HashMap<String,CabStatus> cabsMap, ActorRef<RideService.RideServiceGenericCommand> replyTo) {
		super(context);
		this.rideId = rideId;
		this.custId = custId;
		this.sourceLoc = sourceLoc;
		this.destinationLoc = destinationLoc;
		this.cabsMap = cabsMap;
		this.replyTo = replyTo;
	}
	

	private static ArrayList<CabStatus> sortValues(HashMap<String,CabStatus> map,long srcPos){   
    
		ArrayList<CabStatus> cablist = new ArrayList<CabStatus>(map.values());  

		class CabStatusComparator implements Comparator<CabStatus> {
		    @Override
		    public int compare(CabStatus cab1, CabStatus cab2) {
		    	long cab1RelativeDist = Math.abs(cab1.initialPos-srcPos);
		    	long cab2RelativeDist = Math.abs(cab2.initialPos-srcPos);
		        
		    	if(cab1RelativeDist < cab2RelativeDist) {
		    		return -1;
		    	}else if(cab1RelativeDist == cab2RelativeDist) {
		    		return Long.parseLong(cab1.cabId) < Long.parseLong(cab2.cabId) ? -1 : 1;
		    	}
		    	
		    	return 1;		    	
		    }
		}		
	    
		Collections.sort(cablist, new CabStatusComparator());
		
		return cablist;
	}  	

	public static class Command  {
	}

	public static class RideEnded extends Command {
	}

	
	// Define message handlers here
	
	private Behavior<Command> onCommand(FulfillRide.Command command) {
		Logger.log("Received FulfillRide.Command");
//		
//		Random rand = new Random();
//		for(CabStatus cabStatus : this.cabsMap.values()) {			
//			cabStatus.initialPos = rand.nextInt(100);;			
//		}
//		
//		System.out.println("\n------- Before Sorting -------");
//		for(CabStatus cabStatus : this.cabsMap.values()) {			
//			System.out.println("Cab id: " + cabStatus.cabId + ", initialPos: " 
//					+  cabStatus.initialPos + ", relaDist: " + Math.abs(cabStatus.initialPos-this.sourceLoc));			
//		}
//		
//		ArrayList<CabStatus> sortedCablist = sortValues(this.cabsMap, this.sourceLoc);  
//		
//		System.out.println("\n------- After Sorting -------");
//		for(CabStatus cabStatus : sortedCablist) {			
//			System.out.println("Cab id: " + cabStatus.cabId + ", initialPos: " 
//					+  cabStatus.initialPos + ", relaDist: " + Math.abs(cabStatus.initialPos-this.sourceLoc));				
//		}
		
		return this;
	}
	
	private Behavior<Command> onRideEnded(FulfillRide.RideEnded rideEndedCommand) {
		Logger.log("Received FulfillRide.RideEnded");
		
		return this;
	}
	
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(Command.class, this :: onCommand)
				.build();
	}

	public static Behavior<Command> create(long rideId, String custId, long sourceLoc, long destinationLoc, HashMap<String,CabStatus> cabsMap, ActorRef<RideService.RideServiceGenericCommand> replyTo) {
//		Logger.log("In 'create' of a new FulfillRideGenericCommand actor");
		return Behaviors.setup(context -> {
			return new FulfillRide(context, rideId, custId, sourceLoc, destinationLoc, cabsMap, replyTo);
		});
	}
}
