import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

interface Battle {
    void receiveDamage(int damage);
    int getDamage();
    int getSpellDamage(Spell ability, Entity target);
}

interface Element <T extends Entity> {
    void accept(Visitor<T> visitor);
}

abstract class Entity implements Battle, Element<Entity> {
    ArrayList<Spell> abilities;
    int hp, mana;
    int maxHp, maxMana;
    boolean fireImmunity, iceImmunity, earthImmunity;
    public Entity(int maxHp, int maxMana, boolean fireImmunity,
                  boolean iceImmunity, boolean earthImmunity, ArrayList<Spell> abilities) {
        this.abilities = abilities;
        this.maxHp = maxHp;
        hp = maxHp;
        this.maxMana = maxMana;
        mana = maxMana;
        this.fireImmunity = fireImmunity;
        this.iceImmunity = iceImmunity;
        this.earthImmunity = earthImmunity;
    }
    public Entity(int maxHp, int maxMana, ArrayList<Spell> abilities) {
        this(maxHp, maxMana, false, false, false, abilities);
    }
    public Entity(int maxHp, int maxMana) {
        this(maxHp, maxMana, new ArrayList<>());
    }
    public Entity() {
        this(0, 0, null);
    }
    public int regenHp(int hp) {
        int oldHp = this.hp;
        this.hp = Math.min(this.hp + hp, maxHp);
        // if health is truncated, less than hp will be regened
        return this.hp - oldHp;
    }
    public int regenMana(int mana) {
        int oldMana = this.mana;
        this.mana = Math.min(this.mana + mana, maxMana);
        // if health is truncated, less than hp will be regened
        return this.mana - oldMana;
    }
    public void loseHp(int hp) {
        this.hp = Math.max(this.hp - hp, 0);
    }
    public void loseMana(int mana) {
        this.mana = Math.max(this.mana - mana, 0);
    }
    public boolean useAbility(Spell ability, Entity target, int baseDamage) {
        if (mana < ability.manaCost)
            return false;
        loseMana(ability.manaCost);
        target.accept(ability);
        target.receiveDamage(baseDamage);
        return true;
    }
    public void accept(Visitor<Entity> visitor) {
        visitor.visit(this);
    }
}

