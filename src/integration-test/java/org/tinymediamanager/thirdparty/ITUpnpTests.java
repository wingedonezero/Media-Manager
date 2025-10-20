package org.tinymediamanager.thirdparty;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.jupnp.UpnpService;
import org.jupnp.model.message.header.STAllHeader;
import org.tinymediamanager.thirdparty.upnp.TmmUpnpService;
import org.tinymediamanager.thirdparty.upnp.Upnp;

public class ITUpnpTests {

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void getRenderers() throws InterruptedException, IOException {
    Upnp u = Upnp.getInstance();
    u.sendPlayerSearchRequest();

    System.out.println("Waiting for keypress before shutting down...");
    System.in.read();

    u.shutdown();
  }

  @Test
  public void service() throws InterruptedException, IOException {
    Upnp u = Upnp.getInstance();
    u.startMediaServer();

    System.out.println("Waiting for keypress before shutting down...");
    System.in.read();

    u.shutdown();
  }

  @Test
  public void example() throws InterruptedException, IOException {

    // This will create necessary network resources for UPnP right away
    System.out.println("Starting jUPnP...");
    UpnpService upnpService = new TmmUpnpService(7879);
    upnpService.startup();

    // Send a search message to all devices and services, they should respond soon
    System.out.println("Sending SEARCH message to all devices...");
    upnpService.getControlPoint().search(new STAllHeader());

    // Let's wait 10 seconds for them to respond
    System.out.println("Waiting for keypress before shutting down...");
    System.in.read();

    // Release all resources and advertise BYEBYE to other UPnP devices
    System.out.println("Stopping jUPnP...");
    upnpService.shutdown();

    System.out.println("fin!");
  }
}
