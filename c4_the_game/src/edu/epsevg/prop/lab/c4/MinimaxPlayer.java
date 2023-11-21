package edu.epsevg.prop.lab.c4;

public class MinimaxPlayer implements Jugador , IAuto {
    final private int HEURISTICA_MAXIMA = Integer.MAX_VALUE;
    private int colorJugador;
    private String nomJugador;
    private int profunditatRecerca;
    private int jugadesExplorades;
    private int jugadesReals;

    public MinimaxPlayer(int profunditat) {
        this.profunditatRecerca = profunditat;
        nomJugador = "Mariona & Pau's player amb la profunditat de: (" + profunditat + ")";
    }

    public MinimaxPlayer(int profunditat, boolean estats) {
        this(profunditat);
    }
    
    // per si es crida malament i no es posa res, posem una profunditat per defecte
    public MinimaxPlayer() {
        int profunditat = 5;
        this.profunditatRecerca = profunditat;
        nomJugador = "Mariona & Pau's player amb la profunditat de: (" + profunditat + ")";
    }

    @Override
    public String nom() {
        return nomJugador;
    }

    @Override
    public int moviment(Tauler tauler, int colorJugador) {
        this.colorJugador = colorJugador;

        jugadesExplorades = 0;  
        jugadesReals++;
        int columna = calcularMinimax(tauler, profunditatRecerca);

        return columna;
    }

    private int calcularMinimax(Tauler t, int profunditat) {
        int col = 0;
        Integer valor = -HEURISTICA_MAXIMA - 1;
        int alfa = -HEURISTICA_MAXIMA;
        int beta = HEURISTICA_MAXIMA;

        return processColumns(t, profunditat, col, valor, alfa, beta);
    }

    private int processColumns(Tauler t, int profunditat, int col, Integer valor, int alfa, int beta) {
        for (int i = 0; i < t.getMida(); i++) {
            if (!t.movpossible(i)) continue;

            Tauler aux = new Tauler(t);
            aux.afegeix(i, colorJugador); //Cal fer un tauler auxiliar cada cop
            int min = valorMinim(aux, i, alfa, beta, profunditat - 1);
            if (valor < min) {
                col = i;
                valor = min;
            }
            if (beta < valor) {
                break;
            }
            alfa = Math.max(valor, alfa);
        }
        return col;
    }


    private int avaluarTauler(Tauler t) {
        ++jugadesExplorades;
        int res = 0;
        res = processRowsAndColumns(t, res);

        res += avaluarDiagonals(t);

        return adjustResultAccordingToHeuristica(res);
    }

    private int processRowsAndColumns(Tauler t, int res) {
        for (int i = t.getMida() - 1; i >= 0; --i) {
            res += avaluarColumna(t, i);
            res += (10 * avaluarFila(t, i) / (i + 1));  // Ponderem segons l'alçada del 4 en ratlla horitzontal
        }
        return res;
    }

    private int adjustResultAccordingToHeuristica(int res) {
        if (res >= HEURISTICA_MAXIMA / 2) 
            return HEURISTICA_MAXIMA;
        if (res <= -HEURISTICA_MAXIMA / 2) 
            return -HEURISTICA_MAXIMA;

        return res;
    }


    private int avaluarColumna(Tauler t, int col) {
        Integer first = 0, cont = 0, cont_buides = 0;

        return evaluateColumn(t, col, first, cont, cont_buides);
    }

    private int evaluateColumn(Tauler t, int col, Integer first, Integer cont, Integer cont_buides) {
        for (int i = t.getMida() - 1; i >= 0; --i) {
            int fitxa = t.getColor(i, col);
            if (fitxa == 0) {
                cont_buides++;
                continue;
            }

            if (first == 0) {
                first = fitxa;
            }

            if (first == fitxa) {
                cont += fitxa;
                if (isFourInARow(cont)) {
                    return colorJugador * first * HEURISTICA_MAXIMA;
                }
            } else {
                break;
            }
        }

        return calculateHeuristic(first, cont, cont_buides);
}

private boolean isFourInARow(Integer cont) {
    return cont > 3 || cont < -3;
}

private int calculateHeuristic(Integer first, Integer cont, Integer cont_buides) {
    if (cont == 0 || cont_buides + first * cont < 4)
        return 0;

    return (int) (colorJugador * first * (Math.pow(10.0, first * (cont - first))));
}


    private int avaluarFila(Tauler t, int fil) {
        int cont_buides = 0, cont = 0, color_actual = 0, res = 0, color_aux = 0;

        return processRow(t, fil, cont_buides, cont, color_actual, res, color_aux);
    }

    private int processRow(Tauler t, int fil, int cont_buides, int cont, int color_actual, int res, int color_aux) {
        for (int i = t.getMida() - 1; i >= 0; --i) {
            int fitxa = t.getColor(fil, i);
            if (fitxa == 0) {
                res = updateForEmptySpace(res, cont, cont_buides, color_actual, color_aux);
                cont_buides++;
                color_aux = (color_actual != 0) ? color_actual : color_aux;
                color_actual = 0;
            } else {
                res = updateForNonEmptySpace(t, fil, i, res, cont, cont_buides, color_actual, color_aux, fitxa);
                if (res == colorJugador * color_actual * HEURISTICA_MAXIMA) {
                    return res;
                }
                color_actual = (color_actual == 0) ? fitxa : color_actual * -1;
                cont = (fitxa == color_actual) ? ++cont : 1;
                cont_buides = 0;
            }

            if (i == 0 && cont + cont_buides > 3) {
                res += calculateHeuristicScore(colorJugador, color_actual, cont - 1);
            }
        }
        return res;
    }

