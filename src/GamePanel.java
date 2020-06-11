import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    public static int WIDTH = 400;
    public static int HEIGHT = 400;

    private Thread thread;
    private boolean running;

    private BufferedImage image;
    private Graphics2D g;

    private int FPS = 30;
    private double averageFPS;

    public static Player player;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<PowerUp> powerUps;
    public static ArrayList<Explosion> explosions;
    public static ArrayList<Text> texts;
    public static ArrayList<Sparkle> sparkles;
//    public static ArrayList<Highscores> scores;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private boolean waveStart;
    private int waveDelay = 2000;

    private long slowDownTimer;
    private long slowDownTimerDiff;
    private int slowDownLength = 6000;
    private int endWave = 2;

    Rectangle r = new Rectangle(100,100,100,30);
    String name = "";

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();

        addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() != KeyEvent.VK_Z && e.getKeyCode() != KeyEvent.VK_UP
                && e.getKeyCode() != KeyEvent.VK_DOWN && e.getKeyCode() != KeyEvent.VK_LEFT
                && e.getKeyCode() != KeyEvent.VK_RIGHT && e.getKeyCode() != KeyEvent.VK_SHIFT && e.getKeyCode() != KeyEvent.VK_BACK_SPACE)
                    if (g.getFontMetrics().stringWidth(name) <= r.width - 8) {
                        name += e.getKeyChar();
                    }
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (name != null && name.length() != 0)
                        name = name.substring(0, name.length() - 1);
                }

                //you might not need this is you are rendering constantly
                if (!running) {
                    repaint();
                }
            }
        });
    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    @Override
    public void run() {
        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        player = new Player();
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        powerUps = new ArrayList<PowerUp>();
        explosions = new ArrayList<Explosion>();
        texts = new ArrayList<Text>();
        sparkles = new ArrayList<Sparkle>();
//        scores = new ArrayList<Highscores>();

        waveStartTimer = 0;
        waveStartTimerDiff = 0;
        waveStart = true;
        waveNumber = 0;

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;
        int maxFrameCount = 30;

        long targetTime = 1000 / FPS;

        while (running) {

            startTime = System.nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            if(waveNumber == endWave) {
                startSparkles();
            }

            URDTimeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - URDTimeMillis;

            try {
                Thread.sleep(Math.abs(waitTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == maxFrameCount) {
                averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
            }

        }
            g.setColor(new Color(0, 100, 255));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Century Gothic", Font.PLAIN, 16));
            String s = "G A M E  O V E R";
            int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
            s = "Final Score: " + player.getScore();
            length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 30);
            if (player.getScore() > Integer.parseInt(player.getHighScore())) {
                player.setHighScore(player.getScore());
                paintComponent(g);
            }
            s = "High Score: " + player.getHighScore();
            length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 60);
            gameDraw();

    }

    private void gameUpdate() {

        if (waveStartTimer == 0 && enemies.size() == 0) {
            waveNumber++;
            waveStart = false;
            waveStartTimer = System.nanoTime();
        } else {
            waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }

        if (waveStart && enemies.size() == 0) {
            createNewEnemies();
        }

        player.update();

        for(int i = 0; i < bullets.size(); i++) {
            boolean remove = bullets.get(i).update();
            if (remove) {
                bullets.remove(i);
                i--;
            }
        }

        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).update();
        }

        for (int i = 0; i < powerUps.size(); i++) {
            boolean remove = powerUps.get(i).update();
            if (remove) {
                powerUps.remove(i);
                i--;
            }
        }

        for (int i = 0; i < explosions.size(); i++) {
            boolean remove = explosions.get(i).update();
            if (remove) {
                explosions.remove(i);
                i--;
            }
        }

        for (int i = 0; i < texts.size(); i++) {
            boolean remove = texts.get(i).update();
            if (remove) {
                texts.remove(i);
                i--;
            }
        }

        for (int i = 0; i < sparkles.size(); i++) {
            sparkles.get(i).update();
//            System.out.println("Sparkle update");
        }

        //Bullet - enemy collision
        for (int i = 0; i < bullets.size(); i++) {

            Bullet b = bullets.get(i);
            double bx = b.getX();
            double by = b.getY();
            double br = b.getR();

            for (int j = 0; j < enemies.size(); j++) {

                Enemy e = enemies.get(j);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

                double dx = bx - ex;
                double dy = by - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < br + er) {
                    e.hit();
                    bullets.remove(i);
                    i--;
                    break;
                }

            }
        }

        // check dead enemies
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).isDead()) {
                Enemy e = enemies.get(i);

                double rand = Math.random();
                if (rand < 0.001) powerUps.add(new PowerUp(1, e.getX(), e.getY()));
                else if (rand < 0.020) powerUps.add(new PowerUp(3, e.getX(), e.getY()));
                else if (rand < 0.120) powerUps.add(new PowerUp(2, e.getX(), e.getY()));
                else if (rand < 0.130) powerUps.add(new PowerUp(4, e.getX(), e.getY()));

                player.addScore(e.getType() + e.getRank());
                enemies.remove(i);
                i--;

                e.explode();
                explosions.add(new Explosion(e.getX(), e.getY(), e.getR(), e.getR() + 20));
            }
        }
        if (player.isDead()) {
            running = false;
        }

        //Player - Enemy collision
        if (!player.isRecovering()) {
            int px = player.getX();
            int py = player.getY();
            int pr = player.getR();
            for(int i = 0; i < enemies.size(); i++) {

                Enemy e = enemies.get(i);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

                double dx = px - ex;
                double dy = py - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < pr + er) {
                     player.loseLife();
                }

            }
        }

        //player - powerup collision
        int px = player.getX();
        int py = player.getY();
        int pr = player.getR();
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp p = powerUps.get(i);
            double x = p.getX();
            double y = p.getY();
            double r = p.getR();
            double dx = px - x;
            double dy = py - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            //collected powerup
            if (dist < pr + r) {

                int type = p.getType();

                if (type == 1) {
                    player.gainLife();
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Extra Life"));
                }
                if (type == 2) {
                    player.increasePower(1);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "One Power"));
                }
                if (type == 3) {
                    player.increasePower(2);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Double Power"));
                }
                if (type == 4) {
                    slowDownTimer = System.nanoTime();
                    for(int j = 0; j < enemies.size(); j++) {
                        enemies.get(j).setSlow(true);
                    }
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Slow Down"));
                }

                powerUps.remove(i);
                i--;

            }
        }

        //slowdown update
        if (slowDownTimer != 0) {
            slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
            if (slowDownTimerDiff > slowDownLength) {
                slowDownTimer = 0;
                for(int j = 0; j < enemies.size(); j++) {
                    enemies.get(j).setSlow(false);
                }
            }
        }

    }

    private void gameRender() {
        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if (slowDownTimer != 0) {
            g.setColor(new Color(255, 255, 255, 64));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        player.draw(g);

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g);
        }

        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }

        for(int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).draw(g);
        }

        for (int i = 0; i < explosions.size(); i++) {
            explosions.get(i).draw(g);
        }

        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).draw(g);
        }

        for (int i = 0; i < sparkles.size(); i++) {
            sparkles.get(i).draw(g);
//            System.out.println("Sparkle drawn");
        }

        if (waveStartTimer != 0 && waveNumber < endWave) {
            g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
            String s = " - W A V E  " + waveNumber + "  -";
            int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            int alpha = (int) ( 255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
            if (alpha > 255) alpha = 255;
            g.setColor(new Color(255, 255, 255, alpha));
            g.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
        }

        for (int i = 0; i < player.getLives(); i++) {
            g.setColor(Color.WHITE);
            g.fillOval((int) 20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.WHITE.darker());
            g.drawOval((int) 20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
            g.setStroke(new BasicStroke(1));
        }

        g.setColor(Color.YELLOW);
        g.fillRect(20, 40, player.getPower() * 8, 8);
        g.setColor(Color.YELLOW.darker());
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < player.getRequiredPower(); i++) {
            g.drawRect(20 + 8 * i, 40, 8, 8);
        }
        g.setStroke(new BasicStroke(1));

        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        g.drawString("Score: " + player.getScore(), WIDTH - 100, 30);

        //draw slowdown meter
        if (slowDownTimer != 0) {
            g.setColor(Color.WHITE);
            g.drawRect(20, 60, 100, 8);
            g.fillRect(20, 60,
                    (int) (100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
        }

    }

    private void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    public void paintComponent(Graphics g){
        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Gothic", Font.PLAIN, 16));
        String s = "You got a new highscore! Please enter your name.";
        int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, 90);
        length = r.width;
        g.fillRect((WIDTH - length) / 2, r.y, r.width, r.height);
        g.setColor(Color.BLACK);
        g.drawString(name, (WIDTH - length) / 2, r.y+r.height-5);
        g.setColor(Color.WHITE);
        s = "G A M E  O V E R";
        length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
        s = "Final Score: " + player.getScore();
        length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 30);
        s = "High Score: " + player.getHighScore();
        length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 60);
        gameDraw();
    }

    private void createNewEnemies() {

        enemies.clear();
        Enemy e;

        if (waveNumber == 1) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if (waveNumber == 2) {
            for (int i = 0; i < 8; i++) {
                enemies.add(new Enemy(1, 1));
            }
        }
        if (waveNumber == 3) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 1));
            }
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 2));
        }
        if (waveNumber == 4) {
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
            }
        }
        if (waveNumber == 5) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
        }
        if (waveNumber == 6){
            enemies.add(new Enemy(1, 3));
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
                enemies.add(new Enemy(3, 1));
            }
        }
        if (waveNumber == 7) {
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
            enemies.add(new Enemy(3, 3));
        }
        if (waveNumber == 8) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
        }


    }

    public void startSparkles() {
        for (int i = 0; i < 20; i++) {
            sparkles.add(new Sparkle());
        }
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        sparkles.clear();
                        running = false;
                    }
                },

                3000
        );
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            player.setLeft(true);
        }
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            player.setRight(true);
        }
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            player.setUp(true);
        }
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            player.setDown(true);
        }
        if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SPACE) {
            player.setFiring(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            player.setLeft(false);
        }
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            player.setRight(false);
        }
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            player.setUp(false);
        }
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            player.setDown(false);
        }
        if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SPACE) {
            player.setFiring(false);
        }
    }
}
