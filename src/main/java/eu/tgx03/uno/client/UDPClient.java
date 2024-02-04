package eu.tgx03.uno.client;

import eu.tgx03.uno.messaging.Command;
import eu.tgx03.uno.messaging.Update;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * An implementation of a Client for UNO using UDP datagrams.
 */
public class UDPClient extends Client {

	/**
	 * Used to signal that a new client wants to register.
	 */
	private static final byte[] REGISTER = "REGISTER".getBytes(StandardCharsets.UTF_8);
	/**
	 * How long to sleep between keep-alive packets.
	 */
	private static final int NAT_SLEEP_MILLISECONDS = 1000; // TODO: Hasn't been tested

	/**
	 * The socket used to communicate with the server.
	 */
	private final DatagramSocket socket;
	/**
	 * The address of the game server.
	 */
	private final SocketAddress remoteAddress;
	/**
	 * The ID assigned to this client.
	 */
	private final short id;

	/**
	 * Creates a new UDP client and directly registers it with the server.
	 *
	 * @param hostname The hostname of the game server.
	 * @param port     The port to contact the server on.
	 * @param nat      Whether NAT is in place and shall be dealt with.
	 * @throws IOException Gets thrown when communication with the server could not be established.
	 */
	public UDPClient(String hostname, int port, boolean nat) throws IOException {
		socket = new DatagramSocket();
		remoteAddress = new InetSocketAddress(hostname, port);
		socket.connect(remoteAddress);
		DatagramPacket request = new DatagramPacket(REGISTER, REGISTER.length, remoteAddress);
		socket.send(request);
		byte[] buffer = new byte[2];
		DatagramPacket reply = new DatagramPacket(buffer, 2);
		socket.receive(reply);
		id = ByteBuffer.wrap(buffer).getShort();

		// Create the UDP hole puncher
		if (nat) {
			Thread.ofVirtual().name("HolePuncher").start(() -> {
				DatagramPacket packet = new DatagramPacket(new byte[0], 0, remoteAddress);
				while (!ended) {
					try {
						Thread.sleep(NAT_SLEEP_MILLISECONDS);
						socket.send(packet);
					} catch (IOException | InterruptedException ignored) {
					}
				}
			});
		}
	}

	@Override
	public void kill() {
		super.kill();
		socket.close();
	}

	@Override
	protected void sendCommand(Command command) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytes);
		out.writeShort(id);
		out.writeObject(command);
		DatagramPacket packet = new DatagramPacket(bytes.toByteArray(), bytes.size(), remoteAddress);
		socket.send(packet);
	}

	@Override
	public void run() {
		while (!ended) {
			byte[] buf = new byte[1 << 16];
			DatagramPacket packet = new DatagramPacket(buf, 1 << 16);
			try {
				socket.receive(packet);
				ByteArrayInputStream in = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
				Update update = (Update) new ObjectInputStream(in).readObject();
				super.update(update);
			} catch (IOException | ClassNotFoundException | ClassCastException ignored) {
			}
		}
	}
}