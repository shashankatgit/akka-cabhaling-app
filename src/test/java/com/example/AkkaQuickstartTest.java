package com.example;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import pods.cabs.Main;

import org.junit.ClassRule;
import org.junit.Test;

//#definition
public class AkkaQuickstartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
//#definition

    //#test
//    @Test
//    public void testGreeterActorSendingOfGreeting() {
//        TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
//        ActorRef<Greeter.Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
//        underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
//        testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
//    }
    //#test
    
    @Test
    public void testGreeterActorSendingOfGreeting() {
//      TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
      ActorRef<Main.MainGenericCommand> underTest = testKit.spawn(Main.create(), "main");
//      underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
//      testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
  }
}
