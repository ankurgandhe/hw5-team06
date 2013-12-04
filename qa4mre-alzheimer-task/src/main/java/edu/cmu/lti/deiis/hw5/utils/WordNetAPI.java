package edu.cmu.lti.deiis.hw5.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.cmu.lti.deiis.hw5.constants.WordNetConstants;
import edu.smu.tspell.wordnet.AdjectiveSynset;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;
import edu.smu.tspell.wordnet.impl.file.synset.AdverbReferenceSynset;
import edu.smu.tspell.wordnet.impl.file.synset.VerbReferenceSynset;
import static edu.cmu.lti.deiis.hw5.utils.SetUtil.*;

public class WordNetAPI {
	private static Map<String, Set<String>> cache = null;
	private static  DistributionalSimilarity ds=null;
	private static   Set<String> wordNetStopWords=null;
public static String stopLine="entity,abstraction,concept,idea,abstract entity,act,creation,activity,deed,unit,action,whole,thing,system,physical entity,material,set,collection,group,object,physical object,event";
	static {

		cache = new HashMap<String, Set<String>>();
		System.setProperty(WordNetConstants.DIR_ATT, WordNetConstants.DIR_PATH);
		wordNetStopWords=checkSet(wordNetStopWords);

		SetUtil.addStringArray(wordNetStopWords,(stopLine.split(",")));
		System.out.println("<<<<<<<<<<<<<<<<###############################>>>>>>>>>>>>>>>>>>");
		for(String string:wordNetStopWords)
		{
			System.out.println(string);
		}
		System.out.println("<<<<<<<<<<<<<<<<###############################>>>>>>>>>>>>>>>>>>");
		
		
//		 ds=new DistributionalSimilarity();
	//	 String filename = "./model/alzheimer.tok.model.320";
		// filename="/home/richie/git/hw5-skohli/hw5-team06/qa4mre-alzheimer-task/model/alzheimer.tok.model.320";
		//	try {
			//	ds.readModel(filename);			
			///} catch (IOException e) {
		//		e.printStackTrace();
			//}
	}

	public static Set<String> getHyponyms(String word, Set<String> set) {

		Set<String> cachedWords = cache.get(word);
		if (cachedWords != null)
			return cachedWords;

		WordNetDatabase database = WordNetDatabase.getFileInstance();
		// database.getBaseFormCandidates(word,SynsetType.ADJECTIVE );
		
		set = checkSet(set);
		set = getNounHyponyms(word, set);
		set = getVerbHyponyms(word, set);
		set = getAdjectiveHyponyms(word, set);
		set = getAdverbHyponyms(word, set);
if(false)
		set=addStringArray(set,ds.testModel(word));
		cache.put(word, set);
		return set;

	}
	public static boolean getWordHypernyms(String word, Set<String> set, String req, Set<String> checked) {
		set = checkSet(set);	
		checked = checkSet(checked);	
		//checked
		if(word.equals(req))
			return true;
		
		//if(word.equalsIgnoreCase("entity")||word.equalsIgnoreCase("abstraction")||word.equalsIgnoreCase("concept")||word.equalsIgnoreCase("idea")||word.equalsIgnoreCase("abstract entity")||)
		
		if(wordNetStopWords.contains(word))
		{System.out.println("returned false for "+word);
			return false;
		}

		if(checked.contains(word))
		{System.out.println("returned false for "+word);
			return false;
		}
		String[] multi=word.split(" ");
		for(String currWord:multi)
		{
			if(wordNetStopWords.contains(currWord))
				return false;
		}
		System.out.println(word);

		NounSynset nounSynset;
		// VerbReferenceSynset nounSynset;
		//NounSynset[] hyponyms;
		NounSynset[] hypernyms;

		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Queue<String> queue = new LinkedList<String>();	
		Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
		for (int i = 0; i < synsets.length; i++) {
			 nounSynset = (NounSynset) (synsets[i]);
			// hyponyms = nounSynset.getHyponyms();
			 hypernyms=nounSynset.getHypernyms();
		
			Set<String> checkedWordset = checkSet(null);	
				
			//queue.add("");

			for (int j = 0; j < hypernyms.length; j++) {
					NounSynset hpn = hypernyms[j];
					//hpn.get
				//	System.out.println(hpn);
					addStringArraytoQueue(queue, hpn.getWordForms());
				
					SetUtil.addStringArray(set, hpn.getWordForms());
					
	//				if(j==0)
			//	for(String words:queue)
				
					while(!queue.isEmpty())
					{String	words=queue.poll();
						if(!checkedWordset.contains(words))
					{	
						if(getWordHypernyms(words,set,req,checked))
							return true;
					}
					}
				
					SetUtil.addStringArray(checkedWordset,hpn.getWordForms());
				
				}

		
		
		}
		return false; 
	}
	public static Set<String> getNounHyponyms(String word, Set<String> set) {

		set = checkSet(set);

		// wordnet-sense-index
		NounSynset nounSynset;
		// VerbReferenceSynset nounSynset;
		NounSynset[] hyponyms;
		NounSynset[] hypernyms;

		WordNetDatabase database = WordNetDatabase.getFileInstance();
		// database.getBaseFormCandidates(word, SynsetType.NOUN);
		// Synset[] synsets = database.getSynsets(word, SynsetType.VERB);
		Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
		// database.get
		for (int i = 0; i < synsets.length; i++) {
			nounSynset = (NounSynset) (synsets[i]);
			hyponyms = nounSynset.getHyponyms();
			hypernyms = nounSynset.getHypernyms();
			SetUtil.addStringArray(set, nounSynset.getWordForms());
			//hypernyms.
			for (int j = 0; j < hyponyms.length; j++) {
				NounSynset hpn = hyponyms[j];
				SetUtil.addStringArray(set, hpn.getWordForms());
			}
			
			if(false)

			for (int j = 0; j < hypernyms.length; j++) {
				NounSynset hpn = hypernyms[j];
				SetUtil.addStringArray(set, hpn.getWordForms());
				
				
			}

		}
		return set;

	}

