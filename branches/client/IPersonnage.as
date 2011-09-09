interface IPersonnage
{
   public function deplacePersonnage();
   public function zoomer(valeur:Number);
   public function translater(la:Number, ha:Number);
   public function obtenirImage():MovieClip;
   public function afficher();
   public function tossBanana();
   public function setBrainiac(bool:Boolean);
   public function getBrainiac():Boolean;
   public function getIdPersonnage():Number;
   public function setIdPersonnage(idP:Number);   
   public function cachePersonnage();
   public function obtenirNom():String;
   public function getReconnectionBrainiacAnimaton(brainiacTime:Number);
   public function obtenirProchainePosition():Point;
   public function slippingBanana();
   public function tossBananaShell(perso:IPersonnage);
   public function correctStateBeforeBanane(adversary:String);
   public function obtenirL():Number;
   public function obtenirC():Number;
   public function removeImage();
   public function modifierPointage(val:Number);
   public function getOnFinish():Boolean;
}