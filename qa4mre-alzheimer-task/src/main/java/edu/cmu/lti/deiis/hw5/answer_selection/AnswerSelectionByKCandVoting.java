package edu.cmu.lti.deiis.hw5.answer_selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.deiis.hw5.utils.SentenceUtils;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.utils.Utils;

public class AnswerSelectionByKCandVoting extends JCasAnnotator_ImplBase {

	int K_CANDIDATES = 5;
	double total_cat1 = 0.0;
	double nDocs = 0.0;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		K_CANDIDATES = (Integer) context
				.getConfigParameterValue("K_CANDIDATES");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
		ArrayList<QuestionAnswerSet> qaSet = Utils.fromFSListToCollection(
				testDoc.getQaList(), QuestionAnswerSet.class);
		int matched = 0;
		int total = 0;
		int unanswered = 0;

		for (int i = 0; i < qaSet.size(); i++) {

			Question question = qaSet.get(i).getQuestion();
			System.out.println("Question: " + question.getText());
			ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet
					.get(i).getAnswerList(), Answer.class);
			ArrayList<CandidateSentence> candSentList = Utils
					.fromFSListToCollection(qaSet.get(i)
							.getCandidateSentenceList(),
							CandidateSentence.class);

			int topK = Math.min(K_CANDIDATES, candSentList.size());
			String correct = "";
			boolean isNoneOfTheAbove = false;
			for (int j = 0; j < choiceList.size(); j++) {
				Answer answer = choiceList.get(j);
				answer.setIsSelected(false);
				if (answer.getIsCorrect()) {
					correct = answer.getText();
					// break;
				}
				if (answer.getText().toLowerCase().equals("none of the above")) {
					isNoneOfTheAbove = true;
				}
			}

			HashMap<String, Double> hshAnswer = new HashMap<String, Double>();
			HashMap<String, Double> hshAnswerScore = new HashMap<String, Double>();

			for (int c = 0; c < topK; c++) {

				CandidateSentence candSent = candSentList.get(c);

				ArrayList<CandidateAnswer> candAnswerList = Utils
						.fromFSListToCollection(candSent.getCandAnswerList(),
								CandidateAnswer.class);
				String selectedAnswer = "";
				double maxScore = Double.NEGATIVE_INFINITY;
				double maxRelevanceScore = Double.NEGATIVE_INFINITY;
				for (int j = 0; j < candAnswerList.size(); j++) {

					CandidateAnswer candAns = candAnswerList.get(j);
					String answer = candAns.getText();
					double totalScore = 2 * candAns.getTokenSimilarityScore()
							// + candAns.getQuerySimilarityScore()
							+ 0.33 * candAns.getSynonymScore()
							// + 3*candSent.getRelevanceScore()
							+ 3 * candAns.getVectorSimilarityScore()
							+ candAns.getPMIScore();
					
					String askingFor = question.getAskingFor(); 
					String questionCategory = question.getCategory();
					Double whichpenalty = 1.0;
					Double whichbonus = 1.0;
					Double quantpenalty = 1.0;
					Double quantbonus = 1.0;
					boolean isMatched=false;
					
					if ( ( questionCategory.equals("which") || questionCategory.equals("what") ) && askingFor!=null){
						isMatched = SentenceUtils.doesCandAnswerMatchCategory(candAns, askingFor);
						if (isMatched)
							totalScore = totalScore*whichbonus;
						else
						  totalScore = totalScore*whichpenalty;
					}
					if (questionCategory.equals("howmany")){
						Answer ans=null;
						for (j = 0; j < choiceList.size(); j++) {
							ans = choiceList.get(j);
							if (ans.getText().equals(answer)) 
								break;
						}
						ArrayList<Token> choiceTokens = Utils
								.fromFSListToCollection(ans.getTokenList(),
										Token.class);
						boolean foundQuant = false;
						for (Token tk : choiceTokens){
							if (tk.getPos().equals("CD") || tk.getPos().equals("PDT"))
								foundQuant = true; 
						}
						if (foundQuant)
							totalScore = totalScore*quantbonus;
						else
						  totalScore = totalScore*quantpenalty;
						
					}
					
					if (answer.equals("AD")
							|| answer.equals("None of the above")) {
						totalScore = candAns.getQuerySimilarityScore()
								+ candSent.getRelevanceScore()
								+ candAns.getPMIScore();
						totalScore = 0.0;
					}
					
					if (totalScore > maxScore) {
						maxScore = totalScore;
						selectedAnswer = answer;
						if (candAns.getQuerySimilarityScore() > maxRelevanceScore) {
							maxRelevanceScore = candAns
									.getQuerySimilarityScore();
						}
					}

				}

				// when all scores are low, the candidate sentence should not
				// participate in the voting procedure
				if (maxScore > 0.5) {
					Double existingVal = hshAnswer.get(selectedAnswer);
					if (existingVal == null) {
						existingVal = new Double(0.0);
					}
					hshAnswer.put(selectedAnswer, existingVal + 1.0);
				}

				Double existingVal = hshAnswerScore.get(selectedAnswer);
				if (existingVal == null) {
					existingVal = new Double(0.0);
				}
				hshAnswerScore.put(selectedAnswer, existingVal
						+ maxRelevanceScore);
			}