	public static Set<String> getVerbHyponyms(String word, Set<String> set) {

		set = checkSet(set);

		VerbReferenceSynset nounSynset;
		VerbSynset[] hyponyms;
		VerbSynset[] troponyms;
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(word, SynsetType.VERB);
		// database.get
		for (int i = 0; i < synsets.length; i++) {
			// nounSynset = (NounSynset)(synsets[i]);
			nounSynset = (VerbReferenceSynset) (synsets[i]);
			hyponyms = nounSynset.getHypernyms();
			troponyms = nounSynset.getTroponyms();

			SetUtil.addStringArray(set, nounSynset.getWordForms());

			for (int k = 0; k < troponyms.length; k++) {
				VerbSynset syn = troponyms[k];
				SetUtil.addStringArray(set, syn.getWordForms());
			}

			for (int j = 0; j < hyponyms.length; j++) {
				VerbSynset hpn = hyponyms[j];
				SetUtil.addStringArray(set, hpn.getWordForms());
			
			
			
			}

		}

		return set;

	}

	public static Set<String> getAdjectiveHyponyms(String word, Set<String> set) {
		// Set<String> set=new HashSet<String>();
		set = checkSet(set);

		AdjectiveSynset adjSynset;
		AdjectiveSynset[] related;
		AdjectiveSynset[] similar;
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(word, SynsetType.ADJECTIVE);

		for (int i = 0; i < synsets.length; i++) {
			adjSynset = (AdjectiveSynset) (synsets[i]);
			related = adjSynset.getRelated();
			similar = adjSynset.getSimilar();

			SetUtil.addStringArray(set, adjSynset.getWordForms());

			for (int j = 0; j < similar.length; j++) {
				AdjectiveSynset adjectiveSynset = similar[j];
				SetUtil.addStringArray(set, adjectiveSynset.getWordForms());
			}

			for (int j = 0; j < related.length; j++) {
				AdjectiveSynset hpn = related[j];
				SetUtil.addStringArray(set, hpn.getWordForms());
			}

		}

		return set;

	}

	public static Set<String> getAdverbHyponyms(String word, Set<String> set) {
		// Set<String> set=new HashSet<String>();
		set = checkSet(set);
		AdverbReferenceSynset adverbSynset;
		WordSense[] related;
		// AdverbReferenceSynset[] similar;
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(word, SynsetType.ADVERB);
		Set<String> local = new HashSet<String>();
		for (int i = 0; i < synsets.length; i++) {
			adverbSynset = (AdverbReferenceSynset) (synsets[i]);
			SetUtil.addStringArray(local, adverbSynset.getWordForms());

			// for(adverbSynset.get)
			{
				related = adverbSynset.getPertainyms(word);
				// similar = adjSynset.get

				WordSense[] z = adverbSynset
						.getDerivationallyRelatedForms(word);

				for (WordSense rel : related) {
					local.add(rel.getWordForm());
					// SetUtil.addStringArray(set, rel.getWordForm());
				}

				for (WordSense rel : z) {
					local.add(rel.getWordForm());
					// SetUtil.addStringArray(set, rel.getWordForm());
				}
			}
			/*
			 * for (int j = 0; j < similar.length; j++) { AdjectiveSynset
			 * adjectiveSynset = similar[j]; SetUtil.addStringArray(set,
			 * adjectiveSynset.getWordForms()); }
			 * 
			 * for (int j = 0; j < related.length; j++) { AdjectiveSynset hpn =
			 * related[j]; SetUtil.addStringArray(set, hpn.getWordForms()); }
			 */

		}

		for (String lword : local) {
			set = getNounHyponyms(lword, set);
			set = getVerbHyponyms(lword, set);
			set = getAdjectiveHyponyms(lword, set);
		set.add(lword);
		}

		
		
		
		return set;

	}

	public static void main(String args[]) {
		//Set<String> hyponymList = getHyponyms("mouse", null);

		
		// Set<String> hyponymList=getVerbHyponyms("transform",null);
		// Set<String> hyponymList=getAdjectiveHyponyms("beautiful",null);
		// Set<String> hyponymList =getAdverbHyponyms("swiftly", null);
		/*System.out.println("--------------------hyponymList-------------");
		for (String hpm : hyponymList) {
			System.out.println(hpm);

		}
		
*/		
		boolean bl = getWordHypernyms("spider", null,"insect",null);
		
		System.out.println(bl);
	}

}

