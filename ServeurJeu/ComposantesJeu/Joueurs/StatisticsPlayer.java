/**
 * 
 */
package ServeurJeu.ComposantesJeu.Joueurs;

/**
 * Used to collect stats about player
 * @author Oloieri Lilian 09.2010
 *
 */
public class StatisticsPlayer implements Comparable<StatisticsPlayer>{
	private String username;
	private Integer points;
	private Integer timePosition;
	private int position;
	
	public StatisticsPlayer(String username, int points, int timePosition)
	{
		this.setUsername(username);
		this.points = points;
		this.timePosition = timePosition;
	}
	
	
	public Integer getTimePosition() {
		return timePosition;
	}


	public void setTimePosition(Integer timePosition) {
		this.timePosition = timePosition;
	}


	public Integer getPoints() {
		return points;
	}


	public void setPoints(Integer points) {
		this.points = points;
	}


	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}


	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}


	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}


	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}


	/*
	private static class runStatistics implements Comparator<StatisticsPlayer> {
	    public int compare(StatisticsPlayer one, StatisticsPlayer two) {
	        int i = one.points.compareTo(two.points);
	        if (i == 0) {
	            i = one.timePosition.compareTo(two.timePosition);	            
	        }
	        return i;
	    }
	}*/


	@Override
	public int compareTo(StatisticsPlayer player) {
				
		assert(player != null);
		int relValue = this.points.compareTo(player.getPoints());
		if(relValue == 0)
		{
		   relValue = this.timePosition.compareTo(player.getTimePosition());
		}
		return relValue;
	}


}
