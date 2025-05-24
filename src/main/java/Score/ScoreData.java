package Score;

public class ScoreData {
    public final double finalScore;
    public final int enemiesKilled;
    public final int totalEnemies;
    public final int puzzlesSolved;
    public final int totalPuzzles;
    public final String timeTakenFormatted;
    public final boolean playerDidNotDieInLevel;
    public final double combatScore;
    public final double puzzleScore;
    public final double timeScore;

    public ScoreData(double finalScore, double combatScore, double puzzleScore, double timeScore, int enemiesKilled, int totalEnemies, int puzzlesSolved, int totalPuzzles, String timeTakenFormatted, boolean playerDidNotDieInLevel) {

        this.finalScore = finalScore;
        this.combatScore = combatScore;
        this.puzzleScore = puzzleScore;
        this.timeScore = timeScore;
        this.enemiesKilled = enemiesKilled;
        this.totalEnemies = totalEnemies;
        this.puzzlesSolved = puzzlesSolved;
        this.totalPuzzles = totalPuzzles;
        this.timeTakenFormatted = timeTakenFormatted;
        this.playerDidNotDieInLevel = playerDidNotDieInLevel;
    }
}