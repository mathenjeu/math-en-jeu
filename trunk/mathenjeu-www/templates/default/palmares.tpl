<td align="left">
<h2>{$lang.palmares_titre}</h2>
<div style="width:90%" class="hr"><p>
<div class="centre">
	Le palmarès est mis à jour toutes les heures. <br>
	Les données utilisées pour le palmarès s'étendent sur {$nbJour} {$lang.jours}.<br><br>
    <table border="0" cellspacing="0" class="tableau" style="background-color: #FFFFFF;" width="100%">
        <thead>
        <tr align="left">
            <td valign="top" colspan="4"><h3>{$titre1}</h3></td>
        </tr>
        <tr class="tableau_head" align=center>
            <th>{$lang.palmares_rang}</th>
            <th>{$lang.palmares_joueur}</th>
            <th>{$lang.palmares_moy_point_minute}</th>
            <th>{$lang.palmares_nb_partie}</th>
        </tr>
        </thead>
        {section name=palmares loop=$palmares1}
            {if $palmares1[palmares] eq ""}
                <tr><td colspan="4">{$lang.palmares_aucun_resultat}</td></tr>
            {else}
            <tr class="{cycle name="bgcolor" values="tableau1,tableau2"}">
                <td align="center">{$smarty.section.palmares.index+1}</td>
                <td>{$palmares1[palmares].alias}</td>
                <td align=right>{$palmares1[palmares].moy}</td>
                <td align=right>{$palmares1[palmares].nbPartie}</td>
            </tr>
            {/if}
        {/section}
    </table>
    <br><br>
    <table class="tableau" cellspacing="0" style="background-color: #FFFFFF;" width="100%">
        <thead>
        <tr align="left">
            <td colspan="4"><h3>{$titre2}</h3></td>
        </tr>
        <tr class="tableau_head" align="center">
            <th>{$lang.palmares_rang}</th>
            <th>{$lang.palmares_joueur}</th>
            <th>{$lang.palmares_total_temps}</th>
            <th>{$lang.palmares_nb_partie}</th>
        </tr>
        </thead>
        {section name=palmares loop=$palmares2}
            {if $palmares2[palmares] eq ""}
                <tr><td colspan="4">{$lang.palmares_aucun_resultat}</td></tr>
            {else}
                <tr class="{cycle name="bgcolor" values="tableau1,tableau2"}">
                    <td align="center">{$smarty.section.palmares.index+1}</td>
                    <td>{$palmares2[palmares].alias}</td>
                    <td align=right>{$palmares2[palmares].totalTemps}</td>
                    <td align=right>{$palmares2[palmares].nbPartie}</td>
                </tr>
            {/if}
        {/section}
    </table>
    <br><br>
    <table class="tableau" cellspacing="0" style="background-color: #FFFFFF;" width="100%">
        <thead>
        <tr align="left">
            <td colspan="4"><h3>{$titre3}</h3></td>
        </tr>
        <tr class="tableau_head" align="center">
            <th>{$lang.palmares_rang}</th>
            <th>{$lang.palmares_joueur}</th>
            <th>{$lang.palmares_poucentage_victoire}</th>
            <th>{$lang.palmares_nb_partie}</th>
        </tr>
        </thead>
        {section name=palmares loop=$palmares3}
            {if $palmares3[palmares] eq ""}
                <tr><td colspan="4">{$lang.palmares_aucun_resultat}</td></tr>
            {else}
                <tr class="{cycle name="bgcolor" values="tableau1,tableau2"}">
                    <td align="center">{$smarty.section.palmares.index+1}</td>
                    <td>{$palmares3[palmares].alias}</td>
                    <td align=right>{$palmares3[palmares].poucentage_victoire} %</td>
                    <td align=right>{$palmares3[palmares].nbPartie}</td>
                </tr>
            {/if}
        {/section}
    </table>
</div>
</div>
</td>
