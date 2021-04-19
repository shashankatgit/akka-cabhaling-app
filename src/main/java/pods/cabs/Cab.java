package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.Main.MainGenericCommand;
import pods.cabs.utils.Logger;

public class Cab extends AbstractBehavior<Cab.CabGenericCommand> {
	String cabID;
	
	public Cab(ActorContext<CabGenericCommand> context, String cabID) {
		super(context);
		this.cabID = cabID;
		// TODO Auto-generated constructor stub
	}


	public static final class CabGenericCommand {}
	

	@Override
	public Receive<CabGenericCommand> createReceive() {
		return newReceiveBuilder()
		        .onMessage(CabGenericCommand.class, notUsed -> {
		        	Logger.logErr("Shouldn't have received this generic command for cab-"+this.cabID);
		        	return this;
		        	})
		        .build();
	}
	
	public static Behavior<CabGenericCommand> create(String cabID) {
		Logger.log("In 'create' of a new cab actor : cab-" + cabID);
		
		return Behaviors.setup(context ->
			new Cab(context, cabID));
	}
}
