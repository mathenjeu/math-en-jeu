package ClassesUtilitaires;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Banane;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Boule;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Livre;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Papillon;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.PotionGros;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.PotionPetit;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Telephone;

public class ObjectFactory {

  public static ObjetUtilisable createUsableObject(int pId, String pName) {
    ObjetUtilisable lResult = null;

    if(pName.equals("Livre")) {
      lResult = new Livre(pId, true);
    } else if(pName.equals("Papillon")) {
      lResult = new Papillon(pId, true);
    }else if(pName.equals("Boule")){
      lResult = new Boule(pId, true);
    }else if(pName.equals("Telephone")){
      lResult = new Telephone(pId, true);
    }else if(pName.equals("PotionGros")){
      lResult = new PotionGros(pId, true);
    }else if(pName.equals("PotionPetit")){
      lResult = new PotionPetit(pId, true);
    }else if(pName.equals("Banane")){
      lResult = new Banane(pId, true);
    }

    return lResult;
  }
}

