package edu.cmu.lti.deiis.hw5.annotators;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

public class PhraseAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);

		ArrayList<Sentence> sentenceList = Utils
				.getSentenceListFromTestDocCAS(aJCas);

		for (int i = 0; i < sentenceList.size(); i++) {

			Sentence sent = sentenceList.get(i);
			ArrayList<Token> tokenList = Utils
					.getTokenListFromSentenceList(sent);
			// For noun phrases
			ArrayList<NounPhrase> phraseList = extractNounPhrases(tokenList,
					aJCas);
			FSList fsPhraseList = Utils.createNounPhraseList(aJCas, phraseList);
			fsPhraseList.addToIndexes(aJCas);
			sent.setPhraseList(fsPhraseList);
			// for verb phrases
			ArrayList<VerbPhrase> verbPhraseList = extractVerbPhrases(
					tokenList, aJCas);
			FSList fsVerbPhraseList = Utils.createVerbPhraseList(aJCas,
					verbPhraseList);
			fsVerbPhraseList.addToIndexes(aJCas);
			sent.setVerbPhraseList(fsVerbPhraseList);

			sent.addToIndexes();
			sentenceList.set(i, sent);
		}

		FSList fsSentList = Utils.createSentenceList(aJCas, sentenceList);
		testDoc.setSentenceList(fsSentList);

	}

	public ArrayList<NounPhrase> extractNounPhrases(ArrayList<Token> tokenList,
			JCas jCas) {

		ArrayList<NounPhrase> nounPhraseList = new ArrayList<NounPhrase>();
		String nounPhrase = "";
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			String word = token.getText();
			String pos = token.getPos();

			if (pos.startsWith("NN") || pos.startsWith("JJ")
					|| pos.startsWith("CD")) {
				nounPhrase += word + " ";
			} else {
				nounPhrase = nounPhrase.trim();
				if (!nounPhrase.equals("")) {
					NounPhrase nn = new NounPhrase(jCas);
					nn.setText(nounPhrase);
					nounPhraseList.add(nn);
					// System.out.println("Noun Phrase: "+nounPhrase);
					nounPhrase = "";
				}
			}

		}
		nounPhrase = nounPhrase.trim();
		if (!nounPhrase.equals("")) {
			NounPhrase nn = new NounPhrase(jCas);
			nn.setText(nounPhrase);
			nounPhraseList.add(nn);
		}

		return nounPhraseList;
	}

	public ArrayList<VerbPhrase> extractVerbPhrases(ArrayList<Token> tokenList,
			JCas jCas) {

		ArrayList<VerbPhrase> verbPhraseList = new ArrayList<VerbPhrase>();
		String verbPhrase = "";
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			String word = token.getText();
			String pos = token.getPos();

			if (pos.startsWith("RB") || pos.startsWith("VB")) {
				verbPhrase += word + " ";
			} else {
				verbPhrase = verbPhrase.trim();
				if (!verbPhrase.equals("")) {
					VerbPhrase vb = new VerbPhrase(jCas);
					vb.setText(verbPhrase);
					verbPhraseList.add(vb);
					// System.out.println("Noun Phrase: "+nounPhrase);
					verbPhrase = "";
				}
			}

		}
		verbPhrase = verbPhrase.trim();
		if (!verbPhrase.equals("")) {
			VerbPhrase vb = new VerbPhrase(jCas);
			vb.setText(verbPhrase);
			verbPhraseList.add(vb);
		}

		return verbPhraseList;
	}
}
