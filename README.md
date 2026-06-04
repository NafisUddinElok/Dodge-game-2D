# Dodge Game — Java Swing OOP Project

A 2D arcade-style dodge game built with **Java Swing**, demonstrating core and advanced **Object-Oriented Programming** concepts.


## 🕹️ How to Play

| Key | Action |
|-----|--------|
| `↑ ↓ ← →` | Move character |
| `P` | Pause / Resume |
| `R` | Restart (after Game Over) |
| `ENTER` | Confirm name and start |


## ✨ Features

- **Name input screen** — enter your name before playing
- **Male character** with name tag displayed above head
- **3 health hearts** — survive longer with shield powerups
- **4 enemy types** — Normal, Fast, Big, Chase
- **3 powerups** — Shield, Slow Motion, Health
- **Near-miss combo system** — score bonus for close dodges
- **Smooth difficulty scaling** — speed increases with score
- **Pause system** — press P anytime
- **High score tracking** — persists across restarts in session
- **Sky background** — gradient sky, clouds, sun, ground


# 🏗️ OOP Concepts Used

| Concept | Where |
|---------|-------|
| **Abstract Class** | `GameObject` — base for all game objects |
| **Inheritance** | `NormalBlock → FastBlock, BigBlock, EnemyBlock` |
| **Polymorphism** | `ArrayList<GameObject>` — one loop updates/draws everything |
| **Interface** | `MovementBehavior` — Strategy Pattern for movement |
| **Encapsulation** | `Player` — private fields, public getters |
| **Singleton** | `ScoreManager.getInstance()` |
| **Observer Pattern** | `EventBus` — decoupled event system |
| **Factory Pattern** | `BlockFactory.createRandom()` |
| **Strategy Pattern** | `StraightDown`, `ZigZagMovement`, `FollowMovement` 
| **State Machine** | `PlayerState`, `GameState` enums |
| **Separation of Concerns** | `GameEngine` (logic), `Renderer` (drawing), `InputHandler` (input)


## 📸 Screenshots


<img width="997" height="506" alt="Screenshot 2026-06-05 at 2 16 11 AM" src="https://github.com/user-attachments/assets/91c7cd1a-5793-447c-b889-b7c34ce92efe" />

<img width="1001" height="508" alt="Screenshot 2026-06-05 at 2 17 02 AM" src="https://github.com/user-attachments/assets/6e0d0d6b-8e4b-4ce3-b2b0-e8c160066388" />

<img width="1000" height="498" alt="Screenshot 2026-06-05 at 2 17 19 AM" src="https://github.com/user-attachments/assets/8a6d3cc2-d949-47c0-a622-40f41aeb2892" />


# Demo Video
https://github.com/user-attachments/assets/b99b2e9e-8d7e-4cee-b913-92eae6675230

