package communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Created by Antonio on 08-03-2016.
 */
public class Channel {

    /**
     * IP of the channel
     */
    private String ip;
    /**
     * Port of channel
     */
    private String port;
    /**
     * Socket of the channel
     */
    private MulticastSocket socket;
    /**
     * Type of channel
     */
    private Sockets type;
    /**
     * InetAddress of the channel
     */
    private InetAddress address;

    Channel() {
    	try {
			socket = new MulticastSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    Channel(Sockets type1, String ip1, String port1) {
        ip = ip1;
        port = port1;
        type = type1;

        try {
            address = InetAddress.getByName(ip);
            initializeSocket();
            socket.joinGroup(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Initializes the socket as a multicast socket
     */
    private void initializeSocket() {
        try {
            socket = new MulticastSocket(Integer.parseInt(getPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public Sockets getType() {
        return type;
    }

    public InetAddress getAddress() {
        return address;
    }

	public int getSocketPort() {
		return socket.getLocalPort();
	}
}
