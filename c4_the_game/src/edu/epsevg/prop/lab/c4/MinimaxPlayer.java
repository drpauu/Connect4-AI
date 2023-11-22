package edu.epsevg.prop.lab.c4;

/**
 * Classe MinimaxPlayer que implementa l'algorisme Minimax amb poda Alpha-Beta
 * per al joc Connecta 4.
 */
public class MinimaxPlayer implements Jugador, IAuto {
    final private int HEURISTICA_MAXIMA = Integer.MAX_VALUE; // Valor màxim per a l'heurística
    private int colorJugador; // Color actual del jugador
    private String nomJugador; // Nom del jugador
    private int profunditatRecerca; // Profunditat de recerca per a l'algorisme Minimax
    private int jugadesExplorades; // Comptador de jugades explorades
    private int jugadesReals; // Comptador de jugades reals
    private boolean estats = true;

    /**
     * Constructor de MinimaxPlayer amb profunditat específica.
     * @param profunditat Profunditat de recerca per a l'algorisme Minimax.
     */
    public MinimaxPlayer(int profunditat) {
        this.profunditatRecerca = profunditat;
        nomJugador = "Mariona & Pau's player amb la profunditat de: " + profunditat;
    }

    /**
     * Constructor sobrecarregat de MinimaxPlayer amb opcions d'estats.
     * @param profunditat Profunditat de recerca.
     * @param stats Si s'han de mostrar estadístiques.
     */
    public MinimaxPlayer(int profunditat, boolean stats) {
        this.estats = stats;
        this(profunditat);
        nomJugador = "Mariona & Pau's player amb la profunditat de: " + profunditat;
    }

    /**
     * Constructor per defecte de MinimaxPlayer amb profunditat predefinida.
     */
    public MinimaxPlayer() {
        int profunditat = 5;
        this.profunditatRecerca = profunditat;
        nomJugador = "Mariona & Pau's player amb la profunditat de: " + profunditat;
    }

    /**
     * Retorna el nom del jugador.
     * @return Nom del jugador.
     */
    @Override
    public String nom() {
        return nomJugador;
    }

    /**
     * Realitza un moviment en el tauler.
     * @param tauler El tauler actual del joc.
     * @param colorJugador El color del jugador actual.
     * @return La columna on realitzar el moviment.
     */
    @Override
    public int moviment(Tauler tauler, int colorJugador) {
        this.colorJugador = colorJugador;

        jugadesExplorades = 0;  
        jugadesReals++;
        int columna = calcularMinimax(tauler, profunditatRecerca);

        return columna;
    }
    /**
     * Implementa l'algorisme Minimax amb poda Alpha-Beta.
     * @param t El tauler actual del joc.
     * @param profunditat La profunditat actual de recerca.
     * @return La millor columna per a moure's segons l'algorisme.
     */
    private int calcularMinimax(Tauler t, int profunditat) {
        int col = 0;
        Integer valor = -HEURISTICA_MAXIMA - 1;
        int alfa = -HEURISTICA_MAXIMA;
        int beta = HEURISTICA_MAXIMA;

        return mirarColumnes(t, profunditat, col, valor, alfa, beta);
    }
    /**
     * Explora totes les columnes del tauler per trobar el millor moviment següent.
     * @param t El tauler actual.
     * @param profunditat Profunditat actual de recerca.
     * @param col Columna actual a explorar.
     * @param valor Millor valor trobat fins ara.
     * @param alfa Millor valor que el maximizador pot garantir.
     * @param beta Millor valor que el minimizador pot garantir.
     * @return La millor columna per realitzar un moviment.
     */
    private int mirarColumnes(Tauler t, int profunditat, int col, Integer valor, int alfa, int beta) {
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

    /**
     * Avalua l'estat actual del tauler de joc.
     * @param t El tauler de joc actual.
     * @return Puntuació heurística de l'estat actual del tauler.
     */
    private int avaluarTauler(Tauler t) {
        ++jugadesExplorades;
        int res = 0;
        res = mirarFiC(t, res);

        res += avaluarDiagonals(t);

        return mirarHeuristica(res);
    }

    private int mirarFiC(Tauler t, int res) {
        for (int i = t.getMida() - 1; i >= 0; --i) {
            res += avaluarColumna(t, i);
            res += (10 * avaluarFila(t, i) / (i + 1));  // Ponderem segons l'alçada del 4 en ratlla horitzontal
        }
        return res;
    }

    private int mirarHeuristica(int res) {
        if (res >= HEURISTICA_MAXIMA / 2) 
            return HEURISTICA_MAXIMA;
        if (res <= -HEURISTICA_MAXIMA / 2) 
            return -HEURISTICA_MAXIMA;

        return res;
    }


    private int avaluarColumna(Tauler t, int col) {
        Integer first = 0, cont = 0, cont_buides = 0;

        return eC(t, col, first, cont, cont_buides);
    }

    /**
     * Ajuda en l'avaluació de la columna donada.
     * @param t Tauler de joc.
     * @param col Número de la columna a avaluar.
     * @param first Primera fitxa trobada a la columna.
     * @param cont Comptador de fitxes consecutives.
     * @param cont_buides Comptador d'espais buits.
     * @return Puntuació calculada per la columna.
     */
    private int eC(Tauler t, int col, Integer first, Integer cont, Integer cont_buides) {
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
                if (bingo(cont)) {
                    return colorJugador * first * HEURISTICA_MAXIMA;
                }
            } else {
                break;
            }
        }

