<td width="20%" valign="top">
<div class="sondage">
	<div class="centre">
    <table width="100%" border="0" cellpadding="0" cellspacing="5" align="center">
    <tr>
        <td colspan=2 align="left" class="titresondage">{$titre}</td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
    {section name=reponse loop=$reponseTexte}
    <tr class="blanc" >
        <td align="left" >{$reponseTexte[reponse]}</td>
        <td align="right" valign="bottom">{$reponseValeur[reponse]}%</td>
    </tr>
    {/section}
    <tr class="totalsondage">
        <td colspan="2" align="center">
            <br>{$lang.sondage_nb_reponse} : {$total}
        </td>
    </tr>
    </table>
    </div>
</div>
</td>
