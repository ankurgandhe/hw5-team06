package edu.cmu.lti.deiis.hw5.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;

import edu.cmu.lti.deiis.hw5.constants.WordNetConstants;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Synonym;
import edu.cmu.lti.qalab.utils.Utils;

public class SynonymUtil {

	public static List<NounPhrase> populateSynonyms(
			List<NounPhrase> phraseList, JCas jcas) {
		// phrase
		for (NounPhrase phrase : phraseList) {
			String phraseText = phrase.getCoveredText();
			FSList synFsListphrase = phrase.getSynonyms();
//if(synFsListphrase==null)
	//return null;
			// synFsListphrase.get
		List<Synonym> synList = Utils.fromFSListToCollection(synFsListphrase, Synonym.class);
			String[] phraseWord = phraseText.split(" ");
			Set<String> wordNetSynList = null;
			for (String word : phraseWord) {
				wordNetSynList = WordNetAPI.getHyponyms(word, wordNetSynList);

			}
			synList=	mergeWordNet(synList, wordNetSynList, jcas);

			synFsListphrase=Utils.fromCollectionToFSList(jcas, synList);
			phrase.setSynonyms(synFsListphrase);
		}
		return phraseList;
	}

	public static List<Synonym> mergeWordNet(List<Synonym> container,
			Set<String> wordNetSynList, JCas jcas)// List<String> array)
	{
		for (String text : wordNetSynList) {
			Synonym synonym = new Synonym(jcas);
			synonym.setText(text);
			synonym.setWeight(WordNetConstants.WORDNET_SYNONYM_WEIGHT);

			container.add(synonym);

		}

		return container;
	}

}
