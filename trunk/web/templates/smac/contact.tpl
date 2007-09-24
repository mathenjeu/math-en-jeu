<td align="left" valign="top">
{if $status eq 0}
	<H2>{$lang.contact_titre}</H2>
	<div style="width:90%;align:center" class="hr"></div><p>
	<form name="contact" method="post" action="contact.php?action=envoyer">
	<table width="60%">
	<tr>
		<td colspan="2">{$lang.contact_instruction}</td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td class="erreur">{$erreur}</td></tr>
	<tr>
		<td>{$lang.contact_nom}</td>
	</tr>
	<tr>
		<td><input type="text" name="nom" size="40" value="{$nom}"></td>
	</tr>
	<tr>
		<td>{$lang.contact_courriel}</td>
	</tr>
	<tr>
		<td><input type="text" name="courriel" size="40" value="{$courriel}"></td>
	</tr>
	<tr>
		<td>{$lang.contact_sujet}</td>
	</tr>
	<tr>
		<td><input type="text" name="sujet" size="40" value="{$sujet}"></td>
	</tr>
	<tr>
		<td>{$lang.contact_message}</td>
	</tr>
	<tr>
		<td><textarea name="message" rows="10" cols="40">{$message}</textarea></td>
	</tr>
	<tr>
		<td><input type="submit" value="{$lang.bouton_envoyer}"></td>
	</tr>
	</table>
	</form>
{elseif $status eq 1}
	{$lang.contact_reussi}
{else}
	{$lang.contact_probleme}
{/if}


</td>