abstract class Character extends Entity {
    final String name;
    int exp, level, strength, charisma, dexterity;
    private final int initStrength, initCharisma, initDexterity, initMaxHp, initMaxMana;
    static final int[] expMilestones;
    static {
        // max level is 30, indexing from 1
        expMilestones = new int[30];
        int firstMilestone = 50;
        expMilestones[1] = firstMilestone;
        // 10% slower progression to next level compared to last level
        for (int i = 2; i < 30; i++) {
            expMilestones[i] = 11 * expMilestones[i - 1] / 10;
        }
    }
    public Character(int strength, int charisma, int dexterity,
                     int maxHp, int maxMana, boolean fireImmunity,
                     boolean iceImmunity, boolean earthImmunity,
                     ArrayList<Spell> abilities, String name, int level, int exp) {
        super(maxHp, maxMana, fireImmunity, iceImmunity, earthImmunity, abilities);
        this.strength = strength;
        initStrength = strength;
        this.charisma = charisma;
        initCharisma = charisma;
        this.dexterity = dexterity;
        initDexterity = dexterity;
        initMaxHp = maxHp;
        initMaxMana = maxMana;
        this.name = name;
        this.exp = exp;
        this.level = 1;
        // for initialising characters from accounts (applies level up multiple times)
        while (this.level < level)
            levelUp();
        // after initialising with level up, make hp and mana equal to max
        regenHp(maxHp);
        regenMana(maxMana);
    }
    public Character(int strength, int charisma, int dexterity,
                     int maxHp, int maxMana, boolean fireImmunity,
                     boolean iceImmunity, boolean earthImmunity,
                     String name, int level, int exp) {
        this(strength, charisma, dexterity, maxHp, maxMana, fireImmunity, iceImmunity, earthImmunity, new ArrayList<>(), name, level, exp);
    }
    public Character(int strength, int charisma, int dexterity,
                     int maxHp, int maxMana, boolean fireImmunity,
                     boolean iceImmunity, boolean earthImmunity,
                     ArrayList<Spell> abilities, String name) {
        this(strength, charisma, dexterity, maxHp, maxMana, fireImmunity, iceImmunity, earthImmunity, abilities, name, 1, 0);
    }
    public Character(int strength, int charisma, int dexterity,
                     int maxHp, int maxMana, ArrayList<Spell> abilities, String name) {
        this(strength, charisma, dexterity, maxHp, maxMana, false, false, false, abilities, name);
    }
    public Character(int strength, int charisma, int dexterity,
                     int maxHp, int maxMana, String name) {
        this(strength, charisma, dexterity, maxHp, maxMana, new ArrayList<>(), name);
    }
    public Character(int maxHp, int maxMana, String name) {
        this(0, 0, 0, maxHp, maxMana, name);
    }
    /*
    * The multiplier of level up will have a sqrt(x) progression (having diminished returns at higher levels)
    * Therefore, it will follow the function sqrt(a*(x-2)+b^2), where 'b' is the multiplier at first level up (1 -> 2)
    * and 'a' is a scaling factor which will determine how fast the multiplier drops off in following level ups.
    * */
    private void levelUp() {
        level++;
        exp = 0;
        // formula for level up
        double multiplier = Math.sqrt(0.125 * (level - 2) + 1.21);
        strength = (int) Math.round((double) initStrength * multiplier);
        charisma = (int) Math.round((double) initCharisma * multiplier);
        dexterity = (int) Math.round((double) initDexterity * multiplier);
        maxHp = (int) Math.round((double) initMaxHp * multiplier);
        maxMana = (int) Math.round((double) initMaxMana * multiplier);
    }
    // returns true if level up occurred and false otherwise
    public boolean gainExp(int exp) {
        this.exp += exp;
        // if max level
        if (level == 30)
            return false;
        if (this.exp > expMilestones[level]) {
            // exp left over that carries over to next level
            int diff = this.exp - expMilestones[level];
            levelUp();
            this.exp = diff;
            return true;
        }
        return false;
    }
    public int getSpellDamage(Spell ability, Entity target) {
        int damage = ability.damage;
        // if immune to an ability, deal only 20% spell damage
        if (ability instanceof IceSpell && target.iceImmunity)
            damage /= 5;
        else if (ability instanceof FireSpell && target.fireImmunity)
            damage /= 5;
        else if (ability instanceof EarthSpell && target.earthImmunity)
            damage /= 5;
        // dexterity influences ability power
        damage *= (int) Math.round(Math.log10(dexterity));
        return damage;
    }
    public String toString() {
//        return getClass().getSimpleName() + " " + name + ": strength = " + strength +
//               ", charisma = " + charisma + ", dexterity = " + dexterity +
//               ", level = " + level + ", exp = " + exp + ", maxHp = " + maxHp +
//                ", maxMana = " + maxMana + ", immunities(f,i,e) = " + fireImmunity +
//                " " + iceImmunity + " " + earthImmunity + "\n";
        return getClass().getSimpleName() + " " + name;
    }
}

class Warrior extends Character {
    public Warrior(String name, int exp, int level) {
        super(150, 20, 30, 1100, 20, true, false, false, name, level, exp);
        abilities.add(new EarthSpell());
    }
    public Warrior(String name) {
        this(name, 0, 1);
    }
    public void receiveDamage(int damage) {
        if (charisma + dexterity > 75) {
            Random rng = new Random();
            if (rng.nextBoolean()) {
                damage /= 2;
                JOptionPane.showMessageDialog(null, name + " has received only half damage!");
            }
        }
        loseHp(damage);
    }
    public int getDamage() {
        int damage = (int) ((double) 20 * Math.sqrt(strength));
        if (strength > 200) {
            Random rng = new Random();
            if (rng.nextBoolean())
                damage *= 2;
        }
        return damage;
    }
}

