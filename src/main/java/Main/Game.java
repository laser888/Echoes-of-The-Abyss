package Main;

import javax.swing.JFrame;

public class Game {

    public static void main(String[] args) {

        JFrame window = new JFrame("Echoes of the Abyss");
        window.setContentPane(new GamePanel());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.pack();
        window.setVisible(true);

    }
}
