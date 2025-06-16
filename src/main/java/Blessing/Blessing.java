package Blessing;

public class Blessing {
    private BlessingType type;
    private double value;

    public Blessing(BlessingType type, double value) {
        this.type = type;
        this.value = value;
    }

    public static Blessing rollRandomBlessing() {
        BlessingType[] types = BlessingType.values();
        BlessingType randomType = types[(int)(Math.random() * types.length)];

        double value;

        double randomBonus = 0.05 + (Math.random() * 0.10); // Random value between 0.05 and 0.15

        switch (randomType) {
            case STRENGTH:
            case DAMAGE:
            case CRITDAMAGE:
            case SPEED:
            case DEFENCE:
            case HEALTH:
                // All stat boosts will be a multiplier
                value = 1.0 + randomBonus;
                break;
            default:
                value = 1.05; // Fallback to a 5% bonus
                break;
        }

        value = Math.round(value * 1000.0) / 1000.0;

        return new Blessing(randomType, value);
    }


    public BlessingType getType() {return type;}
    public double getValue() {return value;}
}
