package com.example;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import pods.cabs.Cab;
import pods.cabs.Globals;
import pods.cabs.Main;
import pods.cabs.RideService;
import pods.cabs.RideService.CabSignsIn;
import pods.cabs.Wallet;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.Logger;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.ClassRule;
import org.junit.Test;

//#definition
public class AkkaQuickstartTest {

	@ClassRule
	public static final TestKitJunitResource testKit = new TestKitJunitResource();

	public TestInterface testInterface;
//#definition

	// #test
//    @Test
//    public void testGreeterActorSendingOfGreeting() {
//        TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
//        ActorRef<Greeter.Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
//        underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
//        testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
//    }
	// #test

	@Test
	public void testMainStarted() {
		TestProbe<Main.StartedCommand> mainTestProbe = testKit.createTestProbe();
		ActorRef<Main.Command> underTest = testKit.spawn(Main.create(mainTestProbe.getRef()), "main");
		mainTestProbe.expectMessage(new Main.StartedCommand(true));
		Logger.log("Main Started\n");

		this.testInterface = new TestInterface(testKit);

		publicTest1();
		naivePrivateTest();
		privateTestCase1();
		privateTestCase2();
		privateTestCase3();
		privateTestCase4();
		privateTestCase5();
		privateTestCase6();
		privateTestCase7();
		privateTestCase8();
		privateTestCase9();
		privateTestCase10();

	}

	// Provided in the document
	public void publicTest1() {
		testInterface.startNewTest("Public Test 1");
		ActorRef<Cab.Command> cab101 = Globals.cabs.get("101");
		cab101.tell(new Cab.SignIn(10));
		ActorRef<RideService.Command> rideService = Globals.rideService[0];
		// If we are going to raise multiple requests in this script,
		// better to send them to different RideService actors to achieve
		// load balancing.

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
		rideService.tell(new RideService.RequestRide("201", 10, 100, probe.ref()));
		RideService.RideResponse resp = probe.receiveMessage();
		// Blocks and waits for a response message.
		// There is also an option to block for a bounded period of time
		// and give up after timeout.

		assertTrue(resp.rideId != -1);
		cab101.tell(new Cab.RideEnded(resp.rideId));
	}

	// Just checking all functionality
	public void naivePrivateTest() {
		testInterface.startNewTest("Naive Test");

		long initBalance = Globals.initReadWrapperObj.walletBalance;

		TestProbe<Wallet.ResponseBalance> walletTestProbe = testKit.createTestProbe();
		Globals.wallets.get("201").tell(new Wallet.GetBalance(walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));
		Logger.log("Success : Wallet GetBalance\n");

		Globals.wallets.get("201").tell(new Wallet.AddBalance(200));
		Logger.log("Success : Wallet AddBalance\n");

		Globals.wallets.get("201").tell(new Wallet.DeductBalance(200, walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(10000));
		Logger.log("Success : Wallet DeductBalance\n");

		Globals.wallets.get("201").tell(new Wallet.Reset(walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(initBalance));
		Logger.log("Success : Wallet Reset\n");

		// Testing for insufficient balance
		Globals.wallets.get("201").tell(new Wallet.DeductBalance(initBalance + 100, walletTestProbe.getRef()));
		walletTestProbe.expectMessage(new Wallet.ResponseBalance(-1));
		Logger.log("Success : Wallet Overdeduction disallowd\n");

		TestProbe<Cab.NumRidesReponse> cabTestProbe = testKit.createTestProbe();
		Globals.cabs.get("101").tell(new Cab.NumRides(cabTestProbe.getRef()));
		cabTestProbe.expectMessage(new Cab.NumRidesReponse(0));
		Logger.log("Success : Cab NumRides functional\n");

		try {
			Globals.cabs.get("101").tell(new Cab.SignIn(0));
			Globals.cabs.get("102").tell(new Cab.SignIn(20));
			Globals.cabs.get("103").tell(new Cab.SignIn(40));
			Globals.cabs.get("104").tell(new Cab.SignIn(60));
			Thread.sleep(1000);
			Globals.cabs.get("101").tell(new Cab.SignOut());
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TestProbe<RideService.RideResponse> fufillRideTestProbe = testKit.createTestProbe();
		Globals.rideService[0].tell(new RideService.RequestRide("201", 50, 100, fufillRideTestProbe.getRef()));
		RideService.RideResponse rideResponse = fufillRideTestProbe.receiveMessage();

		if (rideResponse.rideId <= 0) {
			assertTrue(false);
		}

		Globals.cabs.get(rideResponse.cabId).tell(new Cab.RideEnded(rideResponse.rideId));
	}

	// Customer doesn't have sufficient balance and shouldn't get a ride
	// when fare exceeds the wallet balance
	public void privateTestCase1() {
		testInterface.startNewTest("Test 1: Sufficient Balance");

		testInterface.cabSignIn("101", 0);
		testInterface.cabSignIn("102", 50);

		// now attempt a ride which has fare more than wallet balance
		RideService.RideResponse responseMsg = testInterface.requestRide("201", 0, 10000000);
		Logger.log("Test : received ride id : " + responseMsg.rideId);
		assertTrue(responseMsg.rideId < 0);
	}

	// Multiple requests to same cab and all alternate requests should be rejected
	public void privateTestCase2() {
		testInterface.startNewTest("Test 2 : Alternate Requests Reject");

		testInterface.cabSignIn("101", 10);
		testInterface.sleep();

		int acceptedRides = 0;

		RideService.RideResponse responseMsg ;

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			Globals.cabs.get(responseMsg.cabId).tell(new Cab.RideEnded(responseMsg.rideId));
			acceptedRides++;
		}

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			Globals.cabs.get(responseMsg.cabId).tell(new Cab.RideEnded(responseMsg.rideId));
			acceptedRides++;
		}

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			Globals.cabs.get(responseMsg.cabId).tell(new Cab.RideEnded(responseMsg.rideId));
			acceptedRides++;
		}

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			Globals.cabs.get(responseMsg.cabId).tell(new Cab.RideEnded(responseMsg.rideId));
			acceptedRides++;
		}

