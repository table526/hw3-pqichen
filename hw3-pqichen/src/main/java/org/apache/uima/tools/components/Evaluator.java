package org.apache.uima.tools.components;

import java.util.Iterator;

import org.apache.uima.cas.FSIndex;

import edu.cmu.deiis.types.AnswerScore;

import org.apache.uima.collection.CasConsumerDescription;
import org.apache.uima.collection.CasConsumer_ImplBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CasConsumerDescription;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.CasToInlineXml;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.Level;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;


public class Evaluator extends CasConsumer_ImplBase {
  public static final String PARAM_OUTPUTDIR = "OutputDirectory";

  /**
   * Optional configuration parameter that specifies XCAS output files
   */
  public static final String PARAM_XCAS = "XCAS";

  private File mOutputDir;

  private CasToInlineXml cas2xml;

  private int mDocNum;

  private String mXCAS;
  
  private boolean mTEXT;

  public void initialize() throws ResourceInitializationException {
    mDocNum = 0;
    mOutputDir = new File(((String) getConfigParameterValue(PARAM_OUTPUTDIR)).trim());
    if (!mOutputDir.exists()) {
      mOutputDir.mkdirs();
    }
    cas2xml = new CasToInlineXml();
    mXCAS = (String) getConfigParameterValue(PARAM_XCAS);
    mTEXT = !("xcas".equalsIgnoreCase(mXCAS) || "xmi".equalsIgnoreCase(mXCAS));
  }
  public void processCas(CAS aCAS) throws ResourceProcessException {
    JCas aJCas;
    try {
      aJCas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    // Initialize Gold_AS.
    AnswerScore [] Gold_AS;
    int num = 0;
    FSIndex Gold_AS_Index = aJCas.getAnnotationIndex(AnswerScore.type);
    Iterator Gold_AS_Iter = Gold_AS_Index.iterator();   
    while (Gold_AS_Iter.hasNext()) 
    {
      Gold_AS_Iter.next();
      num++;
    }
    Gold_AS = new AnswerScore[num];
    Gold_AS_Iter = Gold_AS_Index.iterator(); 
    int pos = 0;
    while (Gold_AS_Iter.hasNext()) 
    {
      Gold_AS[pos] = (AnswerScore)Gold_AS_Iter.next();
      pos++;
    }
    
    // Initialize Token_AS.
    AnswerScore [] Token_AS;
    Token_AS = new AnswerScore[num];
    FSIndex Token_AS_Index = aJCas.getAnnotationIndex(AnswerScore.type);
    Iterator Token_AS_Iter = Token_AS_Index.iterator();   
    pos = 0;
    while (Token_AS_Iter.hasNext()) 
    {
      Token_AS[pos] = (AnswerScore)Token_AS_Iter.next();
      pos++;
    }
    
    // Initialize NGram_AS.
    AnswerScore [] NGram_AS;
    NGram_AS = new AnswerScore[num];
    FSIndex NGram_AS_Index = aJCas.getAnnotationIndex(AnswerScore.type);
    Iterator NGram_AS_Iter = NGram_AS_Index.iterator();   
    pos = 0;
    while (NGram_AS_Iter.hasNext()) 
    {
      NGram_AS[pos] = (AnswerScore)NGram_AS_Iter.next();
      pos++;
    }

    // Get correct Answer Num.
    int Correct_Num = 0;
    for(int i = 0; i < Gold_AS.length; i++)
    {
      if(Gold_AS[i].getAnswer().getIsCorrect() == true)
        Correct_Num++;
    }
    
    // Calculate Precision of TokenOverlap Method.
    double tmp = 0.0;
    double Threshold = 2.0;
    boolean judge = true;
    int pred_true = 0;
    for(int i = 0; i < Correct_Num; i++)
    {
      for(int j = 0; j < Token_AS.length; j++)
      {
        if(tmp <= Token_AS[i].getScore() && Token_AS[i].getScore() < Threshold)
          tmp = Token_AS[i].getScore();
          judge = Token_AS[i].getAnswer().getIsCorrect();
      }
      Threshold = tmp;
      if(judge == true)
        pred_true++;
      tmp = 0.0;
    }
    double precision_TokenOverlap = (double)pred_true / (double)Correct_Num;
    
    // Calculate Precision of NGramOverlap Method.
    tmp = 0.0;
    Threshold = 2.0;
    judge = true;
    pred_true = 0;
    for(int i = 0; i < Correct_Num; i++)
    {
      for(int j = 0; j < NGram_AS.length; j++)
      {
        if(tmp <= NGram_AS[i].getScore() && NGram_AS[i].getScore() < Threshold)
          tmp = NGram_AS[i].getScore();
          judge = NGram_AS[i].getAnswer().getIsCorrect();
      }
      Threshold = tmp;
      if(judge == true)
        pred_true++;
      tmp = 0.0;
    }
    double precision_NGramOverlap = (double)pred_true / (double)Correct_Num;
    
    // print out results.
    double precision_Gold = 1.00;
    System.out.println("Gold_AnswerScore_Precision:\t" + precision_Gold + "\n" 
            + "TokenOverlap_AnswerScore_Precision:\t" + precision_TokenOverlap + "\n"
            + "NGramOverlap_AnswerScore_Precision:\t" + precision_NGramOverlap + "\n");
  }
  public static CasConsumerDescription getDescription() throws InvalidXMLException {
    InputStream descStream = InlineXmlCasConsumer.class
            .getResourceAsStream("CasConsumerDescriptor.xml");
    return UIMAFramework.getXMLParser().parseCasConsumerDescription(
            new XMLInputSource(descStream, null));
  }
  
  public static URL getDescriptorURL() {
    return InlineXmlCasConsumer.class.getResource("CasConsumerDescriptor.xml");
  }
}
