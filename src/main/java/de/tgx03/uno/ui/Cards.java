package de.tgx03.uno.ui;

import de.tgx03.uno.game.cards.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

public class Cards {

    private static final Image[] RED = new Image[13];
    private static final Image[] GREEN = new Image[13];
    private static final Image[] YELLOW = new Image[13];
    private static final Image[] BLUE = new Image[13];
    private static final Image WILD;
    private static final Image TAKEFOUR;

    static {
        Thread[] threads = new Thread[4];
        threads[0] = new Thread(new Transcoder("RED", RED));
        threads[0].start();
        threads[1] = new Thread(new Transcoder("GREEN", GREEN));
        threads[1].start();
        threads[2] = new Thread(new Transcoder("YELLOW", YELLOW));
        threads[2].start();
        threads[3] = new Thread(new Transcoder("BLUE", BLUE));
        threads[3].start();

        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        String filename = "/cards/WILD.svg";
        TranscoderInput in = new TranscoderInput(Cards.class.getResourceAsStream(filename));
        try {
            transcoder.transcode(in, null);
        } catch (TranscoderException e) {
            System.err.println(e.getMessage());
        }
        WILD = SwingFXUtils.toFXImage(transcoder.img, null);

        filename = "/cards/WILD_TAKEFOUR.svg";
        in = new TranscoderInput(Cards.class.getResourceAsStream(filename));
        try {
            transcoder.transcode(in, null);
        } catch (TranscoderException e) {
            System.err.println(e.getMessage());
        }
        TAKEFOUR = SwingFXUtils.toFXImage(transcoder.img, null);

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static Image getCard(Card card) {
        int number = 0;
        if (card instanceof TakeFour) {
            return TAKEFOUR;
        } else if (card instanceof ChooseColor) {
            return WILD;
        } else if (card instanceof Default) {
            number = ((Default) card).value;
        } else if (card instanceof Reverse) {
            number = 10;
        } else if (card instanceof Skip) {
            number = 11;
        } else if (card instanceof TakeTwo) {
            number = 12;
        } else {
            throw new IllegalArgumentException("Unknown card");
        }
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
            default -> {
                throw new IllegalArgumentException("Card has invalid color");
            }
        }
    }

    private static class Transcoder implements Runnable {

        private final String color;
        private final Image[] target;

        public Transcoder(String color, Image[] target) {
            this.color = color;
            this.target = target;
        }

        @Override
        public void run() {
            BufferedImageTranscoder transcoder = new BufferedImageTranscoder();

            for (int i = 0; i < 10; i++) {
                String filename = "/cards/" + color + i + ".svg";
                TranscoderInput in = new TranscoderInput(getClass().getResourceAsStream(filename));
                try {
                    transcoder.transcode(in, null);
                    target[i] = SwingFXUtils.toFXImage(transcoder.img, null);
                } catch (TranscoderException e) {
                    System.err.println(e.getMessage());
                }
            }

            String filename = "/cards/" + color + "_REVERSE.svg";
            TranscoderInput in = new TranscoderInput(getClass().getResourceAsStream(filename));
            try {
                transcoder.transcode(in, null);
                target[10] = SwingFXUtils.toFXImage(transcoder.img, null);
            } catch (TranscoderException e) {
                System.err.println(e.getMessage());
            }

            filename = "/cards/" + color + "_SKIP.svg";
            in = new TranscoderInput(getClass().getResourceAsStream(filename));
            try {
                transcoder.transcode(in, null);
                target[11] = SwingFXUtils.toFXImage(transcoder.img, null);

            } catch (TranscoderException e) {
                System.err.println(e.getMessage());
            }

            filename = "/cards/" + color + "_TAKETWO.svg";
            in = new TranscoderInput(getClass().getResourceAsStream(filename));
            try {
                transcoder.transcode(in, null);
                target[12] = SwingFXUtils.toFXImage(transcoder.img, null);

            } catch (TranscoderException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {

        private BufferedImage img;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage bufferedImage, TranscoderOutput transcoderOutput) {
            this.img = bufferedImage;
        }
    }
}
