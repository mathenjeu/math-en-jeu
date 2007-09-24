<td class="centre" width="90%" valign="top" align="left">
<div class="centre">
{if $action eq "ajout"}
    <h2><h2>{$lang.ajout_sondage}</h2></h2>
{else}
    <h2>{$lang.modification_sondage}</h2>
{/if}

{if $action eq "ajout"}
    <form name="sondage" method="POST" action="admin_sondage.php?action=insertSondage&amp;nbChoix={$nbChoix}">
{else}
    <form name="sondage" action="admin_sondage.php?action=doModificationSondage&amp;nbChoix={$nbChoix}&amp;cleSondage={$cle}" method="POST">
{/if}

<table class="tableau_admin" border="0" align="left">
    <tr>
        <td class="erreur" colspan="2">{$erreur}</td>
    </tr>
    <tr>
        <td>{$lang.date}</td>
        <td><input type="text" readonly="readonly" name="date" value="{$dateLongue}">
    </tr>
    <tr>
        <td>{$lang.question_sondage}</td>
        <td><input size="50" type="text" name="sondage" value="{$sondage}"></td>
    </tr>
    <tr>
        <td>{$lang.destinataire}</td>
        <td>
            <select name="destinataire">
                <option value="0" {$selected0}>{$lang.type_destinataire.0}</option>
                <option value="1" {$selected1}>{$lang.type_destinataire.1}</option>
                <option value="2" {$selected2}>{$lang.type_destinataire.2}</option>
            </select>
        </td>
    </tr>
    <tr>
	  <td>{$lang.mod_joueur_lang}</td>
	  <td>
	      	<select name="langue" style="width: 100px;">
	      		{if $langue eq 0}
	        		<option value="0" selected>{$lang.francais}</option>
	        		<option value="1">{$lang.anglais}</option>
	        	{else}
	        		<option value="0">{$lang.francais}</option>
	        		<option value="1" selected>{$lang.anglais}</option>
	        	{/if}
	      	</select>
	      </td>
	</tr>
    {section name=reponse loop=$reponse}
        <tr>
            <td>{$lang.choix} {$smarty.section.reponse.index+1}</td>
            <td><input size="40" type=text name="choix{$smarty.section.reponse.index+1}" value="{$reponse[reponse]}"></td>
        </tr>
    {/section}
    <tr>
    {if $action eq "ajout"}
        <td><input type="button" value="{$lang.ajout_choix}" onClick="document.sondage.action='admin_sondage.php?action=ajoutSondage&amp;nbChoix={$nbChoix+1}';document.sondage.submit();"></td>
        <td><input type="button" value="{$lang.enlever_choix}" onClick="document.sondage.action='admin_sondage.php?action=ajoutSondage&amp;nbChoix={$nbChoix-1}';document.sondage.submit();"></td>
    {else}
    	  <td><input type="button" value="{$lang.ajout_choix}" onClick="document.sondage.action='admin_sondage.php?action=modificationSondage&amp;nbChoix={$nbChoix+1}&amp;cleSondage={$cle}';document.sondage.submit();"></td>
        <td><input type="button" value="{$lang.enlever_choix}" onClick="document.sondage.action='admin_sondage.php?action=modificationSondage&amp;nbChoix={$nbChoix-1}&amp;cleSondage={$cle}';document.sondage.submit();"></td>
    {/if}
    </tr>
    <tr>
        <td colspan="2" align="center"><input type="submit" value="{$lang.bouton_envoyer}"></td>
    </tr>
</table>
</form>
</div>
</td>