    private int updateForEmptySpace(int res, int cont, int cont_buides, int color_actual, int color_aux) {
        if (cont + cont_buides > 3) {
            res += calculateHeuristicScore(colorJugador, (color_actual != 0) ? color_actual : color_aux, cont - 1);
        }
        return res;
    }

    private int updateForNonEmptySpace(Tauler t, int fil, int i, int res, int cont, int cont_buides, int color_actual, int color_aux, int fitxa) {
        if (fitxa == color_actual) {
            cont++;
            if (cont > 3) {
                return colorJugador * color_actual * HEURISTICA_MAXIMA;
            }
        } else {
            res = updateForEmptySpace(res, cont, cont_buides, color_actual, color_aux);
        }
        return res;
    }

    private int calculateHeuristicScore(int colorJugador, int color, int exponent) {
        return (int) (colorJugador * color * Math.pow(10.0, exponent));
    }


    private int avaluarDiagonals(Tauler t) {
        int res = 0;
        // Evaluate descending diagonals
        for (int col = t.getMida() - 4, fil = 0; fil < t.getMida() - 2; ) {
            res += evaluateDiagonal(t, col, fil, true);
            if (col > 0) {
                col--;
            } else {
                fil++;
            }
        }
        // Evaluate ascending diagonals
        for (int col = 3, fil = 0; fil < t.getMida() - 2; ) {
            res += evaluateDiagonal(t, col, fil, false);
            if (col < t.getMida() - 1) {
                col++;
            } else {
                fil++;
            }
        }
        return res;
    }

    private int evaluateDiagonal(Tauler t, int col, int fil, boolean isDescending) {
        int cont_buides = 0, cont = 0, color_actual = 0, res = 0;
        for (int i = 0; (isDescending ? i + col < t.getMida() : col - i >= 0) && i + fil < t.getMida(); i++) {
            int fitxa = t.getColor(fil + i, isDescending ? col + i : col - i);
            if (fitxa == 0) {
                cont_buides++;
                if (color_actual != 0 && cont + cont_buides > 3) {
                    res += calculateHeuristic(colorJugador, color_actual, cont);
                }
                color_actual = 0;
            } else {
                if (fitxa == color_actual) {
                    cont++;
                    if (cont > 3) {
                        return colorJugador * color_actual * HEURISTICA_MAXIMA;
                    }
                } else {
                    if (color_actual != 0 && cont + cont_buides > 3) {
                        res += calculateHeuristic(colorJugador, color_actual, cont);
                    }
                    color_actual = fitxa;
                    cont = 1;
                    cont_buides = 0;
                }
            }
        }
        if (color_actual != 0 && cont + cont_buides > 3) {
            res += calculateHeuristic(colorJugador, color_actual, cont);
        }
        return res;
    }

    private int calculateHeuristic(int colorJugador, int color_actual, int cont) {
        return (int) (colorJugador * color_actual * Math.pow(10.0, cont - 1));
    }

    private int valorMaxim(Tauler t, int col, int alfa, int beta, int profunditat) {
        if (t.solucio(col, -colorJugador)) {
            return -HEURISTICA_MAXIMA;
        }

        return profunditat > 0 ? calculateMaxValueWithDepth(t, alfa, beta, profunditat) : avaluarTauler(t);
    }

    private int calculateMaxValueWithDepth(Tauler t, int alfa, int beta, int profunditat) {
        Integer valor = -HEURISTICA_MAXIMA - 1;
        for (int i = 0; i < t.getMida(); ++i) {
            if (!t.movpossible(i)) continue;

            Tauler aux = new Tauler(t);
            aux.afegeix(i, colorJugador);
            valor = Math.max(valor, valorMinim(aux, i, alfa, beta, profunditat - 1));

            if (beta <= valor) {
                break;
            }
            alfa = Math.max(alfa, valor);
        }
        return valor;
    }


    private int valorMinim(Tauler t, int col, int alfa, int beta, int profunditat) {
        if (t.solucio(col, colorJugador)) {
            return HEURISTICA_MAXIMA;
        }

        return profunditat > 0 ? evaluateWithDepth(t, alfa, beta, profunditat) : avaluarTauler(t);
    }

    private int evaluateWithDepth(Tauler t, int alfa, int beta, int profunditat) {
        Integer valor = HEURISTICA_MAXIMA - 1;
        for (int i = 0; i < t.getMida(); i++) {
            if (!t.movpossible(i)) continue;

            Tauler aux = new Tauler(t);
            aux.afegeix(i, -colorJugador);
            valor = Math.min(valor, valorMaxim(aux, i, alfa, beta, profunditat - 1));

            if (valor < alfa) {
                break;
            }
            beta = Math.min(valor, beta);
        }
        return valor;
    }


}