package edu.cmu.lti.deiis.hw5.answer_ranking;

import java.io.IOException;
import java.util.ArrayList;


import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

import edu.cmu.lti.deiis.hw5.utils.DistributionalSimilarity;


public class AnswerChoiceCandAnsVectorSimilarityScorer extends
		JCasAnnotator_ImplBase {

	int K_CANDIDATES = 5;
	DistributionalSimilarity vectorModel;
	double thresh = 0.5;
	boolean oov = false; 
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		K_CANDIDATES = (Integer) context
				.getConfigParameterValue("K_CANDIDATES");
		vectorModel = DistributionalSimilarity.getInstance();
		/*String filename = "model\\alzheimer.tok.model.320";
		//String filename = "model\\alzheimer+12-test.tok.model.320";
		try {
			vectorModel.readModel(filename);			
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
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
			
			for (int c = 0; c < topK; c++) {
				CandidateSentence candSent = candSentList.get(c);

				ArrayList<NounPhrase> candSentNouns = Utils
						.fromFSListToCollection(candSent.getSentence()
								.getPhraseList(), NounPhrase.class);
				ArrayList<NER> candSentNers = Utils.fromFSListToCollection(
						candSent.getSentence().getNerList(), NER.class);
				ArrayList<VerbPhrase> candSentVerbs = Utils
						.fromFSListToCollection(candSent.getSentence()
								.getPhraseList(), VerbPhrase.class);
				
				ArrayList<CandidateAnswer> candAnsList = new ArrayList<CandidateAnswer>();
				for (int j = 0; j < choiceList.size(); j++) {

					Answer answer = choiceList.get(j);
					ArrayList<NounPhrase> choiceNouns = Utils
							.fromFSListToCollection(answer.getNounPhraseList(),
									NounPhrase.class);
					
					ArrayList<VerbPhrase> choiceVerbs = Utils
							.fromFSListToCollection(answer.getVerbPhraseList(),
									VerbPhrase.class);
					
					ArrayList<NER> choiceNERs = Utils.fromFSListToCollection(
							answer.getNerList(), NER.class);
					
					double similarityScore = 0.0;
				
					// Standard way of scoring NER and NNs
					int count=0;
					double distance=0.0;
					for (int k = 0; k < candSentNouns.size(); k++) {
						// If candidate Noun Phrase contains answer NER
						for (int l = 0; l < choiceNERs.size(); l++) {
							distance = vectorModel.getDistance(
									candSentNouns.get(k).getText(), choiceNERs
											.get(l).getText());
							similarityScore += distance;
							count++;
						}

						// If candidate Noun phrase contains answer Nouns
						for (int l = 0; l < choiceNouns.size(); l++) {
							distance = vectorModel.getDistance(
									candSentNouns.get(k).getText(), choiceNouns
											.get(l).getText());
							similarityScore += distance;
							count++;
						}
						
					}
				
					//For verb phrase
					for (int k = 0; k < candSentVerbs.size(); k++) {
						// If candidate Noun Phrase contains answer NER
						for (int l = 0; l < choiceVerbs.size(); l++) {
							distance = vectorModel.getDistance(
									candSentVerbs.get(k).getText(), choiceVerbs
											.get(l).getText());
							similarityScore += distance;
							count++;
						}
					}
					
					
					// Same as above, for NERs
					for (int k = 0; k < candSentNers.size(); k++) {
						for (int l = 0; l < choiceNERs.size(); l++) {
							distance = vectorModel.getDistance(
									candSentNers.get(k).getText(), choiceNERs
											.get(l).getText());
							similarityScore += distance;
							count++;

						}
						for (int l = 0; l < choiceNouns.size(); l++) {
							distance = vectorModel.getDistance(
									candSentNers.get(k).getText(), choiceNouns
											.get(l).getText());
							similarityScore += distance;
							count++;
						}
					}
					
					//similarityScore = similarityScore/count;
					double totalMatch = candSentNouns.size()
							* (choiceNouns.size() + choiceNERs.size())
							+ candSentNers.size()
							* (choiceNouns.size() + choiceNERs.size())
							+ candSentVerbs.size()
							* (choiceVerbs.size());
					
					similarityScore/=totalMatch;
					System.out.println(choiceList.get(j).getText() + "\t"
							+ similarityScore+","+count);
					CandidateAnswer candAnswer = null;
					if (candSent.getCandAnswerList() == null) {
						candAnswer = new CandidateAnswer(aJCas);
					} else {
						candAnswer = Utils.fromFSListToCollection(
								candSent.getCandAnswerList(),
								CandidateAnswer.class).get(j);

					}
					candAnswer.setText(answer.getText());
					candAnswer.setQId(answer.getQuestionId());
					candAnswer.setChoiceIndex(j);
					candAnswer.setVectorSimilarityScore(similarityScore);
					candAnsList.add(candAnswer);
				}

				FSList fsCandAnsList = Utils.fromCollectionToFSList(aJCas,
						candAnsList);
				candSent.setCandAnswerList(fsCandAnsList);
				candSentList.set(c, candSent);

			}

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
