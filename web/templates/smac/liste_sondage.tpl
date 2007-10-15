<td width="90%" valign="top" align="left">
<div class="centre">
	<h2>{$lang.gestion_sondage}</h2>
	<table class="tableau" width="100%" border="0">
	    <thead>
	    <tr class="tableau_head" align="center">
	        <th>{$lang.date_sondage}</th>
	        <th>{$lang.titre}</th>
	        <th width="10%">{$lang.destinataire}</th>
	        <th>{$lang.nb_repondant}</th>
	        <th></th>
	        <th></th>
	        <th></th>
	    </tr>
	    </thead>
	    {section name=sondage loop=$sondages}
	        <tr onmouseover="this.className='tableau_mouse_over';"
	            onmouseout="this.className='{cycle name="mouseout" values="tableau1,tableau2"}';"
	            class="{cycle name="bgcolor" values="tableau1,tableau2"}">
	        <td>{$sondages[sondage].date}</td>
	        <td width="60%">{$sondages[sondage].titre}</td>
	        <td>{$sondages[sondage].destinataire}</td>
	        <td align="right">{$sondages[sondage].total}</td>
	        <td align="center"><a title="{$lang.sondage_detail}" href="#" onclick="window.open('detail_sondage.php?cle={$sondages[sondage].cle}','','left=20,top=20,width=525,height=525,toolbar=0,resizable=0')"><img border="0" alt="{$lang.sondage_detail}" src="{$template}img/icon_loupe.gif"></a></td>
	        <td align="center"><a title="{$lang.sondage_modifier}" href="admin_sondage.php?action=modificationSondage&amp;cleSondage={$sondages[sondage].cle}"><img border="0" alt="{$lang.sondage_modifier}" src="{$template}img/modifier.png"></a></td>
	        <td align="center"><a title="{$lang.sondage_supprimer}" href="admin_sondage.php?action=deleteSondage&amp;cleSondage={$sondages[sondage].cle}" onclick="return validerSupprimer('{$lang.valider_supression}')"><img border="0" alt="{$lang.sondage_supprimer}" src="{$template}img/delete.png"></a></td>
	    </tr>
	    {/section}
	</table>
	<br>
	<input onclick="window.location.href='admin_sondage.php?action=ajoutSondage'" type="button" value="{$lang.bouton_ajout_sondage}" />
</div>
</td>
  

