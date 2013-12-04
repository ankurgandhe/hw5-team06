package edu.cmu.lti.deiis.hw5.answer_ranking;

import java.util.ArrayList;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.deiis.hw5.utils.SetUtil;
import edu.cmu.lti.deiis.hw5.utils.WordNetAPI;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.utils.Utils;

public class AnswerChoiceCandAnsSynonymScorer extends JCasAnnotator_ImplBase {

	int K_CANDIDATES = 5;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		K_CANDIDATES=(Integer)context.getConfigParameterValue("K_CANDIDATES");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
		// String testDocId = testDoc.getId();
		ArrayList<QuestionAnswerSet> qaSet = Utils
				.getQuestionAnswerSetFromTestDocCAS(aJCas);

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
			
			//Candidate answer scoring logic starts here
			for (int c = 0; c < topK; c++) {

				CandidateSentence candSent = candSentList.get(c);

				ArrayList<Token> candSentTokens = Utils
						.fromFSListToCollection(candSent.getSentence().getTokenList(),Token.class);
				//ArrayList<Token> candSentTokens = Utils
					//		.fromFSListToCollection(candSent.getSentence().getPhraseList(),Phrase.class);
				
								//.getPhraseList(), NounPhrase.class);//getNouns
				//ArrayList<NER> candSentNers = Utils.fromFSListToCollection(
					//	candSent.getSentence().getNerList(), NER.class);
				//get NamedEntities
				
				ArrayList<CandidateAnswer> candAnsList = new ArrayList<CandidateAnswer>();
				for (int j = 0; j < choiceList.size(); j++) {

					Answer answer = choiceList.get(j);
					ArrayList<Token> choiceTokens = Utils
							.fromFSListToCollection(answer.getTokenList(),
									Token.class);
		//			ArrayList<NER> choiceNERs = Utils.fromFSListToCollection(
			//				answer.getNerList(), NER.class);

					int synMatch = 0;
					for (int k = 0; k < candSentTokens.size(); k++) {
						String canTokenString=candSentTokens.get(k).getText();
						Set<String> canTokenSyn=WordNetAPI.getHyponyms(canTokenString, null);
						/*for (int l = 0; l < choiceNERs.size(); l++) {
							if (candSentSynonyms.get(k).getText()
									.contains(choiceNERs.get(l).getText())) {
								nnMatch++;
							}
						}*/
						for (int l = 0; l < choiceTokens.size(); l++) {
							
							String choiceTokenString=choiceTokens.get(l).getText();
							Set<String> choiceTokenSyn=WordNetAPI.getHyponyms(choiceTokenString, null);

							if (SetUtil.calculauteIntersection(canTokenSyn, choiceTokenSyn)) {
							synMatch++;
							}
						}
					}


					System.out.println(choiceList.get(j).getText() + "\t"
							+ synMatch);
					CandidateAnswer candAnswer = null;
					if (candSent.getCandAnswerList() == null) {
						candAnswer = new CandidateAnswer(aJCas);
					} else {
						candAnswer = Utils.fromFSListToCollection(
								candSent.getCandAnswerList(),
								CandidateAnswer.class).get(j);// new
																// CandidateAnswer(aJCas);;

					}
					candAnswer.setText(answer.getText());
					candAnswer.setQId(answer.getQuestionId());
					candAnswer.setChoiceIndex(j);
					candAnswer.setSynonymScore(synMatch);
					//candAnswer.set
					//candAnswer.setSimilarityScore(nnMatch);
					//candAnswer.sets
					candAnsList.add(candAnswer);
				}

				FSList fsCandAnsList = Utils.fromCollectionToFSList(aJCas,
						candAnsList);
				candSent.setCandAnswerList(fsCandAnsList);
				candSentList.set(c, candSent);

			}			//Candidate answer scoring logic ends here

			System.out
					.println("================================================");
			FSList fsCandSentList = Utils.fromCollectionToFSList(aJCas,
					candSentList);
			qaSet.get(i).setCandidateSentenceList(fsCandSentList);

		}
		FSList fsQASet = Utils.fromCollectionToFSList(aJCas, qaSet);
		testDoc.setQaList(fsQASet);

	}

}
