package edu.cmu.lti.deiis.hw5.answer_ranking;

import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.utils.Utils;

public class AnswerCandSentSimilarityMatcher  extends JCasAnnotator_ImplBase{

	SolrWrapper solrWrapper=null;
	String serverUrl;
	//IndexSchema indexSchema;
	String coreName;
	String schemaName;
	int TOP_SEARCH_RESULTS=100;
	int TOP_MATCH_RESULTS = 10;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		serverUrl = (String) context.getConfigParameterValue("SOLR_SERVER_URL");
		coreName = (String) context.getConfigParameterValue("SOLR_CORE");
		schemaName = (String) context.getConfigParameterValue("SCHEMA_NAME");
		TOP_SEARCH_RESULTS = (Integer) context.getConfigParameterValue("TOP_SEARCH_RESULTS");
		TOP_MATCH_RESULTS = (Integer) context.getConfigParameterValue("TOP_MATCH_RESULTS");
		try {
			this.solrWrapper = new SolrWrapper(serverUrl+coreName);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		TestDocument testDoc=Utils.getTestDocumentFromCAS(aJCas);
		String testDocId=testDoc.getId();
		ArrayList<Sentence>sentenceList=Utils.getSentenceListFromTestDocCAS(aJCas);
		ArrayList<QuestionAnswerSet>qaSet=Utils.getQuestionAnswerSetFromTestDocCAS(aJCas);
		
		System.out.println("**********************search answer*******************");
		
		for(int i=0;i<qaSet.size();i++){	
			
			Question question=qaSet.get(i).getQuestion();
			
      ArrayList<NounPhrase> questionNouns = Utils.fromFSListToCollection(question.getNounList(),NounPhrase.class);
      ArrayList<NER> questionNERs = Utils.fromFSListToCollection(question.getNerList(), NER.class);
			
			System.out.println("========================================================");
			System.out.println("Question: "+question.getText());
			
			ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet
		          .get(i).getAnswerList(), Answer.class);     
			
			for(int j=0;j<choiceList.size();j++){
			  Answer answer=choiceList.get(j);
			  String searchQuery=this.formSolrQuery(answer);
	      if(searchQuery.trim().equals("")){
	        choiceList.get(j).setAnswerScore(0);
	        continue;
	      }
	      
	      System.out.println("========================================================");
	      System.out.println("Answer: "+answer.getText());
	      
	      
	      ArrayList<CandidateSentence>candidateSentList=new ArrayList<CandidateSentence>();
	      SolrQuery solrQuery=new SolrQuery();
	      solrQuery.add("fq", "docid:"+testDocId);
	      solrQuery.add("q",searchQuery);
	      solrQuery.add("rows",String.valueOf(TOP_SEARCH_RESULTS));
	      solrQuery.setFields("*", "score");
	      int resultNum=0;
	      try {
	        SolrDocumentList results=solrWrapper.runQuery(solrQuery, TOP_SEARCH_RESULTS);
	        resultNum=results.size();
	        for(int k=0;k<results.size();k++){
	          SolrDocument doc=results.get(k);          
	          String sentId=doc.get("id").toString();
	          String docId=doc.get("docid").toString();
	          if(!testDocId.equals(docId)){
	            continue;
	          }
	          String sentIdx=sentId.replace(docId,"").replace("_", "").trim();
	          int idx=Integer.parseInt(sentIdx);
	          if (idx >= sentenceList.size())
	        	  continue;
	          Sentence annSentence=sentenceList.get(idx);
	          
	          double relScore=Double.parseDouble(doc.get("score").toString());
	          CandidateSentence candSent=new CandidateSentence(aJCas);
	          candSent.setSentence(annSentence);
	          candSent.setRelevanceScore(relScore);
	          candidateSentList.add(candSent);
	          
	        }
	        for(int k1=0;k1<candidateSentList.size();k1++){
	          for(int k2=k1+1;k2<candidateSentList.size();k2++){
	            if(candidateSentList.get(k1).getRelevanceScore()<candidateSentList.get(k2).getRelevanceScore()){
	              CandidateSentence t;
	              t=candidateSentList.get(k1);
	              candidateSentList.set(k1, candidateSentList.get(k2));
	              candidateSentList.set(k2,t);
	            }
	          }
	        }
	        for(int k=0;k<Math.min(candidateSentList.size(),TOP_MATCH_RESULTS);k++){
	          System.out.println(candidateSentList.get(k).getRelevanceScore()+"\t"+candidateSentList.get(k).getSentence().getText());
	        }
	        
	      } catch (SolrServerException e) {
	        e.printStackTrace();
	      }
	      
	      
	      ////
	      double totalScore=0;
	      for (int k = 0; k < Math.min(candidateSentList.size(),TOP_MATCH_RESULTS); k++) {

	        CandidateSentence candSent = candidateSentList.get(k);
	        ArrayList<NounPhrase> candSentNouns = Utils
	            .fromFSListToCollection(candSent.getSentence()
	                .getPhraseList(), NounPhrase.class);
	        ArrayList<NER> candSentNers = Utils.fromFSListToCollection(
	            candSent.getSentence().getNerList(), NER.class);

	          int nnMatch = 0;
	          for (int c = 0; c < candSentNouns.size(); c++) {
	            for (int l = 0; l < questionNERs.size(); l++) {
	              if (candSentNouns.get(c).getText()
	                  .contains(questionNERs.get(l).getText())
	                  ||
	                  questionNERs.get(l).getText()
	                  .contains(candSentNouns.get(c).getText())) {
	                nnMatch++;
	              }
	            }
	            for (int l = 0; l < questionNouns.size(); l++) {
	              if (candSentNouns.get(c).getText()
	                  .contains(questionNouns.get(l).getText())
	                  ||
	                  questionNouns.get(l).getText()
	                  .contains(candSentNouns.get(c).getText())) {
	                nnMatch++;
	              }
	            }
	          }

	          for (int c = 0; c < candSentNers.size(); c++) {
	            for (int l = 0; l < questionNERs.size(); l++) {
	              if (candSentNouns.get(c).getText()
	                  .contains(questionNERs.get(l).getText())
	                  ||
	                  questionNERs.get(l).getText()
	                  .contains(candSentNouns.get(c).getText())) {
	                nnMatch++;
	              }
	            }
	            for (int l = 0; l < questionNouns.size(); l++) {
	              if (candSentNouns.get(c).getText()
	                  .contains(questionNouns.get(l).getText())
	                  ||
	                  questionNouns.get(l).getText()
	                  .contains(candSentNouns.get(c).getText())) {
	                nnMatch++;
	              }
	            }
	          }
	          
	          totalScore = totalScore + nnMatch;
	      }
	      if(resultNum!=0){
	      choiceList.get(j).setAnswerScore((double)totalScore/Math.min(candidateSentList.size(),TOP_MATCH_RESULTS));
	      }
	      else{
	      choiceList.get(j).setAnswerScore(0);
	      }
	      System.out.println(choiceList.get(j).getText()+":"+choiceList.get(j).getAnswerScore());
	      System.out.println("========================================================");
	      
			}
			
			System.out.println("========================================================");
			
			FSList fsAnswerList=Utils.fromCollectionToFSList(aJCas, choiceList);
			fsAnswerList.addToIndexes();
			qaSet.get(i).setAnswerList(fsAnswerList);;
			qaSet.get(i).addToIndexes();
		}
			
		FSList fsQASet=Utils.fromCollectionToFSList(aJCas, qaSet);
    testDoc.setQaList(fsQASet);
     
    System.out.println("********************************************************");
   }
			

	public String formSolrQuery(Answer answer){
		String solrQuery="";
		
		ArrayList<NounPhrase>nounPhrases=Utils.fromFSListToCollection(answer.getNounPhraseList(), NounPhrase.class);
		
		for(int i=0;i<nounPhrases.size();i++){
			solrQuery+="nounphrases:\""+nounPhrases.get(i).getText()+"\" ";			
		}
		
		ArrayList<NER>neList=Utils.fromFSListToCollection(answer.getNerList(), NER.class);
		for(int i=0;i<neList.size();i++){
			solrQuery+="namedentities:\""+neList.get(i).getText()+"\" ";
		}
		solrQuery=solrQuery.trim();
		
		return solrQuery;
	}

}
