<td align="left" valign="top">
<table width="100%" border="0" cellspacing="0">
    <tr>
        <td align="left" colspan="2"><h2>{$lang.inscription_joueur}</h2></td>
        <!-- <td width="30%" align="center"><h3>{$lang.inscription_etape} {$etape} / 4</h3></td> -->
    </tr>
</table>
<div style="width:90%;align:center" class="hr"></div><p>
<div class="centre">
{if $etape eq 1}
    <form name="inscription" method="post" action="inscription-joueur.php?action=soumettre">
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
        <td><label for="nom">{$lang.sexe}</label></td>
        <td>
			<select length="10" id="sexe" name="sexe">
				<option value="0" {if $sexe eq 0} selected {/if}>{$lang.feminin}&nbsp;&nbsp;&nbsp;</option> 
				<option value="1" {if $sexe eq 1} selected {/if}>{$lang.masculin}&nbsp;&nbsp;&nbsp;</option>
			</select> <span class="asterix">*</span>
		</td>
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
        <td valign="top"><input id="courriel" type="text" name="courriel" size="40" value="{$courriel|lower}"><br>
        <span class="commentaire">{$lang.inscription_info_courriel}</span></td>
    </tr>
    <tr align="left">
        <td><label for="courriel2">{$lang.courriel_confirm}</label></td>
        <td><input id="courriel2" type="text" name="courriel2" size="40" value="{$courriel2}"></td>
    </tr>
    <tr>
        <td colspan="2" align=center><hr><!--<input type=submit value="{$lang.bouton_etape_suivante}" />--></td>
    </tr>
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
    <tr>
    	<td valign="top">
    		{$lang.niveau_scolaire}
    	</td>
    	<td>
    		<table>
    		{foreach name=outer item=subject from=$subjects}
    		<tr>
        	<td>{$subject.subject.name}</td>
        	<td>
            <select name="subject[{$subject.subject.subject_id}]">
            	{foreach key=key item=item from=$subject.levels}
            		{if $subject.selected eq $item.level_id}
              		<option value="{$item.level_id}" selected>{$item.name}</option>
              	{else}
              		<option value="{$item.level_id}">{$item.name}</option>
              	{/if}
            	{/foreach}
            </select>
            &nbsp;<span class="asterix">*</span>
          </td>
        </tr>
      	{/foreach}
      	<tr>
      		<td>
      			<a target="blank" href="http://isef.ntic.org/equivalence.html">{$lang.inscription_equivalence}</a>
        	</td>
      	</tr>
      	</table>
    	</td>
    </tr>
    
    <tr>
    	<td>
    		<center><input type="submit" value="{$lang.bouton_inscription_terminer}"> &nbsp;</center>
    	</td>
    </tr>
    </table>
    </form>
    {if $erreur eq ""}
    <script type="text/javascript" language="JavaScript">
        document.inscription.prenom.focus();
    </script>
    {/if}
{elseif $etape eq 4}
    {$lang.inscription_succes_inscription}
{/if}
</div>
</td>

