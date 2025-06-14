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
        switch (randomType) {
            case STRENGTH:
            case DAMAGE:
            case CRITDAMAGE:
                value = (Math.random() / 2) + 1.0 ;
                break;
            case SPEED:
            case DEFENCE:
            case HEALTH:
                value = 20;
                break;
            default:
                value = 1;
                break;
        }

        return new Blessing(randomType, value);
    }

    public BlessingType getType() {return type;}
    public double getValue() {return value;}
}
