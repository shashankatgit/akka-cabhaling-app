package pods.cabs;

import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.FulfillRide.Command;
import pods.cabs.Main.MainGenericCommand;
import pods.cabs.Wallet.ResponseBalance;
import pods.cabs.Wallet.WalletGenericCommand;
import pods.cabs.utils.Logger;
import pods.cabs.values.CabStates;

public class Cab extends AbstractBehavior<Cab.CabGenericCommand> {
	String cabId;
	String majorState;
	String minorState;
	long rideID;
	long numRides;
	long numRequestsRecvd;
	ActorRef<FulfillRide.Command> fRide;

	String actorName;

	public Cab(ActorContext<CabGenericCommand> context, String cabId) {
		super(context);
		this.actorName = getContext().getSelf().path().name();
		this.cabId = cabId;
		this.majorState = CabStates.MajorStates.SIGNED_OUT;
		this.minorState = CabStates.MinorStates.NONE;
		this.numRides = 0;
		this.numRequestsRecvd = 0;
		this.fRide = null;
	}

	public static class CabGenericCommand {
	}

	public static class RideEnded extends CabGenericCommand {
		long rideId;

		public RideEnded(long rideId) {
			super();
			this.rideId = rideId;
		}
	}

	public static class SignIn extends CabGenericCommand {
		long initialPos;

		public SignIn(long initialPos) {
			super();
			this.initialPos = initialPos;
		}
	}

	public static class SignOut extends CabGenericCommand {
	}

	public static class NumRides extends CabGenericCommand {
		ActorRef<Cab.NumRidesReponse> replyTo;

		public NumRides(ActorRef<NumRidesReponse> replyTo) {
			super();
			this.replyTo = replyTo;
		}
	}

	public static class Reset extends CabGenericCommand {
		ActorRef<Cab.NumRidesReponse> replyTo;

		public Reset(ActorRef<NumRidesReponse> replyTo) {
			super();
			this.replyTo = replyTo;
		}
	}

	public static class RequestRide extends CabGenericCommand {
		ActorRef<FulfillRide.Command> replyTo;

		public RequestRide(ActorRef<Command> replyTo) {
			super();
			this.replyTo = replyTo;
		}
	}

	public static class RideStarted extends CabGenericCommand {
		long rideId;
		ActorRef<FulfillRide.Command> fRideRef;

		public RideStarted(long rideId, ActorRef<Command> fRideRef) {
			super();
			this.rideId = rideId;
			this.fRideRef = fRideRef;
		}

	}

	public static class RideCanceled extends CabGenericCommand {
	}

	public static class NumRidesReponse extends CabGenericCommand {
		long numRides;

