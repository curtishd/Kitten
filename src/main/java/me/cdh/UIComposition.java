package me.cdh;

import me.cdh.state.Action;
import me.cdh.state.BubbleState;
import me.cdh.state.Direction;
import me.cdh.state.State;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public final class UIComposition {
    // Singleton
    public static final UIComposition INSTANCE = new UIComposition();

    private final JFrame window = new JFrame() {{
        setType(Type.UTILITY);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        var dim = new Dimension(100, 100);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setResizable(false);
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getLocationOnScreen().x - getWidth() / 2, e.getLocationOnScreen().y - getHeight() / 2);
                if (changeAction(Action.RISING)) frameNum = 0;
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (action == Action.RISING) {
                    changeAction(Action.LAYING);
                    frameNum = 0;
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                bubbleState = BubbleState.HEART;
                bubbleFrameNum = 0;
            }
        });
        setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
        setVisible(true);
        add(new Kitty());
    }};
    private static final Random ran = new Random();
    private final Map<String, List<BufferedImage>> frames = Loader.loadImg(EnumSet.allOf(Action.class));
    private final Map<String, List<BufferedImage>> bubbleFrames = Loader.loadImg(EnumSet.allOf(BubbleState.class));
    private int frameNum = 0;
    private Action action = Action.SLEEP;
    private List<BufferedImage> currFrames;
    private Direction layingDir = Direction.RIGHT;
    private State state = State.DEFAULT;
    private Point wanderLoc = new Point(0, 0);
    private BubbleState bubbleState = BubbleState.NONE;
    private List<BufferedImage> currBubbleFrames;
    private int bubbleFrameNum = 0;
    private int bubbleSteps = 0;
    private int animationSteps = 0;

    private UIComposition() {
    }

    void tryWander() {
        if (ran.nextBoolean()) return;
        state = State.WANDER;
        var screenLoc = window.getLocationOnScreen();
        Point loc;
        do {
            var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            loc = new Point(ran.nextInt(
                    screenSize.width - window.getWidth() - 20) + 10,
                    ran.nextInt(screenSize.height - window.getHeight() - 20) + 10
            );
        } while (abs(screenLoc.y - loc.y) <= 400 && abs(screenLoc.x - loc.x) <= 400);
        wanderLoc = loc;
    }
    static BufferedImage flipImage(BufferedImage img) {
        var mirror = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var graphics = mirror.createGraphics();
        var trans = new AffineTransform();
        trans.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
        trans.concatenate(AffineTransform.getTranslateInstance(-img.getWidth(), 0.0));
        graphics.transform(trans);
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();
        return mirror;
    }
    boolean changeAction(Action act) {
        if (act != action) {
            action = act;
            currFrames = frames.get(Objects.requireNonNull(action.name()));
            return true;
        } else return false;
    }

    void actionPerform() {
        var loc = window.getLocation();
        switch (action) {
            case RIGHT -> loc.translate(1, 0);
            case LEFT -> loc.translate(-1, 0);
            case UP -> loc.translate(0, -1);
            case DOWN -> loc.translate(0, 1);
        }
        var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (loc.x > screenSize.width - window.getWidth()) {
            loc.setLocation(screenSize.width - window.getWidth(), loc.y);
        } else if (loc.x < -10) {
            loc.setLocation(-10, loc.y);
        } else if (loc.y > screenSize.height - window.getHeight()) {
            loc.setLocation(loc.x, screenSize.height - window.getHeight());
        } else if (loc.y < -35) {
            loc.setLocation(loc.x, -35);
        }
        window.setLocation(loc);
    }
    void updateAnimation() {
        animationSteps++;
        if (animationSteps >= action.delay()) {
            if (action == Action.LAYING && frameNum == action.frame() - 1) {
                if ((animationSteps - action.delay()) > 40) {
                    animationSteps = 0;
                    frameNum = 0;
                    var ignored = ran.nextBoolean() ? changeAction(Action.CURLED) : changeAction(Action.SLEEP);
                }
            } else if (action == Action.SITTING && frameNum == action.frame() - 1) {
                changeAction(Action.LICKING);
                animationSteps = 0;
                frameNum = 0;
            } else {
                frameNum++;
                animationSteps = 0;
            }
        }
        if (frameNum >= action.frame()) frameNum = 0;
    }

    void manageBubbleState() {
        if (bubbleState != BubbleState.HEART) {
            if (action == Action.SLEEP || action == Action.CURLED) bubbleState = BubbleState.ZZZ;
            else if (action != Action.SITTING) bubbleState = BubbleState.NONE;
        }
        bubbleSteps++;
        currBubbleFrames = bubbleFrames.getOrDefault(bubbleState.name(), bubbleFrames.get(BubbleState.HEART.name()));
        if (bubbleSteps >= bubbleState.delay()) {
            bubbleFrameNum++;
            bubbleSteps = 0;
        }
        if (bubbleFrameNum >= bubbleState.frame()) {
            bubbleFrameNum = 0;
            if (bubbleState == BubbleState.HEART) bubbleState = BubbleState.NONE;
        }
    }
    static void initSystemTray() throws IOException, AWTException {
        if (!SystemTray.isSupported()) return;
        var iconSize = SystemTray.getSystemTray().getTrayIconSize();
        var trayIcon = new TrayIcon(
                ImageIO.read(Objects.requireNonNull(Kitty.class.getClassLoader().getResourceAsStream("kitty.png")))
                        .getScaledInstance(iconSize.width, iconSize.height, Image.SCALE_SMOOTH),
                "kitty"
        );
        var popupMenu = new PopupMenu() {{
            var exit = new MenuItem("Exit");
            exit.addActionListener(e -> System.exit(0));
            add(exit);
        }};
        trayIcon.setPopupMenu(popupMenu);
        SystemTray.getSystemTray().add(trayIcon);
    }

    void updateAction() {
        if (action != Action.RISING) {
            if (state == State.WANDER) {
                var curPos = window.getLocationOnScreen();
                var ignore = abs(curPos.x - wanderLoc.x) >= 3
                        ? curPos.x > wanderLoc.x
                        ? changeAction(Action.LEFT) : changeAction(Action.RIGHT)
                        : curPos.y > wanderLoc.y
                        ? changeAction(Action.UP) : changeAction(Action.DOWN);
                state = wanderLoc.distance(curPos) < 3 ? State.DEFAULT : State.WANDER;
            }
            var flag = false;
            if (action == Action.LEFT) {
                layingDir = Direction.LEFT;
            } else if (action == Action.RIGHT) {
                layingDir = Direction.RIGHT;
            } else if (state != State.WANDER && (action == Action.UP || action == Action.DOWN))
                flag = ran.nextInt(3) >= 1
                        ? changeAction(Action.LAYING) : changeAction(Action.SITTING);
            if (flag) frameNum = 0;
        }
    }

    static boolean isDayTime() {
        var time = LocalDateTime.now().getHour();
        return time > 7 && time < 22;
    }
    // ---------------------GET-----------------------
    public List<BufferedImage> getCurrFrames() {
        return currFrames;
    }
    public int getFrameNum() {
        return frameNum;
    }
    public Action getAction() {
        return action;
    }
    public Direction getLayingDir() {
        return layingDir;
    }
    public BubbleState getBubbleState() {
        return bubbleState;
    }
    public int getBubbleFrameNum() {
        return bubbleFrameNum;
    }
    public List<BufferedImage> getCurrBubbleFrames() {
        return currBubbleFrames;
    }
    public JFrame getWindow() {
        return window;
    }
}