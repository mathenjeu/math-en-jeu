/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Générale Affero (AGPL), telle que publiée par
Affero Inc. ; soit la version 1 de la Licence, ou (à
votre discrétion) une version ultérieure quelconque.

Ce programme est distribué dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans même une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Générale Affero pour plus de détails.

Vous devriez avoir reçu un exemplaire de la Licence Publique
Générale Affero avec ce programme; si ce n'est pas le cas,
écrivez à Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/
// Collection des fonctions statiques a utiliser. Les enlever depuis autres classes.... 
import flash.filters.ColorMatrixFilter;

class UtilsBox
{	
   function UtilsBox()
   {
	   
   }
   public static function drawToolTip(messInfo:String, mcMovie:MovieClip)
   {
	  var stringLength:Number = messInfo.length;
	  var wid:Number = Math.floor(stringLength / 20 * 16);
	  _level0.loader.contentHolder.createEmptyMovieClip("toolTip", _level0.loader.contentHolder.getNextHigesthDepth());
	  _level0.loader.contentHolder.toolTip.swapDepths(mcMovie);
	  drawRoundedRectangle(_level0.loader.contentHolder.toolTip, 120, wid + 10, 15, 0xFFEB5B, 100);
	  _level0.loader.contentHolder.toolTip.createTextField("toolTipMessage", 60, 5, 3, 110, wid);
	
	  // Make the field an label text field
      _level0.loader.contentHolder.toolTip.toolTipMessage.type = "dynamic";
      _level0.loader.contentHolder.toolTip.toolTipMessage.setStyle("fontSize", "2");
      with(_level0.loader.contentHolder.toolTip.toolTipMessage)
      {
	       multiline = true;
	       background = false;
	       text = messInfo;
	       textColor = 0x330000;
	       border = false;
	       _visible = true;
	       //autoSize = true;
		   wordWrap = true;
	       autoSize = "left";
		   maxChars = 70;
      }
	  _level0.loader.contentHolder.toolTip._visible = false;	
   }// end method
   
   // modified code from source - www.adobe.com
   public static function drawRoundedRectangle(target_mc:MovieClip, boxWidth:Number, boxHeight:Number, 
							cornerRadius:Number, fillColor:Number, fillAlpha:Number):Void {
      with (target_mc) {
		
		lineStyle(2, 0x000000, 100);

        beginFill(fillColor, fillAlpha);
        moveTo(cornerRadius, 0);
        lineTo(boxWidth - cornerRadius, 0);
        curveTo(boxWidth, 0, boxWidth, cornerRadius);
        lineTo(boxWidth, cornerRadius);
        lineTo(boxWidth, boxHeight - cornerRadius);
        curveTo(boxWidth, boxHeight, boxWidth - cornerRadius, boxHeight);
        lineTo(boxWidth - cornerRadius, boxHeight);
        lineTo(cornerRadius, boxHeight);
        curveTo(0, boxHeight, 0, boxHeight - cornerRadius);
        lineTo(0, boxHeight - cornerRadius);
        lineTo(0, cornerRadius);
        curveTo(0, 0, cornerRadius, 0);
        lineTo(cornerRadius, 0);
        endFill();
    }
  }//end function
  
  // used to calculate the id of the players picture to show on the game board
  public static function calculatePicture(perso:Number):Number
  {
	 return ((perso-10000)-(perso-10000)%100)/100;
  }  
  
