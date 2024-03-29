package eu.tgx03.uno.ui;

import eu.tgx03.uno.game.cards.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

/**
 * The class responsible for providing the images representing the cards.
 */
public final class Cards {

	/**
	 * The images representing all the red cards.
	 */
	private static final Image[] RED = new Image[13];
	/**
	 * The images representing all green cards.
	 */
	private static final Image[] GREEN = new Image[13];
	/**
	 * The images representing all the yellow cards.
	 */
	private static final Image[] YELLOW = new Image[13];
	/**
	 * The images representing all the blue cards.
	 */
	private static final Image[] BLUE = new Image[13];
	/**
	 * The image of the wild card.
	 */
	private static final Image WILD;
	/**
	 * The image of the wild take four card.
	 */
	private static final Image TAKEFOUR;

	// Initializes the images before first usage
	static {

		// Create the threads transcoding the colored cards
		Thread[] threads = new Thread[4];
		threads[0] = new Thread(new Transcoder("RED", RED));
		threads[0].start();
		threads[1] = new Thread(new Transcoder("GREEN", GREEN));
		threads[1].start();
		threads[2] = new Thread(new Transcoder("YELLOW", YELLOW));
		threads[2].start();
		threads[3] = new Thread(new Transcoder("BLUE", BLUE));
		threads[3].start();

		// Translate the normal wildcard
		BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
		String filename = "/cards/WILD.svg";
		TranscoderInput in = new TranscoderInput(Cards.class.getResourceAsStream(filename));
		try {
			transcoder.transcode(in, null);
		} catch (TranscoderException e) {
			ExceptionDialog.showException(e);
		}
		WILD = SwingFXUtils.toFXImage(transcoder.img, null);

		// Translate the wild take four card
		filename = "/cards/WILD_TAKEFOUR.svg";
		in = new TranscoderInput(Cards.class.getResourceAsStream(filename));
		try {
			transcoder.transcode(in, null);
		} catch (TranscoderException e) {
			ExceptionDialog.showException(e);
		}
		TAKEFOUR = SwingFXUtils.toFXImage(transcoder.img, null);

		// Wait for the other threads to finish
		try {
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException ignored) {
		}
	}

	/**
	 * Private cause utility class.
	 *
	 * @throws IllegalAccessError No.
	 */
	private Cards() throws IllegalAccessError {
		throw new IllegalAccessError("Not instantiable");
	}

	/**
	 * Returns the image corresponding to the provided card.
	 *
	 * @param card The card to get the image for.
	 * @return The corresponding image.
	 */
	@NotNull
	public static Image getCard(@NotNull Card card) {

		// Get the position in the corresponding array
		int number;
		if (card.getClass() == TakeFour.class) {
			return TAKEFOUR;
		} else if (card.getClass() == ChooseColor.class) {
			return WILD;
		} else if (card.getClass() == Default.class) {
			number = ((Default) card).value;
		} else if (card.getClass() == Reverse.class) {
			number = 10;
		} else if (card.getClass() == Skip.class) {
			number = 11;
		} else if (card.getClass() == TakeTwo.class) {
			number = 12;
		} else {
			throw new IllegalArgumentException("Unknown card");
		}

		// Get the card from the corresponding color array
		switch (card.color()) {
			case RED -> {
				return RED[number];
			}
			case YELLOW -> {
				return YELLOW[number];
			}
			case GREEN -> {
				return GREEN[number];
			}
			case BLUE -> {
				return BLUE[number];
			}
			default -> throw new IllegalArgumentException("Card has invalid color");
		}
	}

	/**
	 * A class responsible for transcoding the images in a separate threads.
	 */
	private record Transcoder(@NotNull String color, @NotNull Image[] target) implements Runnable {

		@Override
		public void run() {
			BufferedImageTranscoder transcoder = new BufferedImageTranscoder();

			// Transcode the normal number
			for (int i = 0; i < 10; i++) {
				String filename = "/cards/" + color + i + ".svg";
				TranscoderInput in = new TranscoderInput(getClass().getResourceAsStream(filename));
				try {
					transcoder.transcode(in, null);
					target[i] = SwingFXUtils.toFXImage(transcoder.img, null);
				} catch (TranscoderException e) {
					ExceptionDialog.showException(e);
				}
			}

			// Transcode the reverse card
			String filename = "/cards/" + color + "_REVERSE.svg";
			TranscoderInput in = new TranscoderInput(getClass().getResourceAsStream(filename));
			try {
				transcoder.transcode(in, null);
				target[10] = SwingFXUtils.toFXImage(transcoder.img, null);
			} catch (TranscoderException e) {
				ExceptionDialog.showException(e);
			}

			// Transcode the skip card
			filename = "/cards/" + color + "_SKIP.svg";
			in = new TranscoderInput(getClass().getResourceAsStream(filename));
			try {
				transcoder.transcode(in, null);
				target[11] = SwingFXUtils.toFXImage(transcoder.img, null);

			} catch (TranscoderException e) {
				ExceptionDialog.showException(e);
			}

			// Transcode the take two card
			filename = "/cards/" + color + "_TAKETWO.svg";
			in = new TranscoderInput(getClass().getResourceAsStream(filename));
			try {
				transcoder.transcode(in, null);
				target[12] = SwingFXUtils.toFXImage(transcoder.img, null);

			} catch (TranscoderException e) {
				ExceptionDialog.showException(e);
			}
		}
	}

	/**
	 * I just copied this from the internet, I have no idea what exactly this does.
	 */
	private static class BufferedImageTranscoder extends ImageTranscoder {

		/**
		 * The created image.
		 */
		private BufferedImage img;

		@Override
		@NotNull
		public BufferedImage createImage(int width, int height) {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		@Override
		public void writeImage(@NotNull BufferedImage bufferedImage, @Nullable TranscoderOutput transcoderOutput) {
			this.img = bufferedImage;
		}
	}
}
