package edu.cmu.lti.deiis.hw5.annotators;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.deiis.hw5.utils.SynonymUtil;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

public class QuestionPhraseAnnotator extends JCasAnnotator_ImplBase{

	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		TestDocument testDoc=Utils.getTestDocumentFromCAS(aJCas);
				
		ArrayList<Question>questionList=Utils.getQuestionListFromTestDocCAS(aJCas);
		ArrayList<ArrayList<Answer>>answerList=Utils.getAnswerListFromTestDocCAS(aJCas);
		
		for(int i=0;i<questionList.size();i++){
			
			Question question=questionList.get(i);
			ArrayList<Token>tokenList= Utils.getTokenListFromQuestion(question);
			ArrayList<NounPhrase>phraseList=extractNounPhrases(tokenList,aJCas);
		
			//do something here
			FSList fsPhraseList=Utils.createNounPhraseList(aJCas, phraseList);
			fsPhraseList.addToIndexes(aJCas);
			question.setNounList(fsPhraseList);
			//Extract verb phrases 
			ArrayList<VerbPhrase> verbPhraseList = extractVerbPhrases(tokenList,
					aJCas);
			FSList fsVerbPhraseList = Utils.createVerbPhraseList(aJCas, verbPhraseList);
			fsVerbPhraseList.addToIndexes(aJCas);
			question.setVerbList(fsVerbPhraseList);
			
			question.addToIndexes();
			questionList.set(i, question);
		}
		
		for(int i=0;i<answerList.size();i++){
			
			ArrayList<Answer> choiceList=answerList.get(i);
			for(int j=0;j<choiceList.size();j++){
				Answer ans=choiceList.get(j);
				ArrayList<Token>tokenList= Utils.fromFSListToCollection(ans.getTokenList(),Token.class);
				ArrayList<NounPhrase>phraseList=extractNounPhrases(tokenList,aJCas);
				SynonymUtil.populateSynonyms(phraseList, aJCas);

				FSList fsPhraseList=Utils.createNounPhraseList(aJCas, phraseList);
				fsPhraseList.addToIndexes(aJCas);							
				ans.setNounPhraseList(fsPhraseList);
				//Extract VP
				ArrayList<VerbPhrase> verbPhraseList = extractVerbPhrases(
						tokenList, aJCas);
				FSList fsVerbPhraseList = Utils.createVerbPhraseList(aJCas,
						verbPhraseList);
				fsVerbPhraseList.addToIndexes(aJCas);
				ans.setVerbPhraseList(fsVerbPhraseList);
				
				ans.addToIndexes();
				choiceList.set(j, ans);
			}
			
			answerList.set(i, choiceList);
			
		}
		
		
		//FSList fsQuestionList=Utils.createQuestionList(aJCas, questionList);
		//testDoc.setQuestionList(fsQuestionList);
		
		ArrayList<QuestionAnswerSet>qaSet=Utils.getQuestionAnswerSetFromTestDocCAS(aJCas);
		for(int i=0;i<qaSet.size();i++){
			questionList.get(i).addToIndexes();
			qaSet.get(i).setQuestion(questionList.get(i));
		}
		FSList fsQASet=Utils.createQuestionAnswerSet(aJCas, qaSet);
		
		testDoc.setQaList(fsQASet);
		testDoc.addToIndexes();
		
	}
	
	public ArrayList<NounPhrase> extractNounPhrases(ArrayList<Token> tokenList,JCas jCas){
		
		ArrayList<NounPhrase>nounPhraseList=new ArrayList<NounPhrase>();
		String nounPhrase="";
		Boolean nounFlag = false;
		for(int i=0;i<tokenList.size();i++){
			Token token=tokenList.get(i);
			String word=token.getText();
			String pos=token.getPos();
			//System.out.println("Token: "+word+"/"+pos);
			if(pos.startsWith("NN")){
				nounPhrase+=word+" ";
				nounFlag = true;
			}else if ((pos.startsWith("JJ") || pos.startsWith("CD") ) && !nounFlag ){
			  nounPhrase+=word+" ";
			}else{
				nounPhrase=nounPhrase.trim();
				if(!nounPhrase.equals("") && nounFlag){
					NounPhrase nn=new NounPhrase(jCas);
					nounPhrase=nounPhrase.trim();
					nn.setText(nounPhrase);
					nounPhraseList.add(nn);
					//System.out.println("Noun Phrase: "+nounPhrase);
					nounPhrase="";
					nounFlag = false;
				} else if (!nounFlag) {nounPhrase="";}
			}
					
		}
		nounPhrase=nounPhrase.trim();
		if(!nounPhrase.equals("")){
			NounPhrase nn=new NounPhrase(jCas);
			nn.setText(nounPhrase);
			nounPhraseList.add(nn);
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
			}
			else if  (pos.startsWith("RB") || pos.startsWith("RP") ) {
				verbPhrase += word + " ";
			} else {
				verbPhrase = verbPhrase.trim();
				
				if (!verbPhrase.equals("") && verbFlag) {
					VerbPhrase vb = new VerbPhrase(jCas);
					vb.setText(verbPhrase);
					verbPhraseList.add(vb);
					verbPhrase = "";
					verbFlag=false;
				}
				else
					if (!verbFlag)
						verbPhrase ="";
			}

		}
		verbPhrase = verbPhrase.trim();
		if (!verbPhrase.equals("") && verbFlag) {
			VerbPhrase vb = new VerbPhrase(jCas);
			vb.setText(verbPhrase);
			verbPhraseList.add(vb);
			verbFlag=false;
			verbPhrase="";
		}
		else
			if (!verbFlag)
				verbPhrase ="";

		return verbPhraseList;
	}

}
