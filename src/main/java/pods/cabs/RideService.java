package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.Wallet.WalletGenericCommand;
import pods.cabs.utils.Logger;

public class RideService extends AbstractBehavior<RideService.RideServiceGenericCommand> {
	
	public RideService(ActorContext<RideServiceGenericCommand> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public static final class RideServiceGenericCommand {}
	

	@Override
	public Receive<RideServiceGenericCommand> createReceive() {
		return newReceiveBuilder()
		        .onMessage(RideServiceGenericCommand.class, notUsed -> {
		        	Logger.logErr("Shouldn't have received this generic command for rideservice");
		        	return this;
		        	})
		        .build();
	}
}
