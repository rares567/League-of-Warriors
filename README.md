# League of Warriors

## Overview
League of Warriors is an adventure game implemented in Java with a Swing-based graphical user interface. The game follows the principles of object-oriented programming (OOP) and incorporates design patterns to ensure clean, maintainable, and scalable code.

The game takes place on a dynamically generated grid-based map, where players navigate through various encounters, including battles with enemies, sanctuaries for healing, and portals for progressing to the next level. Players can choose from multiple characters, each with unique abilities and attributes that evolve as they gain experience.

## Features
- **Graphical User Interface (GUI)**: Built with Java Swing for an interactive and visually appealing experience.
- **Dynamic Game Board**: A randomly generated `n × m` grid where each cell contains different events.
- **Character Progression**: Gain experience, level up, and improve attributes like strength, dexterity, and charisma.
- **Turn-Based Combat**: Battle enemies using normal attacks or special abilities (fire, ice, earth) with mana costs.
- **Healing & Recovery**: Visit sanctuaries to restore health and mana.
- **Level Progression**: Pass through portals to reach new levels with larger maps and increased difficulty.

## Gameplay
1. **Login**: Players must log in with an email and password.
2. **Character Selection**: Choose a character from the available options.
3. **Exploration**: Move through the map using the arrows in the GUI
4. **Encounters**:
   - **Enemies**: Engage in turn-based combat.
   - **Sanctuary**: Restore health and mana.
   - **Portal**: Advance to the next level.
   - **Empty Cell**: Safe passage without events.
5. **Game Over**: If health reaches zero, the game ends, and the player can restart.

## Design Patterns Used
- **Factory Pattern**: For creating different types of characters and enemies.
- **Singleton Pattern**: Ensures a single instance of core game components.
- **Visitor Pattern**: Used for applying spells on the player or on the enemy.
- **Builder Pattern**: Allows an easier creation of the information of each account.
