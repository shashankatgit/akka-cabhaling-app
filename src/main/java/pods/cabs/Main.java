package pods.cabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.example.GreeterMain;
import com.example.Greeter.Greeted;
import com.example.GreeterMain.SayHello;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.Cab.CabGenericCommand;
import pods.cabs.Wallet.WalletGenericCommand;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.Logger;
import pods.cabs.utils.InitFileReader.InitReadWrapper;

public class Main extends AbstractBehavior<Main.MainGenericCommand> {
	
	public Main(ActorContext<MainGenericCommand> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public static final class MainGenericCommand {}
	
	public static final class StartedCommand {
		boolean status;
		
		public StartedCommand(boolean status) {
			this.status = status;
		}		
		
		public boolean equals(Object o) {
	      if (this == o) return true;
	      if (o == null || getClass() != o.getClass()) return false;
	      
	      StartedCommand startedCommand = (StartedCommand) o;	      
	      return startedCommand.status == this.status;
	    }
	}
	
    public static Behavior<MainGenericCommand> create(ActorRef<Main.StartedCommand> testProbe) {
    	Logger.log("Main actor being created");
    	
    	return Behaviors.setup(context -> {
    	
    	try {
    		InitReadWrapper wrapperObj = new InitReadWrapper();    		
			InitFileReader.readInitFile(wrapperObj);
			
			long initWalletBalance = wrapperObj.walletBalance;
			
			for (String cabID : wrapperObj.cabIDList) {
				Logger.log("Trying to spawn the actor cab-"+cabID);
				ActorRef<Cab.CabGenericCommand> cabActorRef = context.spawn(Cab.create(cabID), "cab-"+cabID);
				Globals.cabs.put(cabID, cabActorRef);
			}
			
			for (String custID : wrapperObj.custIDList) {
				Logger.log("Trying to spawn the actor wallet-"+custID+" with wallet balance: "+ initWalletBalance);
				ActorRef<Wallet.WalletGenericCommand> walletActorRef = context.spawn(Wallet.create(custID, initWalletBalance), "wallet-"+custID);
				Globals.wallets.put(custID, walletActorRef);
			}		
			
			testProbe.tell(new Main.StartedCommand(true));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return Behaviors.empty();
    	});
    }

	@Override
	public Receive<MainGenericCommand> createReceive() {
		return newReceiveBuilder()
		        .onMessage(MainGenericCommand.class, notUsed -> {
		        	Logger.logErr("Shouldn't have received this generic command for main actor");
		        	return this;
		        	})
		        .build();
	}
}
