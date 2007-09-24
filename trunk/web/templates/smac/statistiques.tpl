<td class="centre" width="90%" valign="top" align="left">
<div class="centre">
	<script type="text/javascript" src="function.js"></script>
	<form name="stats" action="#" onsubmit="return afficherImage()">
	<table border="0" width="100%">
	    <tr>
	        <td>{$lang.stat_type_graph}</td>
	        <td>
	            <select name="typeGraph">
	            <option value="smooth">{$lang.stat_smooth}</option>
	            <option value="baton">{$lang.stat_baton}</option>
	            <option value="ligne">{$lang.stat_ligne}</option>
	            </select>
	        </td>
	    </tr>
	    <tr>
	        <td>{$lang.stat_type}</td>
	        <td>
	            <select name="typeStat">
	            <option value="1">{$lang.stat_nb_partie_mois}</option>
	            <option value="2">{$lang.stat_nb_partie_jour}</option>
	            <option value="3">{$lang.stat_temps_total_jour}</option>
	            </select>
	        <td>
	        <td></td>
	    </tr>
	    <tr>
	        <td>{$lang.stat_param}</td>
	        <td><input type="text" name="param" /></td>
	    </tr>
	    <tr>
	        <td><input type="button" value="{$lang.bouton_afficher}" onClick="afficherImage()"/></td>
	    </tr>
	    <tr>
	        <td colspan="3"><img name="graph" src="" alt="" /></td>
	    </tr>
	</table>
	</form>
</div>
</td>
