package pods.cabs;

import java.util.ArrayList;
import java.util.HashMap;

import akka.actor.typed.ActorRef;
import pods.cabs.Cab.CabGenericCommand;
import pods.cabs.Main.MainGenericCommand;


public class Globals {
	//final?
	public static final HashMap<String,ActorRef<Cab.CabGenericCommand>> cabs;          //Cab Ids are keys
	public static final HashMap<String,ActorRef<Wallet.WalletGenericCommand>> wallets; //Cust Ids are keys
	public static final ArrayList<RideService> rideService;
	
	static {
		cabs = new HashMap<>();
		wallets = new HashMap<>();
		rideService = new ArrayList<>();
	}
}
