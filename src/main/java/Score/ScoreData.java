package Score;

public class ScoreData {

    public final int enemiesKilled;
    public final int totalEnemies;
    public final int puzzlesSolved;
    public final int totalPuzzles;
    public final String timeTakenFormatted;
    public final boolean playerDidNotDieInLevel;

    public final double combatScore;
    public final double puzzleScore;
    public final double timeScore;

    public final double baseScore;
    public final String rank;
    public final double trinketMultiplier;
    public final double finalDisplayScore;
    public final int xpAwarded;

    public ScoreData(double baseScore, String rank, double trinketMultiplier, double finalDisplayScore, int xpAwarded, double combatScore, double puzzleScore, double timeScore, int enemiesKilled, int totalEnemies, int puzzlesSolved, int totalPuzzles, String timeTakenFormatted, boolean playerDidNotDieInLevel) {

        this.baseScore = baseScore;
        this.rank = rank;
        this.trinketMultiplier = trinketMultiplier;
        this.finalDisplayScore = finalDisplayScore;
        this.xpAwarded = xpAwarded;

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
