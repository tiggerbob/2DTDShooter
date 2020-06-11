import java.awt.*;
import java.awt.geom.AffineTransform;

public class Sparkle {

    private double x;
    private double y;
    private double w;
    private double h;

    private double dx;
    private double dy;
    private double rad;
    private double speed;

    private Color color;

    public Sparkle() {
        x = Math.random() * GamePanel.WIDTH;
        y = 0;
        speed = 15;

        double angle = Math.random() * 140 + 20;
        rad = Math.toRadians(angle);

        dx = Math.cos(rad) * speed;
        dy = Math.sin(rad) * speed;

        int rand1 = (int) (Math.random() * 100);
        int rand2 = (int) (Math.random() * 100);

        w = rand1 / 5.0;
        h = rand2 / 5.0;

        rand1 = (int) (Math.random() * 100);

        if (rand1 < 33) {
            color = new Color(255, 0, 0, 175);
        }else if (rand1 < 66) {
            color = new Color(0, 255, 0, 175);
//        } else if (rand1 < 60) {
//            color = Color.BLUE;
//        } else if (rand1 < 80) {
//            color = Color.PINK;
        } else {
            color = new Color(0, 255, 255, 175);
        }
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        AffineTransform old = g.getTransform();
        g.rotate(rad, x, y);
        g.fillRect((int) (x), (int) (y), (int) w, (int) h);
        g.setTransform(old);
    }
}
