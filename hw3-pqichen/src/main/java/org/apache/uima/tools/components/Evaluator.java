/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.tools.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CasConsumerDescription;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.UriUtils;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import edu.cmu.deiis.types.AnswerScore;

/**
 * A simple CAS consumer that writes the CAS to XMI format.
 * <p>
 * This CAS Consumer takes one parameter:
 * <ul>
 * <li><code>OutputDirectory</code> - path to directory into which output files will be written</li>
 * </ul>
 */
public class Evaluator extends CasConsumer_ImplBase {
  /**
   * Name of configuration parameter that must be set to the path of a directory into which the
   * output files will be written.
   */
  public static final String PARAM_OUTPUTDIR = "OutputDirectory";

  private File mOutputDir;

  private int mDocNum;

  public void initialize() throws ResourceInitializationException {
    mDocNum = 0;
    mOutputDir = new File((String) getConfigParameterValue(PARAM_OUTPUTDIR));
    if (!mOutputDir.exists()) {
      mOutputDir.mkdirs();
    }
  }

  /**
   * Processes the CAS which was populated by the TextAnalysisEngines. <br>
   * In this case, the CAS is converted to XMI and written into the output file .
   * 
   * @param aCAS
   *          a CAS which has been populated by the TAEs
   * 
   * @throws ResourceProcessException
   *           if there is an error in processing the Resource
   * 
   * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
   */
  public void processCas(CAS aCAS) throws ResourceProcessException {
    String modelFileName = null;

    JCas aJCas;
    try {
      aJCas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

 // Initialize AS.
    AnswerScore [] AS;
    int num = 0;
    FSIndex AS_Index = aJCas.getAnnotationIndex(AnswerScore.type);
    Iterator AS_Iter = AS_Index.iterator();   
    while (AS_Iter.hasNext()) 
    {
      AS_Iter.next();
      num++;
    }
    AS = new AnswerScore[num];
    AS_Iter = AS_Index.iterator(); 
    int pos = 0;
    while (AS_Iter.hasNext()) 
    {
      AS[pos] = (AnswerScore)AS_Iter.next();
      pos++;
    }
    int Gold_AS_num = 0, Token_AS_num = 0, NGram_AS_num = 0;
    for(int i = 0; i < AS.length; i++)
    {
      if(AS[i].getCasProcessorId() == "GoldAnswerScorer")
      {
        Gold_AS_num++;
      }else if(AS[i].getCasProcessorId() == "TokenOverlapAnswerScorer") 
      {
        Token_AS_num++;
      }else if(AS[i].getCasProcessorId() == "NGramOverlapAnswerScorer")
      {
        NGram_AS_num++;
      }
    }
   
    // Initialize each NGram.
    AnswerScore [] Gold_AS = new AnswerScore[Gold_AS_num];
    AnswerScore [] Token_AS = new AnswerScore[Token_AS_num];
    AnswerScore [] NGram_AS = new AnswerScore[NGram_AS_num];
    Gold_AS_num = 0; Token_AS_num = 0; NGram_AS_num = 0;
    for(int i = 0; i < AS.length; i++)
    {
      if(AS[i].getCasProcessorId() == "GoldAnswerScorer")
      {
        Gold_AS[Gold_AS_num] = AS[i];
        Gold_AS_num++;
      }else if(AS[i].getCasProcessorId() == "TokenOverlapAnswerScorer") 
      {
        Token_AS[Token_AS_num] = AS[i];
        Token_AS_num++;
      }else if(AS[i].getCasProcessorId() == "NGramOverlapAnswerScorer")
      {
        NGram_AS[NGram_AS_num] = AS[i];
        NGram_AS_num++;
      }
    }
    
    // Get correct Answer Num.
    int Correct_Num = 0;
    int bg, ed;
    for(int i = 0; i < Gold_AS.length; i++)
    {
      if(Gold_AS[i].getAnswer().getIsCorrect() == true)
      {
        Correct_Num++;
      }
    }
    
    // Calculate Precision of TokenOverlap Method.
    double tmp = 0.0;
    boolean judge = true;
    int pred_true = 0;
    boolean [] sign_tk = new boolean[Token_AS.length];
    int sign = -1;
    for(int k = 0; k < sign_tk.length; k++)
    {
      sign_tk[k] = false;
    }
    for(int i = 0; i < Correct_Num; i++)
    {
      judge = false;
      sign = -1;
      for(int j = 0; j < Token_AS.length; j++)
      {
        if(tmp <= Token_AS[j].getScore() && sign_tk[j] == false)
        {
          tmp = Token_AS[j].getScore();
          judge = Token_AS[j].getAnswer().getIsCorrect();
          sign = j;
        }
      }
      sign_tk[sign] = true;
      if(judge == true)
        pred_true++;
      tmp = 0.0;
    }
    double precision_TokenOverlap = (double)pred_true / (double)Correct_Num;
    
    // Calculate Precision of NGramOverlap Method.
    tmp = 0.0;
    judge = true;
    pred_true = 0;
    boolean [] sign_ng = new boolean[NGram_AS.length];
    for(int k = 0; k < sign_ng.length; k++)
    {
      sign_ng[k] = false;
    }
    for(int i = 0; i < Correct_Num; i++)
    {
      sign = -1;
      judge = false;
      for(int j = 0; j < NGram_AS.length; j++)
      {
        if(tmp <= NGram_AS[j].getScore() && sign_ng[j] == false)
        {
          tmp = NGram_AS[j].getScore();
          judge = NGram_AS[j].getAnswer().getIsCorrect();
          sign = j;
        }
      }
      sign_ng[sign] = true;
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

  

  /**
   * Parses and returns the descriptor for this collection reader. The descriptor is stored in the
   * uima.jar file and located using the ClassLoader.
   * 
   * @return an object containing all of the information parsed from the descriptor.
   * 
   * @throws InvalidXMLException
   *           if the descriptor is invalid or missing
   */
  public static CasConsumerDescription getDescription() throws InvalidXMLException {
    InputStream descStream = XCasWriterCasConsumer.class
            .getResourceAsStream("XmiWriterCasConsumer.xml");
    return UIMAFramework.getXMLParser().parseCasConsumerDescription(
            new XMLInputSource(descStream, null));
  }
  
  public static URL getDescriptorURL() {
    return XmiWriterCasConsumer.class.getResource("XmiWriterCasConsumer.xml");
  }  
}
