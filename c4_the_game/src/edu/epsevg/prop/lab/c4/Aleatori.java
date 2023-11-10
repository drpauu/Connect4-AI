package edu.epsevg.prop.lab.c4;

/**
 * Jugador aleatori
 * "Alea jacta est"
 * @author Profe
 */
public class Aleatori
  implements Jugador, IAuto
{
  private String nom;
  
  public Aleatori()
  {
    nom = "RandomBanzai";
  }
  
  public int moviment(Tauler t, int color)
  {
    int col = (int)(t.getMida() * Math.random());
    while (!t.movpossible(col)) {
      col = (int)(t.getMida() * Math.random());
    }
    return col;
  }
  
  public String nom()
  {
    return nom;
  }
}