  // used to calculate the filter for our persos
  // this take out the delay in coloring our pictures
  // we need as parameters the color to color it and the id of the picture
  // because each picture has his initial parameters
  public static function colorMatrixPerso(col:String, idD:Number):ColorMatrixFilter
  {
  
	   // to obtain RGB values of our color
       var rr:Number = Number("0x" + col.substr(2,2).toString(10));
       var gg:Number = Number("0x" + col.substr(4,2).toString(10));
       var bb:Number = Number("0x" + col.substr(6,2).toString(10));
	   
	   var angle:Number;

      trace("rr : " + rr + " gg : " + gg + " bb : " + bb);

      // to obtain the multipliers
      // the RGB of base color of perso1 is 245,64,75
      switch(idD)
      {
           case 1:
			     rr = rr/255/0.96;  // to take in consideration the base color of the movie
                 gg = gg/255/0.251;
                 bb = bb/255/0.294;
				 //angle = 0;
                //trace("Choix de la dessin 1"); // 245,64,75
            break;
            
			 case 2:
			     rr = rr/255/0.169;
                 gg = gg/255/0.741;
                 bb = bb/255/0.373;
				 //angle = 30;
				//trace("Choix de la dessin 2"); // 43,189/95
            break;
			
			 case 3:
                 rr = rr/255/0.741;
                 gg = gg/255/0.537;
                 bb = bb/255/0.165;
				 //angle = 60;
                //trace("Choix de la dessin 3"); // 189,137,42
            break;
			
			case 4:
                rr = rr/255/0.188;
                gg = gg/255/0.584;
                bb = bb/255/0.29;
				//angle = 90;
                //trace("Choix de la dessin 4"); // 48,149,74
            break;
			
			 case 5:
                rr = rr/255/0.27;
                gg = gg/255/0.314;
                bb = bb/255/0.53;
				//angle = 120;
                //trace("Choix de la dessin 5");  // 69,80,136
            break;
			
			 case 6:
                 rr = rr/255/0.4;
                 gg = gg/255/0.2;
                 bb = bb/255/0.6;
				 //angle = 150;
                //trace("Choix de la dessin 6"); // 102,51,153
            break;
			
			 case 7:
                 rr = rr/255/0.059;
                 gg = gg/255/0.53;
                 bb = bb/255/0.204;
				 //angle = 180;
                //trace("Choix de la dessin 7"); // 15,136,52
            break;
			
			 case 8:
                 rr = rr/255;
                 gg = gg/255;
                 bb = bb/255;
				 //angle = 210;
                //trace("Choix de la dessin 8");  // 255.255.255
            break;
			
			
			 case 9:
                 rr = rr/255/0.3;
                 gg = gg/255/0.588;
                 bb = bb/255/0.29;
				 //angle = 240;
                //trace("Choix de la dessin 9");  //  79.150.74
            break;
			
			 case 10:
                 rr = rr/255;
                 gg = gg/255/0.12;
                 bb = bb/255/0.12;
				 //angle = 270;
                //trace("Choix de la dessin 10");  // 255.31.31 
            break;
			
			case 11:
                 rr = rr/255;
                 gg = gg/255;
                 bb = bb/255;
				 //angle = 300;
                //trace("Choix de la dessin 11");   // 255.255.255
            break;
			
			case 12:
                 rr = rr/255/0.843;
                 gg = gg/255/0.019;
                 bb = bb/255/0.0118;
				 //trace("Choix de la dessin 12");   //  215.5.3
            break;
			
            default:
                trace("Erreur Inconnue colors utils box");
     }
   
   	   trace("rr : " + rr + " gg : " + gg + " bb : " + bb);


     var matrix:Array = new Array();
     matrix = matrix.concat([rr, 0, 0, 0, 0]); // red
     matrix = matrix.concat([0, gg, 0, 0, 0]); // green
     matrix = matrix.concat([0, 0, bb, 0, 0]); // blue
     matrix = matrix.concat([0, 0, 0, 1, 0]); // alpha
		 
	var filterC:ColorMatrixFilter = new ColorMatrixFilter(matrix);
	 
	return filterC;
	  
  } //end method
  