			String bestChoice = null;
			try {
				bestChoice = findBestChoice(hshAnswer, hshAnswerScore,
						isNoneOfTheAbove);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// choosing "none of the above" if this choice existed and no answer
			// is chosen
			if (bestChoice == null
					&& choiceList.get(choiceList.size() - 1).getText()
							.equals("None of the above")) {
				bestChoice = choiceList.get(choiceList.size() - 1).getText();
			}
			// Select the best Choice we found
			
			for (int j = 0; j < choiceList.size(); j++) {
				Answer answer = choiceList.get(j);
				
				if (answer.getText().equals(bestChoice) && bestChoice!=null){
					answer.setIsSelected(true);
					System.out.println("Selected Choice: " +"\t" + answer.getText());
					break;
				}
					
			}
			
			System.out.println("Correct Choice: " +"\t" + correct);
			System.out.println("Best Choice: " + "\t" + bestChoice);
			
			if (bestChoice == null) {
				unanswered++;
			}
			if (bestChoice != null && correct.equals(bestChoice)) {
				matched++;

			}
			total++;
			System.out
					.println("================================================");

		}

		System.out.println("Correct: " + matched + "/" + total + "="
				+ ((matched * 100.0) / total) + "%");
		// TO DO: Reader of this pipe line should read from xmi generated by
		// SimpleRunCPE
		double cAt1 = (((double) matched) / ((double) total) * unanswered + (double) matched)
				* (1.0 / total);
		System.out.println("c@1 score:" + cAt1);
		total_cat1 += cAt1;
		nDocs++;

	}

	public String findBestChoice(HashMap<String, Double> hshAnswer,
			HashMap<String, Double> hshAnswerScore, boolean isNoneOfTheAbove)
			throws Exception {

		Iterator<String> it = hshAnswer.keySet().iterator();
		String bestAns = null;
		double maxScore = 0;
		double maxScoreScore = 0;
		System.out.println("Aggregated counts; ");
		while (it.hasNext()) {
			String key = it.next();
			Double val = hshAnswer.get(key);
			Double score = hshAnswerScore.get(key);
			System.out.println(key + "\t" + key + "\t" + val + "\t" + score);
			if (val > maxScore) {
				maxScore = val;
				bestAns = key;
				maxScoreScore = score;
			} else if (val == maxScore && score > maxScoreScore) {
				maxScore = val;
				bestAns = key;
				maxScoreScore = score;
			}

		}

		// if no answer could significantly outperform other answers, do not
		// answer this question
		if (isNoneOfTheAbove) {
			if (maxScore < 5)
				bestAns = null;
		} else if (maxScore < 4)
			bestAns = null;

		/*
		 * if (maxScoreScore/maxScore < 0.5){
		 * System.out.println("None of the above" + "\t" + "NOTA"+ "\t" +
		 * maxScoreScore/maxScore); bestAns="None of the above"; }
		 */
		return bestAns;
	}

	public void destroy() {
		System.out.println("Average c@1:" + total_cat1 / nDocs);
	}
}
