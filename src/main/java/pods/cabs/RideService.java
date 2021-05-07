package pods.cabs;

import java.util.HashMap;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.Cab.CabGenericCommand;
import pods.cabs.FulfillRide.Command;
import pods.cabs.Wallet.WalletGenericCommand;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.Logger;
import pods.cabs.values.CabStates;

public class RideService extends AbstractBehavior<RideService.RideServiceGenericCommand> {

	long rideServiceActorId;

	String actorName;

	public HashMap<String, CabStatus> cabsMap;

	public RideService(ActorContext<RideServiceGenericCommand> context, long rideServiceActorId) {
		super(context);
		this.rideServiceActorId = rideServiceActorId;
		this.actorName = getContext().getSelf().path().name();

		cabsMap = new HashMap<>();

		// initialize all cabs in signed out state
		for (String cabId : Globals.initReadWrapperObj.cabIDList) {
			cabsMap.put(cabId, new CabStatus(cabId));
		}
	}

	public static class RideServiceGenericCommand {
	}

	public static class CabSignsIn extends RideServiceGenericCommand {
		String cabId;
		long initialPos;

		public CabSignsIn(String cabId, long initialPos) {
			super();
			this.cabId = cabId;
			this.initialPos = initialPos;
		}
	}

	public static class CabSignsInInternal extends RideServiceGenericCommand {
		String cabId;
		long initialPos;

		public CabSignsInInternal(String cabId, long initialPos) {
			super();
			this.cabId = cabId;
			this.initialPos = initialPos;
		}
	}

	public static class CabSignsOut extends RideServiceGenericCommand {
		String cabId;

		public CabSignsOut(String cabId) {
			super();
			this.cabId = cabId;
		}
	}

	public static class CabSignsOutInternal extends RideServiceGenericCommand {
		String cabId;

		public CabSignsOutInternal(String cabId) {
			super();
			this.cabId = cabId;
		}
	}

	public static class RequestRide extends RideServiceGenericCommand {
		String custId;
		long sourceLoc;
		long destinationLoc;
		ActorRef<RideService.RideResponse> replyTo;

		public RequestRide(String custId, long sourceLoc, long destinationLoc, ActorRef<RideResponse> replyTo) {
			super();
			this.custId = custId;
			this.sourceLoc = sourceLoc;
			this.destinationLoc = destinationLoc;
			this.replyTo = replyTo;
		}
	}

	public static class RideResponse extends RideServiceGenericCommand {
		public long rideId;
		public String cabId;
		public long fare;
		public ActorRef<FulfillRide.Command> fRide;

		public RideResponse(long rideId, String cabId, long fare, ActorRef<Command> fRide) {
			super();
			this.rideId = rideId;
			this.cabId = cabId;
			this.fare = fare;
			this.fRide = fRide;
		}
	}

	public static class RideResponseSuccessInternal extends RideServiceGenericCommand {
		String cabId;

		public RideResponseSuccessInternal(String cabId) {
			super();
			this.cabId = cabId;
		}
	}

	public static class RideEnded extends RideServiceGenericCommand {
		String cabId;
		long destinationPos;

		public RideEnded(String cabId, long destinationPos) {
			super();
			this.cabId = cabId;
			this.destinationPos = destinationPos;
		}
	}

	public static class RideEndedInternal extends RideServiceGenericCommand {
		String cabId;
		long destinationPos;

		public RideEndedInternal(String cabId, long destinationPos) {
			super();
			this.cabId = cabId;
			this.destinationPos = destinationPos;
		}
	}

	// Define message handlers here

	private Behavior<RideServiceGenericCommand> onCabSignsIn(RideService.CabSignsIn cabSignsInCommand) {
		Logger.log(actorName + " : Received RideService.CabSignsIn for cabId : " + cabSignsInCommand.cabId
				+ " on RideService instance " + this.rideServiceActorId);

		// Broadcast the message to all including own
		for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
			Globals.rideService.get(i)
					.tell(new CabSignsInInternal(cabSignsInCommand.cabId, cabSignsInCommand.initialPos));
		}

