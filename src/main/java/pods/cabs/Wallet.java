package pods.cabs;

import com.example.Greeter;
import com.example.Greeter.Greet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.Main.StartedCommand;
import pods.cabs.utils.Logger;

public class Wallet extends AbstractBehavior<Wallet.WalletGenericCommand> {

	String custID;
	long accountBalance;

	public Wallet(ActorContext<WalletGenericCommand> context, String newCustID, long balance) {
		super(context);
		this.custID = newCustID;
		this.accountBalance = balance;
	}

	public static Behavior<WalletGenericCommand> create(String custID, long balance) {
//		Logger.log("In 'create' of a new wallet actor : wallet-" + custID);
		return Behaviors.setup(context -> {
			return new Wallet(context, custID, balance);
		});
	}
	
	@Override
	public Receive<WalletGenericCommand> createReceive() {
//		Logger.log("---------------Inside createReceive of Wallet--------------------");
		return newReceiveBuilder()			
			.onMessage(GetBalance.class, this::onGetBalance)
			.onMessage(AddBalance.class, this::onAddBalance)
			.onMessage(DeductBalance.class, this::onDeductBalance)
			.onMessage(Reset.class, this::onReset)			
			.onMessage(WalletGenericCommand.class, notUsed -> {
				Logger.logErr("Shouldn't have received this generic command for wallet-" + this.custID);
				return this;
				})
			.build();
	}	
	
	//Messages ----------------------------------------------------------------
	
	//Receive 
	public static class WalletGenericCommand {
	}

	public static class GetBalance extends WalletGenericCommand {
		ActorRef<Wallet.ResponseBalance> replyTo;

		public GetBalance(ActorRef<ResponseBalance> replyTo) {
			super();
			this.replyTo = replyTo;
		}		
	}

	public static class DeductBalance extends WalletGenericCommand {
		long toDeduct;
		ActorRef<Wallet.ResponseBalance> replyTo;
		
		public DeductBalance(long toDeduct, ActorRef<ResponseBalance> replyTo) {
			super();
			this.toDeduct = toDeduct;
			this.replyTo = replyTo;
		}
	}

	public static class AddBalance extends WalletGenericCommand {
		long toAdd;

		public AddBalance(long toAdd) {
			super();
			this.toAdd = toAdd;
		}
	}

	public static class Reset extends WalletGenericCommand {
		ActorRef<Wallet.ResponseBalance> replyTo;

		public Reset(ActorRef<ResponseBalance> replyTo) {
			super();
			this.replyTo = replyTo;
		}
	}

	
	//Send
	public static class ResponseBalance extends WalletGenericCommand {
		long balance;

		public ResponseBalance(long balance) {
			super();
			this.balance = balance;
		}
		
		public boolean equals(Object o) {
	      if (this == o) return true;
	      if (o == null || getClass() != o.getClass()) return false;
	      
	      ResponseBalance responseBalance = (ResponseBalance) o;	      
	      return responseBalance.balance == this.balance;
	    }
	}
	
	//Message handlers ----------------------------------------------------------------

	private Behavior<WalletGenericCommand> onGetBalance(Wallet.GetBalance getBalanceCommand) {
		Logger.log("Received Wallet.GetBalance and responding with balance : " + this.accountBalance);
		getBalanceCommand.replyTo.tell(new ResponseBalance(this.accountBalance));
		
		return this;
	}
	
	private Behavior<WalletGenericCommand> onAddBalance(Wallet.AddBalance addBalanceCommand) {
		Logger.log("Received Wallet.AddBalance to add balance : " + addBalanceCommand.toAdd);
		
		if(addBalanceCommand.toAdd >= 0)
			this.accountBalance += addBalanceCommand.toAdd;
		else
			Logger.logErr("Received negative balance for addition");
		
		return this;
	}
	
	private Behavior<WalletGenericCommand> onDeductBalance(Wallet.DeductBalance deductBalanceCommand){
		Logger.log("Received Wallet.DeductBalance to deduct balance : " + deductBalanceCommand.toDeduct);
		
		if(deductBalanceCommand.toDeduct > this.accountBalance) {
			Logger.logErr("Insufficient balance!");
			deductBalanceCommand.replyTo.tell(new Wallet.ResponseBalance(-1));
		} else {
			Logger.log("Balance deduction successful.");
			this.accountBalance -= deductBalanceCommand.toDeduct;
			deductBalanceCommand.replyTo.tell(new Wallet.ResponseBalance(this.accountBalance));
		}
		
		return this;
	}
	
	private Behavior<WalletGenericCommand> onReset(Wallet.Reset resetCommand){
		Logger.log("Received Wallet.Reset");
		
		this.accountBalance = Globals.initReadWrapperObj.walletBalance;
		resetCommand.replyTo.tell(new Wallet.ResponseBalance(this.accountBalance));
		
		return this;
	}
}
