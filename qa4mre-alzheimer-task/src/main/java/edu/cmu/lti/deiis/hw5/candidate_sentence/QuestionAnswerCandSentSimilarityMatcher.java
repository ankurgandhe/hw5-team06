package edu.cmu.lti.deiis.hw5.candidate_sentence;

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
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

public class QuestionAnswerCandSentSimilarityMatcher  extends JCasAnnotator_ImplBase{

	SolrWrapper solrWrapper=null;
	String serverUrl;
	//IndexSchema indexSchema;
	String coreName;
	String schemaName;
	int TOP_SEARCH_RESULTS=5;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		serverUrl = (String) context.getConfigParameterValue("SOLR_SERVER_URL");
		coreName = (String) context.getConfigParameterValue("SOLR_CORE");
		schemaName = (String) context.getConfigParameterValue("SCHEMA_NAME");
		TOP_SEARCH_RESULTS = (Integer) context.getConfigParameterValue("TOP_SEARCH_RESULTS");
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
		
		for(int i=0;i<qaSet.size();i++){

			Question question=qaSet.get(i).getQuestion();
			System.out.println("========================================================");
			System.out.println("Question: "+question.getText());
			ArrayList<CandidateSentence>candidateSentList=new ArrayList<CandidateSentence>();
			
			ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet
              .get(i).getAnswerList(), Answer.class);     
			SolrQuery solrQuery[] = new SolrQuery[choiceList.size()];
			for(int j=0;j<choiceList.size();j++){
			  String searchQuery=this.formSolrQuery(question, choiceList.get(j));
			  if(searchQuery.trim().equals("")){
	        solrQuery[j]=null;
	      }else{
	        solrQuery[j] = new SolrQuery();
	        solrQuery[j].add("fq", "docid:"+testDocId);
	        solrQuery[j].add("q",searchQuery);
	        solrQuery[j].add("rows",String.valueOf(TOP_SEARCH_RESULTS));
	        solrQuery[j].setFields("*", "score");
	      }
			}
			
			try {
			  
			  SolrDocumentList results[]=new SolrDocumentList[choiceList.size()];
			  for(int j=0;j<choiceList.size();j++){
			    if(solrQuery[j]==null){
			      results[j].clear();;
			    }else{
			      results[j]=solrWrapper.runQuery(solrQuery[j], TOP_SEARCH_RESULTS);
			    }
			  }
				
			  int maxSize=0;
			  for(int j=0;j<choiceList.size();j++){
			    if(results[j].size()>maxSize){
			      maxSize=results[j].size();
			    }
			  }
			  
				for(int j=0;j<maxSize;j++){
				  for(int k=0;k<choiceList.size();k++){
				    if(k>=results[j].size()){
				      continue;
				    }
				    SolrDocument doc=results[j].get(k);					
				    String sentId=doc.get("id").toString();
				    String docId=doc.get("docid").toString();
				    if(!testDocId.equals(docId)){
				      continue;
				    }
				    String sentIdx=sentId.replace(docId,"").replace("_", "").trim();
				    int idx=Integer.parseInt(sentIdx);	
				    if (idx> sentenceList.size())
				      continue; 
					
				    Sentence annSentence=sentenceList.get(idx);
				    String sentence=doc.get("text").toString();
				    double relScore=Double.parseDouble(doc.get("score").toString());
				    CandidateSentence candSent=new CandidateSentence(aJCas);
				    candSent.setSentence(annSentence);
				    candSent.setRelevanceScore(relScore);
				    candidateSentList.add(candSent);
				    System.out.println(relScore+"\t"+sentence);
				  }
				}
				
				FSList fsCandidateSentList=Utils.fromCollectionToFSList(aJCas, candidateSentList);
				fsCandidateSentList.addToIndexes();
				qaSet.get(i).setCandidateSentenceList(fsCandidateSentList);
				qaSet.get(i).addToIndexes();
				
			} catch (SolrServerException e) {
				e.printStackTrace();
			}
			FSList fsQASet=Utils.fromCollectionToFSList(aJCas, qaSet);
			testDoc.setQaList(fsQASet);
			
			System.out.println("=========================================================");
		}
	
		
	}

	public String formSolrQuery(Question question, Answer answer){
		String solrQuery="";
		
		ArrayList<NounPhrase>nounPhrasesQ=Utils.fromFSListToCollection(question.getNounList(), NounPhrase.class);
		ArrayList<NounPhrase>nounPhrasesA=Utils.fromFSListToCollection(answer.getNounPhraseList(), NounPhrase.class);
    
		for(int i=0;i<nounPhrasesQ.size();i++){
			solrQuery+="nounphrases:\""+nounPhrasesQ.get(i).getText()+"\" ";			
		}
		
		if(!answer.getText().equals("None of the above")){
		  for(int i=0;i<nounPhrasesA.size();i++){
		    solrQuery+="nounphrases:\""+nounPhrasesA.get(i).getText()+"\" ";      
		  }
		}
		
		ArrayList<NER>neListQ=Utils.fromFSListToCollection(question.getNerList(), NER.class);
		ArrayList<NER>neListA=Utils.fromFSListToCollection(answer.getNerList(), NER.class);
		
		for(int i=0;i<neListQ.size();i++){
			solrQuery+="namedentities:\""+neListQ.get(i).getText()+"\" ";
		}
		
		if(!answer.getText().equals("None of the above")){
		  for(int i=0;i<neListA.size();i++){
		    solrQuery+="namedentities:\""+neListA.get(i).getText()+"\" ";
		  }
		}
		
		ArrayList<VerbPhrase>verbPhrases=Utils.fromFSListToCollection(question.getVerbList(), VerbPhrase.class);
		
		for(int i=0;i<verbPhrases.size();i++){
              solrQuery+="verbphrases:\""+verbPhrases.get(i).getText()+"\" ";                
		}
		
		solrQuery=solrQuery.trim();
		System.out.println(solrQuery);
		return solrQuery;
	}

}