class Mage extends Character {
    public Mage(String name, int exp, int level) {
        super(30, 20, 100, 650, 60, false, true, false, name, level, exp);
        abilities.add(new EarthSpell());
        abilities.add(new FireSpell());
        abilities.add(new IceSpell());
    }
    public Mage(String name) {
        this(name, 0, 1);
    }
    public void receiveDamage(int damage) {
        if (strength + charisma > 76) {
            Random rng = new Random();
            if (rng.nextBoolean()) {
                damage /= 2;
                JOptionPane.showMessageDialog(null, name + " has received only half damage!");
            }
        }
        loseHp(damage);
    }
    public int getDamage() {
        int damage = (int) ((double) 25 * Math.sqrt(strength + charisma));
        if (dexterity > 132) {
            Random rng = new Random();
            if (rng.nextBoolean())
                damage *= 2;
        }
        return damage;
    }
}

class Rogue extends Character {
    public Rogue(String name, int exp, int level) {
        super(50, 90, 40, 900, 40, false, false, true, name, level, exp);
        abilities.add(new EarthSpell());
        abilities.add(new IceSpell());
    }
    public Rogue(String name) {
        this(name, 0, 1);
    }
    public void receiveDamage(int damage) {
        if (strength + dexterity > 130) {
            Random rng = new Random();
            if (rng.nextBoolean()) {
                damage /= 2;
                JOptionPane.showMessageDialog(null, name + " has received only half damage!");
            }
        }
        loseHp(damage);
    }
    public int getDamage() {
        int damage = (int) ((double) 15 * Math.sqrt(dexterity + strength));
        if (charisma > 120) {
            Random rng = new Random();
            if (rng.nextBoolean())
                damage *= 2;
        }
        return damage;
    }
}

class CharacterFactory {
    public static Character create(String type, String name, int exp, int level) {
        switch (type) {
            case "Warrior":
                return new Warrior(name, exp, level);
            case "Mage":
                return new Mage(name, exp, level);
            case "Rogue":
                return new Rogue(name, exp, level);
            default:
                return null;
        }
    }
    public static Character create(String type, String name) {
        switch (type) {
            case "Warrior":
                return new Warrior(name);
            case "Mage":
                return new Mage(name);
            case "Rogue":
                return new Rogue(name);
            default:
                return null;
        }
    }
}

class Enemy extends Entity {
    int damage;
    public Enemy() {
        Random rng = new Random();
        int hp = 400 + rng.nextInt(1001);
        int mana = 30 + rng.nextInt(81);
        damage = 30 + rng.nextInt(71);
        int nrAbilities = 3 + rng.nextInt(4);
        ArrayList<Spell> abilities = new ArrayList<>();
        // minimum one ability of each type
        abilities.add(new IceSpell());
        abilities.add(new FireSpell());
        abilities.add(new EarthSpell());
        for (int i = 0; i < nrAbilities - 3; i++) {
            switch (rng.nextInt(3)) {
                case 0:
                    abilities.add(new IceSpell());
                    break;
                case 1:
                    abilities.add(new FireSpell());
                    break;
                case 2:
                    abilities.add(new EarthSpell());
                    break;
                default:
                    break;
            }
        }
        // 33% chance for each spell immunity
        if (rng.nextInt(3) == 0)
            fireImmunity = true;
        if (rng.nextInt(3) == 0)
            iceImmunity = true;
        if (rng.nextInt(3) == 0)
            earthImmunity = true;
        maxHp = hp;
        this.hp = hp;
        maxMana = mana;
        this.mana = mana;
        this.abilities = abilities;
    }
    public void receiveDamage(int damage) {
        Random rng = new Random();
        if (rng.nextBoolean()) {
            damage /= 2;
            JOptionPane.showMessageDialog(null, "The enemy received only half damage!");
        }
        loseHp(damage);
    }
    public int getDamage() {
        Random rng = new Random();
        if (rng.nextBoolean())
            return damage * 2;
        return damage;
    }
    public int getSpellDamage(Spell ability, Entity target) {
        int damage = ability.damage;
        // if immune to an ability, deal only 20% spell damage
        if (ability instanceof IceSpell && target.iceImmunity)
            damage /= 5;
        else if (ability instanceof FireSpell && target.fireImmunity)
            damage /= 5;
        else if (ability instanceof EarthSpell && target.earthImmunity)
            damage /= 5;
        return damage;
    }
}