        return calcH(first, cont, cont_buides);
    }

    private boolean bingo(Integer cont) {
        return cont > 3 || cont < -3;
    }

    private int calcH(Integer first, Integer cont, Integer cont_buides) {
        if (cont == 0 || cont_buides + first * cont < 4)
            return 0;

        return (int) (colorJugador * first * (Math.pow(10.0, first * (cont - first))));
    }

    /**
     * Avalua les files del tauler.
     * @param t Tauler de joc.
     * @param fil Número de la fila a avaluar.
     * @return Puntuació heurística de la fila.
     */
    private int avaluarFila(Tauler t, int fil) {
        int cont_buides = 0, cont = 0, color_actual = 0, res = 0, color_aux = 0;

        return mirarFila(t, fil, cont_buides, cont, color_actual, res, color_aux);
    }

    /**
     * Processa la fila donada per determinar la seva puntuació.
     * @param t Tauler de joc.
     * @param fil Número de la fila a processar.
     * @param cont_buides Comptador d'espais buits.
     * @param cont Comptador de fitxes consecutives.
     * @param color_actual Color de la fitxa actual.
     * @param res Puntuació acumulada.
     * @param color_aux Color auxiliar per a l'avaluació.
     * @return Puntuació calculada per la fila.
     */
    private int mirarFila(Tauler t, int fil, int cont_buides, int cont, int color_actual, int res, int color_aux) {
        for (int i = t.getMida() - 1; i >= 0; --i) {
            int fitxa = t.getColor(fil, i);
            if (fitxa == 0) {
                res = estabuit(res, cont, cont_buides, color_actual, color_aux);
                cont_buides++;
                color_aux = (color_actual != 0) ? color_actual : color_aux;
                color_actual = 0;
            } else {
                res = noestabuit(t, fil, i, res, cont, cont_buides, color_actual, color_aux, fitxa);
                if (res == colorJugador * color_actual * HEURISTICA_MAXIMA) {
                    return res;
                }
                color_actual = (color_actual == 0) ? fitxa : color_actual * -1;
                cont = (fitxa == color_actual) ? ++cont : 1;
                cont_buides = 0;
            }

            if (i == 0 && cont + cont_buides > 3) {
                res += puntuacioH(colorJugador, color_actual, cont - 1);
            }
        }
        return res;
    }

    private int estabuit(int res, int cont, int cont_buides, int color_actual, int color_aux) {
        if (cont + cont_buides > 3) {
            res += puntuacioH(colorJugador, (color_actual != 0) ? color_actual : color_aux, cont - 1);
        }
        return res;
    }

    private int noestabuit(Tauler t, int fil, int i, int res, int cont, int cont_buides, int color_actual, int color_aux, int fitxa) {
        if (fitxa == color_actual) {
            cont++;
            if (cont > 3) {
                return colorJugador * color_actual * HEURISTICA_MAXIMA;
            }
        } else {
            res = estabuit(res, cont, cont_buides, color_actual, color_aux);
        }
        return res;
    }

    private int puntuacioH(int colorJugador, int color, int exponent) {
        return (int) (colorJugador * color * Math.pow(10.0, exponent));
    }

    /**
     * Avalua les diagonals del tauler per a determinar la millor jugada.
     * Aquest mètode examina tant les diagonals ascendents com les descendents
     * i calcula la puntuació basant-se en les oportunitats de connexió.
     *
     * @param t El tauler de joc.
     * @return La puntuació heurística de les diagonals del tauler.
     */
    private int avaluarDiagonals(Tauler t) {
        int res = 0;
        // Evaluate descending diagonals
        for (int col = t.getMida() - 4, fil = 0; fil < t.getMida() - 2; ) {
            res += eD(t, col, fil, true);
            if (col > 0) {
                col--;
            } else {
                fil++;
            }
        }
        // Evaluate ascending diagonals
        for (int col = 3, fil = 0; fil < t.getMida() - 2; ) {
            res += eD(t, col, fil, false);
            if (col < t.getMida() - 1) {
                col++;
            } else {
                fil++;
            }
        }
        return res;
    }

    /**
     * Ajuda en l'avaluació de la diagonal específica del tauler.
     * Calcula la puntuació heurística basada en les seqüències de fitxes
     * i els espais buits en una diagonal específica.
     *
     * @param t El tauler de joc.
     * @param col La columna inicial de la diagonal a avaluar.
     * @param fil La fila inicial de la diagonal a avaluar.
     * @param isDescending Cert si la diagonal és descendent, fals si és ascendent.
     * @return La puntuació heurística de la diagonal específica.
     */
    private int eD(Tauler t, int col, int fil, boolean isDescending) {
        int cont_buides = 0, cont = 0, color_actual = 0, res = 0;
        for (int i = 0; (isDescending ? i + col < t.getMida() : col - i >= 0) && i + fil < t.getMida(); i++) {
            int fitxa = t.getColor(fil + i, isDescending ? col + i : col - i);
            if (fitxa == 0) {
                cont_buides++;
                if (color_actual != 0 && cont + cont_buides > 3) {
                    res += calcH(colorJugador, color_actual, cont);
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
                        res += calcH(colorJugador, color_actual, cont);
                    }
                    color_actual = fitxa;
                    cont = 1;
                    cont_buides = 0;
                }
            }
        }
        if (color_actual != 0 && cont + cont_buides > 3) {
            res += calcH(colorJugador, color_actual, cont);
        }
        return res;
    }

    private int calcH(int colorJugador, int color_actual, int cont) {
        return (int) (colorJugador * color_actual * Math.pow(10.0, cont - 1));
    }

    /**
     * Determina el valor màxim que el jugador pot aconseguir en el tauler donat,
     * considerant els moviments futurs de l'oponent.
     * Utilitza l'algorisme Minimax amb poda Alpha-Beta.
     *
     * @param t El tauler de joc.
     * @param col La columna de l'últim moviment.
     * @param alfa El millor valor que el maximizador pot garantir fins ara.
     * @param beta El millor valor que el minimizador pot garantir fins ara.
     * @param profunditat La profunditat actual de recerca en l'arbre de joc.
     * @return El valor màxim que el jugador pot garantir amb el millor moviment.
     */
    private int valorMaxim(Tauler t, int col, int alfa, int beta, int profunditat) {
        if (t.solucio(col, -colorJugador)) {
            return -HEURISTICA_MAXIMA;
        }

        return profunditat > 0 ? maxV(t, alfa, beta, profunditat) : avaluarTauler(t);
    }

    /**
     * Ajuda en el càlcul del valor màxim en una profunditat específica de l'arbre de joc.
     * Utilitza recursivitat per explorar els moviments futurs i aplica la poda Alpha-Beta.
     *
     * @param t El tauler de joc.
     * @param alfa El millor valor que el maximizador pot garantir fins ara.
     * @param beta El millor valor que el minimizador pot garantir fins ara.
     * @param profunditat La profunditat actual de recerca en l'arbre de joc.
     * @return El valor màxim possible en aquesta profunditat.
     */
    private int maxV(Tauler t, int alfa, int beta, int profunditat) {
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

    /**
     * Determina el valor mínim que l'oponent pot forçar en el tauler donat,
     * considerant els moviments futurs del jugador.
     * Utilitza l'algorisme Minimax amb poda Alpha-Beta.
     *
     * @param t El tauler de joc.
     * @param col La columna de l'últim moviment.
     * @param alfa El millor valor que el maximizador pot garantir fins ara.
     * @param beta El millor valor que el minimizador pot garantir fins ara.
     * @param profunditat La profunditat actual de recerca en l'arbre de joc.
     * @return El valor mínim que l'oponent pot garantir amb el millor moviment.
     */
    private int valorMinim(Tauler t, int col, int alfa, int beta, int profunditat) {
        if (t.solucio(col, colorJugador)) {
            return HEURISTICA_MAXIMA;
        }

        return profunditat > 0 ? eProf(t, alfa, beta, profunditat) : avaluarTauler(t);
    }

    /**
     * Ajuda en el càlcul del valor mínim en una profunditat específica de l'arbre de joc.
     * Utilitza recursivitat per explorar els moviments futurs i aplica la poda Alpha-Beta.
     *
     * @param t El tauler de joc.
     * @param alfa El millor valor que el maximizador pot garantir fins ara.
     * @param beta El millor valor que el minimizador pot garantir fins ara.
     * @param profunditat La profunditat actual de recerca en l'arbre de joc.
     * @return El valor mínim possible en aquesta profunditat.
     */
    private int eProf(Tauler t, int alfa, int beta, int profunditat) {
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