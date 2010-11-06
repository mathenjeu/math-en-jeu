/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Generale Affero (AGPL), telle que publiee par
Affero Inc. ; soit la version 1 de la Licence, ou (a
votre discretion) une version ulterieure quelconque.

Ce programme est distribue dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans meme une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Generale Affero pour plus de details.

Vous devriez avoir recu un exemplaire de la Licence Publique
Generale Affero avec ce programme; si ce n'est pas le cas,
ecrivez a Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/

class RoomReport
{
	private var players:Object;
	private var questions:Object;
	private var games:Object;
	private var reportType:String;

	function RoomReport(reportRoot:XMLNode)
	{
		players = new Object();
		questions = new Object();
		games = new Object();
		reportType = "summary";
		var lstNoeud:Array = reportRoot.childNodes;
		var count:Number = lstNoeud.length;
		for (var i = 0; i < count; i++)
		{
			var noeud:XMLNode = lstNoeud[i];
			switch (noeud.attributes["type"])
			{
				case "ListeJoueurs" :
					buildPlayers(noeud);
					break;
				case "ListeSWFs" :
					buildQuestionSWFs(noeud);
					break;
				case "ListeSommaireJoueurs" :
					buildPlayerSummaries(noeud);
					break;
				case "ListeSommaireQuestions" :
					buildQuestionSummaries(noeud);
					break;
				case "ListeParties" :
					buildGames(noeud);
					reportType = "full";
					break;
			}
		}
	}

	function getType():String
	{
		return reportType;
	}

	//players is a map of user_id to:
	//    lastname: the last name of the player
	//    firstname: the first name of the player
	//    summaryStats: array of statistices for time period 0=this week, 1=this month, 2=since opening
	//    summaryStats[time_period].gamesPlayed: number of games played by the player during 'time_period'
	//    summaryStats[time_period].maxScore: maximum score achieved by the player during 'time_period'
	//    summaryStats[time_period].sumScores: sum of scores for the player during 'time_period'
	//    summaryStats[time_period].numWins: number of games won by the player during 'time_period'
	//    summaryStats[time_period].numQuestions: number of questions 'seen' by the player during 'time_period'
	//    summaryStats[time_period].numRight: number of questions answered correctly by the player during 'time_period'
	//    summaryStats[time_period].numWrong: number of questions answered incorrectly by the player during 'time_period'
	//Note that there are 3 outcomes for a question: answered right, answered wrong AND not answered.  So
	//numRight+numWrong may not equal numQuestions.  A question is _not answered_ when the player uses an object
	//to change the question he is looking at OR when the game ends while the player is still figuring out what to
	//answer.
	//
	//Important: the lastname, firstname are always present, the summaryStats is only present when creating
	//           a summary report.  In case of a full report summaryStats is undefined and we have more
	//           specific information in the 'games' map (see below)
	function getPlayers():Object
	{
		return players;
	}
	//questions is a map of question_id to:
	//    swfs: array of filenames (in no particular order) containing SWF for the question.
	//    swfs[i]: a SWF filename for the question (there is one SWF per language, but i!=language_id)
	//    summaryStats: array of statistics for time period 0=this week, 1=this month, 2=since opening
	//    summaryStats[time_period].frequency: number of times this question was asked during 'time_period'
	//    summaryStats[time_period].frequencyRight: number of times answered correctly druing 'time_period'
	//    summaryStats[time_period].frequencyWrong: number of times answered incorrectly druing 'time_period'
	//    summaryStats[time_period].timeTaken: sum of time taken to answer the question during 'time_period'
	//    summaryStats[time_period].timeTakenRight: sum of time taken for correct answers during 'time_period'
	//    summaryStats[time_period].timeTakenWrong: sum of time taken for incorrect answers during 'time_period'
	//Note that there are 3 outcomes for a question: answered right, answered wrong AND not answered.  So
	//frequencyRight+frequencyWrong may not equal frequency.  A question is _not answered_ when the player uses
	//an object to change the question he is looking at OR when the game ends while the player is still figuring
	//out what to answer.
	//
	//Important: the swfs array is always present, the summaryStats array is only present when creating
	//           a summary report.  In case of a full report summaryStats is undefined and we have more
	//           specific information in the 'games' map (see below)
	function getQuestions():Object
	{
		return questions;
	}