		public NumRidesReponse(long numRides) {
			super();
			this.numRides = numRides;
		}

		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			NumRidesReponse numRidesReponse = (NumRidesReponse) o;
			return numRidesReponse.numRides == this.numRides;
		}
	}

	// Define message handlers here

	private Behavior<CabGenericCommand> onRideEnded(Cab.RideEnded rideEndedCommand) {
		Logger.log("Received Cab.RideEnded with rideId : " + rideEndedCommand.rideId + " for cab id : " + this.cabId);

		if (this.fRide != null && rideEndedCommand.rideId == this.rideID) {
			fRide.tell(new FulfillRide.RideEnded());
			this.fRide = null;
		} else {
			Logger.logErr("No ongoing ride to end right now for cab id : " + this.cabId);
		}

		return this;
	}

	private Behavior<CabGenericCommand> onSignIn(Cab.SignIn signInCommand) {
		Logger.log("Received Cab.SignIn for cab id : " + this.cabId + ", initialPos: " + signInCommand.initialPos);

		
				
		if(this.majorState == CabStates.MajorStates.SIGNED_OUT) {
			// Generate random integers in range 0 to N_RIDE_SERVICE_INSTANCES
			Random rand = new Random();
			int randRideServiceId = rand.nextInt(Globals.N_RIDE_SERVICE_INSTANCES);
			
			Globals.rideService.get(randRideServiceId).tell(new RideService.CabSignsIn(this.cabId, signInCommand.initialPos));
			
			this.majorState = CabStates.MajorStates.SIGNED_IN;
			this.minorState = CabStates.MinorStates.AVAILABLE;
			this.fRide = null;
			this.numRides = 0;
			this.numRequestsRecvd = 0;
			
			Logger.log(actorName + "Successfully signed in");
		}
		else {
			Logger.logErr(actorName + "Couldn't sign in as cab is already signed in");
		}
		
		return this;
	}

	private Behavior<CabGenericCommand> onSignOut(Cab.SignOut signOutCommand) {
		Logger.log("Received Cab.SignOut for cab id : " + this.cabId);

		if(this.majorState == CabStates.MajorStates.SIGNED_IN && this.minorState == CabStates.MinorStates.AVAILABLE) {
			Random rand = new Random();
			int randRideServiceId = rand.nextInt(Globals.N_RIDE_SERVICE_INSTANCES);
			
			Globals.rideService.get(randRideServiceId).tell(new RideService.CabSignsOut(this.cabId));
			
			this.majorState = CabStates.MajorStates.SIGNED_OUT;
			this.minorState = CabStates.MinorStates.NONE;
			this.fRide = null;
			this.numRides = 0;
			this.numRequestsRecvd = 0;
			
			Logger.log(actorName + "Successfully signed out");
		}
		else {
			Logger.logErr(actorName + "Couldn't sign out as cab is busy or already signed out");
		}
		
		return this;
	}

	private Behavior<CabGenericCommand> onNumRides(Cab.NumRides numRidesCommand) {
		Logger.log("Received Cab.NumRides for cab id : " + this.cabId);
		numRidesCommand.replyTo.tell(new NumRidesReponse(this.numRides));
		return this;
	}

	private Behavior<CabGenericCommand> onReset(Cab.Reset resetCommand) {
		Logger.log("Received Cab.Reset for cab id : " + this.cabId);

		if (this.fRide != null) {
			getContext().getSelf().tell(new Cab.RideEnded(0)); // check this doubt
			fRide = null;
		}

		getContext().getSelf().tell(new Cab.SignOut());

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resetCommand.replyTo.tell(new NumRidesReponse(this.numRides));

		return this;
	}

	private Behavior<CabGenericCommand> onRequestRide(Cab.RequestRide requestRideCommand) {
		Logger.log("Received Cab.RequestRide on cab id : " + this.cabId);
		boolean accepted = false;

		if (this.majorState == CabStates.MajorStates.SIGNED_IN && this.minorState == CabStates.MinorStates.AVAILABLE) {
			if (this.numRequestsRecvd % 2 == 0) {
				accepted = true;
				this.minorState = CabStates.MinorStates.COMMITTED;
				this.numRequestsRecvd++;

			} else {
				Logger.log("Couldn't accept ride as alernate requests to be rejected, cab id : " + this.cabId);
			}
		} else {
			Logger.logErr("Couldn't accept ride as cab is not available, cab id : " + this.cabId);
		}

		requestRideCommand.replyTo.tell(new FulfillRide.RideAcceptedInternal(accepted));

		return this;
	}

	private Behavior<CabGenericCommand> onRideStarted(Cab.RideStarted rideStartedCommand) {
		Logger.log(actorName + " : Received Cab.RideStarted for ride id : " + rideStartedCommand.rideId);
		this.minorState = CabStates.MinorStates.GIVING_RIDE;
		this.rideID = rideStartedCommand.rideId;
		this.fRide = rideStartedCommand.fRideRef;
		this.numRides++;

		return this;
	}

	private Behavior<CabGenericCommand> onRideCanceled(Cab.RideCanceled rideCanceledCommand) {
		Logger.log(actorName + " : Received Cab.RideCancelled");
		this.minorState = CabStates.MinorStates.AVAILABLE;
		return this;
	}

	@Override
	public Receive<CabGenericCommand> createReceive() {
		return newReceiveBuilder()
				.onMessage(RideEnded.class, this::onRideEnded)
				.onMessage(SignIn.class, this::onSignIn)
				.onMessage(SignOut.class, this::onSignOut)
				.onMessage(NumRides.class, this::onNumRides)
				.onMessage(Reset.class, this::onReset)
				.onMessage(RequestRide.class, this::onRequestRide)
				.onMessage(RideStarted.class, this::onRideStarted)
				.onMessage(RideCanceled.class, this::onRideCanceled)
				.onMessage(CabGenericCommand.class, notUsed -> {
					Logger.logErr("Shouldn't have received this generic command for cab-" + this.cabId);
					return this;
				}).build();
	}

	public static Behavior<CabGenericCommand> create(String cabID) {
		Logger.log("In 'create' of a new cab actor : cab-" + cabID);

		return Behaviors.setup(context -> new Cab(context, cabID));
	}
}
