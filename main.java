import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.util.Arrays;
import java.util.Comparator;
import java.awt.geom.GeneralPath;

public class Main extends JPanel {
    private int x = 125;
    private int y = 350;
    private int steeringX = 100;
    private int steeringY = 0;
    private double[][] coordinates = {
            { 50.0, 1100.0 },
            { 100.0, 900.0 },
            { 200.0, 600.0 },
            { 350.0, 300.0 },
            { 400.0, 100.0 }
    };

    private Image sprite;
    private Image steeringWheelSprite;

    private double acceleration = 0;
    private double current_angle = 0;
    private double current_speed = 0;
    private boolean leftKeyPressed = false;
    private boolean rightKeyPressed = false;
    private boolean enterKeyPressed = false;

    private boolean running = true;

    private void drawSmoothRoad(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);

        // Sort coordinates by y-value
        Arrays.sort(coordinates, Comparator.comparingDouble(coord -> coord[1]));

        if (coordinates.length < 2)
            return;

        GeneralPath path = new GeneralPath();
        path.moveTo(coordinates[0][0], coordinates[0][1]);

        for (int i = 0; i < coordinates.length - 1; i++) {
            double x0 = i > 0 ? coordinates[i - 1][0] : coordinates[i][0];
            double y0 = i > 0 ? coordinates[i - 1][1] : coordinates[i][1];
            double x1 = coordinates[i][0];
            double y1 = coordinates[i][1];
            double x2 = coordinates[i + 1][0];
            double y2 = coordinates[i + 1][1];
            double x3 = (i < coordinates.length - 2) ? coordinates[i + 2][0] : coordinates[i + 1][0];
            double y3 = (i < coordinates.length - 2) ? coordinates[i + 2][1] : coordinates[i + 1][1];

            for (double t = 0; t < 1; t += 0.05) {
                double xt = 0.5 * ((-x0 + 3 * x1 - 3 * x2 + x3) * t * t * t +
                        (2 * x0 - 5 * x1 + 4 * x2 - x3) * t * t +
                        (-x0 + x2) * t +
                        2 * x1);
                double yt = 0.5 * ((-y0 + 3 * y1 - 3 * y2 + y3) * t * t * t +
                        (2 * y0 - 5 * y1 + 4 * y2 - y3) * t * t +
                        (-y0 + y2) * t +
                        2 * y1);

                path.lineTo(xt, yt);
            }
        }

        // Draw the final path
        g2d.draw(path);
    }

    public Main() {
        sprite = new ImageIcon("Car_Sprite.png").getImage();
        steeringWheelSprite = new ImageIcon("SteeringWheel.png").getImage();
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftKeyPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightKeyPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    enterKeyPressed = true;
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftKeyPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightKeyPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    enterKeyPressed = false;
                }
            }
        });
    }

    private void gameLoop() {
        while (running) {
            update();
            repaint();
            try {
                Thread.sleep(16); // should be 60 fps
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        if (leftKeyPressed) {
            acceleration = -0.5;
            if (enterKeyPressed) {
                acceleration = -1;
            }
        } else if (rightKeyPressed) {
            acceleration = 0.5;
            if (enterKeyPressed) {
                acceleration = 1;
            }
        } else {
            acceleration = 0;
            if (current_speed > 0) {
                current_speed -= 0.5;
            }
            if (current_speed < 0) {
                current_speed += 0.5;
            }
        }

        // Simulated Friction:
        if (current_speed > 0) {
            current_speed -= 0.25;
        }
        if (current_speed < 0) {
            current_speed += 0.25;
        }

        if (acceleration > 4) {
            acceleration = 4;
        }
        if (acceleration < -4) {
            acceleration = -4;
        }
        if (current_angle > 90) {
            acceleration = -1;
        }
        if (current_angle < -90) {
            acceleration = 1;
        }

        current_speed += acceleration;
        current_angle += current_speed;

        // Road moving
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i][1] += Math.cos(current_angle * Math.PI / 180.0) * 5;
            coordinates[i][0] -= Math.sin(current_angle * Math.PI / 180.0) * 5;
            if (coordinates[i][1] > 1200) {
                coordinates[i][1] = 0;
                coordinates[i][0] = Math.random() * 600.0;
            }
        }

        // Print coordinates (for debugging)
        for (int i = 0; i < coordinates.length; i++) {
            System.out.println("X: " + coordinates[i][0] + "  Y: " + coordinates[i][1]);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw the road with smooth Bézier curves
        drawSmoothRoad(g2d);

        int carSpriteWidth = sprite.getWidth(this);
        int carSpriteHeight = sprite.getHeight(this);
        int carCenterX = carSpriteWidth / 2 + x;
        int carCenterY = carSpriteHeight / 2 + y;

        int steeringWheelWidth = steeringWheelSprite.getWidth(this);
        int steeringWheelHeight = steeringWheelSprite.getHeight(this);
        int steeringWheelCenterX = steeringWheelWidth / 2 + steeringX;
        int steeringWheelCenterY = steeringWheelHeight / 2 + steeringY;

        AffineTransform originalTransform = g2d.getTransform();

        AffineTransform carTransform = new AffineTransform();
        carTransform.translate(x + carCenterX, y + carCenterY);
        carTransform.rotate(Math.toRadians(current_angle + 180));
        carTransform.translate(-carCenterX, -carCenterY);
        g2d.setTransform(carTransform);

        g2d.drawImage(sprite, x, y, this);

        AffineTransform steeringTransform = new AffineTransform();
        steeringTransform.translate(steeringX + steeringWheelCenterX, steeringY + steeringWheelCenterY);
        steeringTransform.rotate(Math.toRadians(acceleration * 100));
        steeringTransform.translate(-steeringWheelCenterX, -steeringWheelCenterY);
        g2d.setTransform(steeringTransform);

        g2d.drawImage(steeringWheelSprite, steeringX, steeringY, this);

        g2d.setTransform(originalTransform);
    }

    public static void main(String[] args) {
        System.out.println("////////////////////////////////////////");
        JFrame frame = new JFrame("Sprite Mover");
        Main panel = new Main();
        frame.add(panel);
        frame.setSize(600, 1200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Thread(panel::gameLoop).start();
    }
}
