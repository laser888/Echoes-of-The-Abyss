package GameState;

import Main.GameAction;
import Main.KeybindManager;
import Main.GamePanel;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsState extends GameState {

    private final GamePanel gamePanel;
    private final KeybindManager keybindManager;
    private Background bg;

    private Font titleFont;
    private Font optionFont;
    private Font instructionFont;
    private Color defaultColor;
    private Color selectedColor;
    private Color messageColor;

    private List<GameAction> configurableActions;
    private List<String> displayOptionNames;
    private int currentSelection = 0;
    private boolean isWaitingForKey = false;
    private GameAction actionToChange = null;
    private String message = "";
    private long messageTimer;

    private int topVisibleRow = 0;
    private int visibleRows;
    private int lineHeight = 18;
    private int listStartY = 65;

    public SettingsState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        this.keybindManager = gsm.getKeybindManager();
        init();
    }

    public void init() {

        try {

            bg = new Background("/Backgrounds/menubg.gif", 1);
            //bg.setVector(-0.1, 0);

            titleFont = new Font("Century Gothic", Font.BOLD, 20);
            optionFont = new Font("Arial", Font.PLAIN, 11);
            instructionFont = new Font("Arial", Font.ITALIC, 10);
            defaultColor = Color.RED;
            selectedColor = Color.BLACK;
            messageColor = new Color(0, 100, 100);

            configurableActions = new ArrayList<>(List.of(GameAction.values()));
            buildDisplayOptionNames();
            message = "";

            int availableHeight = GamePanel.HEIGHT - listStartY - 15;
            visibleRows = Math.max(1, availableHeight / lineHeight);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildDisplayOptionNames() {

        displayOptionNames = new ArrayList<>();

        for (GameAction action : configurableActions) {
            displayOptionNames.add(action.name().replace("_", " "));
        }

        displayOptionNames.add("Save Changes");
        displayOptionNames.add("Reset to Defaults");
        displayOptionNames.add("Back to Menu");
    }

    public void update() {

        if (bg != null) {
            bg.update();
        }
    }

    public void draw(Graphics2D g) {

        if (bg == null) return;

        if (messageTimer > 0 && System.currentTimeMillis() - messageTimer > 3000) {
            message = "";
            messageTimer = 0;
        }

        bg.draw(g);

        g.setFont(titleFont);
        g.setColor(new Color(128, 0, 0));
        String title = "Keybind Settings";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (GamePanel.WIDTH - titleWidth) / 2, 30);


        g.setFont(instructionFont);
        g.setColor(messageColor);

        if (isWaitingForKey && actionToChange != null) {
            message = "Press key for: " + actionToChange.name().replace("_", " ") + " (ESC to cancel)";
        }

        if (message != null && !message.isEmpty()) {
            g.setFont(instructionFont);
            g.setColor(messageColor);
            int msgWidth = g.getFontMetrics().stringWidth(message);
            g.drawString(message, (GamePanel.WIDTH - msgWidth) / 2, 50);
        }

        g.setFont(optionFont);
        Map<GameAction, Integer> currentBinds = keybindManager.getAllKeybinds();

        for (int i = 0; i < visibleRows; i++) {

            int actualItemIndex = topVisibleRow + i;

            if (actualItemIndex >= displayOptionNames.size()) break;

            g.setColor(actualItemIndex == currentSelection ? selectedColor : defaultColor);

            String optionText = displayOptionNames.get(actualItemIndex);
            String keyText = "";

            if (actualItemIndex < configurableActions.size()) {

                GameAction action = configurableActions.get(actualItemIndex);
                keyText = KeyEvent.getKeyText(currentBinds.getOrDefault(action, -1));
                g.drawString(optionText + ": ", 30, listStartY + i * lineHeight);
                g.drawString(keyText, 180, listStartY + i * lineHeight);

            } else {
                g.drawString(optionText, 30, listStartY + i * lineHeight);
            }
        }

        if (topVisibleRow > 0) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("^", GamePanel.WIDTH - 20, listStartY);
        }

        if (topVisibleRow + visibleRows < displayOptionNames.size()) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("v", GamePanel.WIDTH - 20, listStartY + (visibleRows - 1) * lineHeight + g.getFontMetrics().getAscent());
        }
    }

    public void keyPressed(int k) {
        if (isWaitingForKey) {

            if (k == KeyEvent.VK_ESCAPE) {
                isWaitingForKey = false;
                actionToChange = null;
                message = "Change cancelled.";

            } else {

                for (Map.Entry<GameAction, Integer> entry : keybindManager.getAllKeybinds().entrySet()) {
                    if (entry.getKey() != actionToChange && entry.getValue() == k) {
                        message = "Key " + KeyEvent.getKeyText(k) + " used by " + entry.getKey().name().replace("_", " ");
                        break;
                    }
                }

                    keybindManager.setKeybind(actionToChange, k);
                    message = actionToChange.name().replace("_", " ") + " set to " + KeyEvent.getKeyText(k) + ".";
                    isWaitingForKey = false;
                    actionToChange = null;

            }
            return;
        }

        int previousSelection = currentSelection;

        if (k == KeyEvent.VK_UP) {
            currentSelection--;

            if (currentSelection < 0) {
                currentSelection = displayOptionNames.size() - 1;
            }

            message = "";

        } else if (k == KeyEvent.VK_DOWN) {
            currentSelection++;
            if (currentSelection >= displayOptionNames.size()) {
                currentSelection = 0;
            }
            message = "";

        } else if (k == KeyEvent.VK_ENTER) {
            if (currentSelection < configurableActions.size()) {

                actionToChange = configurableActions.get(currentSelection);
                isWaitingForKey = true;
                message = "Press a key for: " + actionToChange.name().replace("_", " ") + " (ESC to cancel)";
            } else {

                int specialOptionIndex = currentSelection - configurableActions.size();

                if (specialOptionIndex == 0) {

                    keybindManager.saveKeybindsToGameData();

                    gsm.saveGameData();
                    message = "Settings Saved!";
                    messageTimer = System.currentTimeMillis();

                } else if (specialOptionIndex == 1) {

                    keybindManager.resetToDefaults();
                    message = "Keybinds reset. Press 'Save Changes' to keep.";
                    messageTimer = System.currentTimeMillis();

                } else if (specialOptionIndex == 2) {
                    gsm.setState(GameStateManager.MENUSTATE);
                }
            }

        } else if (k == KeyEvent.VK_ESCAPE) {
            gsm.setState(GameStateManager.MENUSTATE);
        }

        if (currentSelection < topVisibleRow) {
            topVisibleRow = currentSelection;

        } else if (currentSelection >= topVisibleRow + visibleRows) {
            topVisibleRow = currentSelection - visibleRows + 1;
        }
    }

    public void keyReleased(int k) {}

    public void mousePressed(MouseEvent e) {
        int panelWidth = gamePanel.getCurrentWidth();
        int panelHeight = gamePanel.getCurrentHeight();

        float scaleX = (float) GamePanel.WIDTH / panelWidth;
        float scaleY = (float) GamePanel.HEIGHT / panelHeight;
        int logicalX = (int) (e.getX() * scaleX);
        int logicalY = (int) (e.getY() * scaleY);

        for (int i = 0; i < visibleRows; i++) {

            int actualItemIndex = topVisibleRow + i;
            if (actualItemIndex >= displayOptionNames.size()) break;

            int itemY = listStartY + i * lineHeight;
            int itemHeight = lineHeight;

            int textWidth = gamePanel.getFontMetrics(optionFont).stringWidth(displayOptionNames.get(actualItemIndex));
            Rectangle itemBounds = new Rectangle(30, itemY - optionFont.getSize(), textWidth + 150, itemHeight);

            if (itemBounds.contains(logicalX, logicalY)) {
                currentSelection = actualItemIndex;
                keyPressed(KeyEvent.VK_ENTER);
                System.out.println("Clicked option: " + displayOptionNames.get(actualItemIndex));
                break;
            }
        }
    }

    public int getSpawnX() {return 0;}
    public  int getSpawnY() { return 0;}
}