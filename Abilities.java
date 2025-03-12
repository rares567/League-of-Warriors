interface Visitor <T extends Entity> {
    void visit(T entity);
}

abstract class Spell implements Visitor<Entity> {
    int damage;
    int manaCost;
    public Spell(int damage, int manaCost) {
        this.damage = damage;
        this.manaCost = manaCost;
    }
    public String toString() {
        return "\tDamage: " + damage + " Mana cost: " + manaCost;
    }
}

class IceSpell extends Spell {
    public IceSpell() {
        super(100, 20);
    }
    public void visit(Entity entity) {
        if (entity.iceImmunity)
            entity.receiveDamage(damage / 5);
        else
            entity.receiveDamage(damage);
    }
}

class FireSpell extends Spell {
    public FireSpell() {
        super(150, 30);
    }
    public void visit(Entity entity) {
        if (entity.fireImmunity)
            entity.receiveDamage(damage / 5);
        else
            entity.receiveDamage(damage);
    }
}

class EarthSpell extends Spell {
    public EarthSpell() {
        super(50, 10);
    }
    public void visit(Entity entity) {
        if (entity.earthImmunity)
            entity.receiveDamage(damage / 5);
        else
            entity.receiveDamage(damage);
    }
}
