package eu.tgx03.uno.client;

import eu.tgx03.uno.messaging.Command;
import eu.tgx03.uno.messaging.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Objects;

/**
 * The client of a UNO-Game. It only holds information of its assigned player
 * and handles communication with the host.
 */
public class SocketClient extends Client {

	/**
	 * The input from the host were game updates are received.
	 */
	private final ObjectInputStream input;
	/**
	 * The output to the host where requests are sent through.
	 */
	private final ObjectOutputStream output;


	/**
	 * Creates a new client that is connected to the host and interfaces with it.
	 *
	 * @param host     The hostname of the server.
	 * @param hostPort The port to connect to.
	 * @throws IOException If an error occurred when trying to establish the connection.
	 */
	public SocketClient(@NotNull String host, int hostPort) throws IOException {
		@SuppressWarnings("resource") Socket socket = new Socket(host, hostPort);
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
		Thread thread = new Thread(this, "Client-Receiver");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Informs this client that the game is to be ended.
	 * The client actually only shuts down when either a new update is received
	 * or an error occurs during last transmission.
	 */
	public synchronized void kill() {
		super.kill();
		try {
			input.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	protected void sendCommand(@NotNull Command command) throws IOException {
		output.reset();
		output.writeObject(command);
	}

	/**
	 * Basically the daemon waiting for updates from the server.
	 */
	@Override
	public void run() {
		do {
			try {
				Update update = (Update) input.readObject();
				this.update(update);
				if (update.ended) {
					ended = true;
				}
			} catch (IOException | ClassCastException | ClassNotFoundException e) {
				if (!ended) handleException(e);
			}
		} while (!ended);
		System.out.println("Shutting down client thread");
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (o instanceof SocketClient c) {
			return output == c.output && input == c.input && player == c.player;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(input, output, player);
	}
}
