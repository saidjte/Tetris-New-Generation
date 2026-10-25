package com.tetrisnewgeneration.game.engine

/** One-shot things that happened during the last tick; consumed by UI/audio layers. */
sealed class GameEvent {
    data class PieceMoved(val horizontal: Boolean) : GameEvent()
    data object PieceRotated : GameEvent()
    data class SoftDropped(val rows: Int) : GameEvent()
    data class HardDropped(val rows: Int) : GameEvent()
    data object PieceLocked : GameEvent()
    data class PieceHeld(val swapped: Boolean) : GameEvent()
    data class LinesCleared(
        val lines: Int,
        val tspin: Boolean,
        val backToBack: Boolean,
        val combo: Int,
        val perfectClear: Boolean,
        val points: Long
    ) : GameEvent()

    data class LevelUp(val newLevel: Int) : GameEvent()
    data class GoalReached(val kind: String) : GameEvent()
    data object GameOver : GameEvent()
    data object InvalidAction : GameEvent()
}

/** Player intents fed into the game loop (touch controls, gestures). */
sealed class GameAction {
    data object MoveLeft : GameAction()
    data object MoveRight : GameAction()
    data object RotateCw : GameAction()
    data object RotateCcw : GameAction()
    data object SoftDropOn : GameAction()
    data object SoftDropOff : GameAction()
    data object HardDrop : GameAction()
    data object Hold : GameAction()
    data object TogglePause : GameAction()
}
