<td align="left" valign="top">
<table width="100%" border="0" cellspacing="0">
    <tr>
        <td align="left" colspan="2"><h2>{$lang.inscription_joueur}</h2></td>
        <td width="30%" align="center"><h3>{$lang.inscription_etape} {$etape} / 4</h3></td>
    </tr>
</table>
<div style="width:90%;align:center" class="hr"></div><p>
<div class="centre">
{if $etape eq 1}
    <form name="inscription" method="post" action="inscription-joueur.php?action=etape2#top">
    <table width="80%" border="0" cellspacing="5">
    <tr>
        <td align="left" colspan="2">{$lang.inscription_joueur_texte}</td>
    </tr>
    <tr>
        <td colspan="2" align="left"><h2 id="top">{$lang.inscription_donnee_personnel}</h2></td>
    </tr>
    <tr>
    	<td align="left" colspan="2">{$lang.inscription_1_joueur_info}</td>
    </tr>
    <tr>
    	<td align="left" colspan="2" class="commentaire">{$lang.inscription_asterix_info}</td>
    </tr>
    <tr>
		<td align="left" colspan="2" class="erreur">{$erreur}</td>
	</tr>
    <tr align="left">
        <td width="15%"><label for="prenom">{$lang.prenom}</label></td>
        <td><input id="prenom" type="text" name="prenom" size="20" value="{$prenom}"> <span class="asterix">*</span></td>
    </tr>
    <tr align="left">
        <td><label for="nom">{$lang.nom}</label></td>
        <td><input id="nom" type="text" name="nom" size="20" value="{$nom}"> <span class="asterix">*</span></td>
    </tr>
    <tr align="left">
        <td><label for="ville">{$lang.ville}</label></td>
        <td><input id="ville" type="text" name="ville" size="20" value="{$ville}"> <span class="asterix">*</span></td>
    </tr>
    <tr align="left">
        <td><label for="province">{$lang.province}</label></td>
        <td><input id="province" type="text" name="province" size="20" value="{$province}"> <span class="asterix">*</span></td>
    </tr>
    <tr align="left">
        <td><label for="pays">{$lang.pays}</label></td>
        <td><input id="pays" type="text" name="pays" size="20" value="{$pays}"> <span class="asterix">*</span></td>
    </tr>
    <tr align="left">
        <td valign="top"><label for="courriel">{$lang.courriel}</label></td>
        <td valign="top"><input id="courriel" type="text" name="courriel" size="40" value="{$courriel|lower}"> <span class="asterix">*</span><br>
        <span class="commentaire">{$lang.inscription_info_courriel}</span></td>
    </tr>
    <tr align="left">
        <td><label for="courriel2">{$lang.courriel_confirm}</label></td>
        <td><input id="courriel2" type="text" name="courriel2" size="40" value="{$courriel2}"> <span class="asterix">*</span></td>
    </tr>
    <tr>
        <td colspan="2" align=center><input type=submit value="{$lang.bouton_etape_suivante}" /></td>
    </tr>
    </table>
    </form>
    {if $erreur eq ""}
    <script type="text/javascript" language="JavaScript">
        document.inscription.prenom.focus();
    </script>
    {/if}
{elseif $etape eq 2}
    <form name="inscription" method="post" action="inscription-joueur.php?action=etape3#2">
    <table width="100%" border="0" cellspacing="5">
    <tr>
        <td colspan="2"><h2 id="2">{$lang.inscription_joueur_profil}</h2></td>
    </tr>
    <tr align="left">
    	<td colspan="2">{$lang.inscription_2_joueur_info}</td>
    </tr>
    <tr align="left">
    	<td colspan="2" class="commentaire">{$lang.inscription_asterix_info}</td>
    </tr>
    </table>
    <table align="left" width="100%" border="0" cellspacing="5">
    <tr align="left">
        <td colspan="2" class="erreur">{$erreur}</td>
    </tr>
    <tr align="left">
        <td>{$lang.alias}</td>
        <td><input type="text" name="alias" size="20" value="{$alias}"> <span class="asterix">*</span>
            {if $suggestion_alias ne ""}
            {$lang.inscription_l_alias}
            <font color=#0E45AB>{$suggestion_alias}</font>
            {$lang.inscription_est_disponible}
            {/if}
            <br>
            <span class="commentaire">{$lang.inscription_alias_info}</span>
        </td>
    </tr>
    <tr align="left">
        <td width="15%">{$lang.mot_passe}</td>
        <td><input type="password" name="motDePasse" size="20" maxlength="20" value="{$motDePasse}"> <span class="asterix">*</span>
        	<br><span class="commentaire">{$lang.inscription_mot_de_passe_info}</span>
        </td>
    </tr>
    <tr align="left">
        <td>{$lang.mot_passe_confirm}</td>
        <td><input type="password" name="motDePasse2" size="20" maxlength="20" value="{$motDePasse2}"> <span class="asterix">*</span></td>
    </tr>
    <tr align="left">
        <td>{$lang.niveau_scolaire}</td>
        <td>
        <select name="niveau" style="width: 200px;"
            onChange='document.inscription.action="inscription-joueur.php?action=etablissement";
            document.inscription.submit()'>
        {html_options values=$niveauID output=$niveauTexte selected=$niveau}
        </select>
        <span class="asterix">*</span>
        </td>
    </tr>
    {if $niveau ne 14}
    <tr align="left">
        <td>{$lang.etablissement}</td>
        <td>
        <select name="etablissement" STYLE="width: 400px">
            {html_options values=$etablissementID output=$etablissementTexte selected=$etablissement}
        </select>
        </td>
    </tr>
    <tr align="left">
		<!--<td>{$lang.inscription_alias_prof}</td>-->
	    <td><input type="hidden" name="aliasProf" value="{$aliasProf}"/><br>
	    <!--<span class="commentaire">{$lang.inscription_alias_prof_info}--></td>
	</tr>
	{/if}
    <tr>
        <td colspan="2" align="center"><input type="submit" value="{$lang.bouton_etape_suivante}" /></td>
    </tr>
    </table>
    </form>
    {if $erreur eq ""}
	    <script type="text/javascript" language="JavaScript">
	        document.inscription.alias.focus();
	    </script>
	 {/if}
{elseif $etape eq 3}
    <form name="inscription" method="post" action="inscription-joueur.php?action=soumettre">
    <table width="100%" border="0" cellspacing="5">
    <tr>
        <td colspan=3><h2 id="3">{$lang.inscription_quelques_questions}</h2></td>
    </tr>
    <tr>
        <td colspan=3>{$lang.inscription_quelques_questions_info}</td>
    </tr>
    <tr>
        <td colspan=3>
        <p class="question">{$lang.inscription_math_aime}</p>
        <blockquote>
        {$lang.inscription_math_deteste}&nbsp;
        <input type="radio" name="aimeMaths" value="1"> 1
        <input type="radio" name="aimeMaths" value="2"> 2
        <input type="radio" name="aimeMaths" value="3" checked="checked"> 3
        <input type="radio" name="aimeMaths" value="4"> 4
        <input type="radio" name="aimeMaths" value="5"> 5
        &nbsp; {$lang.inscription_math_adore}
        </blockquote>

        <p class="question">{$lang.inscription_math_considere}</p>
        <blockquote>
        {$lang.inscription_math_difficulte}&nbsp;
        <input type="radio" name="mathConsidere" value="1"> 1
        <input type="radio" name="mathConsidere" value="2"> 2
        <input type="radio" name="mathConsidere" value="3" checked="checked"> 3
        <input type="radio" name="mathConsidere" value="4"> 4
        <input type="radio" name="mathConsidere" value="5"> 5
        &nbsp; {$lang.inscription_math_excellent}
        </blockquote>

        <p class="question">{$lang.inscription_math_etudie}</p>
        <blockquote>
        {$lang.inscription_math_jamais}&nbsp;
        <input type="radio" name="mathEtudie" value="1"> 1
        <input type="radio" name="mathEtudie" value="2"> 2
        <input type="radio" name="mathEtudie" value="3" checked="checked"> 3
        <input type="radio" name="mathEtudie" value="4"> 4
        <input type="radio" name="mathEtudie" value="5"> 5
        &nbsp;{$lang.inscription_math_certainement}
        </blockquote>

        <p class="question">{$lang.inscription_math_decouvert}</p>
        <blockquote>
        <input type="radio" name="mathDecouvert" value="1"> {$lang.inscription_math_moteur}<br>
        <input type="radio" name="mathDecouvert" value="2"> {$lang.inscription_math_prof}<br>
        <input type="radio" name="mathDecouvert" value="3"> {$lang.inscription_math_pub}<br>
        <input type="radio" name="mathDecouvert" value="4"> {$lang.inscription_math_recommendation}<br>
        <input type="radio" name="mathDecouvert" value="5" checked="checked"> {$lang.inscription_math_autre}<br>
        </blockquote>

        <center>
        <input type="submit" value="{$lang.bouton_inscription_terminer}"> &nbsp;
        </center>
        </td>
    </tr>
    </table>
    </form>
{elseif $etape eq 4}
    {$lang.inscription_succes_inscription}
{/if}
</div>
</td>

