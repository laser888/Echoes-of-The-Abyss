package Effects;

public class DamageResult {
    public int damage;
    public boolean isCrit;

    public DamageResult(int damage, boolean isCrit, double rawDamage) {
        this.damage = damage;
        this.isCrit = isCrit;
    }
}