	//games is a map of game_id to:
	//     date: the exact time and date at which the game was played
	//     gameTypeId: the type of game played (1=mathEnJeu, 2=Tournament, 3=Course)
	//     winnerId: the user_id of the winning player (0 means virtual player won)
	//     duration: the duration of the game in minutes
	//     user[uid].score: the score for player with user_id 'uid' in this game
	//     user[uid].question[qid].answerStatus: How user 'uid' answered question 'qid' (0=right,1=wrong,2=not answered)
	//     user[uid].question[qid].timeTaken: The time taken by user 'uid' to answer question 'qid'
	function getGames():Object
	{
		return games;
	}

	//Adds the first and last name to the player map.
	function buildPlayers(playersRoot:XMLNode)
	{
		var lstPlayers:Array = playersRoot.childNodes;
		var count:Number = lstPlayers.length;
		for (var i = 0; i < count; i++)
		{
			var player:XMLNode = lstPlayers[i];
			var id:Number = parseInt(player.attributes["id"], 10);
			var names:Array = player.attributes["n"].split(",");
			if (players[id] == undefined)
			{
				players[id] = new Object();
			}
			players[id].lastName = names[0];
			players[id].firstName = names[1];
		}
	}

	//Adds the 'summaryStats' entry to the players map.  This entry
	//is an array of objects.  The array index is a time period
	//(0=this week, 1=this month, 2=since opening) and the array values
	//are: gamesPlayed, maxScore, sumScores, numWins, numQuestions,
	//     numRight and numWrong for each time period.
	//So after this function is called you can get the number of games
	//played by the players with id 1482 this month by looking at
	//   players[1482].summaryStats[1].gamesPlayed
	//The 1482 refers to the user_id in the DB for that player and the
	//array index 1 means 'this month'.
	function buildPlayerSummaries(playerSummariesRoot:XMLNode)
	{
		var lstPlayerSummaries:Array = playerSummariesRoot.childNodes;
		var numSummaries:Number = lstPlayerSummaries.length;
		for (var i = 0; i < numSummaries; i++)
		{
			var summary:XMLNode = lstPlayerSummaries[i];
			var uid:Number = parseInt(summary.attributes["id"]);
			if (players[uid] == undefined)
			{
				players[uid] = new Object();
			}
			players[uid].summaryStats = new Array();
			var timePeriods:Array = summary.childNodes;
			var numTimePeriods:Number = timePeriods.length;
			for (var tp = 0; tp < numTimePeriods; tp++)
			{
				var period:XMLNode = timePeriods[tp];
				var stats:Object = new Object();
				stats.gamesPlayed = period.attributes["gp"];
				stats.maxScore = period.attributes["ms"];
				stats.sumScores = period.attributes["ss"];
				stats.numWins = period.attributes["w"];
				stats.numQuestions = period.attributes["nq"];
				stats.numRight = period.attributes["nr"];
				stats.numWrong = period.attributes["nw"];
				players[uid].summaryStats.push(stats);
			}
		}
	}

	//Adds the SWF filenames to the questions map.  There can be more then one
	//SWF per question.  There should be one by language for which the question
	//is available.
	function buildQuestionSWFs(questionSWFsRoot:XMLNode)
	{
		var lstQuestionSWFs:Array = questionSWFsRoot.childNodes;
		var numQuestions:Number = lstQuestionSWFs.length;
		for (var i = 0; i < numQuestions; i++)
		{
			var questionSWF:XMLNode = lstQuestionSWFs[i];
			var qid:Number = parseInt(questionSWF.attributes["qid"], 10);
			if (questions[qid] == undefined)
			{
				questions[qid] = new Object();
			}
			questions[qid].swfs = new Array();
			var lstFilenames:Array = questionSWF.childNodes;
			var numSWF:Number = lstFilenames.length;
			for (var j = 0; j < numSWF; j++)
			{
				questions[qid].swfs.push(lstFilenames[j].nodeValue);
			}
		}
	}

