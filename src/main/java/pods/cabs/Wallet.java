package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.utils.Logger;

public class Wallet extends AbstractBehavior<Wallet.WalletGenericCommand> {
	
	String custID;
	long accountBalance;
	
	public Wallet(ActorContext<WalletGenericCommand> context, String newCustID, long balance) {
		super(context);
		this.custID = newCustID;
		this.accountBalance = balance;
	}


	public static final class WalletGenericCommand {}
	

	@Override
	public Receive<WalletGenericCommand> createReceive() {
			return newReceiveBuilder()
			        .onMessage(WalletGenericCommand.class, notUsed -> {
			        	Logger.logErr("Shouldn't have received this generic command for wallet-"+this.custID);
			        	return this;
			        	})
			        .build();
	}
	
	
	public static Behavior<WalletGenericCommand> create(String custID, long balance) {
		Logger.log("In 'create' of a new wallet actor : wallet-" + custID);
		
		return Behaviors.empty();
				
//				setup(context ->
//			new Wallet(context, custID, balance));
	}
}
