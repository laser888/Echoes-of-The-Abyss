package Score;

public class ScoreManager {

    public ScoreData calculateScore(
            int enemiesKilled, int totalEnemiesInLevel,
            int solvedPuzzles, int totalPuzzlesInLevel,
            double parTimeForLevelSeconds, double actualTimeTakenSeconds,
            boolean playerDidNotDieInLevel) {

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

        double trinketMultiplier = getTrinketMultiplier(rank);

        double finalDisplayScore = baseScore * trinketMultiplier;

        int xpAwarded = determineXPAwarded(rank);

        long totalSecondsLong = (long) actualTimeTakenSeconds;
        long minutes = totalSecondsLong / 60;
        long seconds = totalSecondsLong % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        return new ScoreData(
                baseScore, rank, trinketMultiplier, finalDisplayScore, xpAwarded,
                combatScoreVal, puzzleScoreVal, timeScoreVal,
                enemiesKilled, totalEnemiesInLevel,
                solvedPuzzles, totalPuzzlesInLevel,
                timeFormatted, playerDidNotDieInLevel
        );
    }


    private String determineRank(double baseScore) {
        if (baseScore >= 90) return "S+";
        else if (baseScore >= 80) return "S";
        else if (baseScore >= 70) return "A";
        else if (baseScore >= 60) return "B";
        else if (baseScore >= 50) return "C";
        else return "D";
    }

    private double getTrinketMultiplier(String rank) {
        switch (rank) {
            case "S+": return 1.2;
            case "S":  return 1.0;
            case "A":  return 0.8;
            case "B":  return 0.64;
            case "C":  return 0.512;
            case "D":  return 0.41;
            default:   return 1.0;
        }
    }

    private static final int S_PLUS_RANK_XP = 120;
    private static final int S_RANK_XP = 100;
    private static final int A_RANK_XP = 75;
    private static final int B_RANK_XP = 50;
    private static final int C_RANK_XP = 25;
    private static final int D_RANK_XP = 10;

    private int determineXPAwarded(String rank) {
        switch (rank) {
            case "S+": return S_PLUS_RANK_XP;
            case "S":  return S_RANK_XP;
            case "A":  return A_RANK_XP;
            case "B":  return B_RANK_XP;
            case "C":  return C_RANK_XP;
            case "D":  return D_RANK_XP;
            default:   return 0;
        }
    }
}