		Logger.logTestSuccess("Number of accepted rides : " + acceptedRides);
		assertTrue(acceptedRides <= 2);

	}
	
	// The nearest available accepting cab should always be assigned to the customer
	public void privateTestCase3() {
		testInterface.startNewTest("Test 3 : Nearest Cab Assignment");
		
		testInterface.cabSignIn("101", 10);
		testInterface.cabSignIn("102", 20);
		testInterface.cabSignIn("103", 30);
		testInterface.cabSignIn("104", 40);
		
		testInterface.sleep();
		
		RideService.RideResponse response;
		
		response = testInterface.requestRide("201", 45, 50);
		assert(response.cabId.equals("104"));
		
		response = testInterface.requestRide("202", 5, 10);
		assert(response.cabId.equals("101"));
		
		response = testInterface.requestRide("203", 26, 35);
		assert(response.cabId.equals("103"));
	}
	
	// What if the nearest cab doesn't accept the request. Second nearest cab (which is available and accepting)
	// should be assigned.
	public void privateTestCase4() {
		testInterface.startNewTest("Test 4 : Second Nearest Cab Assignment");
		
		testInterface.cabSignIn("101", 10);
		testInterface.cabSignIn("102", 50);
		
		testInterface.sleep();
		
		RideService.RideResponse response;
		
		response = testInterface.requestRide("201", 5, 100);
		assert(response.cabId.equals("101"));
		
		if (response.rideId >= 0) {
			Globals.cabs.get(response.cabId).tell(new Cab.RideEnded(response.rideId));
		}
		
		testInterface.sleep();
		
		response = testInterface.requestRide("202", 105, 20);
		assert(!response.cabId.equals("101"));
		
	}
	
	// Multiple cabs and multiple ride requests
	public void privateTestCase5() {
		testInterface.startNewTest("Test 5 : Multiple cabs, Multiple ride requests");
		
		testInterface.cabSignIn("101", 10);
		testInterface.cabSignIn("102", 20);
		testInterface.cabSignIn("103", 30);
		
		RideService.RideResponse responseMsg;
		responseMsg = testInterface.requestRide("201", 5, 100);
		
		int acceptedRides = 0;
		
		if (responseMsg.rideId >= 0) {
			acceptedRides++;
		}

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			acceptedRides++;
		}

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			acceptedRides++;
		}

		responseMsg = testInterface.requestRide("201", 10, 20);
		if (responseMsg.rideId >= 0) {
			acceptedRides++;
		}
		
		Logger.logTestSuccess("Accepted Rides : " + acceptedRides);
		assertTrue(acceptedRides <= 3);
	}

	public void privateTestCase6() {
		
	}

	public void privateTestCase7() {

	}

	public void privateTestCase8() {

	}

	public void privateTestCase9() {

	}

	public void privateTestCase10() {

	}
}
