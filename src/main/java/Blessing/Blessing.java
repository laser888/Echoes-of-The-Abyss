package Blessing;

// Provides a stat boost for the player
public class Blessing {
    private BlessingType type; // Blessing type
    private double value; // Multiplier value

    // Creates a new blessing with specified type and value
    public Blessing(BlessingType type, double value) {
        this.type = (type != null) ? type : BlessingType.HEALTH; // Defaults to HEALTH if null
        this.value = Math.max(1.0, Math.min(value, 2.0)); // Caps value between 1.0 and 2.0
    }

    // Generates a random blessing with a random type and value
    public static Blessing rollRandomBlessing() {
        BlessingType[] types = BlessingType.values();
        // Selects a random type, defaults to HEALTH if none available
        BlessingType randomType = (types.length > 0) ? types[(int) (Math.random() * types.length)] : BlessingType.HEALTH;

        double randomBonus = 0.25 + (Math.random() * 0.50); // Generates 25% to 75% boost
        double value;

        switch (randomType) {
            case STRENGTH:
            case DAMAGE:
            case CRITDAMAGE:
            case SPEED:
            case DEFENCE:
            case HEALTH:
            case INTELLIGENCE:
                value = 1.0 + randomBonus;
                break;
            default:
                value = 1.25; // Sets 25% boost as fallback
                break;
        }

        value = Math.round(value * 1000.0) / 1000.0; // Rounds to 3 decimal places

        return new Blessing(randomType, value);
    }

    public BlessingType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }
}