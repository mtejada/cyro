package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import static javafx.scene.input.DataFormat.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detect motion.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class DetectMotionExample extends JFrame implements Runnable {

    private static final long serialVersionUID = -585739158170333370L;

    private static final int INTERVAL = 100; // ms

    private ImageIcon motion = null;
    private ImageIcon nothing = null;
    private JLabel label = null;
    private Media nooo = null;
    private Webcam webcam = Webcam.getDefault();
    private int threshold = 25;
    private int inertia = 1; // how long motion is valid
    private MediaPlayer mediaPlayer;
    private boolean Movimiento = false;

    public DetectMotionExample() {

        try {
            motion = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/movement.jpg")));
            nothing = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/wallpaper.png")));
            //URL thing = getClass().getResource("/nooo.mp3");
            URL thing = getClass().getResource("/cyro1.mp3");
            nooo = new Media(thing.toString());
            mediaPlayer = new MediaPlayer(nooo);

        } catch (IOException e) {
            e.printStackTrace();
        }

        label = new JLabel(nothing);

        Thread updater = new Thread(this, "updater-thread");
        updater.setDaemon(true);
        updater.start();

        setTitle("Cyro deja de hacer cagadas!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        //webcam.setViewSize(new Dimension(320, 240));
        webcam.setViewSize(new Dimension(640, 480));
        WebcamPanel panel = new WebcamPanel(webcam);

        add(panel);
        add(label);

        pack();

        add(this.fxPanel);

        setVisible(true);
    }
    public final JFXPanel fxPanel = new JFXPanel();

    public static void main(String[] args) throws InterruptedException {
        new DetectMotionExample();
    }

    @Override
    public void run() {

        //initFX(fxPanel);
        WebcamMotionDetector detector = new WebcamMotionDetector(webcam, threshold, inertia);
        detector.setInterval(INTERVAL);
        detector.start();

        while (true) {

            Icon icon = label.getIcon();
            Status estado = mediaPlayer.getStatus();
            if (mediaPlayer.getCycleDuration().toSeconds() == mediaPlayer.getCurrentTime().toSeconds() && estado != Status.STOPPED) {

                mediaPlayer.stop();
                mediaPlayer = new MediaPlayer(nooo);
                label.setIcon(nothing);
            }
            if (detector.isMotion()) {
                if (icon != motion) {

                    label.setIcon(motion);
                }
                if (!Movimiento) {
                    // si se agrega estado  && != Status.PLAYING && estado != Status.UNKNOWN solo saca una foto
                    Movimiento = true;
                    BufferedImage image = detector.getWebcam().getImage();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        Date date = new Date();
                        String sDate = sdf.format(date);
                        ImageIO.write(image, "JPG", new File(sDate + "_evidencia.jpg"));
                    } catch (IOException ex) {
                        Logger.getLogger(DetectMotionExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //aca que se ejecute
                System.out.println(mediaPlayer.getCycleDuration().toSeconds());
                System.out.println(mediaPlayer.getCurrentTime().toSeconds());

                if (estado != Status.PLAYING && estado != Status.UNKNOWN) {
                    mediaPlayer.play();

                }
                System.out.println(estado.toString());

                System.out.println(estado.toString());
                System.out.println("end");

            } else {
                if (Movimiento) {
                    Movimiento = false;
                }
                //aca que no se ejecute
                if (icon != nothing && estado != Status.PLAYING && estado != Status.UNKNOWN) {
                    label.setIcon(nothing);
                }
            }

            try {
                Thread.sleep(INTERVAL * 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
