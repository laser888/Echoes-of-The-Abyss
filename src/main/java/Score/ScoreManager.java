package Score;

// Calculates and assigns score, rank, and XP after level completion
public class ScoreManager {

    // Computes score data based on player performance
    public ScoreData calculateScore(
            int enemiesKilled, int totalEnemiesInLevel,
            int solvedPuzzles, int totalPuzzlesInLevel,
            double parTimeForLevelSeconds, double actualTimeTakenSeconds,
            boolean playerDidNotDieInLevel, double xpMultiplier) {

        double combatScoreVal;

        // Calculates combat score (percent of enemies killed)
        if (totalEnemiesInLevel > 0) {
            combatScoreVal = ((double) enemiesKilled / totalEnemiesInLevel) * 100.0;
        } else {
            combatScoreVal = (enemiesKilled > 0 || solvedPuzzles > 0 || (parTimeForLevelSeconds > 0 && actualTimeTakenSeconds < parTimeForLevelSeconds)) ? 100.0 : 0.0;
        }
        combatScoreVal = Math.max(0, Math.min(100, combatScoreVal));

        double puzzleScoreVal;
        double puzzleRatio = 0;

        // Calculates puzzle score (percent of puzzles solved)
        if (totalPuzzlesInLevel > 0) {
            puzzleRatio = (double) solvedPuzzles / totalPuzzlesInLevel;
        } else if (solvedPuzzles > 0) {
            puzzleRatio = 1.0;
        }

        puzzleScoreVal = puzzleRatio * 100.0;
        puzzleScoreVal = Math.max(0, Math.min(100, puzzleScoreVal));

        double timeScoreVal;

        // Calculates time score (based on how fast player finishes)
        if (parTimeForLevelSeconds > 0) {
            timeScoreVal = ((parTimeForLevelSeconds - actualTimeTakenSeconds) / parTimeForLevelSeconds) * 100.0;
        } else {
            timeScoreVal = 100.0;
        }
        timeScoreVal = Math.max(0, Math.min(100, timeScoreVal));

        // Combines weighted scores
        double baseScore = (combatScoreVal * 0.5) + (puzzleScoreVal * 0.3) + (timeScoreVal * 0.2);

        // Adds bonus if player didn't die
        if (playerDidNotDieInLevel) {
            baseScore += 10.0;
        }
        baseScore = Math.max(0, baseScore);

        // Determines rank based on base score
        String rank = determineRank(baseScore);

        double finalDisplayScore = baseScore;

        // Calculates XP based on rank and multiplier
        int xpAwarded = determineXPAwarded(rank, xpMultiplier);

        // Formats time as mm:ss
        long totalSecondsLong = (long) actualTimeTakenSeconds;
        long minutes = totalSecondsLong / 60;
        long seconds = totalSecondsLong % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        // Returns complete score package
        return new ScoreData(
                baseScore, rank, finalDisplayScore, xpAwarded,
                combatScoreVal, puzzleScoreVal, timeScoreVal,
                enemiesKilled, totalEnemiesInLevel,
                solvedPuzzles, totalPuzzlesInLevel,
                timeFormatted, playerDidNotDieInLevel
        );
    }

    // Determines letter rank based on base score
    private String determineRank(double baseScore) {
        if (baseScore >= 100) return "S+";
        else if (baseScore >= 95) return "S";
        else if (baseScore >= 85) return "A";
        else if (baseScore >= 70) return "B";
        else if (baseScore >= 50) return "C";
        else return "D";
    }

    // XP values per rank
    private static final int S_PLUS_RANK_XP = 1200;
    private static final int S_RANK_XP = 800;
    private static final int A_RANK_XP = 500;
    private static final int B_RANK_XP = 300;
    private static final int C_RANK_XP = 150;
    private static final int D_RANK_XP = 75;

    // Returns XP based on rank and multiplier
    private int determineXPAwarded(String rank, double xpMultiplier) {
        int baseXp;
        switch (rank) {
            case "S+": baseXp = S_PLUS_RANK_XP; break;
            case "S":  baseXp = S_RANK_XP; break;
            case "A":  baseXp = A_RANK_XP; break;
            case "B":  baseXp = B_RANK_XP; break;
            case "C":  baseXp = C_RANK_XP; break;
            case "D":  baseXp = D_RANK_XP; break;
            default:   baseXp = 0;
        }
        return (int) (baseXp * xpMultiplier);
    }
}
