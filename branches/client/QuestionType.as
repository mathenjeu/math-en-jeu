/*******************************************************************
Math en jeu Copyright (C)

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



class QuestionType
{
    // Constante indiquant que le type de la question est multiple choix
    public static const var MULTIPLE_CHOICE_5:String = "MULTIPLE_CHOICE_5";

    // Constante indiquant que le type de la question est multiple choix avec 5 
    public static const var MULTIPLE_CHOICE:String = "MULTIPLE_CHOICE";

    // Constante indiquant que le type de la question est vrai ou faux
    public static const var TRUE_FALSE:String = "TRUE_OR_FALSE";
	
	 // Constante indiquant que le type de la question est reponse court
    public static const var SHORT_ANSWER:String = "SHORT_ANSWER";
	
	 // Constante indiquant que le type de la question est vrai ou faux
    public static const var MINI_DOKU:String = "MINI_DOKU";
}