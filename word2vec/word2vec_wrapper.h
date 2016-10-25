// Copyright 2016 Mobvoi Inc. All Rights Reserved.
// Author: jianwang@mobvoi.com (Jian Wang)
// Author: chhyu@mobvoi.com(Changhe Yu)

#ifndef UTIL_NLP_WORD2VEC__WRAPPER_H_
#define UTIL_NLP_WORD2VEC__WRAPPER_H_

#include "base/basictypes.h"
#include "base/compat.h"
#include "base/singleton.h"


namespace word2vec {
struct Word2VecData;

struct SemanticSimilarWord {
  string similar_word;
  double cosine_dist;
};

class Word2VecWrapper {
 public:
  virtual ~Word2VecWrapper();

  bool TopN(const string& word,
            vector<SemanticSimilarWord>* sim_word_vec) const;
  double WordDistance(const string& leftword, const string& rightword) const;
 private:
  Word2VecWrapper();
  bool Init(const string& model_file);

  std::unique_ptr<Word2VecData> word2_vec_data_;

  friend class DefaultSingletonTraits<Word2VecWrapper>;

  DISALLOW_COPY_AND_ASSIGN(Word2VecWrapper);
};
}  // namespace word2vec

#endif  // UTIL_NLP_WORD2VEC__WRAPPER_H_
