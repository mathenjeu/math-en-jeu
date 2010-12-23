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

// Collection des fonctions a utiliser. Les enlever depuis autres classes.... 

class UtilsBox
{

	
static function drawToolTip(messInfo:String, mcMovie:MovieClip)
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
static function drawRoundedRectangle(target_mc:MovieClip, boxWidth:Number, boxHeight:Number, 
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


}