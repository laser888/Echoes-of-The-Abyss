package Main;

import javax.swing.JFrame;

// Launches the game window and starts the game panel
public class Game {

    public static void main(String[] args) {

        JFrame window = new JFrame("Echoes Of The Abyss"); // Create window
        window.setContentPane(new GamePanel()); // Add game panel
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit on close
        window.setResizable(true); // Allow resizing
        window.pack(); // Fit contents
        window.setVisible(true); // Show window
    }
}