  // coloring with ColorMatrixFilter a movie
 public static function colorItMatrix(clothesCol:String, mov:MovieClip, idD:Number)
 {
     // to obtain RGB values of our color
       var rr:Number = Number("0x" + clothesCol.substr(2,2).toString(10));
       var gg:Number = Number("0x" + clothesCol.substr(4,2).toString(10));
       var bb:Number = Number("0x" + clothesCol.substr(6,2).toString(10));
	   
	   trace("rr : " + rr + " gg : " + gg + " bb : " + bb);

      // to obtain the multipliers
      // the RGB of base color of perso1 is 245,64,75
      switch(idD)
      {
           case 1:
			     rr = rr/255/0.96;  // to take in consideration the base color of the movie
                 gg = gg/255/0.251;
                 bb = bb/255/0.294;
				 //trace("Choix de la dessin 1"); // 245,64,75
            break;
            
			 case 2:
			     rr = rr/255/0.169;
                 gg = gg/255/0.741;
                 bb = bb/255/0.373;
				 //trace("Choix de la dessin 2"); // 43,189/95
            break;
			
			 case 3:
                  rr = rr/255/0.741;
                  gg = gg/255/0.537;
                  bb = bb/255/0.165;
				  //trace("Choix de la dessin 3"); // 189,137,42
            break;
			
			 case 4:
                rr = rr/255/0.188;
                gg = gg/255/0.584;
                bb = bb/255/0.29;
				//trace("Choix de la dessin 4"); // 48,149,74
            break;
			
			 case 5:
                rr = rr/255/0.27;
                gg = gg/255/0.314;
                bb = bb/255/0.53;
				//trace("Choix de la dessin 5");  // 69,80,136
            break;
			
			 case 6:
                 rr = rr/255/0.4;
                 gg = gg/255/0.2;
                 bb = bb/255/0.6;
				 //trace("Choix de la dessin 6"); // 102,51,153
            break;
			
			 case 7:
                 rr = rr/255/0.059;
                 gg = gg/255/0.53;
                 bb = bb/255/0.204;
				 //trace("Choix de la dessin 7"); // 15,136,52
            break;
			
			 case 8:
                 rr = rr/255;
                 gg = gg/255;
                 bb = bb/255;
				 //trace("Choix de la dessin 8");  // 255.255.255
            break;
			
			
			 case 9:
                 rr = rr/255/0.3;
                 gg = gg/255/0.588;
                 bb = bb/255/0.29;
				 //trace("Choix de la dessin 9");  //  79.150.74
            break;
			
			 case 10:
                 rr = rr/255;
                 gg = gg/255/0.12;
                 bb = bb/255/0.12;
				 //trace("Choix de la dessin 10");  // 255.0.0 
            break;
			
			case 11:
                 rr = rr/255;
                 gg = gg/255;
                 bb = bb/255;
				 //trace("Choix de la dessin 11");   // 255.255.255
            break;
			
			case 12:
                 rr = rr/255/0.843;
                 gg = gg/255/0.019;
                 bb = bb/255/0.0118;
				 //trace("Choix de la dessin 12");   //  215.5.3
            break;
			
            default:
                trace("Erreur Inconnue colors");
     }
   
   	   trace("rr : " + rr + " gg : " + gg + " bb : " + bb);


     var matrix:Array = new Array();
     matrix = matrix.concat([rr, 0, 0, 0, 0]); // red
     matrix = matrix.concat([0, gg, 0, 0, 0]); // green
     matrix = matrix.concat([0, 0, bb, 0, 0]); // blue
     matrix = matrix.concat([0, 0, 0, 1, 0]); // alpha
	
	
	
	
   var filterC:ColorMatrixFilter = new ColorMatrixFilter(matrix);
   //trace("filter: " + filter.matrix);
   
   mov.filters = new Array(filterC);
 }// end method
 /*
 // modified code from source - www.adobe.com
 public static function drawRoundedRectangle(target_mc:MovieClip, boxWidth:Number, boxHeight:Number, cornerRadius:Number, fillColor:Number, fillAlpha:Number):Void {
    with (target_mc) {
		
		lineStyle(2, 0x000000, 100);

        beginFill(fillColor, fillAlpha);
        moveTo(cornerRadius, 0);
        lineTo(boxWidth - cornerRadius, 0);
        curveTo(boxWidth, 0, boxWidth, cornerRadius);
        lineTo(boxWidth, cornerRadius);
        lineTo(boxWidth, boxHeight - cornerRadius);
        curveTo(boxWidth, boxHeight, boxWidth - cornerRadius, boxHeight);
        lineTo(boxWidth - cornerRadius, boxHeight);
        lineTo(cornerRadius, boxHeight);
        curveTo(0, boxHeight, 0, boxHeight - cornerRadius);
        lineTo(0, boxHeight - cornerRadius);
        lineTo(0, cornerRadius);
        curveTo(0, 0, cornerRadius, 0);
        lineTo(cornerRadius, 0);
        endFill();
    }
}//end function*/


} // end class 