package pods.cabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import akka.actor.typed.ActorRef;
import pods.cabs.Cab.CabGenericCommand;
import pods.cabs.Main.MainGenericCommand;
import pods.cabs.utils.InitFileReader.InitReadWrapper;


public class Globals {
	
	public static final int N_RIDE_SERVICE_INSTANCES = 10;
	
	public static final HashMap<String,ActorRef<Cab.CabGenericCommand>> cabs;          //Cab Ids are keys
	public static final HashMap<String,ActorRef<Wallet.WalletGenericCommand>> wallets; //Cust Ids are keys
	public static final ArrayList<ActorRef<RideService.RideServiceGenericCommand>> rideService;
	
	public static InitReadWrapper initReadWrapperObj;
	
	public static final AtomicLong rideIdSequence;
	
	static {
		cabs = new HashMap<>();
		wallets = new HashMap<>();
		rideService = new ArrayList<>();
		rideIdSequence = new AtomicLong(0);
	}
}
