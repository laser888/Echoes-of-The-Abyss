package Score;

public class ScoreManager {

    public ScoreData calculateScore(
            int enemiesKilled, int totalEnemiesInLevel,
            int solvedPuzzles, int totalPuzzlesInLevel,
            double parTimeForLevelSeconds, double actualTimeTakenSeconds,
            boolean playerDidNotDieInLevel, double xpMultiplier) {

        double combatScoreVal;

        if (totalEnemiesInLevel > 0) {
            combatScoreVal = ((double) enemiesKilled / totalEnemiesInLevel) * 100.0;

        } else {
            combatScoreVal = (enemiesKilled > 0 || solvedPuzzles > 0 || (parTimeForLevelSeconds > 0 && actualTimeTakenSeconds < parTimeForLevelSeconds)) ? 100.0 : 0.0;
        }
        combatScoreVal = Math.max(0, Math.min(100, combatScoreVal));

        double puzzleScoreVal;
        double puzzleRatio = 0;

        if (totalPuzzlesInLevel > 0) {
            puzzleRatio = (double) solvedPuzzles / totalPuzzlesInLevel;

        } else if (solvedPuzzles > 0) {
            puzzleRatio = 1.0;
        }

        puzzleScoreVal = (puzzleRatio - (0 * 0.1)) * 100.0;
        puzzleScoreVal = Math.max(0, Math.min(100, puzzleScoreVal));

        double timeScoreVal;

        if (parTimeForLevelSeconds > 0) {
            timeScoreVal = ((parTimeForLevelSeconds - actualTimeTakenSeconds) / parTimeForLevelSeconds) * 100.0;

        } else {
            timeScoreVal = 100.0;
        }
        timeScoreVal = Math.max(0, Math.min(100, timeScoreVal));

        double baseScore = (combatScoreVal * 0.5) + (puzzleScoreVal * 0.3) + (timeScoreVal * 0.2);

        if (playerDidNotDieInLevel) {
            baseScore += 10.0;
        }
        baseScore = Math.max(0, baseScore);

        String rank = determineRank(baseScore);

        double finalDisplayScore = baseScore;

        int xpAwarded = determineXPAwarded(rank, xpMultiplier);

        long totalSecondsLong = (long) actualTimeTakenSeconds;
        long minutes = totalSecondsLong / 60;
        long seconds = totalSecondsLong % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        return new ScoreData(
                baseScore, rank, finalDisplayScore, xpAwarded,
                combatScoreVal, puzzleScoreVal, timeScoreVal,
                enemiesKilled, totalEnemiesInLevel,
                solvedPuzzles, totalPuzzlesInLevel,
                timeFormatted, playerDidNotDieInLevel
        );
    }


    private String determineRank(double baseScore) {
        if (baseScore >= 100) return "S+";
        else if (baseScore >= 95) return "S";
        else if (baseScore >= 85) return "A";
        else if (baseScore >= 70) return "B";
        else if (baseScore >= 50) return "C";
        else return "D";
    }

    private static final int S_PLUS_RANK_XP = 1200;
    private static final int S_RANK_XP = 800;
    private static final int A_RANK_XP = 500;
    private static final int B_RANK_XP = 300;
    private static final int C_RANK_XP = 150;
    private static final int D_RANK_XP = 75;

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