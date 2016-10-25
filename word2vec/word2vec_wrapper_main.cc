// Copyright 2015 Mobvoi Inc. All Rights Reserved.
// Author: jianwang@mobvoi.com (Jian Wang)
// Author: chhyu@mobvoi.com(Changhe Yu)

#include <cstdio>

#include "base/at_exit.h"
#include "third_party/gflags/gflags.h"

#include "util/nlp/word2vec/word2vec_wrapper.h"


DEFINE_string(word2vec_input_file, "", "");


int main(int argc, char** argv) {
  base::AtExitManager at_exit;
  google::ParseCommandLineFlags(&argc, &argv, false);

  word2vec::Word2VecWrapper* word2vec_wrapper =
      Singleton<word2vec::Word2VecWrapper>::get();

  vector<word2vec::SemanticSimilarWord> sim_word_vec;

  //string word = "china";
  string leftword = "china";
  string rightword = "japan";
  double dist;
  //word2vec_wrapper->TopN(word, &sim_word_vec);
  dist = word2vec_wrapper->WordDistance(leftword,rightword); 
  printf("\nleftword: %s\t\t rightword: %s\t\tcosine_dist = %lf\n",leftword.c_str(),rightword.c_str(),dist);
 /*for (auto itr = sim_word_vec.begin(); itr != sim_word_vec.end();++itr) {
    const word2vec::SemanticSimilarWord& sim_word = *itr;
    printf("word: %s, score: %f\n", sim_word.similar_word.c_str(), sim_word.cosine_dist);
  }*/
 
  return 0;
}
