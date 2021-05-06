package pods.cabs;

import java.util.HashMap;

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

//	public static class FulfillRideGenericCommand {
//	}

	public static class Command  {
	}

	public static class RideEnded extends Command {
	}

	
	
	// Define message handlers here
	
	private Behavior<Command> onCommand(FulfillRide.Command command) {
		Logger.log("Received FulfillRide.Command");
		
		return this;
	}
	
	private Behavior<Command> onRideEnded(FulfillRide.RideEnded rideEndedCommand) {
		Logger.log("Received FulfillRide.RideEnded");
		
		return this;
	}
	
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder().onMessage(Command.class, notUsed -> {
			Logger.logErr("Shouldn't have received this generic command for FulfillRide");
			return this;
		}).build();
	}

	public static Behavior<Command> create(long rideId, String custId, long sourceLoc, long destinationLoc, HashMap<String,CabStatus> cabsMap, ActorRef<RideService.RideServiceGenericCommand> replyTo) {
//		Logger.log("In 'create' of a new FulfillRideGenericCommand actor");
		return Behaviors.setup(context -> {
			return new FulfillRide(context, rideId, custId, sourceLoc, destinationLoc, cabsMap, replyTo);
		});
	}
}
