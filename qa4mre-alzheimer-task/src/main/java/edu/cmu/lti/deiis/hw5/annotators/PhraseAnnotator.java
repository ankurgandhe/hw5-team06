package edu.cmu.lti.deiis.hw5.annotators;

import java.util.ArrayList;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.deiis.hw5.utils.SynonymUtil;
import edu.cmu.lti.deiis.hw5.utils.WordNetAPI;
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
			ArrayList<NounPhrase> phraseList = extractNounPhrases(tokenList,
					aJCas);

			SynonymUtil.populateSynonyms(phraseList, aJCas);

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
		String nounOnly="";
		Boolean nounFlag = false;
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			String word = token.getText();
			String pos = token.getPos();

			if (pos.startsWith("NN")) {
				nounPhrase += word + " ";
				nounOnly+=word+" ";
				Set<String>  synset = WordNetAPI.getHyponyms(word,null);
				
				nounFlag = true;
			} else if ((pos.startsWith("JJ") || pos.startsWith("CD"))
					&& !nounFlag) {
				nounPhrase += word + " ";
			} else {
				nounPhrase = nounPhrase.trim();
				nounOnly = nounOnly.trim();
				if (!nounPhrase.equals("") && nounFlag) {
					NounPhrase nn = new NounPhrase(jCas);
					nounPhrase = nounPhrase.trim();
					nn.setText(nounPhrase);
					nounPhraseList.add(nn);
					if (nounOnly!="" && nounOnly!=nounPhrase){
						for (String nouns : nounOnly.split(" ")){
							NounPhrase nnOnly=new NounPhrase(jCas);
							nnOnly.setText(nouns);
							nounPhraseList.add(nnOnly);	
						}
						
					}
					// System.out.println("Noun Phrase: "+nounPhrase);
					nounPhrase = "";
					nounOnly="";
					nounFlag = false;
				} else if (!nounFlag) {
					nounPhrase = "";
				}
			}

		}
		nounPhrase = nounPhrase.trim();
		if (!nounPhrase.equals("")) {
			NounPhrase nn = new NounPhrase(jCas);
			nn.setText(nounPhrase);
			nounPhraseList.add(nn);
			if (nounOnly!="" && nounOnly!=nounPhrase){
				for (String nouns : nounOnly.split(" ")){
					NounPhrase nnOnly=new NounPhrase(jCas);
					nnOnly.setText(nouns);
					nounPhraseList.add(nnOnly);	
				}
				
			}
		}

		return nounPhraseList;
	}

	public ArrayList<VerbPhrase> extractVerbPhrases(ArrayList<Token> tokenList,
			JCas jCas) {

		ArrayList<VerbPhrase> verbPhraseList = new ArrayList<VerbPhrase>();
		String verbPhrase = "";
		boolean verbFlag = false;
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			String word = token.getText();
			String pos = token.getPos();
			if (pos.startsWith("VB")) {
				verbFlag = true;
				verbPhrase += word + " ";
			} else if (pos.startsWith("RB") || pos.startsWith("RP")) {
				verbPhrase += word + " ";
			} else {
				verbPhrase = verbPhrase.trim();

				if (!verbPhrase.equals("") && verbFlag) {
					VerbPhrase vb = new VerbPhrase(jCas);
					vb.setText(verbPhrase);
					verbPhraseList.add(vb);
					verbPhrase = "";
					verbFlag = false;
				} else if (!verbFlag)
					verbPhrase = "";
			}

		}
		verbPhrase = verbPhrase.trim();
		if (!verbPhrase.equals("") && verbFlag) {
			VerbPhrase vb = new VerbPhrase(jCas);
			vb.setText(verbPhrase);
			verbPhraseList.add(vb);
			verbFlag = false;
			verbPhrase = "";
		} else if (!verbFlag)
			verbPhrase = "";

		return verbPhraseList;
	}

	public ArrayList<VerbPhrase> extractNounPhraseSynonyms(
			ArrayList<Token> nounPhraseList, JCas jCas) {

		ArrayList<VerbPhrase> verbPhraseList = new ArrayList<VerbPhrase>();
		String verbPhrase = "";
		boolean verbFlag = false;
		for (int i = 0; i < nounPhraseList.size(); i++) {
			Token token = nounPhraseList.get(i);
			String word = token.getText();
			String pos = token.getPos();
			if (pos.startsWith("VB")) {
				verbFlag = true;
				verbPhrase += word + " ";
			} else if (pos.startsWith("RB") || pos.startsWith("RP")) {
				verbPhrase += word + " ";
			} else {
				verbPhrase = verbPhrase.trim();

				if (!verbPhrase.equals("") && verbFlag) {
					VerbPhrase vb = new VerbPhrase(jCas);
					vb.setText(verbPhrase);
					verbPhraseList.add(vb);
					verbPhrase = "";
					verbFlag = false;
				} else if (!verbFlag)
					verbPhrase = "";
			}

		}
		verbPhrase = verbPhrase.trim();
		if (!verbPhrase.equals("") && verbFlag) {
			VerbPhrase vb = new VerbPhrase(jCas);
			vb.setText(verbPhrase);
			verbPhraseList.add(vb);
			verbFlag = false;
			verbPhrase = "";
		} else if (!verbFlag)
			verbPhrase = "";

		return verbPhraseList;
	}

}
