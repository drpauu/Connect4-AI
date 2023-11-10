package edu.epsevg.prop.lab.c4;

public class MinimaxPlayer implements Jugador {

    private String nom;
    private int depth;

    public MinimaxPlayer() {
        this.nom = "MinimaxPlayer";
        this.depth = 3; // You can adjust the depth
    }

    @Override
    public int moviment(Tauler t, int color) {
        return minimax(t, depth, true, color);
    }

    private int minimax(Tauler t, int depth, boolean isMaximizingPlayer, int color) {
        if (depth == 0 || checkGameOver(t)) {
            return evaluateBoard(t, color);
        }

        if (isMaximizingPlayer) {
            int bestScore = Integer.MIN_VALUE;
            int bestMove = -1;
            for (int move : getValidMoves(t)) {
                makeMove(t, move, color);
                int score = minimax(t, depth - 1, false, color);
                undoMove(t, move);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            return depth == this.depth ? bestMove : bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            int opponentColor = getOpponentColor(color);
            for (int move : getValidMoves(t)) {
                makeMove(t, move, opponentColor);
                int score = minimax(t, depth - 1, true, color);
                undoMove(t, move);
                if (score < bestScore) {
                    bestScore = score;
                }
            }
            return bestScore;
        }
    }

    @Override
    public String nom() {
        return nom;
    }
}
