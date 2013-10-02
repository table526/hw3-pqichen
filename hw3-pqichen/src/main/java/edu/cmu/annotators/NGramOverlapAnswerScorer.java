/*
 * @author Qichen Pan (Andrew_Id: pqichen)
 * 
 * NGramOverlapAnswerScorer: This Annotator is responsible for annotating AnswerScores in NGramOverlap Algorithm.
 * 
 * It maintains a array to store NGramOverlap AnswerScore information.
 * 
 * Previous Question and Answers need to be retrieved.
 * 
 * Previous NGram need to be retrieved.
 * 
 * This algorithm assign a score the same value as the hit ratio of AnswerNGrams and QuestionNGrams. 
 * 
 */
package edu.cmu.annotators;

import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;
import edu.cmu.deiis.types.Token;

public class NGramOverlapAnswerScorer extends JCasAnnotator_ImplBase {
  public AnswerScore [] NGram_AS_List;
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    
    // Initialize question and answers.
    Question Test_Element_Qst;
    FSIndex Question_Index = aJCas.getAnnotationIndex(Question.type);
    Iterator Question_Iter = Question_Index.iterator();   
    Test_Element_Qst = (Question)Question_Iter.next();
    int Test_Element_Q_Num = 1;
    
    Answer [] Test_Element_Asw_List;
    int Test_Element_A_Num = 0;
    FSIndex Answer_Index = aJCas.getAnnotationIndex(Answer.type);
    Iterator Answer_Iter = Answer_Index.iterator();   
    while (Answer_Iter.hasNext()) 
    {
      Answer_Iter.next();
      Test_Element_A_Num++;
    }
    Test_Element_Asw_List = new Answer[Test_Element_A_Num];
    Answer_Iter = Answer_Index.iterator();   
    int pos = 0;
    while (Answer_Iter.hasNext()) 
    {
      Test_Element_Asw_List[pos] = (Answer)Answer_Iter.next();
      pos++;
    }
    
    // Initialize Uni_Gram.
    NGram [] Uni_NG_List;
    int num = 0;
    FSIndex Uni_Gram_Index = aJCas.getAnnotationIndex(NGram.type);
    Iterator Uni_Gram_Iter = Uni_Gram_Index.iterator();   
    while (Uni_Gram_Iter.hasNext()) 
    {
      Uni_Gram_Iter.next();
      num++;
    }
    Uni_NG_List = new NGram[num];
    Uni_Gram_Iter = Uni_Gram_Index.iterator();   
    pos = 0;
    while (Uni_Gram_Iter.hasNext()) 
    {
      Uni_NG_List[pos] = (NGram)Uni_Gram_Iter.next();
      pos++;
    }
    
    // Initialize Bi_Gram.
    NGram [] Bi_NG_List;
    num = 0;
    FSIndex Bi_Gram_Index = aJCas.getAnnotationIndex(NGram.type);
    Iterator Bi_Gram_Iter = Bi_Gram_Index.iterator();   
    while (Bi_Gram_Iter.hasNext()) 
    {
      Bi_Gram_Iter.next();
      num++;
    }
    Bi_NG_List = new NGram[num];
    Bi_Gram_Iter = Bi_Gram_Index.iterator();   
    pos = 0;
    while (Bi_Gram_Iter.hasNext()) 
    {
      Bi_NG_List[pos] = (NGram)Bi_Gram_Iter.next();
      pos++;
    }
    
    // Initialize Tri_Gram.
    NGram [] Tri_NG_List;
    num = 0;
    FSIndex Tri_Gram_Index = aJCas.getAnnotationIndex(NGram.type);
    Iterator Tri_Gram_Iter = Tri_Gram_Index.iterator();   
    while (Tri_Gram_Iter.hasNext()) 
    {
      Tri_Gram_Iter.next();
      num++;
    }
    Tri_NG_List = new NGram[num];
    Tri_Gram_Iter = Tri_Gram_Index.iterator();   
    pos = 0;
    while (Tri_Gram_Iter.hasNext()) 
    {
      Tri_NG_List[pos] = (NGram)Tri_Gram_Iter.next();
      pos++;
    }
    
    // Get text content.
    String Text_Content = aJCas.getDocumentText();
    
    // Declare NGram_AS_List.
    int AS_Num = Test_Element_Asw_List.length;
    NGram_AS_List = new AnswerScore[AS_Num];
    
