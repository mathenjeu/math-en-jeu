interface IOurPersonnage extends IPersonnage
{
   public function ajouterObjet(id:Number, objectName:String);
   public function modifierPointage(valeur:Number);
   public function modifierArgent(valeur:Number);
   public function removeShopObject(idObject:Number);
   public function putNewShopObject(newId:Number, objet:String);			
   public function getFinish(nb:Number);
   public function setMinigameLoade(bool:Boolean);
   public function getMinigameLoade():Boolean;
   public function setBananaTime(tm:Number);
   public function setBananaState(bool:Boolean);
   public function definirProchainePosition(pt:Point, str:String);
   
}