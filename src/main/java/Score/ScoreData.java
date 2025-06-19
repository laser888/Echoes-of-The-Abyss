package Score;

// Stores level score breakdown and performance data
public class ScoreData {

    public final int enemiesKilled; // Number of enemies killed
    public final int totalEnemies; // Total enemies in level
    public final int puzzlesSolved; // Number of puzzles solved
    public final int totalPuzzles; // Total puzzles in level
    public final String timeTakenFormatted; // Time as "mm:ss"
    public final boolean playerDidNotDieInLevel; // Whether player died

    public final double combatScore; // Score from combat
    public final double puzzleScore; // Score from puzzles
    public final double timeScore; // Score from time

    public final double baseScore; // Combined score before bonus
    public final String rank; // Rank string (e.g. S, A, B)
    public final double finalDisplayScore; // Score with multipliers
    public final int xpAwarded; // XP given to player

    // Initializes ScoreData with full performance info
    public ScoreData(
            double baseScore, String rank, double finalDisplayScore, int xpAwarded,
            double combatScore, double puzzleScore, double timeScore,
            int enemiesKilled, int totalEnemies,
            int puzzlesSolved, int totalPuzzles,
            String timeTakenFormatted, boolean playerDidNotDieInLevel
    ) {
        this.baseScore = baseScore;
        this.rank = rank;
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
