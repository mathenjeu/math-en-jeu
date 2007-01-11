<!--INCOMPLET-->
<table width=100%>
    <thead>
    <tr>
        <td colspan=4><u><b>Il y a <?echo $nb?> question(s) en attente de validation!</b></u></td>
    </tr>
    </thead>
    <tr align=center bgcolor=#D5EED5>
        <th>Date de soumission</th>
        <th>Fichier Question</th>
        <th>Fichier Réponse</th>
        <th width="50%">Description</th>
        <th></th>
    </tr>
    {section name=question loop=$questions}
    <tr bgcolor="{cycle name="bgcolor" values="#D5D5D5,#E5E5E5"}">
        <td>{$questions[question].date}</td>
        <td valign=center align=center>FICHIER ICI<img border=0 src="document_icon.jpg" /></td>
        <td valign=center align=center>FICHIER ICI<img border=0 src="document_icon.jpg" /></a></td>
        <td>{$questions[question].description}</td>
        <td valign=center align=center><a alt="Supprimer" title="Supprimer cette question" href="super-admin.php?action=deleteQuestion&cle=<?echo $row->cleQuestion?>"><img border=0 src="delete.png" /></a></td>
    </tr>
    {/section}
</table>
