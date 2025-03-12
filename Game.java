import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

class InvalidCommandException extends Exception {
    public InvalidCommandException(String message) {
        super(message);
    }
}

class LevelCompletedException extends Exception {
    public LevelCompletedException(String message) {
        super(message);
    }
}

class PlayerDiedException extends Exception {
    public PlayerDiedException(String message) {
        super(message);
    }
}

class LeaveGameException extends Exception {
    public LeaveGameException(String message) {
        super(message);
    }
}

class Game extends JFrame {
    private static Game instance = null;
    private final ArrayList<Account> accounts;
    private Grid map = null;
    private final Random rng;
    private Account loggedInAccount = null;
    private Character character = null;
    private Enemy enemy;
    private JPanel characterWindow = null;
    private final JPanel mainPanel, mapWindow, battleWindow, abilitiesWindow, gameOverWindow;
    private boolean isTestMap;
    private int nrLevelsCompleted = 0, nrEnemiesKilled = 0;
    private Game() {
        // general settings
        super("League of Warriors");
        accounts = JsonInput.deserializeAccounts();
        rng = new Random();
        // change JOptionPane font size
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 30));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 30));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel(new CardLayout());
        // create the authentication window
        JPanel authWindow = new JPanel(new GridBagLayout());
        JPanel authPanel = new JPanel();
        authPanel.setLayout(new GridLayout(4, 1, 0, 20));
        JPanel emailPanel = new JPanel(new GridLayout(2, 1));
        JLabel emailLabel = new JLabel("Email: ");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 30));
        JTextField emailField = new JTextField(15);
        emailField.setFont(new Font("Arial", Font.PLAIN, 30));
        emailPanel.add(emailLabel);
        emailPanel.add(emailField);
        JPanel passwordPanel = new JPanel(new GridLayout(2, 1));
        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 30));
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 30));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 30));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryLogin(emailField.getText(), passwordField.getText());
                if (loggedInAccount != null) {
                    showCharacters();
                }
            }
        });
        authPanel.add(emailPanel);
        authPanel.add(passwordPanel);
        authPanel.add(loginButton);
        authWindow.add(authPanel);
        mainPanel.add(authWindow, "AUTH");
        // create map window
        mapWindow = new JPanel(new GridBagLayout());
        mainPanel.add(mapWindow, "MAP");
        // create battle window
        battleWindow = new JPanel(new GridBagLayout());
        mainPanel.add(battleWindow, "BATTLE");
        // create abilities window
        abilitiesWindow = new JPanel(new GridBagLayout());
        mainPanel.add(abilitiesWindow, "ABILITIES");
        // create game over window
        gameOverWindow = new JPanel(new GridBagLayout());
        mainPanel.add(gameOverWindow, "GAME OVER");
        // add main panel to JFrame and make it visible
        add(mainPanel);
        setVisible(true);
    }
    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }
    private void showLogin() {
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "AUTH");
        pack();
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
    private void tryLogin(String email, String password) {
        if (email == null || password == null)
            return;
        Account account = null;
        boolean found = false;
        for (Account acc : accounts) {
            if (acc.getEmail().equals(email)) {
                account = acc;
                found = true;
                break;
            }
        }
        if (!found) {
            JOptionPane.showMessageDialog(this, "Email not found!");
            return;
        }
        if (!account.checkPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password does not match!");
            return;
        }
        loggedInAccount = account;
    }
    private void showCharacters() {
        if (loggedInAccount == null)
            return;
        // only create once since logout and login with different account is not possible without relaunch
        if (characterWindow == null) {
            characterWindow = new JPanel(new GridBagLayout());
            JPanel characterPanel = new JPanel();
            characterPanel.setLayout(new BoxLayout(characterPanel, BoxLayout.Y_AXIS));
            JLabel characterLabel = new JLabel("Choose a character:");
            characterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            characterLabel.setFont(new Font("Arial", Font.BOLD, 30));
            JList<Object> characterList = new JList<>(loggedInAccount.characters.toArray());
            characterList.setFont(new Font("Arial", Font.PLAIN, 30));
            characterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            characterList.setBackground(getBackground());
            JButton selectCharacterButton = new JButton("Select Character");
            selectCharacterButton.setFont(new Font("Arial", Font.BOLD, 30));
            selectCharacterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            selectCharacterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Character selectedCharacter = (Character) characterList.getSelectedValue();
                    if (selectedCharacter != null) {
                        character = selectedCharacter;
                        showMap();
                    } else {
                        JOptionPane.showMessageDialog(characterWindow, "No character selected!");
                    }
                }
            });
            characterPanel.add(characterLabel);
            // used for spacing the items
            characterPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            characterPanel.add(characterList);
            characterPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            characterPanel.add(selectCharacterButton);
            characterWindow.add(characterPanel);
            mainPanel.add(characterWindow, "CHARACTER");
        }
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "CHARACTER");
        mainPanel.setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
    }
    private void showMap() {
        mapWindow.removeAll();
        if (map == null) {
            if (isTestMap)
                map = Grid.generateTestMap();
            else {
                map = Grid.generateMap(3 + rng.nextInt(8), 3 + rng.nextInt(8));
            }
            map.character = character;
        }
        JSplitPane mapSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // stop resizing
        mapSplitPane.setEnabled(false);
        JSplitPane infoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        infoSplitPane.setEnabled(false);
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3));
        JButton northButton = new JButton("NORTH");
        JButton southButton = new JButton("SOUTH");
        JButton eastButton = new JButton("EAST");
        JButton westButton = new JButton("WEST");
        northButton.setFont(new Font("Arial", Font.BOLD, 30));
        southButton.setFont(new Font("Arial", Font.BOLD, 30));
        eastButton.setFont(new Font("Arial", Font.BOLD, 30));
        westButton.setFont(new Font("Arial", Font.BOLD, 30));
        northButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                movePlayer("NORTH");
            }
        });
        southButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                movePlayer("SOUTH");
            }
        });
        eastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                movePlayer("EAST");
            }
        });
        westButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                movePlayer("WEST");
            }
        });
        // add spacing for buttons
        buttonPanel.add(Box.createRigidArea(null));
        buttonPanel.add(northButton);
        buttonPanel.add(Box.createRigidArea(null));
        buttonPanel.add(westButton);
        buttonPanel.add(Box.createRigidArea(null));
        buttonPanel.add(eastButton);
        buttonPanel.add(Box.createRigidArea(null));
        buttonPanel.add(southButton);
        buttonPanel.add(Box.createRigidArea(null));
        // for vertical centering of JTextArea
        JPanel statsPanel = new JPanel(new GridBagLayout());
        JTextArea statsText = new JTextArea();
        statsText.setFont(new Font("Arial", Font.PLAIN, 30));
        statsText.setEditable(false);
        statsText.setBackground(getBackground());
        statsText.append("Level: " + character.level + "\n");
        statsText.append("Experience: " + character.exp + " / " + Character.expMilestones[character.level] + "\n");
        statsText.append("Health: " + character.hp + "\n");
        statsText.append("Mana: " + character.mana);
        statsPanel.add(statsText);
        infoSplitPane.setTopComponent(buttonPanel);
        infoSplitPane.setBottomComponent(statsPanel);
        JPanel mapPanel = new JPanel(new GridBagLayout());
        JPanel mapGrid = new JPanel(new GridLayout(map.width, map.length));
        for (int i = 0; i < map.width; i++) {
            for (int j = 0; j < map.length; j++) {
                Cell cell = map.get(i).get(j);
                JLabel cellLabel;
                if (!cell.visited)
                    // SwingConstants.CENTER for centering text
                    cellLabel = new JLabel("?", SwingConstants.CENTER);
                else
                    cellLabel = new JLabel(cell.toString(), SwingConstants.CENTER);
                cellLabel.setFont(new Font("Arial", Font.BOLD, 30));
                cellLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cellLabel.setMinimumSize(new Dimension(75, 75));
                cellLabel.setPreferredSize(new Dimension(75, 75));
                cellLabel.setMaximumSize(new Dimension(75, 75));
                if (cell == map.currentCell) {
                    cellLabel.setOpaque(true);
                    cellLabel.setBackground(Color.CYAN);
                }
                mapGrid.add(cellLabel);
            }
        }
        mapPanel.add(mapGrid);
        mapSplitPane.setLeftComponent(infoSplitPane);
        mapSplitPane.setRightComponent(mapPanel);
        mapWindow.add(mapSplitPane);
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "MAP");
        // remove flicker from redrawing everything with pack()
        mainPanel.setPreferredSize(new Dimension(1200, 900));
        mainPanel.revalidate();
        mainPanel.repaint();
        pack();
        setLocationRelativeTo(null);
    }
    private void interactWithCell(CellEntityType type) throws LevelCompletedException, PlayerDiedException {
        switch (type) {
            case ENEMY:
                enemy = new Enemy();
                JOptionPane.showMessageDialog(this, "You have encountered an enemy!");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showBattle();
                    }
                });
                break;
            case SANCTUARY:
                int maxHp = map.character.maxHp;
                int maxMana = map.character.maxMana;
                // + 1 for rng bound because bound is excluded
                int regenedHp = map.character.regenHp(maxHp / 3 + rng.nextInt(2 * maxHp / 3 + 1));
                int regenedMana = map.character.regenMana(maxMana / 3 + rng.nextInt(2 * maxMana / 3 + 1));
                JOptionPane.showMessageDialog(this,
                        "You have found a sanctuary and rested.\n" + map.character.name +
                                " has regenerated " + regenedHp + " hp, now having " + map.character.hp +
                                " hp and has regenerated " + regenedMana + " mana, now having " +
                                map.character.mana + " mana.");
                break;
            case PORTAL:
                throw new LevelCompletedException("");
                // default includes VOID/PLAYER cells
            default:
                break;
        }
    }
    // returns true if game should return to menu (character selection)
    private void movePlayer(String direction) {
        try {
            switch (direction) {
                case "NORTH":
                    interactWithCell(map.goNorth());
                    break;
                case "SOUTH":
                    interactWithCell(map.goSouth());
                    break;
                case "EAST":
                    interactWithCell(map.goEast());
                    break;
                case "WEST":
                    interactWithCell(map.goWest());
                    break;
            }
            showMap();
        } catch (ImpossibleMoveException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (LevelCompletedException e) {
            nrLevelsCompleted++;
            StringBuilder sb = new StringBuilder();
            sb.append("You have completed level " + nrLevelsCompleted + "!");
            if (map.character.gainExp(5 * nrLevelsCompleted))
                sb.append("\nLeveled up to level " + map.character.level + "!");
            sb.append("\nGained " + 5 * nrLevelsCompleted + " experience with a total of " +
                             map.character.exp + " exp out of " + Character.expMilestones[map.character.level] +
                             " exp needed for level up.");
            JOptionPane.showMessageDialog(this, sb.toString());
            // increment number of games played
            loggedInAccount.gamesPlayed++;
            // regen is truncated so overflow is not relevant
            map.character.regenMana(map.character.maxMana);
            map.character.regenHp(map.character.maxHp);
            // remove map to be recreated
            map = null;
            showMap();
        } catch (PlayerDiedException e) {
            showGameOver();
        }
    }
    private void showBattle() {
        battleWindow.removeAll();
        // if enemy is dead
        if (enemy.hp == 0) {
            nrEnemiesKilled++;
            // double hp and mana
            map.character.regenHp(map.character.hp);
            map.character.regenMana(map.character.mana);
            int expGained = 5 + rng.nextInt(11);
            StringBuilder sb = new StringBuilder();
            sb.append("Congratulations! " + map.character.name + " has killed the enemy and has doubled the hp and mana to " +
                      map.character.hp + " hp and " + map.character.mana + " mana.");
            if (map.character.gainExp(expGained))
                sb.append("\nLeveled up to level " + map.character.level + "!");
            JOptionPane.showMessageDialog(this, sb.toString());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showMap();
                }
            });
        }
        JSplitPane battleSplitPane = new JSplitPane();
        battleSplitPane.setEnabled(false);
        battleSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane playerSplitPane = new JSplitPane();
        playerSplitPane.setEnabled(false);
        playerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        JLabel playerImage = new JLabel(new ImageIcon("src/images/" +
                                        map.character.getClass().getSimpleName().toLowerCase() + ".jpeg"));
        JPanel playerStatsPanel = new JPanel(new GridLayout(1, 2));
        JPanel playerStatsCenter = new JPanel(new GridBagLayout());
        JTextArea playerStats = new JTextArea();
        playerStats.setFont(new Font("Arial", Font.PLAIN, 30));
        playerStats.setEditable(false);
        playerStats.setBackground(getBackground());
        playerStats.append("Health: " + map.character.hp + "\n");
        playerStats.append("Mana: " + map.character.mana);
        JButton attackButton = new JButton("Attack");
        attackButton.setFont(new Font("Arial", Font.PLAIN, 30));
        attackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAbilities();
            }
        });
        playerStatsCenter.add(playerStats);
        playerStatsPanel.add(playerStatsCenter);
        playerStatsPanel.add(attackButton);
        playerSplitPane.setTopComponent(playerImage);
        playerSplitPane.setBottomComponent(playerStatsPanel);
        JSplitPane enemySplitPane = new JSplitPane();
        enemySplitPane.setEnabled(false);
        enemySplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        JLabel enemyImage = new JLabel(new ImageIcon("src/images/enemy.jpeg"));
        JPanel enemyStatsCenter = new JPanel(new GridBagLayout());
        JTextArea enemyStats = new JTextArea();
        enemyStats.setFont(new Font("Arial", Font.PLAIN, 30));
        enemyStats.setEditable(false);
        enemyStats.setBackground(getBackground());
        enemyStats.append("Health: " + enemy.hp + "\n");
        enemyStats.append("Mana: " + enemy.mana + "\n");
        enemyStats.append("Immunities: ");
        int nrImmunities = 0;
        if (enemy.fireImmunity) {
            enemyStats.append("Fire");
            nrImmunities++;
        }
        if (enemy.iceImmunity) {
            if (nrImmunities > 0)
                enemyStats.append(", ");
            enemyStats.append("Ice");
            nrImmunities++;
        }
        if (enemy.earthImmunity) {
            if (nrImmunities > 0)
                enemyStats.append(", ");
            enemyStats.append("Earth");
            nrImmunities++;
        }
        if (nrImmunities == 0)
            enemyStats.append("Nothing");
        enemyStatsCenter.add(enemyStats);
        enemySplitPane.setTopComponent(enemyImage);
        enemySplitPane.setBottomComponent(enemyStatsCenter);
        battleSplitPane.setLeftComponent(playerSplitPane);
        battleSplitPane.setRightComponent(enemySplitPane);
        battleWindow.add(battleSplitPane);
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "BATTLE");
        mainPanel.setPreferredSize(new Dimension(1400, 900));
        pack();
        setLocationRelativeTo(null);
    }
    private void showAbilities() {
        abilitiesWindow.removeAll();
        // needed for formatting
        int nrAbilities = map.character.abilities.size();
        JSplitPane abilitiesSplitPane1, abilitiesSplitPane2 = null, abilitiesSplitPane3 = null;
        abilitiesSplitPane1 = new JSplitPane();
        abilitiesSplitPane1.setEnabled(false);
        abilitiesSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        if (nrAbilities >= 2) {
            abilitiesSplitPane2 = new JSplitPane();
            abilitiesSplitPane2.setEnabled(false);
            abilitiesSplitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            if (nrAbilities == 3) {
                abilitiesSplitPane3 = new JSplitPane();
                abilitiesSplitPane3.setEnabled(false);
                abilitiesSplitPane3.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            }
        }
        // save damage of getDamage() so that damage when showing the abilities is the same as when using it
        // this solves the problem that calling the getDamage() could return double damage when showing
        // but not when using it (further down)
        int baseDamage = map.character.getDamage();
        // create and show each spell
        for (int i = 0; i < nrAbilities; i++) {
            Spell spell = map.character.abilities.get(i);
            JSplitPane spellSplitPane = new JSplitPane();
            spellSplitPane.setEnabled(false);
            spellSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            JLabel spellImage = new JLabel(new ImageIcon("src/images/" + spell.getClass().getSimpleName() + ".jpeg"));
            JPanel spellStatsPanel = new JPanel(new GridLayout(1, 2));
            JPanel spellStatsCenter = new JPanel(new GridBagLayout());
            JTextArea spellStats = new JTextArea();
            spellStats.setFont(new Font("Arial", Font.PLAIN, 30));
            spellStats.setEditable(false);
            spellStats.setBackground(getBackground());
            spellStats.append("Type: " + spell.getClass().getSimpleName().split("Spell")[0] + "\n");
            spellStats.append("Mana Cost: " + spell.manaCost + "\n");
            spellStats.append("Damage: " + (baseDamage + map.character.getSpellDamage(spell, enemy)));
            spellStatsCenter.add(spellStats);
            JButton chooseButton = new JButton("Choose");
            chooseButton.setFont(new Font("Arial", Font.PLAIN, 30));
            chooseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // if it returns false, it did not have enough mana to use it
                    if (!map.character.useAbility(spell, enemy, baseDamage)) {
                        JOptionPane.showMessageDialog(abilitiesWindow, map.character.name +
                                " does not have enough mana to use this ability.");
                        return;
                    }
                    // if it gets here, ability was used successfully
                    try {
                        takeEnemyTurn();
                        showBattle();
                    } catch (PlayerDiedException ex) {
                        showGameOver();
                    }
                }
            });
            spellStatsPanel.add(spellStatsCenter);
            spellStatsPanel.add(chooseButton);
            spellSplitPane.setTopComponent(spellImage);
            spellSplitPane.setBottomComponent(spellStatsPanel);
            // add spell to correct SplitPane
            switch (i) {
                case 0:
                    abilitiesSplitPane1.setLeftComponent(spellSplitPane);
                    break;
                case 1:
                    abilitiesSplitPane2.setLeftComponent(spellSplitPane);
                    abilitiesSplitPane1.setRightComponent(abilitiesSplitPane2);
                    break;
                case 2:
                    abilitiesSplitPane3.setLeftComponent(spellSplitPane);
                    abilitiesSplitPane2.setRightComponent(abilitiesSplitPane3);
                    break;
            }
        }
        // create the basic attack option
        JSplitPane attackSplitPane = new JSplitPane();
        attackSplitPane.setEnabled(false);
        attackSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        JLabel attackImage = new JLabel(new ImageIcon("src/images/basicAttack.jpeg"));
        JPanel attackStatsPanel = new JPanel(new GridLayout(1, 2));
        JPanel attackStatsCenter = new JPanel(new GridBagLayout());
        JTextArea attackStats = new JTextArea();
        attackStats.setFont(new Font("Arial", Font.PLAIN, 30));
        attackStats.setEditable(false);
        attackStats.setBackground(getBackground());
        attackStats.append("Type: Basic Attack\n");
        attackStats.append("Mana Cost: 0\n");
        attackStats.append("Damage: " + baseDamage);
        attackStatsCenter.add(attackStats);
        JButton chooseButton = new JButton("Choose");
        chooseButton.setFont(new Font("Arial", Font.PLAIN, 30));
        chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enemy.receiveDamage(baseDamage);
                try {
                    takeEnemyTurn();
                    showBattle();
                } catch (PlayerDiedException ex) {
                    showGameOver();
                }
            }
        });
        attackStatsPanel.add(attackStatsCenter);
        attackStatsPanel.add(chooseButton);
        attackSplitPane.setTopComponent(attackImage);
        attackSplitPane.setBottomComponent(attackStatsPanel);
        // add basic attack to correct SplitPane
        switch (nrAbilities) {
            case 1:
                abilitiesSplitPane1.setRightComponent(attackSplitPane);
                break;
            case 2:
                abilitiesSplitPane2.setRightComponent(attackSplitPane);
                break;
            case 3:
                abilitiesSplitPane3.setRightComponent(attackSplitPane);
                break;
        }
        abilitiesWindow.add(abilitiesSplitPane1);
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "ABILITIES");
        // calculate size based on how many abilities are showing up
        mainPanel.setPreferredSize(new Dimension(400 * nrAbilities + 600, 700));
        pack();
        setLocationRelativeTo(null);
    }
    private void takeEnemyTurn() throws PlayerDiedException {
        // cannot take turn if dead
        if (enemy.hp == 0)
            return;
        // enemy's time to attack
        boolean hasUsed;
        int choice;
        Spell usedAbility = null;
        do {
            hasUsed = true;
            // each ability has index from "0" to "size() - 1" and index "size()" is basic attack
            choice = rng.nextInt(enemy.abilities.size() + 1);
            if (choice != enemy.abilities.size()) {
                Spell ability = enemy.abilities.get(choice);
                // if it returns false, it did not have enough mana to use it so retry
                if (!enemy.useAbility(ability, map.character, enemy.getDamage()))
                    hasUsed = false;
                // remove ability if used; if not used remove it anyway since ENEMY CANNOT REGEN MANA!!!!
                usedAbility = enemy.abilities.remove(choice);
            } else {
                map.character.receiveDamage(enemy.getDamage());
            }
        } while (!hasUsed);
        if (choice != enemy.abilities.size())
            JOptionPane.showMessageDialog(this, "The enemy has used " + usedAbility.getClass().getSimpleName() + " and Basic Attack.");
        else
            JOptionPane.showMessageDialog(this, "The enemy has used Basic Attack.");
        if (map.character.hp == 0)
            throw new PlayerDiedException("");
    }
    private void showGameOver() {
        // show game over window
        JSplitPane gameOverSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        gameOverSplitPane.setEnabled(false);
        JPanel gameOverStatsPanel = new JPanel(new GridLayout(1, 2));
        JPanel gameOverStatsCenter = new JPanel(new GridBagLayout());
        JPanel gameOverButtonCenter = new JPanel(new GridBagLayout());
        JTextArea gameOverStats = new JTextArea();
        gameOverStats.setFont(new Font("Arial", Font.PLAIN, 30));
        gameOverStats.setEditable(false);
        gameOverStats.setBackground(getBackground());
        gameOverStats.append("Name: " + map.character.name + "\n");
        gameOverStats.append("Role: " + map.character.getClass().getSimpleName() + "\n");
        gameOverStats.append("Level: " + map.character.level + "\n");
        gameOverStats.append("Experience: " + map.character.exp + " / " + Character.expMilestones[map.character.level] + "\n");
        gameOverStats.append("Maps completed: " + nrLevelsCompleted + "\n");
        // reset for subsequent runs
        nrLevelsCompleted = 0;
        gameOverStats.append("Enemies killed: " + nrEnemiesKilled);
        gameOverStatsCenter.add(gameOverStats);
        JButton backButton = new JButton("<html>Return to<br>Character Selection</html>");
        backButton.setFont(new Font("Arial", Font.PLAIN, 30));
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCharacters();
            }
        });
        gameOverButtonCenter.add(backButton);
        gameOverStatsPanel.add(gameOverStatsCenter);
        gameOverStatsPanel.add(gameOverButtonCenter);
        JLabel playerImage = new JLabel(new ImageIcon("src/images/" +
                map.character.getClass().getSimpleName().toLowerCase() + ".jpeg"));
        gameOverSplitPane.setTopComponent(playerImage);
        gameOverSplitPane.setBottomComponent(gameOverStatsPanel);
        gameOverWindow.add(gameOverSplitPane);
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "GAME OVER");
        mainPanel.setPreferredSize(new Dimension(900, 1000));
        mainPanel.revalidate();
        mainPanel.repaint();
        pack();
        setLocationRelativeTo(null);
        // regen hp and mana for a possible replay
        map.character.regenMana(map.character.maxMana);
        map.character.regenHp(map.character.maxHp);
        // reset map
        map = null;
    }
    private void run(boolean isTestMap) {
        this.isTestMap = isTestMap;
        showLogin();
//        loggedInAccount = accounts.get(0);
//        showCharacters();
    }
    public void run() {
        this.run(false);
    }
    public void runWithTestMap() {
        this.run(true);
    }
}
