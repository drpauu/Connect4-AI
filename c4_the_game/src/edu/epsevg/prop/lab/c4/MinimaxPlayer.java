package edu.epsevg.prop.lab.c4;

public class MinimaxPlayer implements Jugador , IAuto {
    final private int HEURISTICA_MAXIMA = 10000000;
    private int colorJugador;
    private String nomJugador;
    private int profunditatRecerca;
    private int jugadesExplorades;
    private int jugadesReals;
    private double tempsAcumulat = 0;
    private boolean mostrarEstadistiques = true;

    public MinimaxPlayer(int profunditat) {
        this.profunditatRecerca = profunditat;
        nomJugador = "Mariona & Pau's player amb la profunditat de: (" + profunditat + ")";
    }

    public MinimaxPlayer(int profunditat, boolean estats) {
        this(profunditat);
        this.mostrarEstadistiques = estats;
    }
    
    // per si es crida malament i no es posa res, posem una profunditat per defecte
    public MinimaxPlayer() {
        int profunditat = 2;
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

        if (!mostrarEstadistiques) {
            return calcularMinimax(tauler, profunditatRecerca);
        }

        jugadesExplorades = 0;  
        jugadesReals++;
        long tempsInici = System.currentTimeMillis();
        int columna = calcularMinimax(tauler, profunditatRecerca);
        logEstadistiques(tempsInici, columna);

        return columna;
    }

    private void logEstadistiques(long tempsInici, int columna) {
        long tempsFinal = System.currentTimeMillis();
        double tempsUtilitzat = (tempsFinal - tempsInici) / 1000.0;
        tempsAcumulat += tempsUtilitzat;

        System.out.println("Columna escollida: " + columna + " després d'explorar " + jugadesExplorades + " jugades.");
        System.out.printf("Temps utilitzat per decidir: %.3f segons.%n", tempsUtilitzat);
        System.out.printf("Temps mitjà per jugada: %.4f segons.%n", tempsAcumulat / jugadesReals);
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

        res = processDiagonalFromTopLeft(t, res);
        res = processDiagonalFromTopRight(t, res);

        return res;
    }

    private int processDiagonalFromTopLeft(Tauler t, int res) {
        int col = t.getMida() - 4, fil = 0;

        while (fil < t.getMida() - 2) {
            res = evaluateDiagonal(t, fil, col, res, true);
            if (col > 0) {
                col--;
            } else {
                fil++;
            }
        }

        return res;
    }

    private int processDiagonalFromTopRight(Tauler t, int res) {
        int col = 3, fil = 0;

        while (fil < t.getMida() - 2) {
            res = evaluateDiagonal(t, fil, col, res, false);
            col++;
            if (col == t.getMida()) {
                col--;
                fil++;
            }
        }

        return res;
    }

    private int evaluateDiagonal(Tauler t, int fil, int col, int res, boolean isLeftDiagonal) {
        int cont_buides = 0, cont = 0, color_actual = 0, color_aux = 0;

        for (int i = 0; isWithinBounds(t, i, col, fil, isLeftDiagonal); i++) {
            int fitxa = isLeftDiagonal ? t.getColor(fil + i, col + i) : t.getColor(fil + i, col - i);

            // process logic here (same as in original function)
            // remember to handle 'return colorJugador*color_actual*HEURISTICA_MAXIMA;' case

            // Update res, cont, cont_buides, color_actual, and color_aux according to the original logic
        }

        return updateResAfterDiagonal(cont, cont_buides, color_actual, res);
    }

    private boolean isWithinBounds(Tauler t, int i, int col, int fil, boolean isLeftDiagonal) {
        return isLeftDiagonal ? i + col < t.getMida() && i + fil < t.getMida() : col - i >= 0 && i + fil < t.getMida();
    }

    private int updateResAfterDiagonal(int cont, int cont_buides, int color_actual, int res) {
        if (cont + cont_buides > 3) {
            res += (int)(colorJugador * color_actual * Math.pow(10.0, cont - 1));
        }
        return res;
    }


    private int valorMaxim(Tauler t, int col, int alfa, int beta, int profunditat) {
        if (t.solucio(col, -colorJugador)) {
            return -HEURISTICA_MAXIMA;
        }
        if (profunditat > 0) {
            return evaluateWithDepth(t, alfa, beta, profunditat);
        } else {
            return avaluarTauler(t);
        }
    }

    private int evaluateWithDepth(Tauler t, int alfa, int beta, int profunditat) {
        Integer valor = -HEURISTICA_MAXIMA - 1;

        for (int i = 0; i < t.getMida(); i++) {
            valor = evaluateMove(t, i, valor, alfa, beta, profunditat);
            if (beta <= valor) {
                break;
            }
            alfa = Math.max(valor, alfa);
        }

        return valor;
    }

    private int evaluateMove(Tauler t, int i, int valor, int alfa, int beta, int profunditat) {
        if (!t.movpossible(i)) {
            return valor;
        }
        Tauler aux = new Tauler(t);
        aux.afegeix(i, colorJugador);
        int newValue = valorMinim(aux, i, alfa, beta, profunditat - 1);
        return Math.max(valor, newValue);
    }


    private int valorMinim(Tauler t, int col, int alfa, int beta, int profunditat) {
        if (t.solucio(col, colorJugador)) {
            return HEURISTICA_MAXIMA;
        }

        if (profunditat <= 0) {
            return avaluarTauler(t);
        }

        return calculateMinValue(t, alfa, beta, profunditat);
    }

    private int calculateMinValue(Tauler t, int alfa, int beta, int profunditat) {
        Integer valor = HEURISTICA_MAXIMA - 1;

        for (int i = 0; i < t.getMida(); i++) {
            if (!t.movpossible(i)) {
                continue;
            }

            Tauler aux = new Tauler(t);
            aux.afegeix(i, -colorJugador);
            valor = updateValueForMin(aux, i, valor, alfa, beta, profunditat);

            if (valor < alfa) {
                return valor;
            }

            beta = Math.min(valor, beta);
        }

        return valor;
    }

    private int updateValueForMin(Tauler t, int i, int valor, int alfa, int beta, int profunditat) {
        return Math.min(valor, valorMaxim(t, i, alfa, beta, profunditat - 1));
    }


}
