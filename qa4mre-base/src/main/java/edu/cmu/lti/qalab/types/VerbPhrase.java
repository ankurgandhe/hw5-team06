

/* First created by JCasGen Mon Nov 18 17:20:02 EST 2013 */
package edu.cmu.lti.qalab.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Nov 25 09:03:30 EST 2013
 * XML source: C:/Users/gandhe/git/hw5-team06-task/qa4mre-base/src/main/resources/TypeSystemDescriptor.xml
 * @generated */
public class VerbPhrase extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(VerbPhrase.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected VerbPhrase() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public VerbPhrase(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public VerbPhrase(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public VerbPhrase(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: text

  /** getter for text - gets 
   * @generated */
  public String getText() {
    if (VerbPhrase_Type.featOkTst && ((VerbPhrase_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "edu.cmu.lti.qalab.types.VerbPhrase");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VerbPhrase_Type)jcasType).casFeatCode_text);}
    
  /** setter for text - sets  
   * @generated */
  public void setText(String v) {
    if (VerbPhrase_Type.featOkTst && ((VerbPhrase_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "edu.cmu.lti.qalab.types.VerbPhrase");
    jcasType.ll_cas.ll_setStringValue(addr, ((VerbPhrase_Type)jcasType).casFeatCode_text, v);}    
   
    
  //*--------------*
  //* Feature: weight

  /** getter for weight - gets 
   * @generated */
  public double getWeight() {
    if (VerbPhrase_Type.featOkTst && ((VerbPhrase_Type)jcasType).casFeat_weight == null)
      jcasType.jcas.throwFeatMissing("weight", "edu.cmu.lti.qalab.types.VerbPhrase");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((VerbPhrase_Type)jcasType).casFeatCode_weight);}
    
  /** setter for weight - sets  
   * @generated */
  public void setWeight(double v) {
    if (VerbPhrase_Type.featOkTst && ((VerbPhrase_Type)jcasType).casFeat_weight == null)
      jcasType.jcas.throwFeatMissing("weight", "edu.cmu.lti.qalab.types.VerbPhrase");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((VerbPhrase_Type)jcasType).casFeatCode_weight, v);}    
   
    
  //*--------------*
  //* Feature: synonyms

  /** getter for synonyms - gets 
   * @generated */
  public FSList getSynonyms() {
    if (VerbPhrase_Type.featOkTst && ((VerbPhrase_Type)jcasType).casFeat_synonyms == null)
      jcasType.jcas.throwFeatMissing("synonyms", "edu.cmu.lti.qalab.types.VerbPhrase");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((VerbPhrase_Type)jcasType).casFeatCode_synonyms)));}
    
  /** setter for synonyms - sets  
   * @generated */
  public void setSynonyms(FSList v) {
    if (VerbPhrase_Type.featOkTst && ((VerbPhrase_Type)jcasType).casFeat_synonyms == null)
      jcasType.jcas.throwFeatMissing("synonyms", "edu.cmu.lti.qalab.types.VerbPhrase");
    jcasType.ll_cas.ll_setRefValue(addr, ((VerbPhrase_Type)jcasType).casFeatCode_synonyms, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    