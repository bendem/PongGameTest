package be.bendem.gametest.core.graphics;

import be.bendem.gametest.GameTest;
import be.bendem.gametest.core.Killable;
import be.bendem.gametest.core.graphics.shapes.Circle;
import be.bendem.gametest.core.graphics.shapes.Rectangle;
import be.bendem.gametest.core.graphics.shapes.Text;
import be.bendem.gametest.core.graphics.windows.GameFrame;
import be.bendem.gametest.core.logging.Logger;
import be.bendem.gametest.utils.RepeatingTask;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * @author bendem
 */
public class Graphics implements Killable {

    private static final Color[] COLORS = new Color[] {
            new Color(0xFF0000), new Color(0xFF7F00),
            new Color(0xFFFF00), new Color(0x00FF00),
            new Color(0x0000FF), new Color(0x4B0082),
            new Color(0x8B00FF)
    };

    public final int WIDTH;
    public final int HEIGHT;
    public final int PLATFORM_WIDTH;
    public final int PLATFORM_HEIGHT;

    private final Collection<GraphicObject> objects;
    private final GameFrame frame;
    private final RepeatingTask graphicsUpdater;

    public Graphics(GameTest game) {
        this.WIDTH = game.getConfig().getInt("graphics.width", 800);
        this.HEIGHT = game.getConfig().getInt("graphics.height", 500);
        this.PLATFORM_WIDTH = game.getConfig().getInt("graphics.platform.width", 80);
        this.PLATFORM_HEIGHT = game.getConfig().getInt("graphics.platform.height", 10);
        this.objects = new CopyOnWriteArraySet<>();
        this.frame = new GameFrame(game.getEventManager(), this, WIDTH, HEIGHT);

        long updateInterval = TimeUnit.SECONDS.toMillis(1) / game.getConfig().getInt("graphics.fps", 40);
        this.graphicsUpdater = new RepeatingTask(frame::redraw, "graphics-updater", updateInterval);
    }

    public void show() {
        graphicsUpdater.start();
        frame.display();
    }

    @Override
    public void kill() {
        try {
            graphicsUpdater.cancel(500);
        } catch(InterruptedException e) {
            Logger.error("Could not cancel graphic updater", e);
        }
        frame.kill();
    }

    public Rectangle createPlatform() {
        Rectangle platform = new Rectangle(
            new Point2D.Double(
                WIDTH / 2 - PLATFORM_WIDTH / 2,
                HEIGHT - PLATFORM_HEIGHT - 5
            ),
            PLATFORM_WIDTH,
            PLATFORM_HEIGHT,
            true,
            Color.WHITE
        );
        objects.add(platform);
        return platform;
    }

    public Circle createBall() {
        return new Circle(new Point2D.Double(WIDTH / 2, HEIGHT / 2), 7, true, Color.LIGHT_GRAY);
    }

    public Collection<Rectangle> createBricks() {
        Set<Rectangle> bricks = new HashSet<>();
        int brickW = (WIDTH - 5) / 8;
        int brickH = 18;
        int w = 0;
        for(int i = 10; i < HEIGHT / 3; i += brickH + 10) {
            for(int j = 10; j < WIDTH - 10; j += brickW) {
                bricks.add(new Rectangle(new Point2D.Double(j, i), brickW - 10, brickH, true, COLORS[++w % COLORS.length], true));
            }
        }
        return bricks;
    }

    public Collection<Circle> createLifePoints() {
        List<Circle> circles = new ArrayList<>(3);
        int lifeRadius = 7;
        for(int i = 0; i < 3; i++) {
            // TODO That point calculation is horrible
            circles.add(new Circle(new Point2D.Double(WIDTH - 10 - lifeRadius - ((lifeRadius + lifeRadius*2) * i), HEIGHT - lifeRadius*2 - 5), lifeRadius, true, Color.LIGHT_GRAY));
        }
        Collections.reverse(circles);
        return circles;
    }

    public Text createLevelText() {
        return new Text("1", new Point2D.Double(10, HEIGHT - 10), 35, true, Color.LIGHT_GRAY);
    }

    public Collection<GraphicObject> getObjects() {
        return objects;
    }

}
