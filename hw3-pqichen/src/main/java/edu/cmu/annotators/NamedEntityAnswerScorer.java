/*
 * @author Qichen Pan (Andrew_Id: pqichen)
 * 
 * NamedEntityAnswerScorer: This Annotator is responsible for annotating AnswerScores in NamedEntity Algorithm.
 * 
 * It maintains a array to store NamedEntity AnswerScore information.
 * 
 * Previous Question and Answers need to be retrieved.
 * 
 * It is a client to StanfordNLP server to retrieve NamedEntity annotators.
 * 
 * This algorithm assign a score the same value as the hit ratio of NamedEntity in Answers and Questions. 
 * 
 */
package edu.cmu.annotators;

import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;

public class NamedEntityAnswerScorer extends JCasAnnotator_ImplBase {
  public AnswerScore [] NE_AS_List;
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    
    // Initialize NameEntities
    NamedEntityMention [] NEM_List;
    int NEM_Num = 0;
    FSIndex NEM_Index = aJCas.getAnnotationIndex(NamedEntityMention.type);
    Iterator NEM_Iter = NEM_Index.iterator();   
    while (NEM_Iter.hasNext()) 
    {
      NEM_Iter.next();
      NEM_Num++;
    }
    NEM_List = new NamedEntityMention[NEM_Num];
    NEM_Iter = NEM_Index.iterator();   
    int pos = 0;
    while (NEM_Iter.hasNext()) 
    {
      NEM_List[pos] = (NamedEntityMention)NEM_Iter.next();
      pos++;
    }
    
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
    pos = 0;
    while (Answer_Iter.hasNext()) 
    {
      Test_Element_Asw_List[pos] = (Answer)Answer_Iter.next();
      pos++;
    }
    
    // Get text content.
    String Text_Content = aJCas.getDocumentText();
    
    // Declare NE_AS_List.
    int AS_Num = Test_Element_Asw_List.length;
    NE_AS_List = new AnswerScore[AS_Num];
    
    // Initialize AS_List using Asw_List.
    for(int i = 0; i < AS_Num; i++)
    {
      NE_AS_List[i] = new AnswerScore(aJCas, Test_Element_Asw_List[i].getBegin(), Test_Element_Asw_List[i].getEnd());
      NE_AS_List[i].setCasProcessorId("NamedEntityAnswerScorer");
      NE_AS_List[i].setConfidence(0.0);
      NE_AS_List[i].setAnswer(Test_Element_Asw_List[i]);
      NE_AS_List[i].addToIndexes();    
    }
    
    // Calculate Scores for each AS.
    int begin = 0, end = 0;
    int Q_NEM_num = 0;
    // Get Question NamedEntities.
    end = Test_Element_Qst.getEnd();
    for(int i = 0; i < NEM_List.length; i++)
    {
      if(NEM_List[i].getEnd() < end)
      {
        Q_NEM_num++;
      }
    }
    String [] Q_Mention = new String[Q_NEM_num];
    Q_NEM_num = 0;
    for(int i = 0; i < NEM_List.length; i++)
    {
      if(NEM_List[i].getEnd() < end)
      {
        Q_Mention[Q_NEM_num] = NEM_List[i].getMentionType();
        Q_NEM_num++;
      }
    }
    // Get Answer Tokens and calculate overlap.
    int match = 0;
    int A_NEM_num = 0;
    for(int i = 0; i < NE_AS_List.length; i++)
    {
      match = 0;
      begin = Text_Content.indexOf('A', end) + 3;
      end = Text_Content.indexOf('\n', begin) - 2;
      for(int k = 0; k < NEM_List.length; k++)
      {
        if(NEM_List[k].getBegin() > begin && NEM_List[k].getEnd() < end)
        {
          A_NEM_num++;
        }
      }
      String [] A_Mention = new String[A_NEM_num];
      A_NEM_num = 0;
      for(int k = 0; k < NEM_List.length; k++)
      {
        if(NEM_List[k].getBegin() > begin && NEM_List[k].getEnd() < end)
        {
          A_Mention[A_NEM_num] = NEM_List[k].getMentionType();
          A_NEM_num++;
        }
      }
      for(int j = 0; j < A_Mention.length; j++)
      {
        for(int k = 0; k < Q_Mention.length; k++)
        {
          if(A_Mention[j] != null && Q_Mention[k] != null)
          {
            if(A_Mention[j].equals(Q_Mention[k]))
            {
              match++;
              break;
            }
          }
        }
      }
      NE_AS_List[i].setScore((double)(match) / (double)(A_Mention.length));
    }
  }
}
