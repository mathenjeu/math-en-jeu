<td class="centre" width="90%" valign="top" align="left">
<div class="centre">
{if $action eq "ajout"}
    <h2>{$lang.ajout_nouvelle}</h2>
{else}
    <h2>{$lang.modification_nouvelle}</h2>
{/if}

{if $action eq "ajout"}
    <form name="nouvelle" action="admin_nouvelles.php?action=insertNouvelle" method="POST">
{else}
    <form name="nouvelle" action="admin_nouvelles.php?action=doUpdateNouvelle&amp;cleNouvelle={$cle}" method="POST">
{/if}
<table class="tableau_admin" align="left">
    <tr>
        <td>
        </td>
    </tr>
    <tr>
        <td colspan="3" class="erreur">{$erreur}</td>
    <tr>
        <td>{$lang.date}</td>
        <td><input type="text" readonly="readonly" name="date" value="{$dateLongue}">
    </tr>
    <tr>
        <td>{$lang.titre}</td>
        <td><input size="50" type="text" name="titre" value="{$titre}"></td>
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
    <tr>
        <td>{$lang.image}</td>
        <td>
            <select name="image" onChange="document.images.sujet.src=document.nouvelle.image.options[document.nouvelle.image.selectedIndex].value;">
                {html_options options=$img selected=$image}
            </select>
        </td>
    </tr>
    <tr>
        <td><img alt="" name="sujet" src="{$image}"></td>
    </tr>
    <tr>
        <td colspan="2"><textarea cols="70" rows="20" name="nouvelle">{$nouvelle}</textarea></td>
    </tr>
    <tr>
        <td colspan="2">
        <input onclick="window.location.href='admin_nouvelles.php'" type="button" value="{$lang.bouton_retour_liste}">
        <input type="submit" value="{$lang.bouton_envoyer}">
        <input type="button" value="{$lang.previsualise}" onclick="return prev_nouvelle(document.nouvelle.date.value,document.nouvelle.titre.value,document.nouvelle.nouvelle.value,document.nouvelle.image.options[document.nouvelle.image.selectedIndex].value);">
        </td>
    </table>
</form>
</div>
</td>
