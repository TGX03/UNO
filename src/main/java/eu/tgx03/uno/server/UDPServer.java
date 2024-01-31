package eu.tgx03.uno.server;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import eu.tgx03.uno.game.Game;
import eu.tgx03.uno.game.Rules;
import eu.tgx03.uno.messaging.Command;
import eu.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A server implementation for UNO using UDP.
 */
public class UDPServer extends Server implements Runnable {

	/**
	 * The default size for a buffer for receiving commands.
	 */
	private static final int DEFAULT_RECEIVE_SIZE = 200;

	/**
	 * This Random gets used to generate the IDs for the clients.
	 */
	private static final Random RANDOM = new Random();
	/**
	 * Used to signal that a new client wants to register.
	 */
	private static final byte[] REGISTER = "REGISTER".getBytes(StandardCharsets.UTF_8);

	/**
	 * The rules to be used in the game later on.
	 */
	private final Rules rules;
	/**
	 * The socket used for sending and receiving UDP packets.
	 */
	private final DatagramSocket socket;
	/**
	 * The list holding the IDs and addresses of all the clients.
	 */
	private final ArrayList<Client> clients = new ArrayList<>(5);
	/**
	 * The lock for reading from the client list.
	 */
	private final Lock idReaderLock;
	/**
	 * The lock for writing to the client list.
	 */
	private final Lock idWriterLock;

	// Creating the locks.
	{
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		idReaderLock = lock.readLock();
		idWriterLock = lock.writeLock();
	}

	/**
	 * Create a new UDP server.
	 *
	 * @param port  The port to listen on for packages.
	 * @param rules The rules to be used in the game later.
	 * @throws IOException When the socket couldn't be set up.
	 */
	public UDPServer(int port, @Nullable Rules rules) throws IOException {
		this.rules = rules;
		this.socket = new DatagramSocket(port);
	}

	@Override
	public void start() {
		start = true;
		game = new Game(getPlayerCount(), rules);
		byte[] random = new byte[4];
		RANDOM.nextBytes(random);
		DatagramPacket packet = new DatagramPacket(random, 4, socket.getLocalSocketAddress());
		try {
			socket.send(packet);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public int getPlayerCount() {
		return clients.size();
	}

	@Override
	public void kill() {
		super.kill();
		socket.close();
	}

	@Override
	protected void update() {
		game.gameLock.lock();
		short[] cardCount = game.getCardCount();
		idReaderLock.lock();
		clients.parallelStream().forEach(client -> {
			boolean turn = game.getCurrentPlayer() == client.id;
			Update update = new Update(turn, game.hasEnded(), game.getPlayer(client.id), game.getTopCard(), cardCount, (short) game.getStackSize());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				ObjectOutputStream objects = new ObjectOutputStream(out);
				objects.writeObject(update);
				DatagramPacket packet = new DatagramPacket(out.toByteArray(), out.size(), client.address);
				socket.send(packet);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		idReaderLock.unlock();
		game.gameLock.unlock();
	}

	@Override
	public void run() {
		while (!start && !kill) {

			// Wait for new clients and give them IDs
			byte[] buffer = new byte[8];
			DatagramPacket packet = new DatagramPacket(buffer, 8);
			try {
				socket.receive(packet);
				Thread.ofVirtual().start(() -> allocateID(packet));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		// Receive commands from clients
		while (!kill && !game.hasEnded()) {
			byte[] buffer = new byte[DEFAULT_RECEIVE_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, DEFAULT_RECEIVE_SIZE);
			Thread.ofVirtual().start(() -> decodeCommand(packet));
		}

		// After game end, send out a final update.
		update();
		socket.close();
	}

	/**
	 * Create a new ID for a client and give that ID to the client, while storing the address.
	 *
	 * @param packet The packet of the client to store the address of.
	 */
	private void allocateID(DatagramPacket packet) {
		if (!Arrays.equals(packet.getData(), REGISTER)) return;

		// Generate a new ID for the new user.
		Client client = new Client((short) RANDOM.nextInt(), packet.getSocketAddress());
		idWriterLock.lock();
		clients.add(client);
		idWriterLock.unlock();

		// Create and send the answer.
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(client.id);
		DatagramPacket answer = new DatagramPacket(buffer.array(), 2, client.address);
		try {
			socket.send(answer);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Decode a packet containing a command.
	 *
	 * @param packet The packet to decode.
	 */
	private void decodeCommand(DatagramPacket packet) {
		ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
		short id = buffer.getShort();

		try {
			ObjectInputStream input = new ObjectInputStream(new ByteBufferBackedInputStream(buffer));
			Command command = (Command) input.readObject();

			int player = -1;
			idReaderLock.lock();
			for (int i = 0; i < clients.size(); i++) if (clients.get(i).id == id) player = i;

			// If a new address got used, update it internally.
			if (!clients.get(id).address.equals(packet.getSocketAddress())) {
				idReaderLock.unlock();
				idWriterLock.lock();
				clients.get(id).address = packet.getSocketAddress();
				idWriterLock.unlock();
			} else idReaderLock.unlock();

			if (player >= 0) super.executeCommand(player, command);
			else throw new IllegalArgumentException("Unknown player");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (ClassNotFoundException ignored) {  // Probably means an invalid packet was received
		}
	}

	/**
	 * A data class used for storing the ID and address of a client.
	 */
	private static class Client {

		/**
		 * The ID of the corresponding client.
		 */
		private final short id;

		/**
		 * The address of the corresponding client.
		 */
		private SocketAddress address;

		/**
		 * Create a new container for a client.
		 *
		 * @param id      The ID of the client.
		 * @param address The address of the client.
		 */
		public Client(short id, SocketAddress address) {
			this.id = id;
			this.address = address;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Client c) return this.id == c.id;
			else return false;
		}

		@Override
		public int hashCode() {
			return Short.hashCode(this.id);
		}
	}
}