		return this;
	}

	private Behavior<RideServiceGenericCommand> onCabSignsInInternal(
			RideService.CabSignsInInternal cabSignsInInternalCommand) {
		Logger.log(actorName + " : Received RideService.CabSignsInInternal for cabId : "
				+ cabSignsInInternalCommand.cabId + " on RideService instance " + this.rideServiceActorId);

		CabStatus cabStatus = this.cabsMap.get(cabSignsInInternalCommand.cabId);

		if (cabStatus != null && cabStatus.majorState == CabStates.MajorStates.SIGNED_OUT) {
			Logger.log("Successfully signed in cab id : " + cabSignsInInternalCommand.cabId
					+ " on Ride Service instance " + this.rideServiceActorId);

			cabStatus.majorState = CabStates.MajorStates.SIGNED_IN;
			cabStatus.minorState = CabStates.MinorStates.AVAILABLE;
			cabStatus.curPos = cabSignsInInternalCommand.initialPos;
		} else {
			Logger.logErr(actorName + " : Couldn't sign in cab id : " + cabSignsInInternalCommand.cabId
					+ " on Ride Service instance " + this.rideServiceActorId);
		}
		return this;
	}

	private Behavior<RideServiceGenericCommand> onCabSignsOut(RideService.CabSignsOut cabSignsOutCommand) {
		Logger.log("Received RideService.CabSignsOut for cabId : " + cabSignsOutCommand.cabId
				+ " on RideService instance " + this.rideServiceActorId);

		// Broadcast the message to all including own
		for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
			Globals.rideService.get(i).tell(new CabSignsOutInternal(cabSignsOutCommand.cabId));
		}
		return this;
	}

	private Behavior<RideServiceGenericCommand> onCabSignsOutInternal(
			RideService.CabSignsOutInternal cabSignsOutInternalCommand) {
		Logger.log("Received RideService.CabSignsOutInternal for cabId : " + cabSignsOutInternalCommand.cabId
				+ " on RideService instance " + this.rideServiceActorId);

		CabStatus cabStatus = this.cabsMap.get(cabSignsOutInternalCommand.cabId);

		if (cabStatus != null && cabStatus.majorState == CabStates.MajorStates.SIGNED_IN
				&& cabStatus.minorState == CabStates.MinorStates.AVAILABLE) {
			Logger.log("Successfully signed out cab id : " + cabSignsOutInternalCommand.cabId
					+ " on Ride Service instance " + this.rideServiceActorId);

			cabStatus.majorState = CabStates.MajorStates.SIGNED_OUT;
			cabStatus.minorState = CabStates.MinorStates.NONE;
			cabStatus.curPos = -1;
		} else {
			Logger.logErr("Couldn't sign out cab id : " + cabSignsOutInternalCommand.cabId
					+ " on Ride Service instance " + this.rideServiceActorId);
		}
		return this;
	}

	private Behavior<RideServiceGenericCommand> onRequestRide(RideService.RequestRide requestRideCommand) {
		Logger.log(actorName + " : Received RideService.RequestRide for (custId,srcLoc,destLoc) : ("
				+ requestRideCommand.custId + ", " + requestRideCommand.sourceLoc + ", "
				+ requestRideCommand.destinationLoc + ")");
		long rideId = Globals.rideIdSequence.incrementAndGet();

		// Perform sanity checks
		if (requestRideCommand.sourceLoc >= 0 && requestRideCommand.destinationLoc >= 0
				&& Globals.wallets.containsKey(requestRideCommand.custId)) {
			ActorRef<FulfillRide.Command> fRideActorRef = getContext().spawn(
					FulfillRide.create(rideId, requestRideCommand.custId, requestRideCommand.sourceLoc,
							requestRideCommand.destinationLoc, this.cabsMap, requestRideCommand.replyTo),
					"fRide-" + this.rideServiceActorId + "-" + rideId);
			Logger.log(actorName + " : Spawning a FulfillRide actor");
			
			fRideActorRef.tell(new FulfillRide.Command());
		} else {
			Logger.logErr(actorName + " : Invalid ride request");
		}

		return this;
	}

	private Behavior<RideServiceGenericCommand> onRideResponse(RideService.RideResponse rideResponseCommand) {
		Logger.log("Received RideService.RideResponse for (rideId,cabId,fare) : (" + rideResponseCommand.rideId + ", "
				+ rideResponseCommand.cabId + ", " + rideResponseCommand.fare);

		if (rideResponseCommand.rideId > 0) {
			Logger.log(actorName + " : FulfillRide successfully gave ride");
			// Broadcast the message to all including own
			for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
				Globals.rideService.get(i).tell(new RideResponseSuccessInternal(rideResponseCommand.cabId));
			}
		} else {
			Logger.logErr(actorName + " : FulfillRide couldn't give ride");
			// do nothing
		}

		return this;
	}

	private Behavior<RideServiceGenericCommand> onRideResponseSuccessInternal(RideService.RideResponseSuccessInternal rideResponseSuccessInternalCommand) {
		Logger.log(actorName + " : Received RideService.RideResponseSuccessInternal with cabId : "
				+ rideResponseSuccessInternalCommand.cabId);

		// Understand that the cab has started giving ride so, converge your state
		// accordingly
		this.cabsMap.get(rideResponseSuccessInternalCommand.cabId).minorState = CabStates.MinorStates.GIVING_RIDE;

		return this;
	}

	private Behavior<RideServiceGenericCommand> onRideEnded(RideService.RideEnded rideEndedCommand) {
		Logger.log(actorName + " : Received RideService.RideEnded with (cabId,destPos) : " + rideEndedCommand.cabId +", " + rideEndedCommand.destinationPos);

		// Broadcast the message to all including own
		for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
			Globals.rideService.get(i).tell(new RideEndedInternal(rideEndedCommand.cabId, rideEndedCommand.destinationPos));
		}

		return this;
	}

	private Behavior<RideServiceGenericCommand> onRideEndedInternal(RideService.RideEndedInternal rideEndedInternalCommand) {
		Logger.log(actorName + " : Received RideService.RideEndedInternal with cabId : " + rideEndedInternalCommand.cabId);
		
		this.cabsMap.get(rideEndedInternalCommand.cabId).minorState = CabStates.MinorStates.AVAILABLE;
		this.cabsMap.get(rideEndedInternalCommand.cabId).curPos = rideEndedInternalCommand.destinationPos;
		
		return this;
	}

	@Override
	public Receive<RideServiceGenericCommand> createReceive() {
		return newReceiveBuilder()
				.onMessage(CabSignsIn.class, this::onCabSignsIn)
				.onMessage(CabSignsInInternal.class, this::onCabSignsInInternal)
				.onMessage(CabSignsOut.class, this::onCabSignsOut)
				.onMessage(CabSignsOutInternal.class, this::onCabSignsOutInternal)
				.onMessage(RequestRide.class, this::onRequestRide)
				.onMessage(RideResponse.class, this::onRideResponse)
				.onMessage(RideResponseSuccessInternal.class, this::onRideResponseSuccessInternal)
				.onMessage(RideEnded.class, this::onRideEnded)
				.onMessage(RideEndedInternal.class, this::onRideEndedInternal)
				.onMessage(RideServiceGenericCommand.class, notUsed -> {
					Logger.logErr("Shouldn't have received this generic command for rideservice");
					return this;
				}).build();
	}

	public static Behavior<RideServiceGenericCommand> create(long rideServiceActorId) {
//		Logger.log("In 'create' of a new RideService actor, id : " + rideServiceActorId);
		return Behaviors.setup(context -> {
			return new RideService(context, rideServiceActorId);
		});
	}
}