	//Adds the 'summaryStats' entry to the questions map.  This entry
	//is an array of objects.  The array index is a time period
	//(0=this week, 1=this month, 2=since opening) and the array values
	//are: frequency, frequencyRight, frequencyWrong
	//     timeTaken, timeTakenRight, timeTakenWrong
	//So after this function is called you can get the number of times the
	//question with id 8832 was aked this week by looking at
	//   questions[8832].summaryStats[0].frequency
	//The 8832 refers to the question_id in the DB for that question and the
	//array index 0 means 'this week'.
	function buildQuestionSummaries(questionSummariesRoot:XMLNode)
	{
		var lstQuestionSummaries:Array = questionSummariesRoot.childNodes;
		var numSummaries:Number = lstQuestionSummaries.length;
		for (var i = 0; i < numSummaries; i++)
		{
			var summary:XMLNode = lstQuestionSummaries[i];
			var qid:Number = parseInt(summary.attributes["id"]);
			if (questions[qid] == undefined)
			{
				questions[qid] = new Object();
			}
			questions[qid].summaryStats = new Array();
			var timePeriods:Array = summary.childNodes;
			var numTimePeriods:Number = timePeriods.length;
			for (var tp = 0; tp < numTimePeriods; tp++)
			{
				var period:XMLNode = timePeriods[tp];
				var stats:Object = new Object();
				stats.frequency = period.attributes["f"];
				stats.frequencyRight = period.attributes["fr"];
				stats.frequencyWrong = period.attributes["fw"];
				stats.timeTaken = period.attributes["t"];
				stats.timeTakenRight = period.attributes["tr"];
				stats.timeTakenWrong = period.attributes["tw"];
				questions[qid].summaryStats.push(stats);
			}
		}
	}

	//Adds all data to the games map.  (See structure above getGames() function
	//This method is only called when building a full report, when building a summary
	//report the buildQuestionSummaries and buildPlayerSummaries functions are used
	//instead.  It is possible to extract from the games map all the data available
	//for summary reports but the games map contains a lot more information.
	var debugCounter = 0;
	function buildGames(gamesRoot:XMLNode)
	{
		var lstGames:Array = gamesRoot.childNodes;
		var numGames:Number = lstGames.length;
		for (var i = 0; i < numGames; i++)
		{
			var game:XMLNode = lstGames[i];
			var gid:Number = parseInt(game.attributes["id"]);
			games[gid] = new Object();
			games[gid].date = game.attributes["d"];
			games[gid].gameTypeId = game.attributes["gt"];
			games[gid].winnerId = game.attributes["w"];
			games[gid].duration = game.attributes["t"];
			games[gid].user = new Object();
			var lstUsers:Array = game.childNodes;
			var numUsers:Number = lstUsers.length;
			for (var j = 0; j < numUsers; j++)
			{
				var user:XMLNode = lstUsers[j];
				var uid:Number = parseInt(user.attributes["id"], 10);
				games[gid].user[uid] = new Object();
				games[gid].user[uid].score = user.attributes["s"];
				games[gid].user[uid].question = new Object();
				var lstQuestions:Array = user.childNodes;
				var numQuestions:Number = lstQuestions.length;
				for (var k = 0; k < numQuestions; k++)
				{
					var question:XMLNode = lstQuestions[k];
					var qid:Number = parseInt(question.attributes["id"], 10);
					games[gid].user[uid].question[qid] = new Object();
					games[gid].user[uid].question[qid].answerStatus = question.attributes["s"];
					games[gid].user[uid].question[qid].timeTaken = question.attributes["t"];
				}//end question list
			}//end user list
		}//end game list
	}//end buildGames function
}