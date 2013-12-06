package edu.cmu.lti.deiis.hw5.annotators;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.SetUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.deiis.hw5.utils.SetUtil;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Dependency;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.utils.Utils;

public class QuestionCategoryAnnotator extends JCasAnnotator_ImplBase{

	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		TestDocument testDoc=Utils.getTestDocumentFromCAS(aJCas);
				
		ArrayList<Question>questionList=Utils.getQuestionListFromTestDocCAS(aJCas);
		
		for(int i=0;i<questionList.size();i++){
			
			Question question=questionList.get(i);
			
			//get tokens from question
			ArrayList<Token>tokenList= Utils.getTokenListFromQuestion(question);
			
			//decided to do noun phrase search manually, since this has slightly different requirements
			//get dependencies and noun phrases and convert to ArrayList
			/*FSList depFSList = question.getDependencies();
      ArrayList<Dependency> depList = new ArrayList<Dependency>();
      depList = Utils.fromFSListToCollection(depFSList,Dependency.class);
      
      FSList nounFSList = question.getNounList();
      ArrayList<NounPhrase> nounList = new ArrayList<NounPhrase>();
      nounList = Utils.fromFSListToCollection(nounFSList,NounPhrase.class);*/
			
      //get question text
			String qText = question.getText();
			
			//order who, what, when, where, why, how, which, how many, how much
			ArrayList<Boolean> whWords = new ArrayList<Boolean>();
		  
			//create regex matchers for different wh-words
      Matcher whoMatch = Pattern.compile("[Ww]ho").matcher(qText);
      Matcher whatMatch = Pattern.compile("[Ww]hat").matcher(qText);
      Matcher whenMatch = Pattern.compile("[Ww]hen").matcher(qText);
      Matcher whereMatch = Pattern.compile("[Ww]here").matcher(qText);
      Matcher whyMatch = Pattern.compile("[Ww]hy").matcher(qText);
      Matcher howMatch = Pattern.compile("[Hh]ow(?! (many|much))").matcher(qText);
      Matcher whichMatch = Pattern.compile("[Ww]hich").matcher(qText);
      Matcher howmanyMatch = Pattern.compile("[Hh]ow many").matcher(qText);
      Matcher howmuchMatch = Pattern.compile("[Hh]ow much").matcher(qText);
      
    //check for question words (i.e. wh-words, how many, how much, etc.)
      whWords.add(whoMatch.find());
      whWords.add(whatMatch.find());
      whWords.add(whenMatch.find());
      whWords.add(whereMatch.find());
      whWords.add(whyMatch.find());
      whWords.add(howMatch.find());
      whWords.add(whichMatch.find());
      whWords.add(howmanyMatch.find());
      whWords.add(howmuchMatch.find());
      
      //get number of wh-expressions in the question
      int numMatch = 0;
      for (int j=0;j<whWords.size();j++){numMatch+= whWords.get(j)? 1 : 0;}
      
      if (numMatch < 2){
        //if there's only one or no wh-word, set the category accordingly
        if (whWords.get(0)){question.setCategory("who");}
        else if (whWords.get(1)){question.setCategory("what");}
        else if (whWords.get(2)){question.setCategory("when");}
        else if (whWords.get(3)){question.setCategory("where");}
        else if (whWords.get(4)){question.setCategory("why");}
        else if (whWords.get(5)){question.setCategory("how");}
        else if (whWords.get(6)){question.setCategory("which");}
        else if (whWords.get(7)){question.setCategory("howmany");}
        else if (whWords.get(8)){question.setCategory("howmuch");}
        else {question.setCategory("other");}
      } else {
        //if there is more than one wh-word, only set the category for the one that has a W POS tag
        question.setCategory("other");
        Boolean whFound = false;
        for(int j=0;j<tokenList.size();j++){
          Token t = tokenList.get(j);
          String tPos = t.getPos();
          String word = t.getText().toLowerCase();
          if (tPos.startsWith("W") && !whFound){
            if (word.startsWith("who")){question.setCategory("who");whFound=true;}
            else if (word.startsWith("what")){question.setCategory("what");whFound=true;}
            else if (word.startsWith("when")){question.setCategory("when");whFound=true;}
            else if (word.startsWith("where")){question.setCategory("where");whFound=true;}
            else if (word.startsWith("why")){question.setCategory("why");whFound=true;}
            else if (word.startsWith("how")){
              if (j<tokenList.size()-1){
                String next = tokenList.get(j+1).getText().toLowerCase();
                if (next.startsWith("many")){question.setCategory("howmany");whFound=true;}
                else if (next.startsWith("much")){question.setCategory("howmuch");whFound=true;}
                else {question.setCategory("how");whFound=true;}
              }else{question.setCategory("how");whFound=true;}
            }
            else if (word.startsWith("which")){question.setCategory("which");whFound=true;}
            else {question.setCategory("other");}
          }else if (tPos.startsWith("W") && whFound){question.setCategory("conflict");}
        }
      }

			//question.addToIndexes();
			questionList.set(i, question);
			
			/*System.out.println("Question " + (i+1) + ": " + question.getText());
			System.out.print("Category: " + question.getCategory());
			if (numMatch>1) {
			  System.out.println("\t\t   who    what  when   where   why    how    which howmany howmuch");
	      System.out.println("\t\t\t" + numMatch + " " + whWords);  
			} else {System.out.println();}
			System.out.println();*/
			
			//SKT words are used in constructions like "What TYPE OF hormone is that?"
      String skt = "sort,kind,type,brand,category,class,kin,manner,species,variety,sorts,kinds,types,brands,categories,classes,manners,varieties";
			Set<String> sktwords = SetUtil.addStringArray(null, skt.split(","));
			
			ArrayList<Token> askingTokens = new ArrayList<Token>();
			
			//iterate through tokens to find the complement of which/what if they're WDT words
			if (question.getCategory()=="which" | question.getCategory()=="what"){
			  //System.out.println("Question " + (i+1) + ": " + question.getText());
			  
			  Boolean whFlag = false;
			  Boolean nounFlag = false;
			  String asking = "";
			  for(int j=0;j<tokenList.size();j++){
			    Token t = tokenList.get(j);
			    String tPos = t.getPos();
			    String word = t.getText();
			    Boolean copula = false;
			    if (word.equalsIgnoreCase("is")|word.equalsIgnoreCase("are")|word.equalsIgnoreCase("be")){
			      copula = true;
			    }
			    if (tPos.equals("WDT")){
			      whFlag = true;
			    } else if (whFlag) {
			      //if the wh-word is followed by the copula, like in "What is used for blah?"
			      //then the question is not asking for something specific that can be identified
			      if (tPos.startsWith("VB") && !copula && !nounFlag){
			        break;
			      }else if (tPos.startsWith("NN")){
              asking+=word+" ";
              askingTokens.add(t);
              nounFlag = true;
            }else if (tPos.startsWith("JJ") && !nounFlag){
              asking+=word+" ";
              askingTokens.add(t);
            }else if (word.equals("of") && j>0){
              //if the question asks for "What type of X?" then we want X, not "type"
              if (sktwords.contains(tokenList.get(j-1).getText().toLowerCase())){
                //so if the previous token is an SKT word, clear askingFor and keep looking
                asking = "";
                askingTokens.clear();
                nounFlag = false;
              } else {
                //if it was not an SKT word, like in "Which color of the rainbow"
                //then stop and use the noun phrase we found so far
                asking=asking.trim();
                if(!asking.equals("") && nounFlag){
                  asking=asking.trim();
                  question.setAskingFor(asking);
                  question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                  break;
                } else if (!nounFlag) {asking="";askingTokens.clear();}
              }
            }else{
              asking=asking.trim();
              if(!asking.equals("") && nounFlag){
                asking=asking.trim();
                question.setAskingFor(asking);
                question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                break;
              } else if (!nounFlag) {asking="";askingTokens.clear();}
            }
			    }
			  }
			  
			  System.out.println("Category: " + question.getCategory() + "  \t" + "Asking for " + question.getAskingFor());
			  System.out.println();
			}
			
			//iterate through tokens to find out what type of thing howmany is asking for a quantity of
			if (question.getCategory()=="howmany"){
        //System.out.println("Question " + (i+1) + ": " + question.getText());
        
        Boolean howFlag = false;
        Boolean nounFlag = false;
        String asking = "";
        for(int j=0;j<tokenList.size();j++){
          Token t = tokenList.get(j);
          String tPos = t.getPos();
          String word = t.getText();
          if (tPos.equals("WRB")){
            //we found the how, j++ to skip over many
            howFlag = true;
            j++;
          } else if (howFlag) {
            if (tPos.startsWith("NN")){
              asking+=word+" ";
              askingTokens.add(t);
              nounFlag = true;
            }else if (tPos.startsWith("JJ") && !nounFlag){
              asking+=word+" ";
              askingTokens.add(t);
            }else if (word.equals("of") && j>0){
              //if the question asks for "How many types of X?" then we want X, not "types"
              if (sktwords.contains(tokenList.get(j-1).getText().toLowerCase())){
                //so if the previous token is an SKT word, clear askingFor and keep looking
                asking = "";
                askingTokens.clear();
                nounFlag = false;
              } else {
                //if it was not an SKT word, like in "How many colors of the rainbow"
                //then stop and use the noun phrase we found so far
                asking=asking.trim();
                if(!asking.equals("") && nounFlag){
                  asking=asking.trim();
                  question.setAskingFor(asking);
                  question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                  break;
                } else if (!nounFlag) {asking="";askingTokens.clear();}
              }
            }else{
              asking=asking.trim();
              if(!asking.equals("") && nounFlag){
                asking=asking.trim();
                question.setAskingFor(asking);
                question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                break;
              } else if (!nounFlag) {asking="";askingTokens.clear();}
            }
          }
        }
        
        //System.out.println("Category: " + question.getCategory() + "  \t" + "Asking for " + question.getAskingFor());
        //System.out.println();
      }
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

}
