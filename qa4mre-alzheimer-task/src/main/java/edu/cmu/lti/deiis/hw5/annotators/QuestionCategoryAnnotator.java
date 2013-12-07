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
      
      //set category accordingly if the question contains only one wh-word
      if (numMatch < 2){
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
        //else only set the category to the one that's POS-tagged appropriately
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
            //but maybe we can't resolve the conflict 
          }else if (tPos.startsWith("W") && whFound){question.setCategory("conflict");}
        }
      }

			//question.addToIndexes();
			questionList.set(i, question);
			
			//SKT words are like in "what TYPE of hormone" or "what KIND of dog"
      String skt = "sort,kind,type,brand,category,class,kin,manner,species,variety,sorts,kinds,types,brands,categories,classes,manners,varieties";
			Set<String> sktwords = SetUtil.addStringArray(null, skt.split(","));
			
			//ArrayList<Token> askingTokens = new ArrayList<Token>();
			
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
			      //if the question is like "What is used for __" then we don't have a specific askingFor
			      if (tPos.startsWith("VB") && !copula && !nounFlag){
			        break;
			      }else if (tPos.startsWith("NN")){
              asking+=word+" ";
              //askingTokens.add(t);
              nounFlag = true;
            }else if (tPos.startsWith("JJ") && !nounFlag){
              asking+=word+" ";
              //askingTokens.add(t);
            }else if (word.equals("of") && j>0){
              //we eliminate the SKT words if it asks for "What type of hormone?" or similar
              if (sktwords.contains(tokenList.get(j-1).getText().toLowerCase())){
                asking = "";
                //askingTokens.clear();
                nounFlag = false;
              } else {
                asking=asking.trim();
                if(!asking.equals("") && nounFlag){
                  asking=asking.trim();
                  question.setAskingFor(asking);
                  //question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                  break;
                } else if (!nounFlag) {
                	asking="";
                	//askingTokens.clear();
                }
              }
            }else{
              asking=asking.trim();
              if(!asking.equals("") && nounFlag){
                asking=asking.trim();
                question.setAskingFor(asking);
                //question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                break;
              } else if (!nounFlag) {
            	  asking="";
            	  //askingTokens.clear();
              }
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
            //found the how word, j++ to skip over "many"
            howFlag = true;
            j++;
          } else if (howFlag) {
            if (tPos.startsWith("NN")){
              asking+=word+" ";
              //askingTokens.add(t);
              nounFlag = true;
            }else if (tPos.startsWith("JJ") && !nounFlag){
              asking+=word+" ";
              //askingTokens.add(t);
            }else if (word.equals("of") && j>0){
              //eliminate SKT words like in "How many types of amino acids are there?"
              if (sktwords.contains(tokenList.get(j-1).getText().toLowerCase())){
                asking = "";
                //askingTokens.clear();
                nounFlag = false;
              } else {
                asking=asking.trim();
                if(!asking.equals("") && nounFlag){
                  asking=asking.trim();
                  question.setAskingFor(asking);
                  //question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                  break;
                } else if (!nounFlag) {
                	asking="";
                	//askingTokens.clear();
                }
              }
            }else{
              asking=asking.trim();
              if(!asking.equals("") && nounFlag){
                asking=asking.trim();
                question.setAskingFor(asking);
                //question.setAskingForTokens(Utils.fromCollectionToFSList(aJCas,askingTokens));
                break;
              } else if (!nounFlag) {
            	  asking="";
            	  //askingTokens.clear();
              }
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
