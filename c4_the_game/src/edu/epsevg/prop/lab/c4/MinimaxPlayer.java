package edu.epsevg.prop.lab.c4;

public class MinimaxPlayer 
        implements Jugador , IAuto {
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
        nomJugador = "JugadorMinimax(" + profunditat + ")";
    }

    public MinimaxPlayer(int profunditat, boolean estats) {
        this(profunditat);
        this.mostrarEstadistiques = estats;
    }

    @Override
    public String nom() {
        return nomJugador;
    }

    @Override
    public int moviment(Tauler tauler, int colorJugador) {
        this.colorJugador = colorJugador;
        if (mostrarEstadistiques) {
            jugadesExplorades = 0;
            jugadesReals++;
            long tempsInici = System.currentTimeMillis();
            int columna = calcularMinimax(tauler, profunditatRecerca);
            long tempsFinal = System.currentTimeMillis();

            double tempsUtilitzat = (tempsFinal - tempsInici) / 1000.0;
            tempsAcumulat += tempsUtilitzat;
            System.out.println("Columna escollida: " + columna + " després d'explorar " + jugadesExplorades + " jugades.");
            System.out.printf("Temps utilitzat per decidir: %.3f segons.%n", tempsUtilitzat);
            System.out.printf("Temps mitjà per jugada: %.4f segons.%n", tempsAcumulat / jugadesReals);

            return columna;
        } else {
            return calcularMinimax(tauler, profunditatRecerca);
        }
    }

    private int calcularMinimax(Tauler t, int profunditat){
        int col = 0;
        Integer valor = -HEURISTICA_MAXIMA-1;
        int alfa = -HEURISTICA_MAXIMA;
        int beta = HEURISTICA_MAXIMA;
        for (int i = 0; i < t.getMida(); ++i){
            if(t.movpossible(i)){
                Tauler aux = new Tauler(t);
                aux.afegeix(i,colorJugador); //Cal fer un tauler auxiliar cada cop
                int min = valorMinim(aux, i, alfa, beta, profunditat-1);
                if (valor < min){
                    col = i;
                    valor = min;
                }
                if (beta < valor){
                    return col;
                }
                alfa = Math.max(valor,alfa);
            }
        }
        return col;
    }

    private int avaluarTauler(Tauler t) {
        // Pre: 
        // Post: retorna l'heuristica del tauler t segons la definició de la documentació.
        ++jugadesExplorades;
        int res = 0;
        for (int i = t.getMida()-1; i >= 0; --i){
            res+=avaluarColumna(t,i);
            res+=(10*avaluarFila(t,i)/(i+1));  // Ponderem segons l'alçada del 4 en ratlla horitzontal
            // Casos en que es guanya/perd:
            //MAX és suficientment gran com per a garantir que la seva meitat sempre serà major que l'heurística trobada
            // en un tauler de "mida mitjanament normal" així doncs evitem el no entrar en aquest if per motius de que 
            // si hem trobat primer una heurística de -100 i després una de MAX (obtenint com a resultat MAX-100)
            // llavors en aquest cas, si possesim res > MAX, llavors no funcionaria. Amb MAX/2, sí.
            if(res >= HEURISTICA_MAXIMA/2) 
                return HEURISTICA_MAXIMA;
            if(res <= -HEURISTICA_MAXIMA/2) 
                return -HEURISTICA_MAXIMA;
        }
        return res+avaluarDiagonals(t);
    }

    private int avaluarColumna(Tauler t, int col) {
        // Codi per a l'avaluació d'una columna específica
        //Pre: 
    //Post: Retorna l'heuristica vertical per a columna col (veure documentacio per mes detalls)
        Integer first = 0, cont = 0, cont_buides = 0;
        for(int i = t.getMida()-1; i >= 0; --i){
            // Comprovem si no hi ha fitxa i distinguim dos casos
            if(first==0){
                // Si no hi ha fitxa, "first" seguira sent 0 (fins que trobi una fitxa).
                first = t.getColor(i, col);
                if(first != 0)
                    cont += first;
                else
                    cont_buides += 1;
            }else{
                int fitxa = t.getColor(i, col);
                if(first == fitxa) 
                    cont += fitxa;
                else 
                    break;
                if(cont > 3 || cont < -3){
                    //Aixo vol dir que hem 4 en ratlla vertical!
                    return colorJugador*first*HEURISTICA_MAXIMA;
                }
            }
        }
        if(cont == 0 || cont_buides+first*cont<4)
            return 0;
        
        return (int)(colorJugador*first*(Math.pow(10.0,first*(cont-first))));
    }

    private int avaluarFila(Tauler t, int fil){
    //Pre: 
    //Post: Retorna l'heuristica horitzonatl per a fila fil (veure documentacio per mes detalls)
        int cont_buides = 0,cont = 0,color_actual = 0,res = 0,color_aux = 0;
        for(int i = t.getMida()-1; i >= 0; --i){
            int fitxa = t.getColor(fil, i);
            // Comprovem si no hi ha fitxa i distinguim dos casos
            if(fitxa == 0){
                if(color_actual != 0){
                    if(cont+cont_buides>3){
                        res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
                    }
                        color_aux = color_actual;
                        color_actual = 0;
                        cont_buides = 1;
                }else{
                    ++cont_buides;
                    if(cont+cont_buides>3){
                        res+=(int)(colorJugador*color_aux*(Math.pow(10.0,cont-1)));
                        color_aux = 0;
                    }
                }
            }else{
                // Ja hem trobat una fitxa a la fila. Sumem.
                if(fitxa == color_actual){
                    ++cont;
                    if(cont>3)
                        return colorJugador*color_actual*HEURISTICA_MAXIMA;
                }
                else if(color_actual == 0){
                    color_actual = fitxa;
                    cont = 1;
                }else{
                    if(cont+cont_buides>3){
                        res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
                    }
                        color_actual *=-1;
                        cont = 1;
                        cont_buides = 0;
                }
            }
            if(i == 0 && cont+cont_buides>3){
                res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
            }
        }
        return res;
    }

    private int avaluarDiagonals(Tauler t){
    //Pre: 
    //Post: Retorna l'heuristica diagonal pel tauler sencer (veure documentacio per mes detalls)
        int col = t.getMida()-4, fil = 0, res = 0;
        while(fil < t.getMida()-2){
            int cont_buides = 0,cont = 0,color_actual = 0,color_aux = 0;
            for(int i = 0; i+col < t.getMida() && i+fil < t.getMida(); ++i){
                int fitxa = t.getColor(fil+i, col+i);
                if(fitxa == 0){
                    if(color_actual != 0){
                        if(cont+cont_buides>3){
                            res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_aux = color_actual;
                            color_actual = 0;
                            cont_buides = 1;
                    }else{
                        ++cont_buides;
                        if(cont+cont_buides>3){
                            res+=(int)(colorJugador*color_aux*(Math.pow(10.0,cont-1)));
                            color_aux = 0;
                        }
                    }
                }else{
                    if(fitxa == color_actual){
                        ++cont;
                        if(cont>3)
                            return colorJugador*color_actual*HEURISTICA_MAXIMA;
                    }
                    else if(color_actual == 0){
                        color_actual = fitxa;
                        cont = 1;
                    }else{
                        if(cont+cont_buides>3){
                            res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_actual *=-1;
                            cont = 1;
                            cont_buides = 0;
                    }
                }
            }
            if(cont+cont_buides>3){
                    res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
            }
            if(col>0)
                --col;
            else
                ++fil;
        }
        col = 3;
        fil = 0;
        while(fil<t.getMida()-2){
            int cont_buides = 0,cont = 0,color_actual = 0,color_aux = 0;
            for(int i = 0; col-i > 0 && i+fil < t.getMida(); ++i){
                int fitxa = t.getColor(fil+i, col-i);
                if(fitxa == 0){
                    if(color_actual != 0){
                        if(cont+cont_buides>3){
                            res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_aux = color_actual;
                            color_actual = 0;
                            cont_buides = 1;
                    }else{
                        ++cont_buides;
                        if(cont+cont_buides>3){
                            res+=(int)(colorJugador*color_aux*(Math.pow(10.0,cont-1)));
                            color_aux = 0;
                        }
                    }
                }else{
                    if(fitxa == color_actual){
                        ++cont;
                        if(cont>3)
                            return colorJugador*color_actual*HEURISTICA_MAXIMA;
                    }
                    else if(color_actual == 0){
                        color_actual = fitxa;
                        cont = 1;
                    }else{
                        if(cont+cont_buides>3){
                            res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_actual *=-1;
                            cont = 1;
                            cont_buides = 0;
                    }
                }
            }
            if(cont+cont_buides>3){
                    res+=(int)(colorJugador*color_actual*(Math.pow(10.0,cont-1)));
            }
            ++col;
            if(col==t.getMida()){
                --col;
                ++fil;
            }
        }
        return res;
    }

    private int valorMaxim(Tauler t, int col, int alfa, int beta, int profunditat){
        if(t.solucio(col, -colorJugador))
            return -HEURISTICA_MAXIMA;
        if(profunditat > 0){
            Integer valor = -HEURISTICA_MAXIMA-1;
            for (int i = 0; i < t.getMida(); ++i){
                if(t.movpossible(i)){
                    Tauler aux = new Tauler(t);
                    aux.afegeix(i,colorJugador);
                    valor = Math.max(valor, valorMinim(aux,i, alfa, beta, profunditat-1));
                    if (beta < valor){
                        return valor;
                    }
                    alfa = Math.max(valor,alfa);
                }
            }
            return valor;
        }else{
            return avaluarTauler(t);
        }
        
    }

    private int valorMinim(Tauler t, int col, int alfa, int beta, int profunditat){
        if(t.solucio(col, colorJugador))   
            return HEURISTICA_MAXIMA;
        if(profunditat > 0){
            Integer valor = HEURISTICA_MAXIMA-1;
            for (int i = 0; i < t.getMida(); ++i){
                if(t.movpossible(i)){
                    Tauler aux = new Tauler(t);
                    aux.afegeix(i,-colorJugador);
                    valor = Math.min(valor, valorMaxim(aux,i, alfa, beta, profunditat-1));
                    if (valor < alfa){
                        return valor; 
                    }
                    beta = Math.min(valor,beta);
                }
            }
            return valor;
        }
        else{
            return avaluarTauler(t);
        }
    }

}
