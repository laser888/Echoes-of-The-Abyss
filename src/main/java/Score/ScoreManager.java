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
            combatScoreVal = (enemiesKilled > 0) ? 100.0 : 0;
        }
        combatScoreVal = Math.max(0, Math.min(100, combatScoreVal));

        double puzzleScoreVal;
        double puzzleRatio = 0;
        if (totalPuzzlesInLevel > 0) {
            puzzleRatio = (double) solvedPuzzles / totalPuzzlesInLevel;
        } else if (solvedPuzzles > 0) {
            puzzleRatio = 1.0;
        }

        puzzleScoreVal = (puzzleRatio - ( 0 * 0.1)) * 100.0;
        puzzleScoreVal = Math.max(0, puzzleScoreVal);

        double timeScoreVal;
        if (parTimeForLevelSeconds > 0) {
            timeScoreVal = ((parTimeForLevelSeconds - actualTimeTakenSeconds) / parTimeForLevelSeconds) * 100.0;
        } else {
            timeScoreVal = 0;
        }
        timeScoreVal = Math.max(0, timeScoreVal);
        timeScoreVal = Math.min(100, timeScoreVal);


        double calculatedFinalScore = (combatScoreVal * 0.5) +
                (puzzleScoreVal * 0.3) +
                (timeScoreVal * 0.2);

        if (playerDidNotDieInLevel) {
            calculatedFinalScore += 10.0;
        }

        calculatedFinalScore = Math.max(0, calculatedFinalScore);

        long totalSecondsLong = (long) actualTimeTakenSeconds;
        long minutes = totalSecondsLong / 60;
        long seconds = totalSecondsLong % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        return new ScoreData(calculatedFinalScore, combatScoreVal, puzzleScoreVal, timeScoreVal, enemiesKilled, totalEnemiesInLevel, solvedPuzzles, totalPuzzlesInLevel, timeFormatted, playerDidNotDieInLevel);
    }
}