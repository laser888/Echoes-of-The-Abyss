package Effects;

// Holds damage and critical hit data
public class DamageResult {
    public int damage; // Damage amount
    public boolean isCrit; // Indicates critical hit

    // Creates a damage result with damage and crit status
    public DamageResult(int damage, boolean isCrit, double rawDamage) {
        this.damage = Math.max(0, damage); // Ensures non-negative damage
        this.isCrit = isCrit;
    }
}