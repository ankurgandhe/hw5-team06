package edu.cmu.lti.deiis.hw5.utils;

import java.util.List;

import org.apache.uima.jcas.cas.FSList;

import edu.cmu.lti.deiis.hw5.constants.WordNetConstants;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.utils.Utils;

public class SentenceUtils

{
	
	static boolean checkNPList(
			List<NounPhrase> list, String cat)
	{
		for(NounPhrase np:list)
		{
		String nptext=np.getText()	;

		if(WordNetAPI.getWordHypernyms(nptext, null, cat, null,WordNetConstants.WORDNET_HYPERNYM_DEPTH))
			return true;

		if(checkNPList(nptext,cat))
				return true;
			

		

		}
		return false;}

	
	
	
	static boolean checkNPList(
		String text, String cat)
	{
		String[] terms=text.split(" ");
		
		for(String np:terms)
		{
		String nptext=np;

		if(WordNetAPI.getWordHypernyms(nptext, null, cat, null,WordNetConstants.WORDNET_HYPERNYM_DEPTH))
			return true;

		}
		
		
		return false;}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	static boolean doesAnsweMatchCategory(Answer ans, String cat)
{
	
FSList fslist=ans.getNounPhraseList();


List<NounPhrase> list=Utils.fromFSListToCollection(fslist, NounPhrase.class);

return checkNPList(list,cat);
}





static boolean doesCandAnswerMatchCategory(CandidateAnswer cand,String cat)
{String text=cand.getText();

if(checkNPList(text,cat))
	return true;

String coveredText=cand.getCoveredText();
if(checkNPList(coveredText,cat))
	return true;

return false;
}

static boolean doesSentenceMatchCategory(Sentence sent, String cat)
{
	FSList fslist=	sent.getPhraseList();

	List<NounPhrase> list=Utils.fromFSListToCollection(fslist, NounPhrase.class);
	return checkNPList(list,cat);

}








}