    // Initialize AS_List using Asw_List.
    for(int i = 0; i < AS_Num; i++)
    {
      NGram_AS_List[i] = new AnswerScore(aJCas, Test_Element_Asw_List[i].getBegin(), Test_Element_Asw_List[i].getEnd());
      NGram_AS_List[i].setCasProcessorId("NGramOverlapAnswerScorer");
      NGram_AS_List[i].setConfidence(0.0);
      NGram_AS_List[i].setAnswer(Test_Element_Asw_List[i]);
      NGram_AS_List[i].addToIndexes();    
    }
    
    // Declare parameters
    int Uni_Score[] = new int[Test_Element_A_Num] ;
    int Bi_Score[] = new int[Test_Element_A_Num] ;
    int Tri_Score[] = new int[Test_Element_A_Num] ;
    
    int [] pilot = new int[Test_Element_Q_Num + Test_Element_A_Num];
    
    // Calculate Uni Score.
    pilot[0] = Text_Content.substring(Test_Element_Qst.getBegin() + 1,
            Test_Element_Qst.getEnd()).split(" ").length;
    for(int i = 1; i <= Test_Element_A_Num; i++)
    {
      pilot[i] = Text_Content.substring(Test_Element_Asw_List[i - 1].getBegin(),
              Test_Element_Asw_List[i - 1].getEnd() - 1).trim().split(" ").length;
    }
    int s = 0;
    int match = 0;
    for(int i = 1; i < Test_Element_A_Num + 1; i++)
    {
      s = s + pilot[i - 1];
      match = 0;
      for(int j = s; j < s + pilot[i]; j++)
      {
        for(int k = 0; k < pilot[0]; k++ )
        {
          NGram tmp1 = Uni_NG_List[k];
          NGram tmp2 = Uni_NG_List[j];
          if(Text_Content.substring(tmp1.getBegin(), tmp1.getEnd()).equals
                  (Text_Content.substring(tmp2.getBegin(), tmp2.getEnd())))
            match++;
        }
      }
      Uni_Score[i - 1] = match;
    }
    
    // Calculate Bi Score.
    for(int i = 0; i < pilot.length; i++)
    {
      pilot[i] = pilot[i] - 1;
    }
    s = 0;
    for(int i = 1; i < Test_Element_A_Num + 1; i++)
    {
      s = s + pilot[i - 1];
      match = 0;
      for(int j = s; j < s + pilot[i]; j++)
      {
        for(int k = 0; k < pilot[0]; k++ )
        {
          NGram tmp1 = Bi_NG_List[k];
          NGram tmp2 = Bi_NG_List[j];
          if(Text_Content.substring(tmp1.getBegin(), tmp1.getEnd()).equals
                  (Text_Content.substring(tmp2.getBegin(), tmp2.getEnd())))
            match++;
        }
      }
      Bi_Score[i - 1] = match;
    }
    
    // Calculate Tri Score.
    for(int i = 0; i < pilot.length; i++)
    {
      pilot[i] = pilot[i] - 1;
    }
    s = 0;
    for(int i = 1; i < Test_Element_A_Num + 1; i++)
    {
      s = s + pilot[i - 1];
      match = 0;
      for(int j = s; j < s + pilot[i]; j++)
      {
        for(int k = 0; k < pilot[0]; k++ )
        {
          NGram tmp1 = Tri_NG_List[k];
          NGram tmp2 = Tri_NG_List[j];
          if(Text_Content.substring(tmp1.getBegin(), tmp1.getEnd()).equals
                  (Text_Content.substring(tmp2.getBegin(), tmp2.getEnd())))
            match++;
        }
      }
      Tri_Score[i - 1] = match;
    }
    
    // Assign final score.
    for(int i = 0; i < NGram_AS_List.length; i++)
    {
      //double result = ((Uni_Score[i] * ((double)pilot[i] + 2.0000) / ((double)pilot[i] + 1.0000))
      //       + (Bi_Score[i])
      //        + (Tri_Score[i] * (double)pilot[i] / ((double)pilot[i] + 1.0000))) / 3.0000;
      //double result = (Uni_Score[i] + Bi_Score[i] + Tri_Score[i]) / 3;
      double result = (double)(Uni_Score[i] + Bi_Score[i] + Tri_Score[i]) / (double)(3 * pilot[i + 1] + 3);
      NGram_AS_List[i].setScore(result);
    }
  }

}
